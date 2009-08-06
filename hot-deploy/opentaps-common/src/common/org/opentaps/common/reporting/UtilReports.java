/*
 * Copyright (c) 2006 - 2009 Open Source Strategies, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Honest Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Honest Public License for more details.
 *
 * You should have received a copy of the Honest Public License
 * along with this program; if not, write to Funambol,
 * 643 Bair Island Road, Suite 305 - Redwood City, CA 94063, USA
 */

package org.opentaps.common.reporting;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.print.PrintService;
import javax.servlet.http.HttpServletRequest;

import javolution.util.FastList;
import javolution.util.FastMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRValidationFault;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRProperties;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.ofbiz.base.crypto.HashCrypt;
import org.ofbiz.base.location.ComponentLocationResolver;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.cache.UtilCache;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.opentaps.common.reporting.jasper.JRResourceBundle;
import org.opentaps.common.util.UtilMessage;

/**
 * Collection of various helper methods for reporting.
 *
 */
public final class UtilReports {

    private static final String MODULE = UtilReports.class.getName();

    private static UtilCache jasperReportsCompiledCache = null;

    public static String OUT_PATH = "runtime/output/";

    private UtilReports() { }

    /**
     * Mime types and their constants that are supported by JasperReports.
     */
    public static enum ContentType {
        /** XML type. */
        XML ("text/xml"),
        /** PDF type. */
        PDF ("application/pdf"),
        /** HTML type. */
        HTML("text/html"),
        /** CSV type. */
        CSV ("text/csv"),
        /** RTF type. */
        RTF ("application/rtf"),
        /** TXT type. */
        TXT ("text/plain"),
        /** ODT type. */
        ODT ("application/vnd.oasis.opendocument.text"),
        /** XLS type. */
        XLS ("application/vnd.ms-excel");

        private String contentType;

        ContentType(String contentType) {
            this.contentType = contentType;
        }

        /**
         * Gets the mime type string.
         * @return the mime type string
         */
        @Override public String toString() {
            return contentType;
        }

    }

    static {
        jasperReportsCompiledCache = new UtilCache(
                "webapp.JasperReportsOpentaps",
                JRProperties.getIntegerProperty("webapp.JasperReportsCompiled.maxSize", 0),
                0,
                JRProperties.getIntegerProperty("webapp.JasperReportsCompiled.expireTime", 300000),
                JRProperties.getBooleanProperty("webapp.JasperReportsCompiled.useSoftReference"),
                JRProperties.getBooleanProperty("webapp.JasperReportsCompiled.useFileSystemStore")
        );
    }

    /**
     * Return subset of MimeType that are supported by JasperReports.
     * Method mainly intended for use in bsh/ftl for filling avaible report types.
     *
     * @param delegator a <code>GenericDelegator</code> value
     * @return a list of <code>MimeType</code> <code>GenericValue</code>
     * @throws GenericEntityException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static List<GenericValue> getJRSupportedMimeType(GenericDelegator delegator) throws GenericEntityException {
        List<String> supportedTypes = new ArrayList<String>();
        supportedTypes.add(ContentType.CSV.toString());
        supportedTypes.add(ContentType.HTML.toString());
        supportedTypes.add(ContentType.ODT.toString());
        supportedTypes.add(ContentType.PDF.toString());
        supportedTypes.add(ContentType.RTF.toString());
        supportedTypes.add(ContentType.TXT.toString());
        supportedTypes.add(ContentType.XLS.toString());
        supportedTypes.add(ContentType.XML.toString());

        return delegator.findByCondition("MimeType", new EntityExpr("mimeTypeId", EntityOperator.IN, supportedTypes), null, UtilMisc.toList("description"));
    }

    /**
     * Method returns ContentType constant for given MIME type.
     * Applicable only for MIME that are supported by JasperReports.
     *
     * @param contentType MIME type (e.g. "application/pdf")
     * @return a <code>ContentType</code> value
     */
    public static ContentType getContentType(String contentType) {

        if (UtilValidate.isEmpty(contentType)) {
            return null;
        }

        ContentType[] types = ContentType.values();
        for (ContentType type : types) {
            if (contentType.equalsIgnoreCase(type.toString())) {
                return type;
            }
        }
        return null;
    }

    /**
     * Method return list of installed printers.
     * @return list of printer names, or <code>null</code> if none is found
     */
    public static List<String> enumeratePrinters() {
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        List<String> printers = new ArrayList<String>();
        for (PrintService printService : printServices) {
            printers.add(printService.getName());
        }
        if (printers.size() > 0) {
            return printers;
        } else {
            return null;
        }
    }

    /**
     * Helper method that returns PrintService for given printer name.
     *
     * @param printerName the printer name
     * @return a <code>PrintService</code> value
     */
    public static PrintService getPrintServiceByName(String printerName) {
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        for (PrintService printService : printServices) {
            if (printerName.equals(printService.getName())) {
                return printService;
            }
        }
        return null;
    }

    /**
     * <ol>This method returns ready to use JasperReport object according to:
     * <li>Location is link to report design and binary report w/ the same name exists as well.
     *    Report will recompiled and cached if outdated.</li>
     * <li>Location is link to binary report.
     *    Report loaded from file.</li>
     * <li>Otherwise report design will be compiled and cached.</li>
     * </ol>
     *
     * @param location Component URL or absolute path to report. Supports both report design or binary files.
     * @return a <code>JasperReport</code> value
     * @throws MalformedURLException if an error occurs
     * @throws FileNotFoundException if an error occurs
     * @throws JRException if an error occurs
     */
    public static JasperReport getReportObject(String location) throws MalformedURLException, FileNotFoundException, JRException {
        return getReportObject(location, null);
    }

    /**
     * <ol>This method returns ready to use JasperReport object according to:
     * <li>Location is link to report design and binary report w/ the same name exists as well.
     *    Report will recompiled and cached if outdated.</li>
     * <li>Location is link to binary report.
     *    Report loaded from file.</li>
     * <li>Otherwise report design will be compiled and cached.</li>
     * </ol>
     *
     * @param location Component URL or absolute path to report. Supports both report design or binary files.
     * @param request Useful if method is called from view handler to get correct locale and error reporting. May be <code>null</code>.
     * @return a <code>JasperReport</code> value
     * @throws MalformedURLException if an error occurs
     * @throws FileNotFoundException if an error occurs
     * @throws JRException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static JasperReport getReportObject(String location, HttpServletRequest request) throws MalformedURLException, FileNotFoundException, JRException {

        Locale locale = UtilHttp.getLocale(request);

        String cacheLineKey = HashCrypt.getDigestHash(location);
        // report location can be either component URL form or absolute file system path
        String baseLocation = UtilValidate.isUrl(location) ? ComponentLocationResolver.getBaseLocation(location).toString() : location;
        String candidateLocation = null;

        JasperDesign design = null;
        long designLastModified = 0;
        // checks if report design accessible and load it.
        if (baseLocation.endsWith(".jrxml") || baseLocation.endsWith(".xml")) {
            File designFile = new File(baseLocation);
            if (!designFile.exists() || !designFile.canRead() || !designFile.isFile()) {
                String errMessage = UtilMessage.expandLabel("OpentapsError_ReportNotFound", locale, UtilMisc.toMap("location", baseLocation));
                throw new FileNotFoundException(errMessage);
            }
            designLastModified = designFile.lastModified();
            design = JRXmlLoader.load(designFile);
            // verify report design if requested
            if (JRProperties.getBooleanProperty("webapp.jasperreports.verify.design")) {
                Collection<JRValidationFault> designErrors = JasperCompileManager.verifyDesign(design);
                if (UtilValidate.isNotEmpty(designErrors)) {
                    for (JRValidationFault fault : designErrors) {
                        if (request != null) {
                            UtilMessage.addError(request, "OpentapsError_ReportDesignError", UtilMisc.toMap("message", fault.getMessage()));
                        } else {
                            Debug.logWarning(fault.getMessage(), MODULE);
                        }
                    }
                }
            }
            candidateLocation = String.format("%1$s.jasper", baseLocation.substring(0, baseLocation.lastIndexOf(".")));
        }

        JasperReport report = null;
        long reportLastModified = 0;
        // checks if report exists and accessible.
        if (baseLocation.endsWith(".jasper")) {
            File reportFile = new File(baseLocation);
            if (!reportFile.exists() || !reportFile.canRead() || !reportFile.isFile()) {
                String errMessage = UtilMessage.expandLabel("OpentapsError_ReportNotFound", locale, UtilMisc.toMap("location", baseLocation));
                throw new FileNotFoundException(errMessage);
            }
            reportLastModified = reportFile.lastModified();
        }

        // add synchronized block to avoid cache corruption
        synchronized (jasperReportsCompiledCache) {

            // recompiles report if outdated and cache it
            if (UtilValidate.isNotEmpty(candidateLocation)) {
                File candidateFile = new File(candidateLocation);
                if (candidateFile.exists() && candidateFile.canRead() && candidateFile.isFile() && (designLastModified > candidateFile.lastModified())) {
                    JasperCompileManager.compileReportToFile(design, candidateLocation);
                    report = (JasperReport) JRLoader.loadObject(candidateFile);
                    if (UtilValidate.isNotEmpty(report)) {
                        jasperReportsCompiledCache.remove(cacheLineKey);
                        jasperReportsCompiledCache.put(cacheLineKey, report);
                    } else {
                        jasperReportsCompiledCache.remove(cacheLineKey);
                    }
                }
            }

            // if report hasn't loaded before try get it from cache or load/compile from design.
            if (UtilValidate.isEmpty(report)) {
                try {
                    report = (JasperReport) jasperReportsCompiledCache.get(cacheLineKey);
                } catch (Exception e) {
                    Debug.logError(e, "Error getting the report from the cache.", MODULE);
                }

                if (UtilValidate.isEmpty(report) && UtilValidate.isNotEmpty(design)) {
                    if (reportLastModified > 0) {
                        report = (JasperReport) JRLoader.loadObject(baseLocation);
                    } else {
                        report = JasperCompileManager.compileReport(design);
                    }
                    try {
                        jasperReportsCompiledCache.put(cacheLineKey, report);
                    } catch (Exception e) {
                        Debug.logError(e, "Error putting the report in the cache.", MODULE);
                    }
                }
            }
        }

        if (UtilValidate.isEmpty(report)) {
            String errMessage = UtilMessage.expandLabel("OpentapsError_ReportNotFound", locale, UtilMisc.toMap("location", baseLocation));
            throw new FileNotFoundException(errMessage);
        }

        return report;
    }

    /**
     * use jasper exporter to generate pdf.
     * @param jrParameters a <code>Map<String, Object></code> instance
     * @param jrDataSource a <code>JRMapCollectionDataSource</code> instance
     * @param locale a <code>Locale</code> instance
     * @param jrxml a <code>String</code> value
     * @param contentType a <code>ContentType</code> instance
     * @param reportName a <code>String</code> value
     * @param author a <code>String</code> value
     * @throws MalformedURLException if an error occurs
     * @throws FileNotFoundException if an error occurs
     * @throws JRException if an error occurs
     */
    public static void generatePdf(Map<String, Object> jrParameters, JRMapCollectionDataSource jrDataSource, Locale locale, String jrxml, ContentType contentType, String reportName, String author) throws MalformedURLException, FileNotFoundException, JRException {

        // Collects parameters/properties
        jrParameters.put("REPORT_LOCALE", locale);
        JRResourceBundle resources = new JRResourceBundle(locale);
        if (resources.size() > 0) {
            jrParameters.put("REPORT_RESOURCE_BUNDLE", resources);
        }
        // Trying to get report object from the given location.
        JasperReport report = UtilReports.getReportObject(jrxml);
        jrParameters.put("isPlainFormat", Boolean.FALSE);

        // use data source to fill report
        JasperPrint jp = JasperFillManager.fillReport(report, jrParameters, jrDataSource);

        if (jp.getPages().size() < 1) {
            Debug.logError("Report is empty.", MODULE);
        } else {
            Debug.logInfo("Got report, there are " + jp.getPages().size() + " pages.", MODULE);
        }

        // Generates and exports report
        Map<Object, Object> exporterParameters = new HashMap<Object, Object>();
        JRExporter exporter = null;
        if (contentType.equals(ContentType.PDF)) {
            exporter = new JRPdfExporter();
            // add product name as creator
            String opentaps = UtilProperties.getPropertyValue("OpentapsUiLabels.properties", "OpentapsProductName");
            if (UtilValidate.isNotEmpty(opentaps)) exporterParameters.put(JRPdfExporterParameter.METADATA_CREATOR, opentaps);
            if (UtilValidate.isNotEmpty(author)) {
                exporterParameters.put(JRPdfExporterParameter.METADATA_AUTHOR, author);
            }

        } else if (contentType.equals(ContentType.HTML)) {
            exporter = new JRHtmlExporter();
            exporterParameters.put(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
            exporterParameters.put(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.FALSE);
        } else if (contentType.equals(ContentType.XLS)) {
            exporter = new JExcelApiExporter();
        } else if (contentType.equals(ContentType.XML)) {
            exporter = new JRXmlExporter();
        } else if (contentType.equals(ContentType.CSV)) {
            exporter = new JRCsvExporter();
        } else if (contentType.equals(ContentType.RTF)){
            exporter = new JRRtfExporter();
        } else if (contentType.equals(ContentType.TXT)){
            exporter = new JRTextExporter();
            exporterParameters.put(JRTextExporterParameter.CHARACTER_WIDTH, new Integer(80));
            exporterParameters.put(JRTextExporterParameter.CHARACTER_HEIGHT, new Integer(25));
        } else if (contentType.equals(ContentType.ODT)) {
            exporter = new JROdtExporter();
        }

        exporterParameters.put(JRExporterParameter.JASPER_PRINT, jp);
        exporterParameters.put(JRExporterParameter.OUTPUT_FILE_NAME, OUT_PATH + reportName);

        exporter.setParameters(exporterParameters);
        exporter.exportReport();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>>getManagedReports(String componentName, GenericDelegator delegator, Locale locale) {
        try {
            EntityConditionList conditions = new EntityConditionList(
                    UtilMisc.toList(
                            new EntityExpr("application", EntityOperator.EQUALS, componentName),
                            new EntityExpr("showInSelect", EntityOperator.EQUALS, "Y")
                    ), EntityOperator.AND
            );

            List<GenericValue> applicationGroups = delegator.findByCondition("ReportGroup", conditions, null, null , UtilMisc.toList("sequenceNum", "description"), null); 
            if (UtilValidate.isEmpty(applicationGroups)) {
                return null;
            }

            ResourceBundle analyticsLabels = ResourceBundle.getBundle("org/opentaps/analytics/locale/messages", locale);

            List<Map<String, Object>> reportsGroupedList = FastList.<Map<String, Object>>newInstance();

            for (GenericValue group : applicationGroups) {
                List<GenericValue> members;
                members = delegator.findByAnd("ReportGroupMemberRegistry", UtilMisc.toMap("reportGroupId", group.getString("reportGroupId")));

                // Entries of reports which come from analytics have shorName & description fields as
                // reference to resource string in Eclipse format, e.g. %ReportTitle.
                // In this case we try expand them to strings using analytics resources.
                for (GenericValue registryEntry : members) {
                    String shortName = registryEntry.getString("shortName");
                    if (shortName.startsWith("%") && analyticsLabels != null) {
                        String label = analyticsLabels.getString(shortName.substring(1));
                        if (UtilValidate.isNotEmpty(label)) {
                            registryEntry.set("shortName", label);
                        }
                    }
                    String description = registryEntry.getString("description");
                    if (description.startsWith("%") && analyticsLabels != null) {
                        String label = analyticsLabels.getString(description.substring(1));
                        if (UtilValidate.isNotEmpty(label)) {
                            registryEntry.set("description", label);
                        }
                    }
                }

                Map<String, Object> reportsGroup = FastMap.newInstance();
                reportsGroup.putAll(group.getAllFields());
                reportsGroup.put("reports", EntityUtil.orderBy(members, UtilMisc.toList("sequenceNum", "shortName")));
                reportsGroupedList.add(reportsGroup);
            }

            if (UtilValidate.isNotEmpty(reportsGroupedList)) {
                return reportsGroupedList;
            }

        } catch (GenericEntityException e) {
            Debug.logError(e.getMessage(), MODULE);
        }

        return null;
    }
}

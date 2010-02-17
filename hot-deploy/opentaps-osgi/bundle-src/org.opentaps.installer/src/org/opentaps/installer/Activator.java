/*
 * Copyright (c) 2006 - 2010 Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opentaps.installer;

import javax.servlet.ServletException;

import org.opentaps.core.bundle.AbstractBundle;
import org.opentaps.installer.service.InstallerNavigation;
import org.opentaps.installer.service.InstallerStep;
import org.opentaps.installer.service.OSSInstaller;
import org.opentaps.installer.service.impl.OSSInstallerImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


class NavigationServletCustomizer implements ServiceTrackerCustomizer {

    private static final String INSTALLER_NAVIGATION_ALIAS = "/InstNav";

    private BundleContext context;

    public NavigationServletCustomizer(BundleContext context) {
        this.context = context;
    }

    public Object addingService(ServiceReference reference) {
        try {
            HttpService service = (HttpService) context.getService(reference);
            if (service != null) {
                service.registerServlet(INSTALLER_NAVIGATION_ALIAS, new InstallerNavigation(), null, null);
            }
        } catch (ServletException e) {
            Activator.getInstance().logError(e.getMessage(), e, null);
        } catch (NamespaceException e) {
            Activator.getInstance().logError(e.getMessage(), e, null);
        }
        return null;
    }

    public void modifiedService(ServiceReference reference, Object alias) {
        // do nothing
    }

    public void removedService(ServiceReference reference, Object alias) {
        HttpService service = (HttpService) context.getService(reference);
        if (service != null) {
            service.unregister(INSTALLER_NAVIGATION_ALIAS);
        }
    }
}

public class Activator extends AbstractBundle {

    // the shared instance
    private static BundleActivator bundle;

    private BundleContext context;
 
    private ServiceTracker navHttpSrvcTracker;
    private ServiceTracker installerTracker;
    private ServiceTracker stepsTracker;

    /** {@inheritDoc} */
    public void start(BundleContext context) throws Exception {

        bundle = this;
        this.context = context;
        super.start(context);

        navHttpSrvcTracker = new ServiceTracker(context, HttpService.class.getName(), new NavigationServletCustomizer(context));
        navHttpSrvcTracker.open();

        // register bundle services
        OSSInstaller installer = new OSSInstallerImpl();
        context.registerService(OSSInstaller.class.getName(), installer, null);

        installerTracker = new ServiceTracker(context, OSSInstaller.class.getName(), null);
        installerTracker.open();

        Filter stepsFlt = context.createFilter("(objectClass=" + InstallerStep.class.getName() + ")");
        stepsTracker = new ServiceTracker(context, stepsFlt, null);
        stepsTracker.open();
    }

    /** {@inheritDoc} */
    public void stop(BundleContext context) throws Exception {

        stepsTracker.close();
        navHttpSrvcTracker.close();
        installerTracker.close();

        super.stop(context);
        context = null;
        bundle = null;
    }

    public static Activator getInstance() {
        return (Activator) bundle;
    }

    public static OSSInstaller getInstaller() {
        Activator bundle = (org.opentaps.installer.Activator) getInstance();
        return (OSSInstaller) bundle.installerTracker.getService();
    }

    public static ServiceReference[] findInstSteps() {
        Activator bundle = (org.opentaps.installer.Activator) getInstance();
        return bundle.stepsTracker.getServiceReferences();
    }

    public InstallerStep findStep(ServiceReference reference) {
        return (InstallerStep) stepsTracker.getService(reference);
    }
}

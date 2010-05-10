package org.opentaps.base.entities;

/*
 * Copyright (c) 2008 - 2009 Open Source Strategies, Inc.
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

// DO NOT EDIT THIS FILE!  THIS IS AUTO GENERATED AND WILL GET WRITTEN OVER PERIODICALLY WHEN THE DATA MODEL CHANGES
// EXTEND THIS CLASS INSTEAD.

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import javolution.util.FastMap;

import org.opentaps.foundation.entity.Entity;
import org.opentaps.foundation.entity.EntityFieldInterface;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.repository.RepositoryInterface;
import javax.persistence.*;
import org.hibernate.search.annotations.*;
import java.lang.String;
import java.sql.Timestamp;

/**
 * Auto generated base entity CustRequestType.
 */
@javax.persistence.Entity
@Table(name="CUST_REQUEST_TYPE")
public class CustRequestType extends Entity {
static {
java.util.Map<String, String> fields = new java.util.HashMap<String, String>();
        fields.put("custRequestTypeId", "CUST_REQUEST_TYPE_ID");
        fields.put("parentTypeId", "PARENT_TYPE_ID");
        fields.put("hasTable", "HAS_TABLE");
        fields.put("description", "DESCRIPTION");
        fields.put("partyId", "PARTY_ID");
        fields.put("lastUpdatedStamp", "LAST_UPDATED_STAMP");
        fields.put("lastUpdatedTxStamp", "LAST_UPDATED_TX_STAMP");
        fields.put("createdStamp", "CREATED_STAMP");
        fields.put("createdTxStamp", "CREATED_TX_STAMP");
fieldMapColumns.put("CustRequestType", fields);
}
  public static enum Fields implements EntityFieldInterface<CustRequestType> {
    custRequestTypeId("custRequestTypeId"),
    parentTypeId("parentTypeId"),
    hasTable("hasTable"),
    description("description"),
    partyId("partyId"),
    lastUpdatedStamp("lastUpdatedStamp"),
    lastUpdatedTxStamp("lastUpdatedTxStamp"),
    createdStamp("createdStamp"),
    createdTxStamp("createdTxStamp");
    private final String fieldName;
    private Fields(String name) { fieldName = name; }
    /** {@inheritDoc} */
    public String getName() { return fieldName; }
    /** {@inheritDoc} */
    public String asc() { return fieldName + " ASC"; }
    /** {@inheritDoc} */
    public String desc() { return fieldName + " DESC"; }
  }

   @org.hibernate.annotations.GenericGenerator(name="CustRequestType_GEN",  strategy="org.opentaps.foundation.entity.hibernate.OpentapsIdentifierGenerator")
   @GeneratedValue(generator="CustRequestType_GEN")
   @Id
   @Column(name="CUST_REQUEST_TYPE_ID")
   private String custRequestTypeId;
   @Column(name="PARENT_TYPE_ID")
   private String parentTypeId;
   @Column(name="HAS_TABLE")
   private String hasTable;
   @Column(name="DESCRIPTION")
   private String description;
   @Column(name="PARTY_ID")
   private String partyId;
   @Column(name="LAST_UPDATED_STAMP")
   private Timestamp lastUpdatedStamp;
   @Column(name="LAST_UPDATED_TX_STAMP")
   private Timestamp lastUpdatedTxStamp;
   @Column(name="CREATED_STAMP")
   private Timestamp createdStamp;
   @Column(name="CREATED_TX_STAMP")
   private Timestamp createdTxStamp;
   @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY)
   @JoinColumn(name="PARENT_TYPE_ID", insertable=false, updatable=false)
   @org.hibernate.annotations.Generated(
      org.hibernate.annotations.GenerationTime.ALWAYS
   )
   
   private CustRequestType parentCustRequestType = null;
   @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY)
   @JoinColumn(name="PARTY_ID", insertable=false, updatable=false)
   @org.hibernate.annotations.Generated(
      org.hibernate.annotations.GenerationTime.ALWAYS
   )
   
   private Party party = null;
   @OneToMany(fetch=FetchType.LAZY)
   @JoinColumn(name="PARTY_ID_FROM")
   
   private List<PartyRelationship> partyRelationships = null;
   @OneToMany(fetch=FetchType.LAZY)
   @JoinColumn(name="CUST_REQUEST_TYPE_ID")
   
   private List<CustRequest> custRequests = null;
   @OneToMany(fetch=FetchType.LAZY)
   @JoinColumn(name="CUST_REQUEST_TYPE_ID")
   
   private List<CustRequestCategory> custRequestCategorys = null;
   @OneToMany(fetch=FetchType.LAZY)
   @JoinColumn(name="CUST_REQUEST_TYPE_ID")
   
   private List<CustRequestResolution> custRequestResolutions = null;
   @OneToMany(fetch=FetchType.LAZY)
   @JoinColumn(name="PARENT_TYPE_ID")
   
   private List<CustRequestType> childCustRequestTypes = null;
   @OneToMany(fetch=FetchType.LAZY, mappedBy="custRequestType", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
   @JoinColumn(name="CUST_REQUEST_TYPE_ID")
   
   private List<CustRequestTypeAttr> custRequestTypeAttrs = null;

  /**
   * Default constructor.
   */
  public CustRequestType() {
      super();
      this.baseEntityName = "CustRequestType";
      this.isView = false;
      this.resourceName = "OrderEntityLabels";
      this.primaryKeyNames = new ArrayList<String>();
      this.primaryKeyNames.add("custRequestTypeId");
      this.allFieldsNames = new ArrayList<String>();
      this.allFieldsNames.add("custRequestTypeId");this.allFieldsNames.add("parentTypeId");this.allFieldsNames.add("hasTable");this.allFieldsNames.add("description");this.allFieldsNames.add("partyId");this.allFieldsNames.add("lastUpdatedStamp");this.allFieldsNames.add("lastUpdatedTxStamp");this.allFieldsNames.add("createdStamp");this.allFieldsNames.add("createdTxStamp");
      this.nonPrimaryKeyNames = new ArrayList<String>();
      this.nonPrimaryKeyNames.addAll(allFieldsNames);
      this.nonPrimaryKeyNames.removeAll(primaryKeyNames);
  }

  /**
   * Constructor with a repository.
   * @param repository a <code>RepositoryInterface</code> value
   */
  public CustRequestType(RepositoryInterface repository) {
      this();
      initRepository(repository);
  }

    /**
     * Auto generated value setter.
     * @param custRequestTypeId the custRequestTypeId to set
     */
    public void setCustRequestTypeId(String custRequestTypeId) {
        this.custRequestTypeId = custRequestTypeId;
    }
    /**
     * Auto generated value setter.
     * @param parentTypeId the parentTypeId to set
     */
    public void setParentTypeId(String parentTypeId) {
        this.parentTypeId = parentTypeId;
    }
    /**
     * Auto generated value setter.
     * @param hasTable the hasTable to set
     */
    public void setHasTable(String hasTable) {
        this.hasTable = hasTable;
    }
    /**
     * Auto generated value setter.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Auto generated value setter.
     * @param partyId the partyId to set
     */
    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }
    /**
     * Auto generated value setter.
     * @param lastUpdatedStamp the lastUpdatedStamp to set
     */
    public void setLastUpdatedStamp(Timestamp lastUpdatedStamp) {
        this.lastUpdatedStamp = lastUpdatedStamp;
    }
    /**
     * Auto generated value setter.
     * @param lastUpdatedTxStamp the lastUpdatedTxStamp to set
     */
    public void setLastUpdatedTxStamp(Timestamp lastUpdatedTxStamp) {
        this.lastUpdatedTxStamp = lastUpdatedTxStamp;
    }
    /**
     * Auto generated value setter.
     * @param createdStamp the createdStamp to set
     */
    public void setCreatedStamp(Timestamp createdStamp) {
        this.createdStamp = createdStamp;
    }
    /**
     * Auto generated value setter.
     * @param createdTxStamp the createdTxStamp to set
     */
    public void setCreatedTxStamp(Timestamp createdTxStamp) {
        this.createdTxStamp = createdTxStamp;
    }

    /**
     * Auto generated value accessor.
     * @return <code>String</code>
     */
    public String getCustRequestTypeId() {
        return this.custRequestTypeId;
    }
    /**
     * Auto generated value accessor.
     * @return <code>String</code>
     */
    public String getParentTypeId() {
        return this.parentTypeId;
    }
    /**
     * Auto generated value accessor.
     * @return <code>String</code>
     */
    public String getHasTable() {
        return this.hasTable;
    }
    /**
     * Auto generated value accessor.
     * @return <code>String</code>
     */
    public String getDescription() {
        return this.description;
    }
    /**
     * Auto generated value accessor.
     * @return <code>String</code>
     */
    public String getPartyId() {
        return this.partyId;
    }
    /**
     * Auto generated value accessor.
     * @return <code>Timestamp</code>
     */
    public Timestamp getLastUpdatedStamp() {
        return this.lastUpdatedStamp;
    }
    /**
     * Auto generated value accessor.
     * @return <code>Timestamp</code>
     */
    public Timestamp getLastUpdatedTxStamp() {
        return this.lastUpdatedTxStamp;
    }
    /**
     * Auto generated value accessor.
     * @return <code>Timestamp</code>
     */
    public Timestamp getCreatedStamp() {
        return this.createdStamp;
    }
    /**
     * Auto generated value accessor.
     * @return <code>Timestamp</code>
     */
    public Timestamp getCreatedTxStamp() {
        return this.createdTxStamp;
    }

    /**
     * Auto generated method that gets the related <code>CustRequestType</code> by the relation named <code>ParentCustRequestType</code>.
     * @return the <code>CustRequestType</code>
     * @throws RepositoryException if an error occurs
     */
    public CustRequestType getParentCustRequestType() throws RepositoryException {
        if (this.parentCustRequestType == null) {
            this.parentCustRequestType = getRelatedOne(CustRequestType.class, "ParentCustRequestType");
        }
        return this.parentCustRequestType;
    }
    /**
     * Auto generated method that gets the related <code>Party</code> by the relation named <code>Party</code>.
     * @return the <code>Party</code>
     * @throws RepositoryException if an error occurs
     */
    public Party getParty() throws RepositoryException {
        if (this.party == null) {
            this.party = getRelatedOne(Party.class, "Party");
        }
        return this.party;
    }
    /**
     * Auto generated method that gets the related <code>PartyRelationship</code> by the relation named <code>PartyRelationship</code>.
     * @return the list of <code>PartyRelationship</code>
     * @throws RepositoryException if an error occurs
     */
    public List<? extends PartyRelationship> getPartyRelationships() throws RepositoryException {
        if (this.partyRelationships == null) {
            this.partyRelationships = getRelated(PartyRelationship.class, "PartyRelationship");
        }
        return this.partyRelationships;
    }
    /**
     * Auto generated method that gets the related <code>CustRequest</code> by the relation named <code>CustRequest</code>.
     * @return the list of <code>CustRequest</code>
     * @throws RepositoryException if an error occurs
     */
    public List<? extends CustRequest> getCustRequests() throws RepositoryException {
        if (this.custRequests == null) {
            this.custRequests = getRelated(CustRequest.class, "CustRequest");
        }
        return this.custRequests;
    }
    /**
     * Auto generated method that gets the related <code>CustRequestCategory</code> by the relation named <code>CustRequestCategory</code>.
     * @return the list of <code>CustRequestCategory</code>
     * @throws RepositoryException if an error occurs
     */
    public List<? extends CustRequestCategory> getCustRequestCategorys() throws RepositoryException {
        if (this.custRequestCategorys == null) {
            this.custRequestCategorys = getRelated(CustRequestCategory.class, "CustRequestCategory");
        }
        return this.custRequestCategorys;
    }
    /**
     * Auto generated method that gets the related <code>CustRequestResolution</code> by the relation named <code>CustRequestResolution</code>.
     * @return the list of <code>CustRequestResolution</code>
     * @throws RepositoryException if an error occurs
     */
    public List<? extends CustRequestResolution> getCustRequestResolutions() throws RepositoryException {
        if (this.custRequestResolutions == null) {
            this.custRequestResolutions = getRelated(CustRequestResolution.class, "CustRequestResolution");
        }
        return this.custRequestResolutions;
    }
    /**
     * Auto generated method that gets the related <code>CustRequestType</code> by the relation named <code>ChildCustRequestType</code>.
     * @return the list of <code>CustRequestType</code>
     * @throws RepositoryException if an error occurs
     */
    public List<? extends CustRequestType> getChildCustRequestTypes() throws RepositoryException {
        if (this.childCustRequestTypes == null) {
            this.childCustRequestTypes = getRelated(CustRequestType.class, "ChildCustRequestType");
        }
        return this.childCustRequestTypes;
    }
    /**
     * Auto generated method that gets the related <code>CustRequestTypeAttr</code> by the relation named <code>CustRequestTypeAttr</code>.
     * @return the list of <code>CustRequestTypeAttr</code>
     * @throws RepositoryException if an error occurs
     */
    public List<? extends CustRequestTypeAttr> getCustRequestTypeAttrs() throws RepositoryException {
        if (this.custRequestTypeAttrs == null) {
            this.custRequestTypeAttrs = getRelated(CustRequestTypeAttr.class, "CustRequestTypeAttr");
        }
        return this.custRequestTypeAttrs;
    }

    /**
     * Auto generated value setter.
     * @param parentCustRequestType the parentCustRequestType to set
    */
    public void setParentCustRequestType(CustRequestType parentCustRequestType) {
        this.parentCustRequestType = parentCustRequestType;
    }
    /**
     * Auto generated value setter.
     * @param party the party to set
    */
    public void setParty(Party party) {
        this.party = party;
    }
    /**
     * Auto generated value setter.
     * @param partyRelationships the partyRelationships to set
    */
    public void setPartyRelationships(List<PartyRelationship> partyRelationships) {
        this.partyRelationships = partyRelationships;
    }
    /**
     * Auto generated value setter.
     * @param custRequests the custRequests to set
    */
    public void setCustRequests(List<CustRequest> custRequests) {
        this.custRequests = custRequests;
    }
    /**
     * Auto generated value setter.
     * @param custRequestCategorys the custRequestCategorys to set
    */
    public void setCustRequestCategorys(List<CustRequestCategory> custRequestCategorys) {
        this.custRequestCategorys = custRequestCategorys;
    }
    /**
     * Auto generated value setter.
     * @param custRequestResolutions the custRequestResolutions to set
    */
    public void setCustRequestResolutions(List<CustRequestResolution> custRequestResolutions) {
        this.custRequestResolutions = custRequestResolutions;
    }
    /**
     * Auto generated value setter.
     * @param childCustRequestTypes the childCustRequestTypes to set
    */
    public void setChildCustRequestTypes(List<CustRequestType> childCustRequestTypes) {
        this.childCustRequestTypes = childCustRequestTypes;
    }
    /**
     * Auto generated value setter.
     * @param custRequestTypeAttrs the custRequestTypeAttrs to set
    */
    public void setCustRequestTypeAttrs(List<CustRequestTypeAttr> custRequestTypeAttrs) {
        this.custRequestTypeAttrs = custRequestTypeAttrs;
    }

    /**
     * Auto generated method that add item to collection.
     */
    public void addCustRequestTypeAttr(CustRequestTypeAttr custRequestTypeAttr) {
        if (this.custRequestTypeAttrs == null) {
            this.custRequestTypeAttrs = new ArrayList<CustRequestTypeAttr>();
        }
        this.custRequestTypeAttrs.add(custRequestTypeAttr);
    }
    /**
     * Auto generated method that remove item from collection.
     */
    public void removeCustRequestTypeAttr(CustRequestTypeAttr custRequestTypeAttr) {
        if (this.custRequestTypeAttrs == null) {
            return;
        }
        this.custRequestTypeAttrs.remove(custRequestTypeAttr);
    }
    /**
     * Auto generated method that clear items from collection.
     */
    public void clearCustRequestTypeAttr() {
        if (this.custRequestTypeAttrs == null) {
            return;
        }
        this.custRequestTypeAttrs.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void fromMap(Map<String, Object> mapValue) {
        preInit();
        setCustRequestTypeId((String) mapValue.get("custRequestTypeId"));
        setParentTypeId((String) mapValue.get("parentTypeId"));
        setHasTable((String) mapValue.get("hasTable"));
        setDescription((String) mapValue.get("description"));
        setPartyId((String) mapValue.get("partyId"));
        setLastUpdatedStamp((Timestamp) mapValue.get("lastUpdatedStamp"));
        setLastUpdatedTxStamp((Timestamp) mapValue.get("lastUpdatedTxStamp"));
        setCreatedStamp((Timestamp) mapValue.get("createdStamp"));
        setCreatedTxStamp((Timestamp) mapValue.get("createdTxStamp"));
        postInit();
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> mapValue = new FastMap<String, Object>();
        mapValue.put("custRequestTypeId", getCustRequestTypeId());
        mapValue.put("parentTypeId", getParentTypeId());
        mapValue.put("hasTable", getHasTable());
        mapValue.put("description", getDescription());
        mapValue.put("partyId", getPartyId());
        mapValue.put("lastUpdatedStamp", getLastUpdatedStamp());
        mapValue.put("lastUpdatedTxStamp", getLastUpdatedTxStamp());
        mapValue.put("createdStamp", getCreatedStamp());
        mapValue.put("createdTxStamp", getCreatedTxStamp());
        return mapValue;
    }


}
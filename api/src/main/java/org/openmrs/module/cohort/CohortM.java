/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "cohort")
public class CohortM extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "cohort_id")
	private Integer cohortId;
	
	private String name;
	
	private String description;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "location_id")
	private Location location;
	
	private Date startDate;
	
	private Date endDate;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "cohort_type_id")
	private CohortType cohortType;
	
	@OneToMany(mappedBy = "cohort", cascade = CascadeType.ALL)
	private List<CohortAttribute> attributes = new ArrayList<>();
	
	@OneToMany(mappedBy = "cohort", cascade = CascadeType.ALL)
	private List<CohortMember> cohortMembers = new ArrayList<>();
	
	@Column(name = "is_group_cohort", nullable = false)
	private Boolean groupCohort;
	
	public Integer getCohortId() {
		return cohortId;
	}
	
	public void setCohortId(Integer cohortId) {
		this.cohortId = cohortId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public CohortType getCohortType() {
		return cohortType;
	}
	
	public void setCohortType(CohortType cohortType) {
		this.cohortType = cohortType;
	}
	
	public void setCohortMembers(List<CohortMember> cohortMembers) {
		this.cohortMembers = cohortMembers;
	}
	
	public List<CohortMember> getCohortMembers() {
		if (cohortMembers == null) {
			cohortMembers = new ArrayList<>();
		}
		return cohortMembers;
	}
	
	public List<CohortMember> getActiveCohortMembers() {
		List<CohortMember> members = new ArrayList<>();
		for (CohortMember member : getCohortMembers()) {
			if (!member.getVoided()) {
				members.add(member);
			}
		}
		return members;
	}
	
	public List<CohortAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<>();
		}
		return attributes;
	}
	
	public CohortMember getMember(String uuid) {
		if (uuid != null) {
			for (CohortMember member : getCohortMembers()) {
				if (uuid.equals(member.getUuid()) && !member.getVoided()) {
					return member;
				}
			}
		}
		return null;
	}
	
	public void setAttributes(List<CohortAttribute> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * Returns only the non-voided attributes for this cohort
	 *
	 * @return list attributes
	 * @should not get voided attributes
	 * @should not fail with null attributes
	 */
	public List<CohortAttribute> getActiveAttributes() {
		List<CohortAttribute> attrs = new ArrayList<>();
		for (CohortAttribute attr : getAttributes()) {
			if (!attr.getVoided()) {
				attrs.add(attr);
			}
		}
		return attrs;
	}
	
	/**
	 * Convenience method to add the <code>attribute</code> to this cohort attribute list if the
	 * attribute doesn't exist already.<br>
	 * <br>
	 * Voids any current attribute with type = <code>newAttribute.getAttributeType()</code><br>
	 * <br>
	 * NOTE: This effectively limits cohorts to only one attribute of any given type **
	 *
	 * @param newAttribute CohortAttribute to add to the CohortM
	 * @should fail when new attribute exist
	 * @should fail when new attribute are the same type with same value
	 * @should void old attribute when new attribute are the same type with different value
	 * @should remove attribute when old attribute are temporary
	 * @should not save an attribute with a null value
	 * @should not save an attribute with a blank string value
	 * @should void old attribute when a null or blank string value is added
	 */
	public void addAttribute(CohortAttribute newAttribute) {
		newAttribute.setCohort(this);
		boolean newIsNull = !StringUtils.hasText(newAttribute.getValue());
		
		for (CohortAttribute currentAttribute : getActiveAttributes()) {
			if (currentAttribute.equals(newAttribute)) {
				return; // if we have the same CohortAttributeId, don't add the new attribute
			} else if (currentAttribute.getCohortAttributeType().equals(newAttribute.getCohortAttributeType())) {
				if (currentAttribute.getValue() != null && currentAttribute.getValue().equals(newAttribute.getValue())) {
					// this cohort already has this attribute
					return;
				}
				
				// if the to-be-added attribute isn't already voided itself
				// and if we have the same type, different value
				if (!newAttribute.getVoided() || newIsNull) {
					if (currentAttribute.getCreator() != null) {
						currentAttribute.voidAttribute("New value: " + newAttribute.getValue());
					} else {
						// remove the attribute if it was just temporary (didn't have a creator
						// attached to it yet)
						removeAttribute(currentAttribute);
					}
				}
			}
		}
		if (!OpenmrsUtil.collectionContains(attributes, newAttribute) && !newIsNull) {
			attributes.add(newAttribute);
		}
	}
	
	/**
	 * Convenience method to get the <code>attribute</code> from this cohort's attribute list if the
	 * attribute exists already.
	 *
	 * @param attribute
	 * @should not fail when cohort attribute is null
	 * @should not fail when cohort attribute is not exist
	 * @should remove attribute when exist
	 */
	public void removeAttribute(CohortAttribute attribute) {
		if (attributes != null) {
			attributes.remove(attribute);
		}
	}
	
	/**
	 * Convenience Method to return the first non-voided cohort attribute matching a cohort attribute
	 * type. <br>
	 * <br>
	 * Returns null if this cohort has no non-voided {@link CohortAttribute} with the given
	 * {@link CohortAttributeType}, the given {@link CohortAttributeType} is null, or this cohort has no
	 * attributes.
	 *
	 * @param cohortAttributeType the CohortAttributeType to look for (can be a stub, see
	 *            {@link CohortAttributeType#equals(Object)} for how its compared)
	 * @return CohortAttribute that matches the given type
	 * @should not fail when attribute type is null
	 * @should not return voided attribute
	 * @should return null when existing CohortAttributeType is voided
	 */
	public CohortAttribute getAttribute(CohortAttributeType cohortAttributeType) {
		if (cohortAttributeType != null) {
			for (CohortAttribute attribute : getAttributes()) {
				if (cohortAttributeType.equals(attribute.getCohortAttributeType()) && !attribute.getVoided()) {
					return attribute;
				}
			}
		}
		return null;
	}
	
	/**
	 * Convenience method to get this cohort's first attribute that has a CohortAttributeType.name equal
	 * to <code>attributeName</code>.<br>
	 * <br>
	 * Returns null if this cohort has no non-voided {@link CohortAttribute} with the given type name,
	 * the given name is null, or this cohort has no attributes.
	 *
	 * @param attributeName the name string to match on
	 * @return CohortAttribute whose {@link CohortAttributeType#getName()} matches the given name string
	 * @should return cohort attribute based on attributeName
	 * @should return null if AttributeName is voided
	 */
	public CohortAttribute getAttribute(String attributeName) {
		if (attributeName != null) {
			for (CohortAttribute attribute : getAttributes()) {
				CohortAttributeType type = attribute.getCohortAttributeType();
				if (type != null && attributeName.equals(type.getName()) && !attribute.getVoided()) {
					return attribute;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Convenience method to get this cohort's first attribute that has a CohortAttributeTypeId equal to
	 * <code>attributeTypeId</code>.<br>
	 * <br>
	 * Returns null if this cohort has no non-voided {@link CohortAttribute} with the given type id or
	 * this cohort has no attributes.<br>
	 * <br>
	 * The given id cannot be null.
	 *
	 * @param attributeTypeId the id of the {@link CohortAttributeType} to look for
	 * @return CohortAttribute whose {@link CohortAttributeType#getId()} equals the given Integer id
	 * @should return CohortAttribute based on attributeTypeId
	 * @should return null when existing CohortAttribute with matching attribute type id is voided
	 */
	public CohortAttribute getAttribute(Integer attributeTypeId) {
		for (CohortAttribute attribute : getActiveAttributes()) {
			if (attributeTypeId.equals(attribute.getCohortAttributeType().getCohortAttributeTypeId())) {
				return attribute;
			}
		}
		return null;
	}
	
	/**
	 * Convenience method to get all of this cohort's attributes that have a CohortAttributeType.name
	 * equal to <code>attributeName</code>.
	 *
	 * @param attributeName
	 * @should return all CohortAttributes with matching attributeType names
	 */
	public List<CohortAttribute> getAttributes(String attributeName) {
		List<CohortAttribute> cohortAttributes = new ArrayList<>();
		
		for (CohortAttribute attribute : getActiveAttributes()) {
			CohortAttributeType type = attribute.getCohortAttributeType();
			if (type != null && attributeName.equals(type.getName())) {
				cohortAttributes.add(attribute);
			}
		}
		
		return cohortAttributes;
	}
	
	/**
	 * Convenience method to get all of this cohort's attributes that have a CohortAttributeType.id
	 * equal to <code>attributeTypeId</code>.
	 *
	 * @param attributeTypeId
	 * @should return empty list when matching CohortAttribute by id is voided
	 * @should return list of cohort attributes based on AttributeTypeId
	 */
	public List<CohortAttribute> getAttributes(Integer attributeTypeId) {
		List<CohortAttribute> ret = new ArrayList<>();
		
		for (CohortAttribute attribute : getActiveAttributes()) {
			if (attributeTypeId.equals(attribute.getCohortAttributeType().getCohortAttributeTypeId())) {
				ret.add(attribute);
			}
		}
		
		return ret;
	}
	
	/**
	 * Convenience method to get all of this cohort's attributes that have a CohortAttributeType equal
	 * to <code>CohortAttributeType</code>.
	 *
	 * @param CohortAttributeType Cohort attribute type
	 */
	public List<CohortAttribute> getAttributes(CohortAttributeType CohortAttributeType) {
		List<CohortAttribute> ret = new ArrayList<>();
		for (CohortAttribute attribute : getAttributes()) {
			if (CohortAttributeType.equals(attribute.getCohortAttributeType()) && !attribute.getVoided()) {
				ret.add(attribute);
			}
		}
		return ret;
	}
	
	/**
	 * Convenience method for viewing all of the cohort's current attributes
	 *
	 * @return Returns a string with all the attributes
	 */
	public String printAttributes() {
		StringBuilder s = new StringBuilder("");
		
		for (CohortAttribute attribute : getAttributes()) {
			s.append(attribute.getCohortAttributeType()).append(" : ").append(attribute.getValue()).append(" : voided? ")
			        .append(attribute.getVoided()).append("\n");
		}
		
		return s.toString();
	}
	
	/**
	 * Returns whether this cohort is a group
	 *
	 * @deprecated since 3.0.0
	 * @see {{@link #getGroupCohort()}}
	 */
	@Deprecated
	public Boolean isGroupCohort() {
		return groupCohort;
	}
	
	public void setGroupCohort(Boolean groupCohort) {
		this.groupCohort = groupCohort;
	}
	
	public Boolean getGroupCohort() {
		return this.groupCohort;
	}
	
	@Override
	public Integer getId() {
		return getCohortId();
	}
	
	@Override
	public void setId(Integer cohortId) {
		setCohortId(cohortId);
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}

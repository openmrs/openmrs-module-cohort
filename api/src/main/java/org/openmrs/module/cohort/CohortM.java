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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Where;
import org.openmrs.Auditable;
import org.openmrs.BaseCustomizableData;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.module.cohort.definition.CohortDefinitionHandler;
import org.openmrs.module.cohort.definition.ManualCohortDefinitionHandler;
import org.openmrs.module.cohort.exceptions.ManualChangeNotSupportedException;

@Slf4j
@Entity
@Table(name = "cohort")
public class CohortM extends BaseCustomizableData<CohortAttribute> implements Auditable, Customizable<CohortAttribute> {
	
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
	private Set<CohortMember> cohortMembers = new HashSet<>();
	
	@OneToMany(mappedBy = "cohort", cascade = CascadeType.ALL)
	@Where(clause = "voided = 0 and (start_date is null or start_date <= current_timestamp()) and (end_date is null or end_date >= current_timestamp())")
	private Set<CohortMember> activeCohortMembers;
	
	@Column(name = "is_group_cohort", nullable = false)
	private Boolean groupCohort;
	
	@Column(name = "definition_handler", nullable = false)
	private String definitionHandlerClassname;
	
	@Lob
	@Column(name = "definition_handler_config")
	private String definitionHandlerConfig;
	
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
	
	public Set<CohortMember> getCohortMembers() {
		if (cohortMembers == null) {
			cohortMembers = new HashSet<>();
		}
		return cohortMembers;
	}
	
	public Set<CohortMember> getActiveCohortMembers() {
		if (activeCohortMembers == null) {
			activeCohortMembers = new HashSet<>();
		}
		return activeCohortMembers;
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
	
	public String getDefinitionHandlerClassname() {
		return definitionHandlerClassname;
	}
	
	public void setDefinitionHandlerClassname(String definitionHandlerClassname) {
		this.definitionHandlerClassname = definitionHandlerClassname;
	}
	
	public String getDefinitionHandlerConfig() {
		return definitionHandlerConfig;
	}
	
	public void setDefinitionHandlerConfig(String definitionHandlerConfig) {
		this.definitionHandlerConfig = definitionHandlerConfig;
	}
	
	public void addMembership(CohortMember cohortMember) {
		CohortDefinitionHandler cohortDefinition = getDefinitionHandler();
		if (cohortDefinition instanceof ManualCohortDefinitionHandler) {
			((ManualCohortDefinitionHandler) cohortDefinition).addMember(this, cohortMember);
		} else {
			throw new ManualChangeNotSupportedException("Manual changes to this cohort aren't supported");
		}
	}
	
	public void removeMembership(CohortMember cohortMember) {
		if (getDefinitionHandlerClassname().isEmpty()) {
			throw new APIException("CohortDefinitionHandler is null");
		}
		CohortDefinitionHandler cohortDefinition = getDefinitionHandler();
		if (cohortDefinition instanceof ManualCohortDefinitionHandler) {
			((ManualCohortDefinitionHandler) cohortDefinition).removeMember(this, cohortMember);
		} else {
			throw new ManualChangeNotSupportedException("Manual changes to this cohort aren't supported");
		}
	}
	
	/**
	 * Loads cohort definition handler class - If the cohortDefinitionHandlerClassname is null, use
	 * defaultCohortDefinitionHandler
	 *
	 * @return CohortDefinitionHandler
	 * @throws org.openmrs.api.APIException If fails to load cohortDefinitionHandlerClass
	 */
	@SneakyThrows
	@SuppressWarnings("unchecked")
	public CohortDefinitionHandler getDefinitionHandler() {
		Class<?> definitionHandlerClass;
		try {
			definitionHandlerClass = Context.loadClass(getDefinitionHandlerClassname());
		}
		catch (ClassNotFoundException e) {
			log.error("Failed to load class {}", getDefinitionHandlerClassname(), e);
			throw new APIException("CohortM.failed.load.definitionHandlerClass",
			        new Object[] { getDefinitionHandlerClassname() }, e);
		}
		
		if (definitionHandlerClass == null) {
			log.error("Failed to load class {}", getDefinitionHandlerClassname());
			throw new APIException("CohortM.failed.load.definitionHandlerClass",
			        new Object[] { getDefinitionHandlerClassname() });
		}
		
		List<? extends CohortDefinitionHandler> handlers = (List<? extends CohortDefinitionHandler>) Context
		        .getRegisteredComponents(definitionHandlerClass);
		
		if (!handlers.isEmpty()) {
			if (log.isWarnEnabled()) {
				if (handlers.size() > 1) {
					log.warn("Found {} possible handlers for {}; using the first result", handlers.size(),
					    getDefinitionHandlerClassname());
				}
			}
			return handlers.get(0);
		}
		
		return (CohortDefinitionHandler) definitionHandlerClass.getDeclaredConstructor().newInstance();
	}
	
	public int size() {
		return getActiveCohortMembers().size();
	}
}

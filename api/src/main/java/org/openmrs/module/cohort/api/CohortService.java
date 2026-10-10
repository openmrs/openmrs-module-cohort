/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.api;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortType;
import org.springframework.transaction.annotation.Transactional;

public interface CohortService extends OpenmrsService {
	
	String MANAGE_COHORTS_PRIVILEGE = "Manage Cohorts In Cohort Module";
	
	String VIEW_COHORTS_PRIVILEGE = "View Cohorts In Cohort Module";
	
	String EDIT_COHORTS_PRIVILEGE = "Edit Cohorts in Cohort Module";
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortM getCohortM(@NotNull String name);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortM getCohortM(@NotNull int id);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortM getCohortMByUuid(@NotNull String uuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortM> findCohortMByLocationUuid(@NotNull String locationUuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortM> findCohortMByPatientUuid(@NotNull String patientUuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortM> findAll();
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	CohortM saveCohortM(@NotNull CohortM cohortType);
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	void voidCohortM(@NotNull CohortM cohort, String reason);
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	void purgeCohortM(@NotNull CohortM cohortType);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortAttribute getCohortAttributeByUuid(@NotNull String uuid);
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	CohortAttribute saveCohortAttribute(@NotNull CohortAttribute attribute);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortAttribute> findCohortAttributesByCohortUuid(@NotNull String cohortUuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortAttribute> findCohortAttributesByTypeUuid(@NotNull String attributeTypeUuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortAttribute> findCohortAttributesByTypeName(@NotNull String attributeTypeName);
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	void voidCohortAttribute(@NotNull CohortAttribute attribute, String retiredReason);
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	void purgeCohortAttribute(@NotNull CohortAttribute attribute);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortAttributeType getCohortAttributeTypeByUuid(@NotNull String uuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortAttributeType getCohortAttributeTypeByName(@NotNull String name);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortAttributeType> findAllCohortAttributeTypes();
	
	@Transactional
	@Authorized(MANAGE_COHORTS_PRIVILEGE)
	CohortAttributeType saveCohortAttributeType(@NotNull CohortAttributeType attributeType);
	
	@Transactional
	@Authorized(MANAGE_COHORTS_PRIVILEGE)
	void voidCohortAttributeType(@NotNull CohortAttributeType attributeType, String retiredReason);
	
	@Transactional
	@Authorized(MANAGE_COHORTS_PRIVILEGE)
	void purgeCohortAttributeType(@NotNull CohortAttributeType attributeType);
	
	//Search
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	List<CohortM> findMatchingCohortMs(String nameMatching, Map<String, String> attributes, CohortType cohortType,
	        boolean includeVoided);
}

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

import static org.openmrs.module.cohort.api.CohortService.EDIT_COHORTS_PRIVILEGE;
import static org.openmrs.module.cohort.api.CohortService.MANAGE_COHORTS_PRIVILEGE;
import static org.openmrs.module.cohort.api.CohortService.VIEW_COHORTS_PRIVILEGE;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.CohortMemberAttributeType;
import org.springframework.transaction.annotation.Transactional;

public interface CohortMemberService extends OpenmrsService {
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortMember getCohortMemberByUuid(@NotNull String uuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortMember getCohortMemberByName(@NotNull String name);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortMember> findAllCohortMembers();
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	CohortMember saveCohortMember(@NotNull CohortMember cohortMember);
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	void voidCohortMember(@NotNull CohortMember cohortMember, String voidReason);
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	void purgeCohortMember(@NotNull CohortMember cohortMember);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortMemberAttributeType getCohortMemberAttributeTypeByUuid(@NotNull String uuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortMemberAttributeType> findAllCohortMemberAttributeTypes();
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	CohortMemberAttribute saveCohortMemberAttribute(CohortMemberAttribute cohortMemberAttributeType);
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	void voidCohortMemberAttribute(CohortMemberAttribute cohortMemberAttribute, String voidReason);
	
	@Transactional
	@Authorized(EDIT_COHORTS_PRIVILEGE)
	void purgeCohortMemberAttribute(CohortMemberAttribute cohortMemberAttribute);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortMemberAttribute getCohortMemberAttributeByUuid(@NotNull String uuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortMemberAttribute> getCohortMemberAttributesByTypeUuid(@NotNull String attributeTypeUuid);
	
	@Transactional
	@Authorized(MANAGE_COHORTS_PRIVILEGE)
	CohortMemberAttributeType saveCohortMemberAttributeType(CohortMemberAttributeType cohortMemberAttributeType);
	
	@Transactional
	@Authorized(MANAGE_COHORTS_PRIVILEGE)
	void voidCohortMemberAttributeType(CohortMemberAttributeType cohortMemberAttributeType, String voidReason);
	
	@Transactional
	@Authorized(MANAGE_COHORTS_PRIVILEGE)
	void purgeCohortMemberAttributeType(CohortMemberAttributeType cohortMemberAttributeType);
	
	//Search methods
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortMember> findCohortMembersByCohortUuid(@NotNull String cohortUuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortMember> findCohortMembersByPatientUuid(@NotNull String patientUuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortMember> findCohortMembersByPatientName(@NotNull String patientName);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortMember> findCohortMembersByCohortAndPatientName(@NotNull String cohortUuid,
	        @NotNull String patientName);
}

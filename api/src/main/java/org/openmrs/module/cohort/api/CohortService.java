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

import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortType;
import org.springframework.transaction.annotation.Transactional;

public interface CohortService extends OpenmrsService {
	
	@Transactional(readOnly = true)
	CohortM getCohortM(@NotNull String name);
	
	@Transactional(readOnly = true)
	CohortM getCohortM(@NotNull int id);
	
	@Transactional(readOnly = true)
	CohortM getCohortMByUuid(@NotNull String uuid);
	
	@Transactional(readOnly = true)
	Collection<CohortM> findCohortMByLocationUuid(@NotNull String locationUuid);
	
	@Transactional(readOnly = true)
	Collection<CohortM> findCohortMByPatientUuid(@NotNull String patientUuid);
	
	@Transactional(readOnly = true)
	Collection<CohortM> findAll();
	
	@Transactional
	CohortM saveCohortM(@NotNull CohortM cohortType);
	
	@Transactional
	void voidCohortM(@NotNull CohortM cohort, String reason);
	
	@Transactional
	void purgeCohortM(@NotNull CohortM cohortType);
	
	@Transactional(readOnly = true)
	CohortAttribute getCohortAttributeByUuid(@NotNull String uuid);
	
	@Transactional
	CohortAttribute saveCohortAttribute(@NotNull CohortAttribute attribute);
	
	@Transactional(readOnly = true)
	Collection<CohortAttribute> findCohortAttributesByCohortUuid(@NotNull String cohortUuid);
	
	@Transactional(readOnly = true)
	Collection<CohortAttribute> findCohortAttributesByTypeUuid(@NotNull String attributeTypeUuid);
	
	@Transactional(readOnly = true)
	Collection<CohortAttribute> findCohortAttributesByTypeName(@NotNull String attributeTypeName);
	
	@Transactional
	void voidCohortAttribute(@NotNull CohortAttribute attribute, String retiredReason);
	
	@Transactional
	void purgeCohortAttribute(@NotNull CohortAttribute attribute);
	
	@Transactional(readOnly = true)
	CohortAttributeType getCohortAttributeTypeByUuid(@NotNull String uuid);
	
	@Transactional(readOnly = true)
	CohortAttributeType getCohortAttributeTypeByName(@NotNull String name);
	
	@Transactional(readOnly = true)
	Collection<CohortAttributeType> findAllCohortAttributeTypes();
	
	@Transactional
	CohortAttributeType saveCohortAttributeType(@NotNull CohortAttributeType attributeType);
	
	@Transactional
	void voidCohortAttributeType(@NotNull CohortAttributeType attributeType, String retiredReason);
	
	@Transactional
	void purgeCohortAttributeType(@NotNull CohortAttributeType attributeType);
	
	//Search
	@Transactional(readOnly = true)
	List<CohortM> findMatchingCohortMs(String nameMatching, Map<String, String> attributes, CohortType cohortType,
	        boolean includeVoided);
}

package org.openmrs.module.cohort.api;

import static org.openmrs.module.cohort.api.CohortService.MANAGE_COHORTS_PRIVILEGE;
import static org.openmrs.module.cohort.api.CohortService.VIEW_COHORTS_PRIVILEGE;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.cohort.CohortType;
import org.springframework.transaction.annotation.Transactional;

public interface CohortTypeService extends OpenmrsService {
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortType getCohortTypeByUuid(@NotNull String uuid);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortType getCohortTypeByUuid(@NotNull String uuid, boolean includeVoided);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortType getCohortTypeByName(@NotNull String name);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	CohortType getCohortTypeByName(@NotNull String name, boolean includeVoided);
	
	@Transactional(readOnly = true)
	@Authorized(VIEW_COHORTS_PRIVILEGE)
	Collection<CohortType> findAllCohortTypes();
	
	@Transactional
	@Authorized(MANAGE_COHORTS_PRIVILEGE)
	CohortType saveCohortType(@NotNull CohortType cohortType);
	
	@Transactional
	@Authorized(MANAGE_COHORTS_PRIVILEGE)
	void voidCohortType(@NotNull CohortType cohortType, String voidedReason);
	
	@Transactional
	@Authorized(MANAGE_COHORTS_PRIVILEGE)
	void purgeCohortType(@NotNull CohortType cohortType);
	
}

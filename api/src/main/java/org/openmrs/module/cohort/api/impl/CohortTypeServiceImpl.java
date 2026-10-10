package org.openmrs.module.cohort.api.impl;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortTypeService;
import org.openmrs.module.cohort.api.dao.GenericDao;
import org.openmrs.module.cohort.api.dao.search.PropValue;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Setter(AccessLevel.PACKAGE)
public class CohortTypeServiceImpl extends BaseOpenmrsService implements CohortTypeService {
	
	private final GenericDao<CohortType> dao;
	
	public CohortTypeServiceImpl(GenericDao<CohortType> dao) {
		this.dao = dao;
	}
	
	@Override
	public CohortType getCohortTypeByUuid(@NotNull String uuid) {
		return getCohortTypeByUuid(uuid, false);
	}
	
	@Override
	public CohortType getCohortTypeByUuid(String uuid, boolean includeVoided) {
		return dao.get(uuid, includeVoided);
	}
	
	@Override
	public CohortType getCohortTypeByName(@NotNull String name) {
		return getCohortTypeByName(name, false);
	}
	
	@Override
	public CohortType getCohortTypeByName(String name, boolean includeVoided) {
		return dao.findByUniqueProp(PropValue.builder().property("name").value(name).build(), includeVoided);
	}
	
	@Override
	public Collection<CohortType> findAllCohortTypes() {
		return dao.findAll();
	}
	
	@Override
	public CohortType saveCohortType(CohortType cohortType) {
		return dao.createOrUpdate(cohortType);
	}
	
	@Override
	public void voidCohortType(@NotNull CohortType cohortType, String voidedReason) {
		if (cohortType == null) {
			return;
		}
		dao.createOrUpdate(cohortType);
	}
	
	@Override
	public void purgeCohortType(CohortType cohortType) {
		dao.delete(cohortType);
	}
}

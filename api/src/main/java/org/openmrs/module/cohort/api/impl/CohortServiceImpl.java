/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.api.impl;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.cohort.api.dao.GenericDao;
import org.openmrs.module.cohort.api.dao.search.PropValue;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Setter(AccessLevel.PACKAGE)
public class CohortServiceImpl extends BaseOpenmrsService implements CohortService {
	
	private final GenericDao<CohortM> cohortDao;
	
	private final GenericDao<CohortAttribute> cohortAttributeDao;
	
	private final GenericDao<CohortAttributeType> cohortAttributeTypeDao;
	
	public CohortServiceImpl(GenericDao<CohortM> cohortDao, GenericDao<CohortAttribute> cohortAttributeDao,
	    GenericDao<CohortAttributeType> cohortAttributeTypeDao) {
		this.cohortDao = cohortDao;
		this.cohortAttributeDao = cohortAttributeDao;
		this.cohortAttributeTypeDao = cohortAttributeTypeDao;
	}
	
	@Override
	public CohortM getCohortMByUuid(@NotNull String uuid) {
		return cohortDao.get(uuid);
	}
	
	@Override
	public CohortM getCohortM(@NotNull String name) {
		return cohortDao.findByUniqueProp(PropValue.builder().property("name").value(name).build());
	}
	
	@Override
	public CohortM getCohortM(int id) {
		return cohortDao.get(id);
	}
	
	@Override
	public Collection<CohortM> findCohortMByLocationUuid(@NotNull String locationUuid) {
		return cohortDao.findBy(
		    PropValue.builder().property("uuid").associationPath(Optional.of("location")).value(locationUuid).build());
	}
	
	@Override
	public Collection<CohortM> findCohortMByPatientUuid(@NotNull String patientUuid) {
		return cohortDao.findBy(
		    PropValue.builder().property("uuid").associationPath(Optional.of("patient")).value(patientUuid).build());
	}
	
	@Override
	public Collection<CohortM> findAll() {
		return cohortDao.findAll();
	}
	
	@Override
	public CohortM saveCohortM(@NotNull CohortM cohortM) {
		CustomDatatypeUtil.saveAttributesIfNecessary(cohortM);
		if (Hibernate.isInitialized(cohortM.getCohortMembers())) {
			// members added through the cohort are inserted by cascade, so their attribute values are serialized here too
			cohortM.getCohortMembers().stream().filter(member -> member.getId() == null)
			        .forEach(CustomDatatypeUtil::saveAttributesIfNecessary);
		}
		return cohortDao.createOrUpdate(cohortM);
	}
	
	@Override
	public void voidCohortM(CohortM cohort, String reason) {
		if (cohort == null) {
			return;
		}
		
		cohortDao.createOrUpdate(cohort);
	}
	
	@Override
	public void purgeCohortM(@NotNull CohortM cohort) {
		cohortDao.delete(cohort);
	}
	
	@Override
	public CohortAttribute getCohortAttributeByUuid(@NotNull String uuid) {
		return cohortAttributeDao.get(uuid);
	}
	
	@Override
	public CohortAttribute saveCohortAttribute(@NotNull CohortAttribute attribute) {
		CustomDatatypeUtil.saveIfDirty(attribute);
		return cohortAttributeDao.createOrUpdate(attribute);
	}
	
	@Override
	public Collection<CohortAttribute> findCohortAttributesByCohortUuid(String cohortUuid) {
		return cohortAttributeDao.findBy(
		    PropValue.builder().associationPath(Optional.of("cohort")).property("uuid").value(cohortUuid).build());
	}
	
	@Override
	public Collection<CohortAttribute> findCohortAttributesByTypeUuid(String attributeTypeUuid) {
		return cohortAttributeDao.findBy(PropValue.builder().associationPath(Optional.of("attributeType")).property("uuid")
		        .value(attributeTypeUuid).build());
	}
	
	@Override
	public Collection<CohortAttribute> findCohortAttributesByTypeName(String attributeTypeName) {
		return cohortAttributeDao.findBy(PropValue.builder().associationPath(Optional.of("attributeType")).property("name")
		        .value(attributeTypeName).build());
	}
	
	@Override
	public void voidCohortAttribute(@NotNull CohortAttribute attribute, String retiredReason) {
		if (attribute == null) {
			return;
		}
		
		cohortAttributeDao.createOrUpdate(attribute);
	}
	
	@Override
	public void purgeCohortAttribute(@NotNull CohortAttribute attribute) {
		cohortAttributeDao.delete(attribute);
	}
	
	@Override
	public CohortAttributeType getCohortAttributeTypeByUuid(@NotNull String uuid) {
		return cohortAttributeTypeDao.get(uuid);
	}
	
	@Override
	public CohortAttributeType getCohortAttributeTypeByName(String name) {
		return cohortAttributeTypeDao.findByUniqueProp(PropValue.builder().property("name").value(name).build());
	}
	
	@Override
	public Collection<CohortAttributeType> findAllCohortAttributeTypes() {
		return cohortAttributeTypeDao.findAll();
	}
	
	@Override
	public CohortAttributeType saveCohortAttributeType(@NotNull CohortAttributeType attributeType) {
		return cohortAttributeTypeDao.createOrUpdate(attributeType);
	}
	
	@Override
	public void voidCohortAttributeType(@NotNull CohortAttributeType attributeType, String retiredReason) {
		if (attributeType == null) {
			return;
		}
		
		cohortAttributeTypeDao.createOrUpdate(attributeType);
	}
	
	@Override
	public void purgeCohortAttributeType(@NotNull CohortAttributeType attributeType) {
		cohortAttributeTypeDao.delete(attributeType);
	}
	
	@Override
	public List<CohortM> findMatchingCohortMs(String nameMatching, Map<String, String> attributes, CohortType cohortType,
	        boolean includeVoided) {
		return cohortDao.getSearchHandler().findCohorts(nameMatching, attributes, cohortType, includeVoided);
	}
}

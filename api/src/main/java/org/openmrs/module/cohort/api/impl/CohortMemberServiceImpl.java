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
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Setter;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.CohortMemberAttributeType;
import org.openmrs.module.cohort.api.CohortMemberService;
import org.openmrs.module.cohort.api.dao.GenericDao;
import org.openmrs.module.cohort.api.dao.search.PropValue;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Setter(AccessLevel.PACKAGE)
public class CohortMemberServiceImpl extends BaseOpenmrsService implements CohortMemberService {
	
	private final GenericDao<CohortMember> cohortMemberDao;
	
	private final GenericDao<CohortMemberAttributeType> cohortMemberAttributeTypeDao;
	
	private final GenericDao<CohortMemberAttribute> cohortMemberAttributeDao;
	
	public CohortMemberServiceImpl(GenericDao<CohortMember> cohortMemberDao,
	    GenericDao<CohortMemberAttributeType> cohortMemberAttributeTypeDao,
	    GenericDao<CohortMemberAttribute> cohortMemberAttributeDao) {
		this.cohortMemberDao = cohortMemberDao;
		this.cohortMemberAttributeTypeDao = cohortMemberAttributeTypeDao;
		this.cohortMemberAttributeDao = cohortMemberAttributeDao;
	}
	
	@Override
	@Transactional(readOnly = true)
	public CohortMember getCohortMemberByUuid(@NotNull String uuid) {
		return cohortMemberDao.get(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public CohortMember getCohortMemberByName(String name) {
		return cohortMemberDao.findByUniqueProp(PropValue.builder().property("name").value(name).build());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<CohortMember> findAllCohortMembers() {
		return cohortMemberDao.findAll();
	}
	
	@Override
	public CohortMember saveCohortMember(CohortMember cohortMember) {
		CustomDatatypeUtil.saveAttributesIfNecessary(cohortMember);
		return cohortMemberDao.createOrUpdate(cohortMember);
	}
	
	@Override
	public void voidCohortMember(CohortMember cohortMember, String voidReason) {
		if (cohortMember == null) {
			return;
		}
		
		cohortMemberDao.createOrUpdate(cohortMember);
	}
	
	@Override
	public void purgeCohortMember(CohortMember cohortMember) {
		cohortMemberDao.delete(cohortMember);
	}
	
	@Override
	@Transactional(readOnly = true)
	public CohortMemberAttributeType getCohortMemberAttributeTypeByUuid(String uuid) {
		return cohortMemberAttributeTypeDao.get(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<CohortMemberAttributeType> findAllCohortMemberAttributeTypes() {
		return cohortMemberAttributeTypeDao.findAll();
	}
	
	@Override
	public CohortMemberAttributeType saveCohortMemberAttributeType(CohortMemberAttributeType cohortMemberAttributeType) {
		return cohortMemberAttributeTypeDao.createOrUpdate(cohortMemberAttributeType);
	}
	
	@Override
	public void voidCohortMemberAttributeType(CohortMemberAttributeType cohortMemberAttributeType, String voidReason) {
		cohortMemberAttributeType.setRetired(true);
		cohortMemberAttributeType.setRetireReason(voidReason);
		cohortMemberAttributeTypeDao.createOrUpdate(cohortMemberAttributeType);
	}
	
	@Override
	public void purgeCohortMemberAttributeType(CohortMemberAttributeType cohortMemberAttributeType) {
		cohortMemberAttributeTypeDao.delete(cohortMemberAttributeType);
	}
	
	@Override
	@Transactional(readOnly = true)
	public CohortMemberAttribute getCohortMemberAttributeByUuid(@NotNull String uuid) {
		return cohortMemberAttributeDao.get(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<CohortMemberAttribute> getCohortMemberAttributesByTypeUuid(@NotNull String uuid) {
		return cohortMemberAttributeDao.findBy(
		    PropValue.builder().property("uuid").associationPath(Optional.of("attributeType")).value(uuid).build());
	}
	
	@Override
	public CohortMemberAttribute saveCohortMemberAttribute(CohortMemberAttribute cohortMemberAttribute) {
		CustomDatatypeUtil.saveIfDirty(cohortMemberAttribute);
		return cohortMemberAttributeDao.createOrUpdate(cohortMemberAttribute);
	}
	
	@Override
	public void voidCohortMemberAttribute(CohortMemberAttribute attribute, String voidReason) {
		attribute.setVoided(true);
		attribute.setVoidReason(voidReason);
		cohortMemberAttributeDao.createOrUpdate(attribute);
	}
	
	@Override
	public void purgeCohortMemberAttribute(CohortMemberAttribute cohortMemberAttribute) {
		cohortMemberAttributeDao.delete(cohortMemberAttribute);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<CohortMember> findCohortMembersByCohortUuid(String cohortUuid) {
		return cohortMemberDao.findByAnd((cb, root) -> cb.equal(root.join("cohort").get("uuid"), cohortUuid),
		    (cb, root) -> cb.isNull(root.get("endDate")));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<CohortMember> findCohortMembersByPatientUuid(String patientUuid) {
		return cohortMemberDao.findByAnd((cb, root) -> cb.equal(root.join("patient").get("uuid"), patientUuid),
		    (cb, root) -> cb.isNull(root.get("endDate")));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<CohortMember> findCohortMembersByPatientName(@NotNull String patientName) {
		return cohortMemberDao.getSearchHandler().findCohortMembersByPatientNames(patientName);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<CohortMember> findCohortMembersByCohortAndPatientName(@NotNull String cohortUuid,
	        @NotNull String patientName) {
		return cohortMemberDao.getSearchHandler().findCohortMembersByCohortAndPatient(cohortUuid, patientName);
	}
}

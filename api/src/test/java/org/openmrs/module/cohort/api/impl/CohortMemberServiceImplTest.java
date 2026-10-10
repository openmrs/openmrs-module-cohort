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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.CohortMemberAttributeType;
import org.openmrs.module.cohort.api.dao.GenericDao;
import org.openmrs.module.cohort.api.dao.search.SearchQueryHandler;

@ExtendWith(MockitoExtension.class)
public class CohortMemberServiceImplTest {
	
	private final String COHORT_MEMBER_ATTRIBUTE_TYPE_UUID = "6e0d1303-9a41-40ce-b951-3d8e2aadbf99";
	
	private final String COHORT_MEMBER_ATTRIBUTE_UUID = "32816782-d578-401c-8475-8ccbb26ce001";
	
	@Mock
	private GenericDao<CohortMemberAttribute> cohortMemberAttributeDao;
	
	@Mock
	private GenericDao<CohortMemberAttributeType> cohortMemberAttributeTypeDao;
	
	@Mock
	private GenericDao<CohortMember> cohortMemberDao;
	
	@Mock
	private SearchQueryHandler searchHandler;
	
	private CohortMemberServiceImpl cohortMemberService;
	
	@BeforeEach
	public void setup() {
		cohortMemberService = new CohortMemberServiceImpl(cohortMemberDao, cohortMemberAttributeTypeDao,
		        cohortMemberAttributeDao);
	}
	
	@Test
	public void getCohortMemberAttributeType_shouldReturnMatchingCohortMemberAttributeType() {
		CohortMemberAttributeType cohortMemberAttributeType = mock(CohortMemberAttributeType.class);
		when(cohortMemberAttributeTypeDao.get(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID)).thenReturn(cohortMemberAttributeType);
		when(cohortMemberAttributeType.getUuid()).thenReturn(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID);
		
		CohortMemberAttributeType result = cohortMemberService
		        .getCohortMemberAttributeTypeByUuid(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), is(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void getAllCohortMemberAttributeTypes() {
	}
	
	@Test
	public void saveCohortMemberAttributeType() {
		CohortMemberAttributeType cohortMemberAttributeType = mock(CohortMemberAttributeType.class);
		when(cohortMemberAttributeTypeDao.createOrUpdate(cohortMemberAttributeType)).thenReturn(cohortMemberAttributeType);
		when(cohortMemberAttributeType.getId()).thenReturn(1);
		
		CohortMemberAttributeType result = cohortMemberService.saveCohortMemberAttributeType(cohortMemberAttributeType);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), is(1));
	}
	
	@Test
	public void purgeCohortMemberAttributeType() {
		CohortMemberAttributeType cohortMemberAttributeType = mock(CohortMemberAttributeType.class);
		
		cohortMemberService.purgeCohortMemberAttributeType(cohortMemberAttributeType);
		
		verify(cohortMemberAttributeTypeDao).delete(cohortMemberAttributeType);
	}
	
	@Test
	public void getCohortMemberAttributeByUuid() {
		CohortMemberAttribute cohortMemberAttribute = mock(CohortMemberAttribute.class);
		when(cohortMemberAttributeDao.get(COHORT_MEMBER_ATTRIBUTE_UUID)).thenReturn(cohortMemberAttribute);
		when(cohortMemberAttribute.getUuid()).thenReturn(COHORT_MEMBER_ATTRIBUTE_UUID);
		
		CohortMemberAttribute result = cohortMemberService.getCohortMemberAttributeByUuid(COHORT_MEMBER_ATTRIBUTE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), is(COHORT_MEMBER_ATTRIBUTE_UUID));
	}
	
	@Test
	public void saveCohortMemberAttribute() {
		CohortMemberAttribute cohortMemberAttribute = mock(CohortMemberAttribute.class);
		when(cohortMemberAttributeDao.createOrUpdate(cohortMemberAttribute)).thenReturn(cohortMemberAttribute);
		when(cohortMemberAttribute.getId()).thenReturn(1);
		
		CohortMemberAttribute result = cohortMemberService.saveCohortMemberAttribute(cohortMemberAttribute);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), is(1));
	}
	
	@Test
	public void purgeCohortMemberAttribute() {
	}
}

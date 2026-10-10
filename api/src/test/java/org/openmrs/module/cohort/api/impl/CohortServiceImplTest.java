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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Location;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.dao.GenericDao;
import org.openmrs.module.cohort.api.dao.search.PropValue;

@ExtendWith(MockitoExtension.class)
public class CohortServiceImplTest {
	
	private static final String COHORT_UUID = "5c28c01f-199a-48c7-8693-2a504dd1f4ab";
	
	private static final String COHORT_ATTRIBUTE_UUID = "06036a52-cf51-4182-9283-dedb15fea65a";
	
	@Mock
	private GenericDao<CohortM> cohortDao;
	
	@Mock
	private GenericDao<CohortAttribute> cohortAttributeDao;
	
	@Mock
	private GenericDao<CohortAttributeType> cohortAttributeTypeDao;
	
	private CohortServiceImpl cohortService;
	
	@BeforeEach
	public void setup() {
		cohortService = new CohortServiceImpl(cohortDao, cohortAttributeDao, cohortAttributeTypeDao);
	}
	
	@Test
	public void createOrUpdate_shouldCreateNewCohort() {
		CohortType cohortType = mock(CohortType.class);
		CohortM cohortM = new CohortM();
		cohortM.setCohortId(12);
		cohortM.setCohortType(cohortType);
		
		when(cohortDao.createOrUpdate(cohortM)).thenReturn(cohortM);
		
		CohortM result = cohortService.saveCohortM(cohortM);
		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(12));
	}
	
	@Test
	public void getAllCohortAttributeTypes_shouldGetAllCohortAttributeTypes() {
		CohortAttributeType attributeType = mock(CohortAttributeType.class);
		
		when(cohortAttributeTypeDao.findAll()).thenReturn(Collections.singletonList(attributeType));
		
		Collection<CohortAttributeType> allAttributeTypes = cohortService.findAllCohortAttributeTypes();
		assertThat(allAttributeTypes, notNullValue());
		assertThat(allAttributeTypes.size(), equalTo(1));
	}
	
	@Test
	public void getAllCohorts_shouldGetAllCohorts() {
		CohortM cohort = mock(CohortM.class);
		
		when(cohortDao.findAll()).thenReturn(Collections.singletonList(cohort));
		
		Collection<CohortM> resultList = cohortService.findAll();
		assertThat(resultList, notNullValue());
		assertThat(resultList, hasSize(1));
	}
	
	@Test
	public void shouldCreateNewCohortAttribute() {
		CohortAttribute cohortAttribute = mock(CohortAttribute.class);
		
		when(cohortAttribute.getCohortAttributeId()).thenReturn(345);
		when(cohortAttributeDao.createOrUpdate(cohortAttribute)).thenReturn(cohortAttribute);
		
		CohortAttribute attribute = cohortService.saveCohortAttribute(cohortAttribute);
		assertThat(attribute, notNullValue());
		assertThat(attribute.getCohortAttributeId(), equalTo(345));
	}
	
	@Test
	public void shouldGetCohortByUuid() {
		CohortM cohortM = mock(CohortM.class);
		
		when(cohortM.getUuid()).thenReturn(COHORT_UUID);
		when(cohortDao.get(COHORT_UUID)).thenReturn(cohortM);
		
		CohortM result = cohortService.getCohortMByUuid(COHORT_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(COHORT_UUID));
	}
	
	@Test
	public void shouldGetCohortAttributeByUuid() {
		CohortAttribute cohortAttribute = mock(CohortAttribute.class);
		
		when(cohortAttribute.getCohortAttributeId()).thenReturn(12);
		when(cohortAttribute.getUuid()).thenReturn(COHORT_ATTRIBUTE_UUID);
		when(cohortAttributeDao.get(COHORT_ATTRIBUTE_UUID)).thenReturn(cohortAttribute);
		
		CohortAttribute result = cohortService.getCohortAttributeByUuid(COHORT_ATTRIBUTE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getCohortAttributeId(), equalTo(12));
		assertThat(result.getUuid(), equalTo(COHORT_ATTRIBUTE_UUID));
	}
	
	@Test
	public void shouldGetCohortsByLocationUuid() {
		String locationUuid = "4834jk3-n34nm30-34nm34-348nl";
		CohortM cohort = mock(CohortM.class);
		Location location = mock(Location.class);
		
		when(cohort.getLocation()).thenReturn(location);
		when(cohortDao.findBy(
		    PropValue.builder().associationPath(Optional.of("location")).property("uuid").value(locationUuid).build()))
		            .thenReturn(Collections.singletonList(cohort));
		
		Collection<CohortM> cohortsByLocationUuid = cohortService.findCohortMByLocationUuid(locationUuid);
		assertThat(cohortsByLocationUuid, notNullValue());
		assertThat(cohortsByLocationUuid, hasSize(1));
		
		cohortsByLocationUuid.forEach(cohortM -> {
			assertThat(cohortM.getLocation(), equalTo(location));
		});
	}
}

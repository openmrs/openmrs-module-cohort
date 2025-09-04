/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.api.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortTypeService;
import org.openmrs.module.cohort.api.dao.search.PropValue;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class CohortTypeGenericDaoTest extends BaseModuleContextSensitiveTest {
	
	private static final String COHORT_TYPE_INITIAL_TEST_DATA_XML = "org/openmrs/module/cohort/api/hibernate/db/CohortTypeDaoTest_initialTestData.xml";
	
	private static final String COHORT_TYPE_UUID = "94517bf9-d8d7-4726-b4f1-a2dff6b36e2d";
	
	private static final int COHORT_TYPE_ID = 101;
	
	private static final String COHORT_TYPE_NAME = "cohort type name";
	
	@Autowired
	@Qualifier("cohortTypeDao")
	private GenericDao<CohortType> dao;
	
	@Before
	public void setup() throws Exception {
		executeDataSet(COHORT_TYPE_INITIAL_TEST_DATA_XML);
	}
	
	@Test
	public void shouldFindCohortTypeByID() {
		CohortType cohortType = dao
		        .findByUniqueProp(PropValue.builder().property("cohortTypeId").value(COHORT_TYPE_ID).build());
		assertThat(cohortType, notNullValue());
		assertThat(cohortType.getCohortTypeId(), equalTo(COHORT_TYPE_ID));
		
	}
	
	@Test
	public void shouldFindCohortTypeByUuid() {
		CohortType cohortType = dao.get(COHORT_TYPE_UUID);
		assertThat(cohortType, notNullValue());
		assertThat(cohortType.getUuid(), equalTo(COHORT_TYPE_UUID));
		
	}
	
	@Test
	public void shouldFindCohortTypeByName() {
		CohortType cohortType = dao.findByUniqueProp(PropValue.builder().property("name").value(COHORT_TYPE_NAME).build());
		assertThat(cohortType, notNullValue());
		assertThat(cohortType.getUuid(), equalTo(COHORT_TYPE_UUID));
		assertThat(cohortType.getName(), equalTo(COHORT_TYPE_NAME));
		
	}
	
	@Test
	public void shouldVoidCohortType() {
		CohortType cohortType = dao.get(COHORT_TYPE_UUID);
		assertThat(false, equalTo(cohortType.getVoided()));
		Context.getService(CohortTypeService.class).voidCohortType(cohortType, "delete cohort type");
		assertThat(true, equalTo(cohortType.getVoided()));
		CohortType voidedCohortType = dao.get(COHORT_TYPE_UUID);
		assertThat(voidedCohortType, nullValue());
	}
	
	@Test
	public void shouldPurgeCohortType() {
		CohortType cohortType = dao.get(COHORT_TYPE_UUID);
		assertNotNull(cohortType);
		dao.delete(cohortType);
		CohortType afterAttemptPurge = dao.get(COHORT_TYPE_UUID);
		assertNull(afterAttemptPurge);
	}
	
	@Test(expected = PersistenceException.class)
	public void shouldThrowExceptionForPurgeCohortType() {
		CohortType cohortType = dao.get("94517bf9-d9d6-4726-b4f1-a2dff6b36e2d");
		assertNotNull(cohortType);
		dao.delete(cohortType);
		Context.flushSession();
	}
	
}

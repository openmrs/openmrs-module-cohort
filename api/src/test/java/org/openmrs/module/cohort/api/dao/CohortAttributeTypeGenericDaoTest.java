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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.api.dao.search.PropValue;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class CohortAttributeTypeGenericDaoTest extends BaseModuleContextSensitiveTest {
	
	private static final String COHORT_ATTRIBUTE_TYPE_INITIAL_TEST_DATA_XML = "org/openmrs/module/cohort/api/hibernate/db/CohortAttributeTypeDaoTest_initialTestData.xml";
	
	private static final String COHORT_ATTRIBUTE_TYPE_UUID = "7fb7fe43-2813-4ebc-78dc-2e5d30251hj6";
	
	private static final String VOIDED_COHORT_ATTRIBUTE_TYPE_UUID = "9eb7fe43-2813-4ebc-80dc-2e5d30251bb7";
	
	private static final int COHORT_ATTRIBUTE_TYPE_ID = 2;
	
	@Autowired
	@Qualifier("cohortAttributeTypeDao")
	private GenericDao<CohortAttributeType> dao;
	
	@BeforeEach
	public void setup() throws Exception {
		executeDataSet(COHORT_ATTRIBUTE_TYPE_INITIAL_TEST_DATA_XML);
	}
	
	@Test
	public void shouldGetCohortAttributeTypeById() {
		CohortAttributeType cohortAttributeType = dao.findByUniqueProp(
		    PropValue.builder().property("cohortAttributeTypeId").value(COHORT_ATTRIBUTE_TYPE_ID).build());
		assertThat(cohortAttributeType, notNullValue());
		assertThat(cohortAttributeType.getCohortAttributeTypeId(), notNullValue());
		assertThat(cohortAttributeType.getCohortAttributeTypeId(), equalTo(COHORT_ATTRIBUTE_TYPE_ID));
	}
	
	@Test
	public void getByUuid_shouldReturnCohortAttributeType() {
		CohortAttributeType cohortAttributeType = dao.get(COHORT_ATTRIBUTE_TYPE_UUID);
		assertThat(cohortAttributeType, notNullValue());
		assertThat(cohortAttributeType.getCohortAttributeTypeId(), notNullValue());
		assertThat(cohortAttributeType.getCohortAttributeTypeId(), equalTo(COHORT_ATTRIBUTE_TYPE_ID));
		assertThat(cohortAttributeType.getUuid(), equalTo(COHORT_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void getByUuid_shouldReturnNullForVoidedCohortAttributeType() {
		CohortAttributeType cohortAttributeType = dao.get(VOIDED_COHORT_ATTRIBUTE_TYPE_UUID);
		assertThat(cohortAttributeType, nullValue());
	}
	
	@Test
	public void shouldCreateNewCohortAttributeType() {
		CohortAttributeType cohortAttributeTypeToCreate = new CohortAttributeType();
		cohortAttributeTypeToCreate.setUuid("fg89jk-34jkl5ks-4583-34jks90");
		cohortAttributeTypeToCreate.setName("test cohort attribute type");
		
		CohortAttributeType newlyCreatedAttributeType = dao.createOrUpdate(cohortAttributeTypeToCreate);
		assertThat(newlyCreatedAttributeType, notNullValue());
		assertThat(newlyCreatedAttributeType.getCohortAttributeTypeId(), notNullValue());
		assertThat(newlyCreatedAttributeType.getName(), is(cohortAttributeTypeToCreate.getName()));
	}
	
	@Test
	public void shouldUpdateCohortAttributeType() {
		CohortAttributeType cohortAttributeTypeToUpdate = dao.get(COHORT_ATTRIBUTE_TYPE_UUID);
		assertThat(cohortAttributeTypeToUpdate, notNullValue());
		assertThat(cohortAttributeTypeToUpdate.getDescription(), equalTo("Test cohort attribute type description"));
		cohortAttributeTypeToUpdate.setDescription("Updated cohort attribute type");
		
		dao.createOrUpdate(cohortAttributeTypeToUpdate);
		
		CohortAttributeType cohortAttributeType = dao.get(cohortAttributeTypeToUpdate.getUuid());
		assertThat(cohortAttributeType, notNullValue());
		assertThat(cohortAttributeType.getUuid(), equalTo(cohortAttributeTypeToUpdate.getUuid()));
		assertThat(cohortAttributeType.getCohortAttributeTypeId(), equalTo(COHORT_ATTRIBUTE_TYPE_ID));
		assertThat(cohortAttributeType.getDescription(), equalTo("Updated cohort attribute type"));
	}
}

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
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.cohort.CohortMemberAttributeType;
import org.openmrs.module.cohort.api.TestDataUtils;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class CohortMemberAttributeTypeGenericDaoTest extends BaseModuleContextSensitiveTest {
	
	String COHORT_MEMBER_ATTRIBUTE_TYPE_INITIAL_TEST_DATA_XML = "org/openmrs/module/cohort/api/hibernate/db/CohortMemberAttributeTypeDaoTest_initialTestData.xml";
	
	private static final String COHORT_MEMBER_ATTRIBUTE_TYPE_NAME = "cohort member attributeType Name";
	
	private final String COHORT_MEMBER_ATTRIBUTE_TYPE_UUID = "9eb7fe43-2813-4ebc-80dc-2e5d30251bb7";
	
	@Autowired
	@Qualifier("cohortMemberAttributeTypeDao")
	private GenericDao<CohortMemberAttributeType> dao;
	
	@BeforeEach
	public void setup() throws Exception {
		executeDataSet(COHORT_MEMBER_ATTRIBUTE_TYPE_INITIAL_TEST_DATA_XML);
	}
	
	@Test
	public void shouldGetCohortMemberAttributeTypeByUuid() {
		CohortMemberAttributeType attributeType = dao.get(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID);
		
		assertThat(attributeType, notNullValue());
		assertThat(attributeType.getUuid(), equalTo(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID));
	}
	
	@Test
	public void shouldGetAllCohortMemberAttributeTypes() {
		Collection<CohortMemberAttributeType> attributeTypes = dao.findAll();
		
		assertThat(attributeTypes, notNullValue());
		assertThat(attributeTypes, hasSize(1));
		assertThat(attributeTypes, everyItem(hasProperty("name", notNullValue())));
	}
	
	@Test
	public void shouldCreateNewCohortMemberAttributeType() {
		CohortMemberAttributeType cohortMemberAttributeTypeToCreate = TestDataUtils.COHORT_MEMBER_ATTRIBUTE_TYPE();
		cohortMemberAttributeTypeToCreate.setId(null);
		CohortMemberAttributeType cohortMemberAttributeType = dao.createOrUpdate(cohortMemberAttributeTypeToCreate);
		assertThat(cohortMemberAttributeType, notNullValue());
		assertThat(cohortMemberAttributeType.getId(), notNullValue());
		assertThat(cohortMemberAttributeType.getName(), equalTo(COHORT_MEMBER_ATTRIBUTE_TYPE_NAME));
	}
	
	@Test
	public void shouldVoidCohortMemberAttributeType() {
		CohortMemberAttributeType attributeTypeToRetire = dao.get(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID);
		attributeTypeToRetire.setRetireReason("Voided via cohort rest call");
		attributeTypeToRetire.setRetired(true);
		dao.createOrUpdate(attributeTypeToRetire);
		
		CohortMemberAttributeType attributeType = dao.get(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID, true);
		
		assertThat(attributeType, notNullValue());
		assertThat(attributeType.getRetired(), is(true));
		assertThat(attributeType.getRetireReason(), is("Voided via cohort rest call"));
	}
	
	@Test
	public void shouldPurgeCohortMemberAttributeType() {
		dao.delete(dao.get(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID));
		
		assertThat(dao.get(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID), nullValue());
	}
}

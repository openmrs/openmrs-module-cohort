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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.api.TestDataUtils;
import org.openmrs.module.cohort.api.dao.search.PropValue;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class CohortMemberAttributeGenericDaoTest extends BaseModuleContextSensitiveTest {
	
	//The order is salient
	private static final String[] COHORT_MEMBER_ATTRIBUTE_INITIAL_TEST_DATA_XML = new String[] {
	        "org/openmrs/module/cohort/api/hibernate/db/CohortDaoTest_initialTestData.xml",
	        "org/openmrs/module/cohort/api/hibernate/db/CohortMemberDaoTest_initialTestData.xml",
	        "org/openmrs/module/cohort/api/hibernate/db/CohortMemberAttributeTypeDaoTest_initialTestData.xml",
	        "org/openmrs/module/cohort/api/hibernate/db/CohortMemberAttributeDaoTest_initialTestData.xml" };
	
	private static final String COHORT_MEMBER_ATTRIBUTE_UUID = "ddadadd8-8034-4a28-9441-2eb2e7679e10";
	
	private static final String COHORT_MEMBER_ATTRIBUTE_TYPE_UUID = "9eb7fe43-2813-4ebc-80dc-2e5d30251bb7";
	
	private static final int COHORT_MEMBER_ATTRIBUTE_ID = 1;
	
	private static final String COHORT_MEMBER_ATTRIBUTE_VALUE = "cohortMemberAttribute";
	
	@Autowired
	@Qualifier("cohortMemberAttributeDao")
	private GenericDao<CohortMemberAttribute> dao;
	
	@BeforeEach
	public void setup() throws Exception {
		for (String dataset : COHORT_MEMBER_ATTRIBUTE_INITIAL_TEST_DATA_XML) {
			executeDataSet(dataset);
		}
	}
	
	@Test
	public void shouldGetCohortMemberAttributeByUuid() {
		CohortMemberAttribute cohortMemberAttribute = dao.get(COHORT_MEMBER_ATTRIBUTE_UUID);
		assertThat(cohortMemberAttribute, notNullValue());
		assertThat(cohortMemberAttribute.getId(), notNullValue());
		assertThat(cohortMemberAttribute.getId(), is(COHORT_MEMBER_ATTRIBUTE_ID));
		assertThat(cohortMemberAttribute.getUuid(), equalTo(COHORT_MEMBER_ATTRIBUTE_UUID));
	}
	
	@Test
	public void shouldGetCohortMemberAttributesByTypeUuid() {
		Collection<CohortMemberAttribute> memberAttributes = dao.findBy(PropValue.builder().property("uuid")
		        .associationPath(Optional.of("attributeType")).value(COHORT_MEMBER_ATTRIBUTE_TYPE_UUID).build());
		
		assertThat(memberAttributes, notNullValue());
		assertThat(memberAttributes, hasSize(1));
	}
	
	@Test
	public void shouldCreateNewCohortMemberAttribute() {
		CohortMemberAttribute existingAttribute = dao.get(COHORT_MEMBER_ATTRIBUTE_UUID);
		CohortMemberAttribute cohortAttributeToCreate = TestDataUtils.COHORT_MEMBER_ATTRIBUTE();
		cohortAttributeToCreate.setId(null);
		cohortAttributeToCreate.setCohortMember(existingAttribute.getCohortMember());
		cohortAttributeToCreate.setAttributeType(existingAttribute.getAttributeType());
		cohortAttributeToCreate.setValueReferenceInternal(COHORT_MEMBER_ATTRIBUTE_VALUE);
		CohortMemberAttribute cohortAttribute = dao.createOrUpdate(cohortAttributeToCreate);
		
		assertThat(cohortAttribute, notNullValue());
		assertThat(cohortAttribute.getId(), notNullValue());
		assertThat(cohortAttribute.getValue(), equalTo(COHORT_MEMBER_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void shouldVoidCohortMemberAttribute() {
		CohortMemberAttribute attributeToVoid = dao.get(COHORT_MEMBER_ATTRIBUTE_UUID);
		attributeToVoid.setVoided(true);
		attributeToVoid.setVoidReason("Voided via cohort rest call");
		dao.createOrUpdate(attributeToVoid);
		
		CohortMemberAttribute attribute = dao.get(COHORT_MEMBER_ATTRIBUTE_UUID, true);
		
		assertThat(attribute, notNullValue());
		assertThat(attribute.getVoided(), is(true));
		assertThat(attribute.getVoidReason(), is("Voided via cohort rest call"));
	}
	
	@Test
	public void shouldPurgeCohortMemberAttribute() {
		dao.delete(dao.get(COHORT_MEMBER_ATTRIBUTE_UUID));
		
		assertThat(dao.get(COHORT_MEMBER_ATTRIBUTE_UUID), nullValue());
	}
}

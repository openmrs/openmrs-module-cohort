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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collection;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.api.SpringTestConfiguration;
import org.openmrs.module.cohort.api.TestDataUtils;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = SpringTestConfiguration.class, inheritLocations = false)
public class CohortAttributeGenericDaoTest extends BaseModuleContextSensitiveTest {
	
	private static final String COHORT_ATTRIBUTE_INITIAL_TEST_DATA_XML = "org/openmrs/module/cohort/api/hibernate/db/CohortAttributeDaoTest_initialTestData.xml";
	
	private static final String VOIDED_COHORT_ATTRIBUTE_UUID = "ddadadd8-8034-4a28-9441-2eb2e7679e10";
	
	private static final String COHORT_ATTRIBUTE_UUID = "99ada908-8034-4a28-9441-2eb2e8979e32";
	
	private static final int COHORT_ATTRIBUTE_ID = 2;
	
	private static final String VOIDED_COHORT_ATTRIBUTE = "Test cohort attribute";
	
	private static final String UN_VOIDED_COHORT_ATTRIBUTE = "System generated patient";
	
	private static final int TEST_COHORT_ATTRIBUTE_ID = 200;
	
	private static final String COHORT_ATTRIBUTE_VALUE = "cohortAttribute";
	
	@Autowired
	@Qualifier("cohort.genericDao")
	private IGenericDao<CohortAttribute> dao;
	
	@Before
	public void setup() throws Exception {
		dao.setClazz(CohortAttribute.class);
		executeDataSet(COHORT_ATTRIBUTE_INITIAL_TEST_DATA_XML);
	}
	
	@Test
	public void getByUuid_shouldReturnCohortAttribute() {
		CohortAttribute cohortAttribute = dao.get(COHORT_ATTRIBUTE_UUID);
		assertThat(cohortAttribute, notNullValue());
		assertThat(cohortAttribute.getCohortAttributeId(), equalTo(COHORT_ATTRIBUTE_ID));
		assertThat(cohortAttribute.getUuid(), equalTo(COHORT_ATTRIBUTE_UUID));
	}
	
	@Test
	public void getByUuid_shouldReturnNullForVoidedCohortAttribute() {
		CohortAttribute cohortAttribute = dao.get(VOIDED_COHORT_ATTRIBUTE_UUID);
		assertThat(cohortAttribute, nullValue());
	}
	
	@Test
	public void shouldCreateNewCohortAttribute() {
		CohortAttribute cohortAttribute = dao.createOrUpdate(TestDataUtils.COHORT_ATTRIBUTE());
		assertThat(cohortAttribute, notNullValue());
		assertThat(cohortAttribute.getCohortAttributeId(), notNullValue());
		assertThat(cohortAttribute.getCohortAttributeId(), equalTo(TEST_COHORT_ATTRIBUTE_ID));
		assertThat(cohortAttribute.getValue(), equalTo(COHORT_ATTRIBUTE_VALUE));
	}
	
	@Test
	public void shouldFindMatchingVoidedCohortAttributes() {
		Collection<CohortAttribute> results = dao.findBy(
		    PropValue.builder().property("value").value(VOIDED_COHORT_ATTRIBUTE).associationPath(Optional.empty()).build(),
		    true);
		assertThat(results, notNullValue());
		assertThat(results, not(Matchers.empty()));
		assertThat(results, Matchers.hasSize(equalTo(1)));
	}
	
	@Test
	public void shouldFindMatchingUnVoidedCohortAttributes() {
		Collection<CohortAttribute> results = dao.findBy(PropValue.builder().property("value")
		        .value(UN_VOIDED_COHORT_ATTRIBUTE).associationPath(Optional.empty()).build());
		assertThat(results, notNullValue());
		assertThat(results, not(Matchers.empty()));
		assertThat(results, Matchers.hasSize(equalTo(1)));
	}
}

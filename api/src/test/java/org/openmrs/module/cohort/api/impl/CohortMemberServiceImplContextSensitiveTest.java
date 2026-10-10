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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.CohortMemberAttributeType;
import org.openmrs.module.cohort.api.CohortMemberService;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class CohortMemberServiceImplContextSensitiveTest extends BaseModuleContextSensitiveTest {
	
	//Order of the array is salient
	private static final String[] COHORT_MEMBER_SEARCH_TEST_DATA_XML = {
	        "org/openmrs/module/cohort/api/hibernate/db/CohortDaoTest_initialTestData.xml",
	        "org/openmrs/module/cohort/api/hibernate/db/CohortMemberDaoTest_initialTestData.xml",
	        "org/openmrs/module/cohort/api/hibernate/db/CohortMemberSearchTest_initialTestData.xml" };
	
	private static final String COVID_COHORT_UUID = "7f9a2479-c14a-4bfc-bcaa-632860258519";
	
	private static final String PATIENT_UUID = "61b38324-e2fd-4feb-95b7-9e9a2a4400df";
	
	private static final String PATIENT_COVID_MEMBER_UUID = "23517bf9-d8d7-4726-b4f1-a2dff6b36w32";
	
	private static final String PATIENT_REJOINED_TB_MEMBER_UUID = "294b9246-78db-47e4-af73-fd7020d87497";
	
	private static final String OTHER_PATIENT_COVID_MEMBER_UUID = "757c3449-75c7-4020-ac4e-202de9f0df22";
	
	@BeforeEach
	public void setup() throws Exception {
		for (String dataset : COHORT_MEMBER_SEARCH_TEST_DATA_XML) {
			executeDataSet(dataset);
		}
	}
	
	@Test
	public void findCohortMembersByCohortUuid_shouldReturnTheMembersOfTheCohortWithoutAnEndDate() {
		Collection<CohortMember> members = Context.getService(CohortMemberService.class)
		        .findCohortMembersByCohortUuid(COVID_COHORT_UUID);
		
		assertThat(uuidsOf(members), containsInAnyOrder(PATIENT_COVID_MEMBER_UUID, OTHER_PATIENT_COVID_MEMBER_UUID));
	}
	
	@Test
	public void findCohortMembersByPatientUuid_shouldReturnTheMembershipsOfThePatientWithoutAnEndDate() {
		Collection<CohortMember> members = Context.getService(CohortMemberService.class)
		        .findCohortMembersByPatientUuid(PATIENT_UUID);
		
		assertThat(uuidsOf(members), containsInAnyOrder(PATIENT_COVID_MEMBER_UUID, PATIENT_REJOINED_TB_MEMBER_UUID));
	}
	
	@Test
	public void saveCohortMemberAttribute_shouldSaveTheValueOfANewAttribute() {
		CohortMemberService cohortMemberService = Context.getService(CohortMemberService.class);
		CohortMemberAttributeType role = new CohortMemberAttributeType();
		role.setName("Role");
		role.setDatatypeClassname(FreeTextDatatype.class.getName());
		cohortMemberService.saveCohortMemberAttributeType(role);
		CohortMemberAttribute attribute = new CohortMemberAttribute();
		attribute.setCohortMember(cohortMemberService.getCohortMemberByUuid(PATIENT_COVID_MEMBER_UUID));
		attribute.setAttributeType(role);
		attribute.setValue("treasurer");
		
		cohortMemberService.saveCohortMemberAttribute(attribute);
		Context.flushSession();
		Context.clearSession();
		
		assertThat(cohortMemberService.getCohortMemberAttributeByUuid(attribute.getUuid()).getValueReference(),
		    equalTo("treasurer"));
	}
	
	private static List<String> uuidsOf(Collection<CohortMember> members) {
		return members.stream().map(CohortMember::getUuid).collect(Collectors.toList());
	}
}

package org.openmrs.module.cohort.api.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class CohortMemberGenericDaoTest extends BaseModuleContextSensitiveTest {
	
	//Order of the array is salient
	private static final String[] COHORT_MEMBER_INITIAL_TEST_DATA_XML = {
	        "org/openmrs/module/cohort/api/hibernate/db/CohortDaoTest_initialTestData.xml",
	        "org/openmrs/module/cohort/api/hibernate/db/CohortMemberDaoTest_initialTestData.xml" };
	
	private static final String COHORT_MEMBER_UUID = "23517bf9-d8d7-4726-b4f1-a2dff6b36w32";
	
	private static final String COHORT_UUID = "7f9a2479-c14a-4bfc-bcaa-632860258519";
	
	private static final String BAD_COHORT_UUID = "cxx90e-c14a-4bfc-xx56xx-ui8s860258xxx";
	
	private static final String NAME = "F";
	
	private static final String FAMILY_NAME = "Doe";
	
	private static final String GIVEN_NAME = "John";
	
	@Autowired
	@Qualifier("cohortMemberDao")
	private GenericDao<CohortMember> dao;
	
	@BeforeEach
	public void setup() throws Exception {
		for (String data : COHORT_MEMBER_INITIAL_TEST_DATA_XML) {
			executeDataSet(data);
		}
	}
	
	@Test
	public void shouldGetCohortMemberByUuid() {
		CohortMember cohortMember = dao.get(COHORT_MEMBER_UUID);
		
		assertThat(cohortMember, notNullValue());
		assertThat(cohortMember.getUuid(), is(COHORT_MEMBER_UUID));
	}
	
	@Test
	public void shouldReturnCollectionOfCohortMembersWhenFindByPatientGivenName() {
		Collection<CohortMember> cohortMembers = dao.getSearchHandler().findCohortMembersByPatientNames(GIVEN_NAME);
		
		assertThat(cohortMembers, notNullValue());
		// the single cohort member's patient has two names starting with "John"; Hibernate 6+ de-duplicates root entities
		assertThat(cohortMembers, hasSize(1));
		
		for (CohortMember member : cohortMembers) {
			assertThat(member.getPatient().getGivenName(), is(GIVEN_NAME));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfCohortMembersWhenFindByPatientFamilyName() {
		Collection<CohortMember> cohortMembers = dao.getSearchHandler().findCohortMembersByPatientNames(FAMILY_NAME);
		
		assertThat(cohortMembers, notNullValue());
		assertThat(cohortMembers, hasSize(1));
		
		for (CohortMember member : cohortMembers) {
			assertThat(member.getPatient().getFamilyName(), is(FAMILY_NAME));
		}
	}
	
	@Test
	public void shouldReturnCollectionOfCohortMembersWhenFindByPatientFamilyNameAndCohortUuid() {
		Collection<CohortMember> cohortMembers = dao.getSearchHandler().findCohortMembersByCohortAndPatient(COHORT_UUID,
		    NAME);
		
		assertThat(cohortMembers, notNullValue());
		assertThat(cohortMembers, hasSize(1));
		
		for (CohortMember member : cohortMembers) {
			assertThat(member.getPatient().getFamilyName(), is(FAMILY_NAME));
		}
	}
	
	@Test
	public void shouldReturnEmptyCollectionOfCohortMembersWhenFindByPatientFamilyNameAndBadCohortUuid() {
		Collection<CohortMember> cohortMembers = dao.getSearchHandler().findCohortMembersByCohortAndPatient(BAD_COHORT_UUID,
		    NAME);
		
		assertThat(cohortMembers, notNullValue());
		assertThat(cohortMembers, hasSize(0));
		
		for (CohortMember member : cohortMembers) {
			assertThat(member.getPatient().getFamilyName(), is(FAMILY_NAME));
		}
	}
}

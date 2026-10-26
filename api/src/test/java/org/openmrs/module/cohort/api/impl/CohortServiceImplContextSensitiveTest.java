package org.openmrs.module.cohort.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortMemberService;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.cohort.api.CohortTypeService;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class CohortServiceImplContextSensitiveTest extends BaseModuleContextSensitiveTest {
	
	@Test
	public void shouldBeRegisteredAsService() {
		assertThat(Context.getService(CohortService.class), notNullValue());
	}
	
	@Test
	public void saveCohortM_shouldSaveCohort() {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setDescription("Test Cohort Description");
		
		CohortM result = Context.getService(CohortService.class).saveCohortM(cohortM);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
	}
	
	@Test
	public void saveCohortM_shouldUpdateCohort() {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setDescription("Test Cohort Description");
		CohortM savedCohort = Context.getService(CohortService.class).saveCohortM(cohortM);
		savedCohort.setName("Updated Test Cohort");
		
		CohortM result = Context.getService(CohortService.class).saveCohortM(savedCohort);
		
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo("Updated Test Cohort"));
	}
	
	@Test
	public void saveCohortM_shouldSaveCohortMembers() {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		
		Patient patient = Context.getPatientService().getPatient(7);
		CohortMember cm = new CohortMember(patient);
		cm.setStartDate(new Date(System.currentTimeMillis() - 1000));
		cohortM.addMemberships(cm);
		cohortM.setDescription("Test Cohort Description");
		CohortM result = Context.getService(CohortService.class).saveCohortM(cohortM);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		assertThat(result.getCohortMembers().iterator().next(), notNullValue());
		assertThat(result.getCohortMembers().iterator().next().getPatient(), equalTo(patient));
	}
	
	@Test
	public void saveCohortM_shouldSaveCohortMembersForExistingCohort() {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setDescription("Test Cohort Description");
		CohortM savedCohort = Context.getService(CohortService.class).saveCohortM(cohortM);
		
		Patient patient = Context.getPatientService().getPatient(7);
		CohortMember cm = new CohortMember(patient);
		cm.setStartDate(new Date(System.currentTimeMillis() - 1000));
		savedCohort.addMemberships(cm);
		
		Context.getService(CohortService.class).saveCohortM(savedCohort);
		Context.getRegisteredComponent("sessionFactory", SessionFactory.class).getCurrentSession().flush();
		
		CohortM result = Context.getService(CohortService.class).getCohortM(savedCohort.getCohortId());
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		assertThat(result.getCohortMembers().iterator().next(), notNullValue());
		assertThat(result.getCohortMembers().iterator().next().getPatient(), equalTo(patient));
	}
	
	@Test
	public void saveCohortM_shouldRemoveMembersForExistingCohort() {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setDescription("Test Cohort Description");
		
		Patient patient = Context.getPatientService().getPatient(7);
		CohortMember cm = new CohortMember(patient);
		cm.setStartDate(new Date(System.currentTimeMillis() - 1000));
		
		cohortM.addMemberships(cm);
		
		CohortM existingCohort = Context.getService(CohortService.class).saveCohortM(cohortM);
		
		existingCohort.removeMemberships(existingCohort.getActiveCohortMembers().iterator().next());
		
		CohortM result = Context.getService(CohortService.class).saveCohortM(existingCohort);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(0));
	}
	
	@Test
	public void purgeCohortM_shouldNotPurgeMemberPatients() {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setDescription("Test Cohort Description");
		
		CohortMember cm = new CohortMember(Context.getPatientService().getPatient(7));
		cm.setStartDate(new Date(System.currentTimeMillis() - 1000));
		cohortM.addMemberships(cm);
		
		CohortM savedCohort = Context.getService(CohortService.class).saveCohortM(cohortM);
		Context.flushSession();
		
		Context.getService(CohortService.class).purgeCohortM(savedCohort);
		Context.flushSession();
		Context.clearSession();
		
		assertThat(Context.getService(CohortService.class).getCohortM(savedCohort.getCohortId()), nullValue());
		assertThat(Context.getService(CohortMemberService.class).getCohortMemberByUuid(cm.getUuid()), nullValue());
		assertThat(Context.getPatientService().getPatient(7), notNullValue());
	}
	
	@Test
	public void purgeCohortM_shouldNotPurgeCohortTypeOrLocation() {
		CohortType cohortType = new CohortType();
		cohortType.setName("Test Cohort Type");
		cohortType.setDescription("Test Cohort Type Description");
		Context.getService(CohortTypeService.class).saveCohortType(cohortType);
		
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setDescription("Test Cohort Description");
		cohortM.setCohortType(cohortType);
		cohortM.setLocation(Context.getLocationService().getLocation(1));
		
		CohortM savedCohort = Context.getService(CohortService.class).saveCohortM(cohortM);
		Context.flushSession();
		
		Context.getService(CohortService.class).purgeCohortM(savedCohort);
		Context.flushSession();
		Context.clearSession();
		
		assertThat(Context.getService(CohortService.class).getCohortM(savedCohort.getCohortId()), nullValue());
		assertThat(Context.getService(CohortTypeService.class).getCohortTypeByUuid(cohortType.getUuid()), notNullValue());
		assertThat(Context.getLocationService().getLocation(1), notNullValue());
	}
	
}

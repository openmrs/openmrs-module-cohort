package org.openmrs.module.cohort.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortService;
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
		cohortM.setCohortType(new CohortType());
		cohortM.setDescription("Test Cohort Description");
		
		CohortM result = Context.getService(CohortService.class).saveCohortM(cohortM);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), notNullValue());
	}
	
	@Test
	public void saveCohortM_shouldUpdateCohort() {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setCohortType(new CohortType());
		cohortM.setDescription("Test Cohort Description");
		
		CohortM savedCohort = Context.getService(CohortService.class).saveCohortM(cohortM);
		savedCohort.setName("Updated Test Cohort");
		
		CohortM result = Context.getService(CohortService.class).saveCohortM(savedCohort);
		
		assertThat(result, notNullValue());
		assertThat(result.getName(), equalTo("Updated Test Cohort"));
	}
	
	@Test
	public void saveCohortM_shouldSaveCohortMembers() throws ParseException {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setCohortType(new CohortType());
		cohortM.setDescription("Test Cohort Description");
		
		Patient patient = Context.getPatientService().getPatient(7);
		CohortMember cm = new CohortMember(patient);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		Date date = sdf.parse("2021-06-07 00:00:00.0");
		cm.setStartDate(date);
		cohortM.addMemberships(cm);
		
		CohortM result = Context.getService(CohortService.class).saveCohortM(cohortM);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		assertThat(result.getCohortMembers().iterator().next(), notNullValue());
		assertThat(result.getCohortMembers().iterator().next().getPatient(), equalTo(patient));
	}
	
	@Test
	public void saveCohortM_shouldSaveCohortMembersForExistingCohort() throws ParseException {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setCohortType(new CohortType());
		cohortM.setDescription("Test Cohort Description");
		
		CohortM savedCohort = Context.getService(CohortService.class).saveCohortM(cohortM);
		
		Patient patient = Context.getPatientService().getPatient(7);
		CohortMember cm = new CohortMember(patient);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		Date date = sdf.parse("2021-06-07 00:00:00.0");
		cm.setStartDate(date);
		savedCohort.addMemberships(cm);
		
		Context.getService(CohortService.class).saveCohortM(savedCohort);
		
		CohortM result = Context.getService(CohortService.class).getCohortM(savedCohort.getCohortId());
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(1));
		assertThat(result.getCohortMembers().iterator().next(), notNullValue());
		assertThat(result.getCohortMembers().iterator().next().getPatient(), equalTo(patient));
	}
	
	@Test
	public void saveCohortM_shouldRemoveMembersForExistingCohort() throws ParseException {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setCohortType(new CohortType());
		cohortM.setDescription("Test Cohort Description");
		
		Patient patient = Context.getPatientService().getPatient(7);
		CohortMember cm = new CohortMember(patient);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		Date date = sdf.parse("2021-06-07 00:00:00.0");
		cm.setStartDate(date);
		
		cohortM.addMemberships(cm);
		
		CohortM existingCohort = Context.getService(CohortService.class).saveCohortM(cohortM);
		
		existingCohort.removeMemberships(existingCohort.getActiveCohortMembers().iterator().next());
		
		CohortM result = Context.getService(CohortService.class).saveCohortM(existingCohort);
		
		assertThat(result, notNullValue());
		assertThat(result.size(), equalTo(0));
	}
	
}

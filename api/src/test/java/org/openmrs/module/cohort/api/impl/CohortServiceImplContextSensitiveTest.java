package org.openmrs.module.cohort.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class CohortServiceImplContextSensitiveTest extends BaseModuleContextSensitiveTest {
	
	private static final String COHORT_SEARCH_TEST_DATA_XML = "org/openmrs/module/cohort/api/hibernate/db/CohortSearchTest_initialTestData.xml";
	
	private static final String KISUMU_MOTHERS_COHORT_UUID = "fb0e766c-8560-49f8-9a12-952ed2941c35";
	
	private static final String NAIROBI_YOUTH_COHORT_UUID = "bc21b1d1-4e7c-423c-a43d-af18711e24cb";
	
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
	public void saveCohortM_shouldSaveCohortMembers() {
		CohortM cohortM = new CohortM();
		cohortM.setName("Test Cohort");
		cohortM.setCohortType(new CohortType());
		
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
		cohortM.setCohortType(new CohortType());
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
		cohortM.setCohortType(new CohortType());
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
	public void saveCohortAttribute_shouldSaveTheValueOfANewAttribute() {
		CohortService cohortService = Context.getService(CohortService.class);
		CohortAttributeType facility = new CohortAttributeType();
		facility.setName("Facility");
		facility.setDatatypeClassname(FreeTextDatatype.class.getName());
		cohortService.saveCohortAttributeType(facility);
		CohortM cohort = new CohortM();
		cohort.setName("Test Cohort");
		cohort.setDescription("Test Cohort Description");
		cohortService.saveCohortM(cohort);
		CohortAttribute attribute = new CohortAttribute();
		attribute.setCohort(cohort);
		attribute.setAttributeType(facility);
		attribute.setValue("Kisumu");
		
		cohortService.saveCohortAttribute(attribute);
		Context.flushSession();
		Context.clearSession();
		
		assertThat(cohortService.getCohortAttributeByUuid(attribute.getUuid()).getValueReference(), equalTo("Kisumu"));
	}
	
	@Test
	public void findMatchingCohortMs_shouldReturnCohortsWithAnActiveAttributeOfTheGivenTypeContainingTheValue()
	        throws Exception {
		executeDataSet(COHORT_SEARCH_TEST_DATA_XML);
		
		List<CohortM> cohorts = Context.getService(CohortService.class).findMatchingCohortMs(null,
		    Collections.singletonMap("Facility", "Kisumu"), null, false);
		
		// Kisumu mothers has two matching attributes and is returned once. Nairobi youth mentions Kisumu only in a
		// voided Facility attribute and in its Program, and Machakos elders has no attributes at all.
		assertThat(uuidsOf(cohorts), contains(KISUMU_MOTHERS_COHORT_UUID));
	}
	
	@Test
	public void findMatchingCohortMs_shouldReturnCohortsMatchingAnyOfTheGivenAttributes() throws Exception {
		executeDataSet(COHORT_SEARCH_TEST_DATA_XML);
		Map<String, String> attributes = new HashMap<>();
		attributes.put("Facility", "Kisumu");
		attributes.put("Program", "youth");
		
		List<CohortM> cohorts = Context.getService(CohortService.class).findMatchingCohortMs(null, attributes, null, false);
		
		assertThat(uuidsOf(cohorts), containsInAnyOrder(KISUMU_MOTHERS_COHORT_UUID, NAIROBI_YOUTH_COHORT_UUID));
	}
	
	@Test
	public void findMatchingCohortMs_shouldReturnNoCohortsForAnAttributeTypeThatDoesNotExist() throws Exception {
		executeDataSet(COHORT_SEARCH_TEST_DATA_XML);
		
		List<CohortM> cohorts = Context.getService(CohortService.class).findMatchingCohortMs(null,
		    Collections.singletonMap("District", "Kisumu"), null, false);
		
		assertThat(cohorts, empty());
	}
	
	private static List<String> uuidsOf(List<CohortM> cohorts) {
		return cohorts.stream().map(CohortM::getUuid).collect(Collectors.toList());
	}
}

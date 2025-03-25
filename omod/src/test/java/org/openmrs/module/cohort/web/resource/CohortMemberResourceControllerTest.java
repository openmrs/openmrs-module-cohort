package org.openmrs.module.cohort.web.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.api.CohortMemberService;
import org.openmrs.module.cohort.api.CohortService;
import org.springframework.context.annotation.Description;

public class CohortMemberResourceControllerTest extends BaseWebControllerTest {
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private CohortM cohort;
	
	private Patient patient;
	
	public String getURI() {
		return "/rest/v1/cohortm/cohortmember";
	}
	
	public String getUuid() {
		return null;
	}
	
	public long getAllCount() {
		return 0;
	}
	
	@Before
	public void setUp() {
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		cohort = createMinimalCohort();
		
		Location location = Context.getLocationService().saveLocation(createMinimalLocation());
		PatientIdentifierType patientIdentifierType = Context.getPatientService()
		        .savePatientIdentifierType(createMinimalPatientIdentifierType());
		patient = createMinimalPatient(location, patientIdentifierType);
		Context.getPatientService().savePatient(patient);
	}
	
	@Test
	@Description("Verifies if a new CohortMember can be successfully created and saved")
	public void shouldCreateNewCohortMember() throws Exception {
		Context.getService(CohortService.class).saveCohortM(cohort);
		
		String json = String.format(
		    "{ \"cohort\": \"%s\", \"patient\":\"%s\", \"startDate\":\"2023-08-22T01:00:00.000+0000\" }", cohort.getUuid(),
		    patient.getUuid());
		
		assertEquals(0, Context.getService(CohortMemberService.class).findAllCohortMembers().size());
		
		handle(newPostRequest(getURI(), json));
		
		Collection<CohortMember> cohortMembers = Context.getService(CohortMemberService.class).findAllCohortMembers();
		assertEquals(1, cohortMembers.size());
		
		CohortMember cohortMember = cohortMembers.iterator().next();
		assertEquals(cohort.getUuid(), cohortMember.getCohort().getUuid());
		assertEquals(patient.getUuid(), cohortMember.getPatient().getUuid());
		assertNotNull(cohortMember.getStartDate());
		assertNull(cohortMember.getEndDate());
	}
	
	@Test
	@Description("Verifies if an existing CohortMember can be successfully updated")
	public void shouldUpdateExistingCohortMember() throws Exception {
		CohortMember cohortMember = createMinimalCohortMember();
		cohort.addMemberships(cohortMember);
		
		Context.getService(CohortService.class).saveCohortM(cohort);
		Context.getService(CohortMemberService.class).saveCohortMember(cohortMember);
		
		String json = String.format(
		    "{ \"cohort\": \"%s\", \"patient\":\"%s\", \"startDate\":\"2023-08-22T01:00:00.000+0000\", \"endDate\":\"2023-09-22T01:00:00.000+0000\" }",
		    cohort.getUuid(), patient.getUuid());
		
		assertEquals(1, Context.getService(CohortMemberService.class).findAllCohortMembers().size());
		assertNull(cohortMember.getEndDate());
		
		handle(newPostRequest(getURI(), json));
		
		Collection<CohortMember> cohortMembers = Context.getService(CohortMemberService.class).findAllCohortMembers();
		assertEquals(1, cohortMembers.size());
		
		CohortMember updatedCohortMember = cohortMembers.iterator().next();
		assertNotNull(updatedCohortMember.getEndDate());
		assertEquals(cohortMember.getEndDate(), updatedCohortMember.getEndDate());
	}
	
	@Test
	@Description("Verifies if an existing CohortMember's endDate can be updated")
	public void shouldUpdateEndDateOnACohortMember() throws Exception {
		CohortMember cohortMember = createMinimalCohortMember();
		cohort.addMemberships(cohortMember);
		
		Context.getService(CohortService.class).saveCohortM(cohort);
		Context.getService(CohortMemberService.class).saveCohortMember(cohortMember);
		
		String expectedEndDate = "2023-08-22T01:00:00.000+0000";
		String json = String.format("{ \"endDate\": \"%s\" }", expectedEndDate);
		
		assertNull(cohortMember.getEndDate());
		
		handle(newPostRequest(getURI() + "/" + cohortMember.getUuid(), json));
		
		Collection<CohortMember> cohortMembers = Context.getService(CohortMemberService.class).findAllCohortMembers();
		
		CohortMember updatedCohortMember = cohortMembers.iterator().next();
		assertEquals(cohortMember.getUuid(), updatedCohortMember.getUuid());
		assertNotNull(updatedCohortMember.getEndDate());
		assertEquals(sdf.parse(expectedEndDate), updatedCohortMember.getEndDate());
	}
	
	private CohortM createMinimalCohort() {
		CohortM cohort = new CohortM();
		cohort.setName("sample cohort");
		cohort.setUuid("4eece76a-111e-40cb-be1c-e717801876f6");
		cohort.setDescription("sample cohort");
		return cohort;
	}
	
	private Patient createMinimalPatient(Location location, PatientIdentifierType patientIdentifierType) {
		Patient patient = new Patient();
		patient.setUuid(UUID.randomUUID().toString());
		
		PersonName pName = new PersonName();
		pName.setGivenName("John");
		pName.setMiddleName("A.");
		pName.setFamilyName("Doe");
		
		patient.addName(pName);
		patient.setGender("M");
		
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("123456");
		patientIdentifier.setLocation(location);
		patientIdentifier.setIdentifierType(patientIdentifierType);
		
		patient.addIdentifier(patientIdentifier);
		
		return patient;
	}
	
	private Location createMinimalLocation() {
		Location location = new Location();
		location.setName("Sample Location");
		location.setDescription("Sample Location Description");
		return location;
	}
	
	private PatientIdentifierType createMinimalPatientIdentifierType() {
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setName("Sample Identifier Type");
		identifierType.setDescription("Sample Identifier Type Description");
		return identifierType;
	}
	
	private CohortMember createMinimalCohortMember() {
		CohortMember cohortMember = new CohortMember();
		cohortMember.setCohort(cohort);
		cohortMember.setPatient(patient);
		cohortMember.setStartDate(Date.from(Instant.now()));
		return cohortMember;
	}
	
}

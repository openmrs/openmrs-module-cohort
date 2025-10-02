package org.openmrs.module.fhir2.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupMemberComponent;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator;
import org.openmrs.module.fhir2.api.translators.GroupTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.model.GroupMember;

@RunWith(MockitoJUnitRunner.class)
public class GroupTranslatorImplTest {
	
	private static final String COHORT_UUID = "787e12bd-314e-4cc4-9b4d-1cdff9be9545";
	
	private static final String GROUP_NAME = "Patient with VL > 2";
	
	private static final Integer PATIENT_ID = 7;
	
	@Mock
	private PatientService patientService;
	
	@Mock
	private GroupMemberTranslator groupMemberTranslator;
	
	@Mock
	private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;
	
	private GroupTranslatorImpl groupTranslator;
	
	@Before
	public void setup() {
		groupTranslator = new GroupTranslatorImpl();
		groupTranslator.setPatientService(patientService);
		groupTranslator.setGroupMemberTranslator(groupMemberTranslator);
		groupTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
	}
	
	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowWhenCohortIsNull() {
		groupTranslator.toFhirResource((CohortM) null);
	}
	
	@Test
	public void toFhirResource_shouldTranslateMembersAndMetadata() {
		Date dateCreated = new Date();
		Date dateChanged = new Date(dateCreated.getTime() + 1000);
		
		CohortM cohort = new CohortM();
		cohort.setUuid(COHORT_UUID);
		cohort.setName(GROUP_NAME);
		cohort.setDescription("HIV patients with latest VL > 2");
		cohort.setVoided(false);
		cohort.setDateCreated(dateCreated);
		cohort.setDateChanged(dateChanged);
		
		CohortMember cohortMemberWithPatient = new CohortMember();
		Patient patient = new Patient();
		patient.setId(PATIENT_ID);
		cohortMemberWithPatient.setPatient(patient);
		cohortMemberWithPatient.setStartDate(dateCreated);
		cohortMemberWithPatient.setEndDate(dateChanged);
		cohortMemberWithPatient.setVoided(true);
		
		CohortMember cohortMemberWithoutPatient = new CohortMember();
		
		Set<CohortMember> members = new HashSet<>(Arrays.asList(cohortMemberWithPatient, cohortMemberWithoutPatient));
		cohort.getCohortMembers().addAll(members);
		
		GroupMember groupMember = new GroupMember(new Reference("Patient/" + PATIENT_ID));
		when(groupMemberTranslator.toFhirResource(PATIENT_ID)).thenReturn(groupMember);
		
		GroupTranslator translator = groupTranslator;
		Group result = translator.toFhirResource(cohort);
		
		assertThat(result, notNullValue());
		assertThat(result.getId(), is(COHORT_UUID));
		assertThat(result.getName(), is(GROUP_NAME));
		assertThat(result.getType(), is(GroupType.PERSON));
		assertThat(result.getQuantity(), is(members.size()));
		assertThat(result.getMember(), hasSize(1));
		
		GroupMemberComponent translatedMember = result.getMember().get(0);
		assertThat(translatedMember.getEntity(), equalTo(groupMember.getEntity()));
		assertThat(translatedMember.getPeriod().getStart(), is(dateCreated));
		assertThat(translatedMember.getPeriod().getEnd(), is(dateChanged));
		assertThat(translatedMember.getInactive(), is(true));
		
		assertThat(result.getMeta().getLastUpdated(), is(dateChanged));
		assertThat(result.getMeta().getVersionId(), is(String.valueOf(dateChanged.getTime())));
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowWhenGroupIsNull() {
		groupTranslator.toOpenmrsType((Group) null);
	}
	
	@Test(expected = NullPointerException.class)
	public void toOpenmrsTypeWithExisting_shouldThrowWhenGroupIsNull() {
		groupTranslator.toOpenmrsType(new CohortM(), null);
	}
	
	@Test
	public void toOpenmrsType_shouldTranslateMembersWithPeriodAndInactive() {
		CohortM existing = new CohortM();
		existing.setUuid(COHORT_UUID);
		existing.setName("Initial name");
		existing.getCohortMembers().add(new CohortMember());
		
		Group group = new Group();
		group.setId(COHORT_UUID);
		group.setName(GROUP_NAME);
		group.setActive(true);
		
		GroupMemberComponent memberWithPatient = new GroupMemberComponent();
		memberWithPatient.setEntity(new Reference("Patient/" + PATIENT_ID));
		Date start = new Date();
		Date end = new Date(start.getTime() + 5000);
		memberWithPatient.getPeriod().setStart(start);
		memberWithPatient.getPeriod().setEnd(end);
		memberWithPatient.setInactive(true);
		
		GroupMemberComponent memberWithoutPatient = new GroupMemberComponent();
		memberWithoutPatient.setEntity(new Reference("Patient/" + (PATIENT_ID + 1)));
		
		List<GroupMemberComponent> memberComponents = Arrays.asList(memberWithPatient, memberWithoutPatient);
		group.setMember(memberComponents);
		
		Patient patient = new Patient();
		patient.setId(PATIENT_ID);
		
		when(groupMemberTranslator.toOpenmrsType(any(GroupMember.class))).thenReturn(PATIENT_ID, PATIENT_ID + 1);
		when(patientService.getPatient(PATIENT_ID)).thenReturn(patient);
		when(patientService.getPatient(PATIENT_ID + 1)).thenReturn(null);
		CohortM translated = groupTranslator.toOpenmrsType(existing, group);
		
		assertThat(translated, is(existing));
		assertThat(translated.getName(), is(GROUP_NAME));
		assertThat(translated.getCohortMembers(), hasSize(1));
		
		CohortMember savedMember = translated.getCohortMembers().iterator().next();
		assertThat(savedMember.getPatient(), is(patient));
		assertThat(savedMember.getCohort(), is(existing));
		assertThat(savedMember.getStartDate(), is(start));
		assertThat(savedMember.getEndDate(), is(end));
		assertThat(savedMember.getVoided(), is(true));
		
		ArgumentCaptor<GroupMember> memberCaptor = ArgumentCaptor.forClass(GroupMember.class);
		verify(groupMemberTranslator, times(2)).toOpenmrsType(memberCaptor.capture());
		
		List<GroupMember> capturedMembers = memberCaptor.getAllValues();
		assertThat(capturedMembers, hasSize(2));
		assertThat(capturedMembers.get(0).getEntity().getReference(), is("Patient/" + PATIENT_ID));
		assertThat(capturedMembers.get(1).getEntity().getReference(), is("Patient/" + (PATIENT_ID + 1)));
	}
	
	@Test
	public void toOpenmrsType_shouldClearExistingMembersWhenNoGroupMembersPresent() {
		CohortM existing = new CohortM();
		existing.getCohortMembers().addAll(new HashSet<>(Arrays.asList(new CohortMember(), new CohortMember())));
		
		Group group = new Group();
		group.setMember((List<GroupMemberComponent>) null);
		
		CohortM translated = groupTranslator.toOpenmrsType(existing, group);
		
		assertThat(translated.getCohortMembers(), hasSize(0));
	}
}

package org.openmrs.module.cohort.fhir2.translator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupMemberComponent;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.definition.CohortDefinitionHandler;
import org.openmrs.module.cohort.definition.ManualCohortDefinitionHandler;
import org.openmrs.module.cohort.fhir2.util.CohortFhirUtils;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;

@RunWith(MockitoJUnitRunner.class)
public class GroupTranslatorImplTest {
	
	@Mock
	private LocationReferenceTranslator locationReferenceTranslator;
	
	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;
	
	private GroupTranslatorImpl translator;
	
	@Before
	public void setup() {
		translator = new GroupTranslatorImpl();
		translator.setLocationReferenceTranslator(locationReferenceTranslator);
		translator.setPatientReferenceTranslator(patientReferenceTranslator);
	}
	
	@Test
	public void toFhirResource_shouldTranslateCohortFields() {
		CohortType type = new CohortType();
		type.setUuid("type-uuid");
		type.setName("Manual");
		
		Location location = new Location();
		location.setUuid("loc-uuid");
		
		CohortM cohort = new CohortM();
		cohort.setUuid("cohort-uuid");
		cohort.setName("HIV Patients");
		cohort.setVoided(false);
		cohort.setCohortType(type);
		cohort.setLocation(location);
		cohort.setDescription("Cohort description");
		cohort.setDateCreated(new Date());
		
		Patient patient = new Patient();
		patient.setId(7);
		CohortMember member = new CohortMember(patient);
		member.setStartDate(new Date());
		cohort.getCohortMembers().add(member);
		
		Reference patientReference = new Reference("Patient/7");
		when(patientReferenceTranslator.toFhirResource(patient)).thenReturn(patientReference);
		
		Reference locationReference = new Reference("Location/loc-uuid");
		when(locationReferenceTranslator.toFhirResource(location)).thenReturn(locationReference);
		
		try (MockedStatic<CohortFhirUtils> cohortFhirUtils = org.mockito.Mockito.mockStatic(CohortFhirUtils.class)) {
			cohortFhirUtils
			        .when(() -> CohortFhirUtils.getDataTranslation(org.mockito.ArgumentMatchers.any(CohortType.class)))
			        .thenReturn("Manual Translation");
			
			Group result = translator.toFhirResource(cohort);
			
			assertThat(result.getId(), is("Group/cohort-uuid"));
			assertThat(result.getType(), is(GroupType.PERSON));
			assertThat(result.getQuantity(), is(1));
			assertThat(result.getMember(), hasSize(1));
			
			GroupMemberComponent component = result.getMemberFirstRep();
			assertThat(component.getEntity(), is(patientReference));
			assertThat(component.getInactive(), is(false));
			
			Extension typeExtension = result.getExtensionByUrl(GroupTranslatorImpl.COHORT_TYPE_EXTENSION_URL);
			assertThat(typeExtension.getValue(), instanceOf(CodeableConcept.class));
			CodeableConcept concept = (CodeableConcept) typeExtension.getValue();
			assertThat(concept.getCodingFirstRep().getDisplay(), is("Manual Translation"));
			assertThat(concept.getText(), is("Manual Translation"));
			
			Extension locationExtension = result.getExtensionByUrl(GroupTranslatorImpl.GROUP_LOCATION_EXTENSION_URL);
			assertThat(locationExtension.getValue(), is(locationReference));
			
			Extension descriptionExtension = result.getExtensionByUrl(GroupTranslatorImpl.GROUP_DESCRIPTION_EXTENSION_URL);
			assertThat(descriptionExtension.getValue(), instanceOf(StringType.class));
			assertThat(((StringType) descriptionExtension.getValue()).getValue(), is("Cohort description"));
		}
	}
	
	@Test
	public void toOpenmrsType_shouldUseMembershipOperations() {
		RecordingCohort existing = new RecordingCohort();
		existing.setDescription("Original description");
		Patient existingPatient = new Patient();
		existingPatient.setId(3);
		CohortMember current = new CohortMember(existingPatient);
		current.setCohort(existing);
		existing.getCohortMembers().add(current);
		
		Patient newPatient = new Patient();
		newPatient.setId(4);
		
		Reference existingReference = new Reference("Patient/3");
		Reference newReference = new Reference("Patient/4");
		
		when(patientReferenceTranslator.toOpenmrsType(existingReference)).thenReturn(existingPatient);
		when(patientReferenceTranslator.toOpenmrsType(newReference)).thenReturn(newPatient);
		
		Group group = new Group();
		group.setId("Group/cohort-uuid");
		group.addExtension(
		    new Extension(GroupTranslatorImpl.GROUP_DESCRIPTION_EXTENSION_URL, new StringType("Updated description")));
		
		GroupMemberComponent keepInactive = group.addMember();
		keepInactive.setEntity(existingReference);
		keepInactive.setInactive(true);
		
		GroupMemberComponent addActive = group.addMember();
		addActive.setEntity(newReference);
		
		translator.toOpenmrsType(existing, group);
		
		assertThat(existing.addedMembers.size(), is(1));
		CohortMember added = existing.addedMembers.get(0);
		assertThat(added.getPatient(), is(newPatient));
		
		assertThat(existing.removedMembers.size(), is(1));
		CohortMember removed = existing.removedMembers.get(0);
		assertThat(removed.getPatient(), is(existingPatient));
		assertThat(removed.getEndDate(), notNullValue());
		
		assertThat(existing.getDescription(), is("Updated description"));
	}
	
	@Test
	public void toFhirResource_shouldFallbackToTypeNameWhenTranslationMissing() {
		CohortType type = new CohortType();
		type.setUuid("type-uuid");
		type.setName("Manual");
		
		CohortM cohort = new CohortM();
		cohort.setCohortType(type);
		
		try (MockedStatic<CohortFhirUtils> cohortFhirUtils = org.mockito.Mockito.mockStatic(CohortFhirUtils.class)) {
			cohortFhirUtils
			        .when(() -> CohortFhirUtils.getDataTranslation(org.mockito.ArgumentMatchers.any(CohortType.class)))
			        .thenReturn("");
			
			Group result = translator.toFhirResource(cohort);
			
			Extension typeExtension = result.getExtensionByUrl(GroupTranslatorImpl.COHORT_TYPE_EXTENSION_URL);
			assertThat(typeExtension.getValue(), instanceOf(CodeableConcept.class));
			CodeableConcept concept = (CodeableConcept) typeExtension.getValue();
			assertThat(concept.getCodingFirstRep().getDisplay(), is("Manual"));
			assertThat(concept.getText(), is("Manual"));
		}
	}
	
	private static class RecordingCohort extends CohortM {
		
		private final List<CohortMember> addedMembers = new ArrayList<>();
		
		private final List<CohortMember> removedMembers = new ArrayList<>();
		
		@Override
		public CohortDefinitionHandler getDefinitionHandler() {
			return new ManualCohortDefinitionHandler() {
				
				@Override
				public void addMembers(CohortM cohort, CohortMember... cohortMembers) {
					addMembershipsInternal(cohortMembers);
				}
				
				@Override
				public void removeMembers(CohortM cohort, CohortMember... cohortMembers) {
					removeMembershipsInternal(cohortMembers);
				}
			};
		}
		
		@Override
		public void addMemberships(CohortMember... cohortMembers) {
			addMembershipsInternal(cohortMembers);
		}
		
		@Override
		public void removeMemberships(CohortMember... cohortMembers) {
			removeMembershipsInternal(cohortMembers);
		}
		
		private void addMembershipsInternal(CohortMember... cohortMembers) {
			for (CohortMember member : cohortMembers) {
				if (member == null) {
					continue;
				}
				member.setCohort(this);
				getCohortMembers().add(member);
				addedMembers.add(member);
			}
		}
		
		private void removeMembershipsInternal(CohortMember... cohortMembers) {
			for (CohortMember member : cohortMembers) {
				if (member == null) {
					continue;
				}
				if (member.getEndDate() == null) {
					member.setEndDate(new Date());
				}
				getCohortMembers().remove(member);
				removedMembers.add(member);
			}
		}
	}
}

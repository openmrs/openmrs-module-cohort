package org.openmrs.module.cohort.fhir2.provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.cohort.fhir2.translator.GroupTranslator;

@RunWith(MockitoJUnitRunner.class)
public class GroupFhirResourceProviderTest {
	
	private static final String GROUP_UUID = "5fa2a82f-23e9-41b7-a6b5-6bcae2c5b383";
	
	@Mock
	private CohortService cohortService;
	
	@Mock
	private GroupTranslator groupTranslator;
	
	private GroupFhirResourceProvider provider;
	
	@Before
	public void setup() {
		provider = new GroupFhirResourceProvider();
		provider.setCohortService(cohortService);
		provider.setGroupTranslator(groupTranslator);
	}
	
	@Test
	public void getResourceType_shouldReturnGroupClass() {
		assertThat(provider.getResourceType(), equalTo(Group.class));
	}
	
	@Test
	public void searchGroups_shouldReturnTranslatedResults() {
		CohortM cohort = new CohortM();
		Group translatedGroup = new Group();
		
		when(cohortService.findMatchingCohortMs("vl", null, null, false)).thenReturn(Collections.singletonList(cohort));
		when(groupTranslator.toFhirResource(cohort)).thenReturn(translatedGroup);
		
		IBundleProvider result = provider.searchGroups(new StringParam("vl"));
		
		assertThat(result, instanceOf(SimpleBundleProvider.class));
		
		List<IBaseResource> resources = result.getResources(0, 10);
		assertThat(resources, contains(translatedGroup));
		
		verify(cohortService).findMatchingCohortMs("vl", null, null, false);
		verify(groupTranslator).toFhirResource(cohort);
	}
	
	@Test
	public void searchGroups_shouldHandleNullNameParameter() {
		when(cohortService.findMatchingCohortMs(null, null, null, false)).thenReturn(Collections.emptyList());
		
		IBundleProvider result = provider.searchGroups(null);
		
		assertThat(result.getResources(0, 1), is(Collections.emptyList()));
		
		verify(cohortService).findMatchingCohortMs(null, null, null, false);
		verifyNoInteractions(groupTranslator);
	}
	
	@Test
	public void getGroupById_shouldReturnTranslatedGroup() {
		CohortM cohort = new CohortM();
		Group translatedGroup = new Group();
		
		when(cohortService.getCohortMByUuid(GROUP_UUID)).thenReturn(cohort);
		when(groupTranslator.toFhirResource(cohort)).thenReturn(translatedGroup);
		
		Group result = provider.getGroupById(new IdType("Group", GROUP_UUID));
		
		assertThat(result, is(translatedGroup));
		verify(groupTranslator).toFhirResource(cohort);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void getGroupById_shouldThrowIfCohortNotFound() {
		when(cohortService.getCohortMByUuid(GROUP_UUID)).thenReturn(null);
		
		provider.getGroupById(new IdType("Group", GROUP_UUID));
	}
	
	@Test
	public void createGroup_shouldPersistTranslatedCohort() {
		Group input = new Group();
		CohortM cohortToSave = new CohortM();
		CohortM savedCohort = new CohortM();
		savedCohort.setUuid(GROUP_UUID);
		Group translatedGroup = new Group();
		
		when(groupTranslator.toOpenmrsType(input)).thenReturn(cohortToSave);
		when(cohortService.saveCohortM(cohortToSave)).thenReturn(savedCohort);
		when(groupTranslator.toFhirResource(savedCohort)).thenReturn(translatedGroup);
		
		MethodOutcome outcome = provider.createGroup(input);
		
		assertThat(outcome.getCreated(), is(true));
		assertThat(outcome.getResource(), is(translatedGroup));
		assertThat(outcome.getId() == null || outcome.getId().isEmpty(), is(true));
		
		verify(groupTranslator).toOpenmrsType(input);
		verify(groupTranslator).toFhirResource(savedCohort);
		verify(cohortService).saveCohortM(cohortToSave);
	}
	
	@Test
	public void updateGroup_shouldPersistExistingCohort() {
		IdType id = new IdType("Group", GROUP_UUID);
		Group input = new Group();
		CohortM existing = new CohortM();
		CohortM updated = new CohortM();
		CohortM saved = new CohortM();
		saved.setUuid(GROUP_UUID);
		Group translatedGroup = new Group();
		
		when(cohortService.getCohortMByUuid(GROUP_UUID)).thenReturn(existing);
		when(groupTranslator.toOpenmrsType(existing, input)).thenReturn(updated);
		when(cohortService.saveCohortM(updated)).thenReturn(saved);
		when(groupTranslator.toFhirResource(saved)).thenReturn(translatedGroup);
		
		MethodOutcome outcome = provider.updateGroup(id, input);
		
		assertThat(outcome.getCreated(), is(false));
		assertThat(outcome.getResource(), is(translatedGroup));
		assertThat(outcome.getId() == null || outcome.getId().isEmpty(), is(true));
		
		verify(groupTranslator).toOpenmrsType(existing, input);
		verify(cohortService).saveCohortM(updated);
		verify(groupTranslator).toFhirResource(saved);
	}
	
	@Test
	public void patchGroup_shouldApplyJsonPatchAndPersistCohort() {
		IdType id = new IdType("Group", GROUP_UUID);
		CohortM existing = new CohortM();
		CohortM updated = new CohortM();
		CohortM saved = new CohortM();
		saved.setUuid(GROUP_UUID);
		Group existingGroup = new Group();
		existingGroup.setName("Initial name");
		Group savedGroup = new Group();
		String patchBody = "[{\"op\":\"replace\",\"path\":\"/name\",\"value\":\"Updated name\"}]";
		
		when(cohortService.getCohortMByUuid(GROUP_UUID)).thenReturn(existing);
		when(groupTranslator.toFhirResource(existing)).thenReturn(existingGroup);
		when(groupTranslator.toOpenmrsType(eq(existing), any(Group.class))).thenReturn(updated);
		when(cohortService.saveCohortM(updated)).thenReturn(saved);
		when(groupTranslator.toFhirResource(saved)).thenReturn(savedGroup);
		
		MethodOutcome outcome = provider.patchGroup(id, PatchTypeEnum.JSON_PATCH, patchBody, null);
		
		assertThat(outcome.getCreated(), is(false));
		assertThat(outcome.getResource(), is(savedGroup));
		
		ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
		verify(groupTranslator).toOpenmrsType(eq(existing), groupCaptor.capture());
		assertThat(groupCaptor.getValue().getName(), is("Updated name"));
		
		verify(cohortService).saveCohortM(updated);
	}
	
	@Test
	public void patchGroup_shouldApplyXmlPatchAndPersistCohort() {
		IdType id = new IdType("Group", GROUP_UUID);
		CohortM existing = new CohortM();
		CohortM updated = new CohortM();
		CohortM saved = new CohortM();
		saved.setUuid(GROUP_UUID);
		Group existingGroup = new Group();
		existingGroup.setName("Initial name");
		Group savedGroup = new Group();
		String patchBody = "<patch xmlns=\"urn:ietf:params:xml:ns:xml-patch\" xmlns:f=\"http://hl7.org/fhir\">"
		        + "<replace sel=\"/f:Group/f:name/@value\">Updated name</replace></patch>";
		
		when(cohortService.getCohortMByUuid(GROUP_UUID)).thenReturn(existing);
		when(groupTranslator.toFhirResource(existing)).thenReturn(existingGroup);
		when(groupTranslator.toOpenmrsType(eq(existing), any(Group.class))).thenReturn(updated);
		when(cohortService.saveCohortM(updated)).thenReturn(saved);
		when(groupTranslator.toFhirResource(saved)).thenReturn(savedGroup);
		
		MethodOutcome outcome = provider.patchGroup(id, PatchTypeEnum.XML_PATCH, patchBody, null);
		
		assertThat(outcome.getCreated(), is(false));
		assertThat(outcome.getResource(), is(savedGroup));
		
		ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
		verify(groupTranslator).toOpenmrsType(eq(existing), groupCaptor.capture());
		assertThat(groupCaptor.getValue().getName(), is("Updated name"));
		
		verify(cohortService).saveCohortM(updated);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void patchGroup_shouldThrowWhenIdMissing() {
		provider.patchGroup(null, PatchTypeEnum.JSON_PATCH, "{}", null);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void patchGroup_shouldThrowWhenIdPartMissing() {
		provider.patchGroup(new IdType(), PatchTypeEnum.JSON_PATCH, "{}", null);
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void patchGroup_shouldThrowWhenCohortNotFound() {
		IdType id = new IdType("Group", GROUP_UUID);
		when(cohortService.getCohortMByUuid(GROUP_UUID)).thenReturn(null);
		
		provider.patchGroup(id, PatchTypeEnum.JSON_PATCH, "{}", null);
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateGroup_shouldThrowWhenIdMissing() {
		provider.updateGroup(null, new Group());
	}
	
	@Test(expected = InvalidRequestException.class)
	public void updateGroup_shouldThrowWhenIdPartMissing() {
		provider.updateGroup(new IdType(), new Group());
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void updateGroup_shouldThrowWhenCohortNotFound() {
		IdType id = new IdType("Group", GROUP_UUID);
		when(cohortService.getCohortMByUuid(GROUP_UUID)).thenReturn(null);
		
		provider.updateGroup(id, new Group());
	}
	
	@Test
	public void deleteGroup_shouldVoidCohort() {
		CohortM cohort = new CohortM();
		when(cohortService.getCohortMByUuid(GROUP_UUID)).thenReturn(cohort);
		
		OperationOutcome outcome = provider.deleteGroup(new IdType("Group", GROUP_UUID));
		
		assertThat(outcome, notNullValue());
		verify(cohortService).voidCohortM(cohort, "voided via FHIR request");
	}
	
	@Test(expected = ResourceNotFoundException.class)
	public void deleteGroup_shouldThrowWhenCohortNotFound() {
		when(cohortService.getCohortMByUuid(GROUP_UUID)).thenReturn(null);
		
		provider.deleteGroup(new IdType("Group", GROUP_UUID));
	}
}

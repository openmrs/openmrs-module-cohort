package org.openmrs.module.cohort.web.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.Attribute;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.CohortMemberAttributeType;
import org.openmrs.module.cohort.api.CohortMemberService;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.jupiter.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cohort updates sent the way a server runs them: outside a test transaction, so each service call
 * commits on its own, and in a fresh session that only flushes when one of them commits. Inside the
 * usual test transaction nothing commits, so these requests fail or pass there for other reasons.
 * The data a test commits is deleted after it.
 */
public class CohortResourceCommitTest extends MainResourceControllerTest {
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Override
	public String getURI() {
		return "cohortm/cohort";
	}
	
	@Override
	public String getUuid() {
		return null;
	}
	
	@AfterEach
	public void deleteCommittedData() {
		deleteAllData();
	}
	
	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldSaveTheAttributesOfACohortWhoseUpdateAlsoRemovesAMember() throws Exception {
		CohortAttributeType facility = new CohortAttributeType();
		facility.setName("Facility");
		facility.setDatatypeClassname(FreeTextDatatype.class.getName());
		Context.getService(CohortService.class).saveCohortAttributeType(facility);
		CohortM cohort = saveCohort("cohort name");
		CohortMember kept = saveMember(cohort, 7);
		CohortMember removed = saveMember(cohort, 8);
		
		sendAsARequest(newPostRequest(getURI() + "/" + cohort.getUuid(),
		    "{ \"attributes\": [ { \"attributeType\": \"" + facility.getUuid() + "\", \"value\": \"Nairobi\" } ], "
		            + "\"cohortMembers\": [ { \"uuid\": \"" + kept.getUuid() + "\" } ] }"));
		
		assertEquals("[Nairobi]",
		    valuesOf(Context.getService(CohortService.class).getCohortMByUuid(cohort.getUuid()).getActiveAttributes()));
		assertFalse(memberOf(cohort, kept).getVoided());
		CohortMember voided = memberOf(cohort, removed);
		assertTrue(voided.getVoided());
		assertEquals("Cohort member removed", voided.getVoidReason());
		assertNotNull(voided.getVoidedBy());
		assertNotNull(voided.getDateVoided());
	}
	
	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldReplaceTheAttributesOfAnExistingMemberUpdatedThroughItsCohort() throws Exception {
		CohortMemberAttributeType role = new CohortMemberAttributeType();
		role.setName("Role");
		role.setDatatypeClassname(FreeTextDatatype.class.getName());
		Context.getService(CohortMemberService.class).saveCohortMemberAttributeType(role);
		CohortM cohort = saveCohort("cohort name");
		CohortMember secretary = newMember(cohort, 7);
		CohortMemberAttribute attribute = new CohortMemberAttribute();
		attribute.setAttributeType(role);
		attribute.setValue("secretary");
		secretary.addAttribute(attribute);
		Context.getService(CohortMemberService.class).saveCohortMember(secretary);
		CohortMember other = saveMember(cohort, 8);
		
		sendAsARequest(newPostRequest(getURI() + "/" + cohort.getUuid(),
		    "{ \"name\": \"renamed\", \"cohortMembers\": [ { \"uuid\": \"" + secretary.getUuid() + "\", \"attributes\": [ { "
		            + "\"attributeType\": \"" + role.getUuid() + "\", \"value\": \"treasurer\" } ] }, { \"uuid\": \""
		            + other.getUuid() + "\" } ] }"));
		
		assertEquals("renamed", Context.getService(CohortService.class).getCohortMByUuid(cohort.getUuid()).getName());
		CohortMember updated = Context.getService(CohortMemberService.class).getCohortMemberByUuid(secretary.getUuid());
		assertEquals("[treasurer]", valuesOf(updated.getActiveAttributes()));
		assertEquals("[secretary, treasurer]", valuesOf(updated.getAttributes()));
	}
	
	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldChangeNothingWhenAnUpdateThatReplacesTheMembersFailsValidation() throws Exception {
		saveCohort("taken name");
		CohortM cohort = saveCohort("cohort name");
		CohortMember first = saveMember(cohort, 7);
		CohortMember second = saveMember(cohort, 8);
		// the whole member list, given as patients, the way O3 fast data entry's Edit Group sends it
		MockHttpServletRequest request = newPostRequest(getURI() + "/" + cohort.getUuid(),
		    "{ \"name\": \"taken name\", \"cohortMembers\": [ " + newMemberJson(7) + ", " + newMemberJson(8) + " ] }");
		
		assertThrows(ValidationException.class, () -> sendAsARequest(request));
		
		assertEquals("cohort name", Context.getService(CohortService.class).getCohortMByUuid(cohort.getUuid()).getName());
		assertFalse(memberOf(cohort, first).getVoided());
		assertFalse(memberOf(cohort, second).getVoided());
	}
	
	/** Finds the member among all of the cohort's members, voided ones included. */
	private static CohortMember memberOf(CohortM cohort, CohortMember member) {
		return Context.getService(CohortService.class).getCohortMByUuid(cohort.getUuid()).getCohortMembers().stream()
		        .filter(m -> m.getUuid().equals(member.getUuid())).findFirst().orElseThrow(AssertionError::new);
	}
	
	private void sendAsARequest(MockHttpServletRequest request) throws Exception {
		openNewSession();
		try {
			handle(request);
		}
		finally {
			openNewSession();
		}
	}
	
	private void openNewSession() {
		while (Context.isSessionOpen()) {
			Context.closeSession();
		}
		Context.openSession();
		authenticate();
	}
	
	private CohortM saveCohort(String name) {
		CohortM cohort = new CohortM();
		cohort.setName(name);
		cohort.setDescription("cohort description");
		return Context.getService(CohortService.class).saveCohortM(cohort);
	}
	
	private CohortMember newMember(CohortM cohort, int patientId) {
		CohortMember member = new CohortMember(Context.getPatientService().getPatient(patientId));
		member.setCohort(cohort);
		member.setStartDate(new Date(System.currentTimeMillis() - 1000));
		return member;
	}
	
	private CohortMember saveMember(CohortM cohort, int patientId) {
		return Context.getService(CohortMemberService.class).saveCohortMember(newMember(cohort, patientId));
	}
	
	private static String newMemberJson(int patientId) {
		return "{ \"patient\": \"" + Context.getPatientService().getPatient(patientId).getUuid()
		        + "\", \"startDate\": \"2025-10-09T00:00:00.000+0000\" }";
	}
	
	private static String valuesOf(Collection<? extends Attribute<?, ?>> attributes) {
		return attributes.stream().map(Attribute::getValueReference).sorted().collect(Collectors.toList()).toString();
	}
	
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
		
	}
	
	@Override
	public void shouldGetFullByUuid() throws Exception {
		
	}
	
	@Override
	public void shouldGetRefByUuid() throws Exception {
		
	}
	
	@Override
	public void shouldGetAll() throws Exception {
		
	}
}

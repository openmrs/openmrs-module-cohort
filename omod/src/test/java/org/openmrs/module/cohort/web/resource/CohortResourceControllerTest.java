package org.openmrs.module.cohort.web.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.CohortMemberAttributeType;
import org.openmrs.module.cohort.api.CohortMemberService;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.jupiter.MainResourceControllerTest;

public class CohortResourceControllerTest extends MainResourceControllerTest {
	
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
	
	@Test
	public void shouldCreateCohortWithMembers() throws Exception {
		
		CohortM cohort = Context.getService(CohortService.class).getCohortM("cohort name");
		assertNull(cohort);
		
		String json = "{ \"name\":\"cohort name\", \"description\":\"cohort description\"," + "\"cohortMembers\": [ {"
		        + "\"patient\":\"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\","
		        + "\"startDate\": \"2025-09-04T00:00:00.000+0000\" " + " } ]" + "}";
		
		handle(newPostRequest(getURI(), json));
		
		cohort = Context.getService(CohortService.class).getCohortM("cohort name");
		assertNotNull(cohort);
		assertEquals("cohort description", cohort.getDescription());
		
		Set<CohortMember> members = cohort.getActiveCohortMembers();
		assertEquals(1, members.size());
		assertEquals("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", members.iterator().next().getPatient().getUuid());
	}
	
	@Test
	public void shouldUpdateCohortWhileKeepingName() throws Exception {
		
		CohortM cohort = Context.getService(CohortService.class).getCohortM("cohort name");
		assertNull(cohort);
		
		String createJson = "{ \"name\":\"cohort name\", \"description\":\"cohort description\"," + "\"cohortMembers\": [ {"
		        + "\"patient\":\"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\","
		        + "\"startDate\": \"2025-09-04T00:00:00.000+0000\" " + " } ]" + "}";
		
		handle(newPostRequest(getURI(), createJson));
		cohort = Context.getService(CohortService.class).getCohortM("cohort name");
		
		String updateJson = "{ \"name\":\"cohort name\", \"description\":\"updated cohort description\" } ]" + "}";
		
		handle(newPostRequest(getURI() + "/" + cohort.getUuid(), updateJson));
		cohort = Context.getService(CohortService.class).getCohortM("cohort name");
		
		assertNotNull(cohort);
		assertEquals("updated cohort description", cohort.getDescription());
	}
	
	@Test
	public void shouldReplaceTheAttributesOfACohortOnUpdate() throws Exception {
		CohortAttributeType facility = saveCohortAttributeType("Facility");
		CohortAttributeType program = saveCohortAttributeType("Program");
		CohortM cohort = new CohortM();
		cohort.setName("cohort name");
		cohort.setDescription("cohort description");
		cohort.addAttribute(newCohortAttribute(facility, "Kisumu"));
		cohort.addAttribute(newCohortAttribute(program, "Youth"));
		Context.getService(CohortService.class).saveCohortM(cohort);
		
		String json = "{ \"attributes\": [ { \"attributeType\": \"" + facility.getUuid()
		        + "\", \"value\": \"Nairobi\" } ] }";
		
		handle(newPostRequest(getURI() + "/" + cohort.getUuid(), json));
		Context.flushSession();
		Context.clearSession();
		
		CohortM updatedCohort = Context.getService(CohortService.class).getCohortMByUuid(cohort.getUuid());
		assertEquals(Collections.singletonList("Nairobi"), valueReferencesOf(updatedCohort.getActiveAttributes()));
		assertEquals(Arrays.asList("Kisumu", "Youth"), valueReferencesOf(
		    updatedCohort.getAttributes().stream().filter(CohortAttribute::getVoided).collect(Collectors.toList())));
	}
	
	@Test
	public void shouldCreateCohortWithMembersAndTheirAttributes() throws Exception {
		CohortMemberAttributeType role = new CohortMemberAttributeType();
		role.setName("Role");
		role.setDatatypeClassname(FreeTextDatatype.class.getName());
		Context.getService(CohortMemberService.class).saveCohortMemberAttributeType(role);
		
		String json = "{ \"name\":\"cohort name\", \"description\":\"cohort description\"," + "\"cohortMembers\": [ {"
		        + "\"patient\":\"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\","
		        + "\"startDate\": \"2025-09-04T00:00:00.000+0000\"," + "\"attributes\": [ { \"attributeType\": \""
		        + role.getUuid() + "\", \"value\": \"treasurer\" } ] } ] }";
		
		handle(newPostRequest(getURI(), json));
		Context.flushSession();
		Context.clearSession();
		
		Set<CohortMember> members = Context.getService(CohortService.class).getCohortM("cohort name")
		        .getActiveCohortMembers();
		assertEquals(1, members.size());
		Collection<CohortMemberAttribute> attributes = members.iterator().next().getActiveAttributes();
		assertEquals(1, attributes.size());
		CohortMemberAttribute attribute = attributes.iterator().next();
		assertEquals(role.getUuid(), attribute.getAttributeType().getUuid());
		assertEquals("treasurer", attribute.getValueReference());
	}
	
	private CohortAttributeType saveCohortAttributeType(String name) {
		CohortAttributeType attributeType = new CohortAttributeType();
		attributeType.setName(name);
		attributeType.setDatatypeClassname(FreeTextDatatype.class.getName());
		return Context.getService(CohortService.class).saveCohortAttributeType(attributeType);
	}
	
	private CohortAttribute newCohortAttribute(CohortAttributeType attributeType, String valueReference) {
		CohortAttribute attribute = new CohortAttribute();
		attribute.setAttributeType(attributeType);
		attribute.setValueReferenceInternal(valueReference);
		return attribute;
	}
	
	private static List<String> valueReferencesOf(Collection<CohortAttribute> attributes) {
		return attributes.stream().map(CohortAttribute::getValueReference).sorted().collect(Collectors.toList());
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
}

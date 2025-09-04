package org.openmrs.module.cohort.web.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
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

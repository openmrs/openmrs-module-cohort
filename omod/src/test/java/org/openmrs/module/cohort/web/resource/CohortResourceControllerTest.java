package org.openmrs.module.cohort.web.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.api.CohortService;

public class CohortResourceControllerTest extends BaseWebControllerTest {
	
	public long getAllCount() {
		return 0;
	}
	
	public String getURI() {
		return "/rest/v1/cohortm/cohort";
	}
	
	public String getUuid() {
		return null;
	}
	
	@Test
	public void shouldCreateCohortWithMembers() throws Exception {
		
		CohortM cohort = Context.getService(CohortService.class).getCohortM("cohort name");
		assertNull(cohort);
		
		String json = "{ \"name\":\"cohort name\", \"description\":\"cohort description\"," + "\"startDate\":\"2023-08-22T01:00:00.000+0000\","
		        + "\"cohortMembers\": [ {" + "\"patient\":\"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\"" + " } ]" + "}";
		
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
		        + "\"patient\":\"da7f524f-27ce-4bb2-86d6-6d1d05312bd5\"" + " } ]" + "}";
		
		handle(newPostRequest(getURI(), createJson));
		cohort = Context.getService(CohortService.class).getCohortM("cohort name");
		
		String updateJson = "{ \"name\":\"cohort name\", \"description\":\"updated cohort description\" } ]" + "}";
		
		handle(newPostRequest(getURI() + "/" + cohort.getUuid(), updateJson));
		cohort = Context.getService(CohortService.class).getCohortM("cohort name");
		
		assertNotNull(cohort);
		assertEquals("updated cohort description", cohort.getDescription());
	}
	
}

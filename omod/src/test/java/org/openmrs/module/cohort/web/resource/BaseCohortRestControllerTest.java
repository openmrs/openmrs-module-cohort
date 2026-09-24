package org.openmrs.module.cohort.web.resource;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeAll;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.jupiter.MainResourceControllerTest;

/**
 * Provides shared setup for REST controller tests.
 */
public abstract class BaseCohortRestControllerTest extends MainResourceControllerTest {
	
	private static final String DEFAULT_REST_URI_PREFIX = "/ws/rest/v1";
	
	@BeforeAll
	public static void setUpRestUriPrefix() {
		if (RestConstants.URI_PREFIX == null) {
			setUriPrefix(DEFAULT_REST_URI_PREFIX);
		}
	}
	
	private static void setUriPrefix(String prefix) {
		try {
			Field uriPrefixField = RestConstants.class.getDeclaredField("URI_PREFIX");
			uriPrefixField.setAccessible(true);
			
			Object currentValue = uriPrefixField.get(null);
			if (currentValue == null) {
				uriPrefixField.set(null, prefix);
			}
		}
		catch (NoSuchFieldException | IllegalAccessException ex) {
			throw new IllegalStateException("Failed to initialize RestConstants.URI_PREFIX", ex);
		}
	}
}

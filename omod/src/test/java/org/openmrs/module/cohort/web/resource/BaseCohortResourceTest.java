/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.web.resource;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PrepareForTest({ Context.class, RestUtil.class })
public class BaseCohortResourceTest<K extends OpenmrsObject, T extends DelegatingResourceHandler<K>> {
	
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private T resource;
	
	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private K object;
	
	@Before
	public void prepareMocks() {
		PowerMockito.mockStatic(RestUtil.class);
		
		PowerMockito.mockStatic(Context.class);
		//By pass authentication
		when(Context.isAuthenticated()).thenReturn(true);
	}
	
	public void verifyDefaultRepresentation(String... properties) {
		DelegatingResourceDescription defaultResourceDescription = resource
		        .getRepresentationDescription(new DefaultRepresentation());
		
		assertThat(defaultResourceDescription, notNullValue());
		for (String p : properties) {
			assertThat(defaultResourceDescription.getProperties(), hasKey(p));
		}
	}
	
	public void verifyFullRepresentation(String... properties) {
		DelegatingResourceDescription fullResourceDescription = resource
		        .getRepresentationDescription(new FullRepresentation());
		
		assertThat(fullResourceDescription, notNullValue());
		for (String p : properties) {
			assertThat(fullResourceDescription.getProperties(), hasKey(p));
		}
	}
	
	public void verifyCreatableProperties(String... properties) {
		DelegatingResourceDescription creatableProperties = resource.getCreatableProperties();
		
		assertThat(creatableProperties, notNullValue());
		assertThat(creatableProperties.getProperties().keySet(), contains(properties));
	}
	
	public void verifyUpdatableProperties(String... properties) {
		DelegatingResourceDescription updatableProperties = resource.getUpdatableProperties();
		
		assertThat(updatableProperties, notNullValue());
		assertThat(updatableProperties.getProperties().keySet(), contains(properties));
	}
}

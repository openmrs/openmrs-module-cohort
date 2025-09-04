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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;

public class CohortAttributeTypeResourceTest extends BaseCohortResourceTest<CohortAttributeType, CohortAttributeTypeResource> {
	
	private static final String COHORT_ATTRIBUTE_TYPE_UUID = "8tae735a-fca0-11e5-9e59-08002719a786";
	
	private static final String COHORT_ATTRIBUTE_TYPE_NAME = "Test cohort attribute type";
	
	private CohortService cohortService;
	
	CohortAttributeType cohortAttributeType;
	
	@BeforeEach
	public void setup() {
		cohortService = mock(CohortService.class);
		cohortAttributeType = new CohortAttributeType();
		cohortAttributeType.setUuid(COHORT_ATTRIBUTE_TYPE_UUID);
		cohortAttributeType.setName(COHORT_ATTRIBUTE_TYPE_NAME);
		
		//Mocks
		getContextMock().when(() -> Context.getService(CohortService.class)).thenReturn(cohortService);
		
		this.setResource(new CohortAttributeTypeResource());
		this.setObject(cohortAttributeType);
	}
	
	@Test
	public void shouldGetRegisteredService() {
		assertThat(cohortService, notNullValue());
	}
	
	@Test
	public void shouldReturnDefaultRepresentation() {
		verifyDefaultRepresentation("name", "description", "uuid");
	}
	
	@Test
	public void shouldReturnFullRepresentation() {
		verifyFullRepresentation("name", "description", "uuid", "auditInfo");
	}
	
	@Test
	public void shouldReturnNullForRepresentationOtherThenDefaultOrFull() {
		CustomRepresentation customRepresentation = new CustomRepresentation("some-rep");
		assertThat(getResource().getRepresentationDescription(customRepresentation), is(nullValue()));
		
		NamedRepresentation namedRepresentation = new NamedRepresentation("some-named-rep");
		assertThat(getResource().getRepresentationDescription(namedRepresentation), is(nullValue()));
		
		RefRepresentation refRepresentation = new RefRepresentation();
		assertThat(getResource().getRepresentationDescription(refRepresentation), is(nullValue()));
	}
	
	@Test
	public void shouldGetResourceByUniqueUuid() {
		when(cohortService.getCohortAttributeTypeByUuid(COHORT_ATTRIBUTE_TYPE_UUID)).thenReturn(cohortAttributeType);
		
		CohortAttributeType result = getResource().getByUniqueId(COHORT_ATTRIBUTE_TYPE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), is(COHORT_ATTRIBUTE_TYPE_UUID));
		assertThat(result.getName(), is(COHORT_ATTRIBUTE_TYPE_NAME));
	}
	
	@Test
	public void shouldCreateNewResource() {
		when(cohortService.saveCohortAttributeType(getObject())).thenReturn(getObject());
		
		CohortAttributeType newlyCreatedObject = getResource().save(getObject());
		assertThat(newlyCreatedObject, notNullValue());
		assertThat(newlyCreatedObject.getUuid(), is(COHORT_ATTRIBUTE_TYPE_UUID));
		assertThat(newlyCreatedObject.getName(), is(COHORT_ATTRIBUTE_TYPE_NAME));
	}
	
	@Test
	public void shouldGetAllResources() {
		when(cohortService.findAllCohortAttributeTypes()).thenReturn(Collections.singletonList(getObject()));
		
		PageableResult results = getResource().doGetAll(new RequestContext());
		
		assertThat(results, notNullValue());
	}
	
	@Test
	public void shouldInstantiateNewDelegate() {
		assertThat(getResource().newDelegate(), notNullValue());
	}
	
	@Test
	public void verifyResourceVersion() {
		assertThat(getResource().getResourceVersion(), is("1.9"));
	}
	
}

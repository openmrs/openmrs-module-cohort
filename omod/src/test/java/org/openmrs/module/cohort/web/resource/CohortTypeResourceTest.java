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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortTypeService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;

public class CohortTypeResourceTest extends BaseCohortResourceTest<CohortType, CohortTypeResource> {
	
	private static final String COHORT_TYPE_UUID = "4hje098a-fca0-34e5-9e59-18552719a3";
	
	private CohortTypeService cohortTypeService;
	
	CohortType cohortType;
	
	@BeforeEach
	public void setup() {
		cohortType = new CohortType();
		cohortType.setUuid(COHORT_TYPE_UUID);
		
		//Mocks
		cohortTypeService = mock(CohortTypeService.class);
		when(Context.getService(CohortTypeService.class)).thenReturn(cohortTypeService);
		
		this.setResource(new CohortTypeResource());
		this.setObject(cohortType);
	}
	
	@Test
	public void shouldGetRegisteredService() {
		assertThat(cohortTypeService, notNullValue());
	}
	
	@Test
	public void shouldReturnDefaultRepresentation() {
		verifyDefaultRepresentation("uuid", "name", "description", "display");
	}
	
	@Test
	public void shouldReturnFullRepresentation() {
		verifyFullRepresentation("uuid", "name", "description", "display", "auditInfo");
	}
	
	@Test
	public void shouldVerifyCreatableProperties() {
		verifyCreatableProperties("name", "description");
	}
	
	@Test
	public void shouldVerifyUpdatableProperties() {
		verifyUpdatableProperties("name", "description");
	}
	
	@Test
	public void shouldGetAllResource() {
		when(cohortTypeService.findAllCohortTypes()).thenReturn(Collections.singletonList(cohortType));
		
		PageableResult result = getResource().doGetAll(new RequestContext());
		assertThat(result, notNullValue());
	}
	
	@Test
	public void shouldGetResourceByUniqueUuid() {
		when(cohortTypeService.getCohortTypeByUuid(COHORT_TYPE_UUID)).thenReturn(cohortType);
		
		CohortType result = getResource().getByUniqueId(COHORT_TYPE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), is(COHORT_TYPE_UUID));
	}
	
	@Test
	public void shouldCreateNewResource() {
		when(cohortTypeService.saveCohortType(getObject())).thenReturn(getObject());
		
		CohortType newlyCreatedObject = getResource().save(getObject());
		assertThat(newlyCreatedObject, notNullValue());
		assertThat(newlyCreatedObject.getUuid(), is(COHORT_TYPE_UUID));
	}
	
	@Test
	public void shouldInstantiateNewDelegate() {
		assertThat(getResource().newDelegate(), notNullValue());
	}
	
	@Test
	public void verifyResourceVersion() {
		assertThat(getResource().getResourceVersion(), is("1.8"));
	}
}

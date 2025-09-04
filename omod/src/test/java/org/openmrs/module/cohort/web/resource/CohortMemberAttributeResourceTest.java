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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.api.CohortMemberService;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;

public class CohortMemberAttributeResourceTest extends BaseCohortResourceTest<CohortMemberAttribute, CohortMemberAttributeResource> {
	
	private static final String COHORT_MEMBER_ATTRIBUTE_UUID = "4hje098a-fca0-34e5-9e59-18552719a3";
	
	private CohortMemberService cohortMemberService;
	
	CohortMemberAttribute cohortMemberAttribute;
	
	@BeforeEach
	public void setup() {
		cohortMemberService = mock(CohortMemberService.class);
		cohortMemberAttribute = new CohortMemberAttribute();
		cohortMemberAttribute.setUuid(COHORT_MEMBER_ATTRIBUTE_UUID);
		
		//Mocks
		when(Context.getService(CohortMemberService.class)).thenReturn(cohortMemberService);
		
		this.setResource(new CohortMemberAttributeResource());
		this.setObject(cohortMemberAttribute);
	}
	
	@Test
	public void shouldGetRegisteredService() {
		assertThat(cohortMemberService, notNullValue());
	}
	
	@Test
	public void shouldReturnDefaultRepresentation() {
		verifyDefaultRepresentation("uuid", "value", "display");
	}
	
	@Test
	public void shouldReturnFullRepresentation() {
		verifyFullRepresentation("uuid", "value", "display", "auditInfo");
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
		when(cohortMemberService.getCohortMemberAttributeByUuid(COHORT_MEMBER_ATTRIBUTE_UUID))
		        .thenReturn(cohortMemberAttribute);
		
		CohortMemberAttribute result = getResource().getByUniqueId(COHORT_MEMBER_ATTRIBUTE_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), is(COHORT_MEMBER_ATTRIBUTE_UUID));
	}
	
	@Test
	public void shouldCreateNewResource() {
		when(cohortMemberService.saveCohortMemberAttribute(getObject())).thenReturn(getObject());
		
		CohortMemberAttribute newlyCreatedObject = getResource().save(getObject());
		assertThat(newlyCreatedObject, notNullValue());
		assertThat(newlyCreatedObject.getUuid(), is(COHORT_MEMBER_ATTRIBUTE_UUID));
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

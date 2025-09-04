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

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.api.CohortMemberService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.springframework.context.annotation.Description;

public class CohortMemberResourceTest extends BaseCohortResourceTest<CohortMember, CohortMemberResource> {
	
	private static final String COHORT_MEMBER_UUID = "6hje567a-fca0-11e5-9e59-08002719a7";
	
	private CohortMemberService cohortMemberService;
	
	CohortMember cohortMember;
	
	@BeforeEach
	public void setup() {
		cohortMember = new CohortMember();
		cohortMember.setUuid(COHORT_MEMBER_UUID);
		
		//Mocks
		cohortMemberService = mock(CohortMemberService.class);
		when(Context.getService(CohortMemberService.class)).thenReturn(cohortMemberService);
		
		this.setResource(new CohortMemberResource());
		this.setObject(cohortMember);
	}
	
	@Test
	public void shouldGetRegisteredService() {
		assertThat(cohortMemberService, notNullValue());
	}
	
	@Test
	public void shouldReturnDefaultRepresentation() {
		verifyDefaultRepresentation("patient", "startDate", "endDate", "uuid", "attributes", "cohort");
	}
	
	@Test
	public void shouldReturnFullRepresentation() {
		verifyFullRepresentation("patient", "startDate", "endDate", "uuid", "attributes", "cohort", "auditInfo");
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
		when(cohortMemberService.getCohortMemberByUuid(COHORT_MEMBER_UUID)).thenReturn(cohortMember);
		
		CohortMember result = getResource().getByUniqueId(COHORT_MEMBER_UUID);
		assertThat(result, notNullValue());
		assertThat(result.getUuid(), is(COHORT_MEMBER_UUID));
	}
	
	@Test
	public void shouldCreateNewResource() {
		CohortM cohort = mock(CohortM.class);
		cohortMember.setCohort(cohort);
		
		when(cohortMemberService.saveCohortMember(getObject())).thenReturn(getObject());
		when(cohort.getVoided()).thenReturn(false);
		
		CohortMember newlyCreatedObject = getResource().save(getObject());
		assertThat(newlyCreatedObject, notNullValue());
		assertThat(newlyCreatedObject.getUuid(), is(COHORT_MEMBER_UUID));
	}
	
	@Test
	@Description("Should raise exception for duplicate member with no end date if the existing member has no endDate")
	public void shouldRaiseRuntimeExceptionWhenSavingExistingMemberWithoutEndDate() {
		// Mocking objects
		CohortM cohort = mock(CohortM.class);
		Patient currentMemberPatient = mock(Patient.class);
		Patient newMemberPatient = mock(Patient.class);
		CohortMember duplicateCohortMember = new CohortMember();
		String patientUUID = UUID.randomUUID().toString();
		
		// Setting up existing member
		cohortMember.setCohort(cohort);
		cohortMember.setPatient(currentMemberPatient);
		cohortMember.setEndDate(null);
		
		// Setting up new member
		duplicateCohortMember.setUuid(COHORT_MEMBER_UUID);
		duplicateCohortMember.setCohort(cohort);
		duplicateCohortMember.setPatient(newMemberPatient);
		duplicateCohortMember.setEndDate(null);
		
		// Mock behavior
		when(cohort.getVoided()).thenReturn(false);
		when(cohort.getCohortMembers()).thenReturn(Collections.singleton(cohortMember));
		when(currentMemberPatient.getUuid()).thenReturn(patientUUID);
		when(newMemberPatient.getUuid()).thenReturn(patientUUID);
		
		// Preconditions check
		assertEquals(newMemberPatient.getUuid(), currentMemberPatient.getUuid());
		assertEquals(duplicateCohortMember.getUuid(), cohortMember.getUuid());
		assertNull(cohortMember.getEndDate());
		assertNull(duplicateCohortMember.getEndDate());
		
		// Validate expected Exception
		try {
			getResource().save(duplicateCohortMember);
			fail();
		}
		catch (RuntimeException e) {
			// test passed
		}
	}
	
	@Test
	public void shouldUpdateExistingMemberWhenEndDateIsNonNull() {
		// Mocking objects
		CohortM cohort = mock(CohortM.class);
		Patient currentMemberPatient = mock(Patient.class);
		Patient updatedMemberPatient = mock(Patient.class);
		CohortMember updatedCohortMember = new CohortMember();
		String patientUUID = UUID.randomUUID().toString();
		
		// Setting up existing member
		cohortMember.setCohort(cohort);
		cohortMember.setPatient(currentMemberPatient);
		cohortMember.setEndDate(null);
		
		// Setting up new member
		updatedCohortMember.setUuid(COHORT_MEMBER_UUID);
		updatedCohortMember.setCohort(cohort);
		updatedCohortMember.setPatient(updatedMemberPatient);
		updatedCohortMember.setEndDate(Date.from(Instant.now()));
		
		// Mock behavior
		when(cohort.getVoided()).thenReturn(false);
		when(cohort.getCohortMembers()).thenReturn(Collections.singleton(cohortMember));
		when(currentMemberPatient.getUuid()).thenReturn(patientUUID);
		when(updatedMemberPatient.getUuid()).thenReturn(patientUUID);
		when(cohortMemberService.saveCohortMember(updatedCohortMember)).thenReturn(updatedCohortMember);
		
		// Verify behaviour
		CohortMember newlyCreatedObject = getResource().save(updatedCohortMember);
		assertNotNull(newlyCreatedObject);
		assertNotNull(newlyCreatedObject.getEndDate());
		assertEquals(updatedCohortMember.getUuid(), newlyCreatedObject.getUuid());
		assertEquals(updatedCohortMember.getEndDate(), newlyCreatedObject.getEndDate());
	}
	
	@Test
	public void shouldReinstateMemberWithNullEndDate() {
		// Mocking objects
		CohortM cohort = mock(CohortM.class);
		Patient currentMemberPatient = mock(Patient.class);
		Patient updatedMemberPatient = mock(Patient.class);
		CohortMember updatedCohortMember = new CohortMember();
		String patientUUID = UUID.randomUUID().toString();
		
		// Setting up existing member
		cohortMember.setCohort(cohort);
		cohortMember.setPatient(currentMemberPatient);
		cohortMember.setEndDate(Date.from(Instant.now()));
		
		// Setting up new member
		updatedCohortMember.setUuid(COHORT_MEMBER_UUID);
		updatedCohortMember.setCohort(cohort);
		updatedCohortMember.setPatient(updatedMemberPatient);
		updatedCohortMember.setEndDate(null);
		
		// Mock behavior
		when(cohort.getVoided()).thenReturn(false);
		when(cohort.getCohortMembers()).thenReturn(Collections.singleton(cohortMember));
		when(currentMemberPatient.getUuid()).thenReturn(patientUUID);
		when(updatedMemberPatient.getUuid()).thenReturn(patientUUID);
		when(cohortMemberService.saveCohortMember(updatedCohortMember)).thenReturn(updatedCohortMember);
		
		// Verify behaviour
		CohortMember newlyCreatedObject = getResource().save(updatedCohortMember);
		assertNotNull(newlyCreatedObject);
		assertNull(newlyCreatedObject.getEndDate());
		assertEquals(updatedCohortMember.getUuid(), newlyCreatedObject.getUuid());
		assertEquals(updatedCohortMember.getEndDate(), newlyCreatedObject.getEndDate());
	}
	
	@Test
	public void shouldGetAllResources() {
		assertThrows(ResourceDoesNotSupportOperationException.class, () -> {
			getResource().getAll(new RequestContext());
		});
	}
	
	@Test
	public void shouldInstantiateNewDelegate() {
		assertThat(getResource().newDelegate(), notNullValue());
	}
	
	@Test
	public void verifyResourceVersion() {
		assertThat(getResource().getResourceVersion(), is("1.8"));
	}
	
	@Test
	public void verifyUri() {
		assertThat(getResource().getUri(getObject()), endsWith("/cohortm/cohortmember/" + COHORT_MEMBER_UUID));
	}
}

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

import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeCrudResource1_9;

@Slf4j
@SuppressWarnings("unused")
@SubResource(parent = CohortResource.class, path = "attribute", supportedClass = CohortAttribute.class, supportedOpenmrsVersions = {
        "1.8 - 9.*" })
public class CohortAttributeResource extends BaseAttributeCrudResource1_9<CohortAttribute, CohortM, CohortResource> {
	
	private final CohortService cohortService;
	
	public CohortAttributeResource() {
		this.cohortService = Context.getRegisteredComponent("cohort.cohortService", CohortService.class);
	}
	
	/**
	 * Sets cohortAttributeType of a given cohortAttribute.
	 *
	 * @param cohortAttribute cohort attribute
	 * @param attributeType cohort attribute type to set
	 */
	@PropertySetter("attributeType")
	public static void setAttributeType(CohortAttribute cohortAttribute, CohortAttributeType attributeType) {
		cohortAttribute.setAttributeType(attributeType);
	}
	
	/**
	 * @param cohortAttribute attribute
	 * @return the parent of the given instance of this subresource
	 */
	@Override
	public CohortM getParent(CohortAttribute cohortAttribute) {
		return cohortAttribute.getCohort();
	}
	
	/**
	 * Sets the parent property on the given instance of this subresource
	 *
	 * @param cohortAttribute cohort attribute
	 * @param cohort parent cohort
	 */
	@Override
	public void setParent(CohortAttribute cohortAttribute, CohortM cohort) {
		cohortAttribute.setCohort(cohort);
	}
	
	/**
	 * Implementations should override this method to return a list of all instances that belong to the
	 * given parent
	 *
	 * @param cohort parent cohort
	 * @param context request context
	 * @throws org.openmrs.module.webservices.rest.web.response.ResponseException e
	 */
	@Override
	public PageableResult doGetAll(CohortM cohort, RequestContext context) throws ResponseException {
		return new NeedsPaging<>(new ArrayList<>(cohort.getActiveAttributes()), context);
	}
	
	@Override
	public CohortAttribute save(CohortAttribute cohortAttribute) {
		return cohortService.saveCohortAttribute(cohortAttribute);
	}
	
	@Override
	protected void delete(CohortAttribute cohortAttribute, String reason, RequestContext request) throws ResponseException {
		cohortService.voidCohortAttribute(cohortAttribute, reason);
	}
	
	@Override
	public void purge(CohortAttribute cohortAttribute, RequestContext request) throws ResponseException {
		cohortService.purgeCohortAttribute(cohortAttribute);
	}
	
	@Override
	public CohortAttribute newDelegate() {
		return new CohortAttribute();
	}
	
	@Override
	public CohortAttribute getByUniqueId(@NotNull String uuid) {
		return cohortService.getCohortAttributeByUuid(uuid);
	}
	
	@Override
	public String getUri(Object instance) {
		log.debug("URI: {}", super.getUri(instance));
		return super.getUri(instance);
	}
}

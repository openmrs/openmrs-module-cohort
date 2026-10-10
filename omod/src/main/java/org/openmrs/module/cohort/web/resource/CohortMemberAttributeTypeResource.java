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

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortMemberAttributeType;
import org.openmrs.module.cohort.api.CohortMemberService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;

@SuppressWarnings("unused")
@Resource(name = RestConstants.VERSION_1 + CohortMainRestController.COHORT_NAMESPACE
        + "/cohort-member-attribute-type", supportedClass = CohortMemberAttributeType.class, supportedOpenmrsVersions = {
                "1.8 - 9.*" })
public class CohortMemberAttributeTypeResource extends BaseAttributeTypeCrudResource1_9<CohortMemberAttributeType> {
	
	private final CohortMemberService cohortMemberService;
	
	public CohortMemberAttributeTypeResource() {
		this.cohortMemberService = Context.getRegisteredComponent("cohort.cohortMemberService", CohortMemberService.class);
	}
	
	@Override
	public CohortMemberAttributeType newDelegate() {
		return new CohortMemberAttributeType();
	}
	
	@Override
	public CohortMemberAttributeType getByUniqueId(String uuid) {
		return cohortMemberService.getCohortMemberAttributeTypeByUuid(uuid);
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<>((List<CohortMemberAttributeType>) cohortMemberService.findAllCohortMemberAttributeTypes(),
		        context);
	}
	
	@Override
	public CohortMemberAttributeType save(CohortMemberAttributeType cohortMemberAttributeType) {
		return cohortMemberService.saveCohortMemberAttributeType(cohortMemberAttributeType);
	}
	
	@Override
	public void delete(String uuid, String reason, RequestContext context) throws ResponseException {
		cohortMemberService.voidCohortMemberAttributeType(getByUniqueId(uuid), reason);
	}
	
	@Override
	public void delete(CohortMemberAttributeType cohortMemberAttributeType, String voidReason, RequestContext requestContext)
	        throws ResponseException {
		cohortMemberService.voidCohortMemberAttributeType(cohortMemberAttributeType, voidReason);
	}
	
	@Override
	public void purge(CohortMemberAttributeType cohortMemberAttributeType, RequestContext requestContext)
	        throws ResponseException {
		cohortMemberService.purgeCohortMemberAttributeType(cohortMemberAttributeType);
	}
}

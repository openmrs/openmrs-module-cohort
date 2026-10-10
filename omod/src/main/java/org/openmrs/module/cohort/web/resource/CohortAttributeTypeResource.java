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

import java.util.ArrayList;

import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;

@SuppressWarnings("unused")
@Resource(name = RestConstants.VERSION_1 + CohortMainRestController.COHORT_NAMESPACE
        + "/cohortattributetype", supportedClass = CohortAttributeType.class, supportedOpenmrsVersions = { "1.9 - 9.*" })
public class CohortAttributeTypeResource extends BaseAttributeTypeCrudResource1_9<CohortAttributeType> {
	
	@Override
	public CohortAttributeType save(CohortAttributeType cohortAttributeType) {
		return Context.getService(CohortService.class).saveCohortAttributeType(cohortAttributeType);
	}
	
	@Override
	public void purge(CohortAttributeType cohortAttributeType, RequestContext context) throws ResponseException {
		Context.getService(CohortService.class).purgeCohortAttributeType(cohortAttributeType);
	}
	
	@Override
	public CohortAttributeType newDelegate() {
		return new CohortAttributeType();
	}
	
	@Override
	public CohortAttributeType getByUniqueId(String uuid) {
		return Context.getService(CohortService.class).getCohortAttributeTypeByUuid(uuid);
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<>(new ArrayList<>(Context.getService(CohortService.class).findAllCohortAttributeTypes()),
		        context);
	}
}

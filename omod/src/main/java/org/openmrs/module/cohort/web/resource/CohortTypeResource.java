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

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortTypeService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@SuppressWarnings("unused")
@Resource(name = RestConstants.VERSION_1 + CohortMainRestController.COHORT_NAMESPACE
        + "/cohorttype", supportedClass = CohortType.class, supportedOpenmrsVersions = { "1.8 - 9.*" })
public class CohortTypeResource extends DataDelegatingCrudResource<CohortType> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (Context.isAuthenticated()) {
			if (rep instanceof DefaultRepresentation) {
				final DelegatingResourceDescription description = new DelegatingResourceDescription();
				description.addProperty("name");
				description.addProperty("description");
				description.addProperty("uuid");
				description.addProperty("display");
				description.addSelfLink();
				description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
				return description;
			} else if (rep instanceof FullRepresentation) {
				final DelegatingResourceDescription description = new DelegatingResourceDescription();
				description.addProperty("name");
				description.addProperty("description");
				description.addProperty("uuid");
				description.addProperty("display");
				description.addProperty("auditInfo");
				description.addSelfLink();
				return description;
			}
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addProperty("description");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		return getCreatableProperties();
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			model.property("name", new StringProperty());
			model.property("description", new StringProperty());
			model.property("uuid", new StringProperty());
			model.property("display", new StringProperty());
		} else if (rep instanceof FullRepresentation) {
			model.property("name", new StringProperty());
			model.property("description", new StringProperty());
			model.property("uuid", new StringProperty());
			model.property("display", new StringProperty());
			model.property("auditInfo", new StringProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl().property("name", new StringProperty()).property("description", new StringProperty());
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	@Override
	public CohortType save(CohortType cohortType) {
		return Context.getService(CohortTypeService.class).saveCohortType(cohortType);
	}
	
	@Override
	protected void delete(CohortType cohortType, String reason, RequestContext request) throws ResponseException {
		Context.getService(CohortTypeService.class).voidCohortType(cohortType, reason);
	}
	
	@Override
	public void purge(CohortType cohortType, RequestContext request) throws ResponseException {
		Context.getService(CohortTypeService.class).purgeCohortType(cohortType);
	}
	
	@Override
	public CohortType newDelegate() {
		return new CohortType();
	}
	
	@Override
	public CohortType getByUniqueId(String uuidOrName) {
		CohortType cohortType = Context.getService(CohortTypeService.class).getCohortTypeByUuid(uuidOrName);
		//If getByUuid is null. Try searching by name
		if (cohortType == null) {
			cohortType = Context.getService(CohortTypeService.class).getCohortTypeByName(uuidOrName);
		}
		return cohortType;
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<>((List<CohortType>) Context.getService(CohortTypeService.class).findAllCohortTypes(),
		        context);
	}
	
	@PropertyGetter("display")
	public static String getDisplay(CohortType cohortType) {
		return cohortType.getName();
	}
}

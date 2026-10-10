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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.cohort.api.CohortTypeService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
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

@Slf4j
@SuppressWarnings("unused")
@Resource(name = RestConstants.VERSION_1 + CohortMainRestController.COHORT_NAMESPACE
        + "/cohort", supportedClass = CohortM.class, supportedOpenmrsVersions = { "1.8 - 9.*" })
public class CohortResource extends DataDelegatingCrudResource<CohortM> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			final DelegatingResourceDescription defaultDescription = getSharedDelegatingResourceDescription();
			defaultDescription.addProperty("uuid");
			defaultDescription.addProperty("location", Representation.REF);
			defaultDescription.addProperty("cohortType", Representation.REF);
			defaultDescription.addProperty("attributes", Representation.REF);
			defaultDescription.addProperty("voided");
			defaultDescription.addProperty("voidReason");
			defaultDescription.addProperty("display");
			defaultDescription.addSelfLink();
			defaultDescription.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return defaultDescription;
		} else if (rep instanceof FullRepresentation) {
			final DelegatingResourceDescription description = getSharedDelegatingResourceDescription();
			description.addProperty("location", Representation.FULL);
			description.addProperty("cohortMembers", Representation.FULL);
			description.addProperty("cohortType", Representation.FULL);
			description.addProperty("attributes", Representation.DEFAULT);
			description.addProperty("voided");
			description.addProperty("voidReason");
			description.addProperty("uuid");
			description.addProperty("auditInfo");
			description.addProperty("display");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	private DelegatingResourceDescription getSharedDelegatingResourceDescription() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("name");
		description.addProperty("description");
		description.addProperty("startDate");
		description.addProperty("endDate");
		description.addProperty("groupCohort");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		addSharedDelegatingResourceProperties(description);
		description.addProperty("voided");
		description.addProperty("groupCohort");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		addSharedDelegatingResourceProperties(description);
		description.addProperty("groupCohort");
		description.addProperty("voided");
		description.addProperty("voidReason");
		return description;
	}
	
	private void addSharedDelegatingResourceProperties(DelegatingResourceDescription description) {
		description.addRequiredProperty("name");
		description.addProperty("description");
		description.addProperty("location");
		description.addProperty("startDate");
		description.addProperty("endDate");
		description.addProperty("cohortType");
		description.addProperty("definitionHandlerClassname");
		description.addProperty("attributes");
		description.addProperty("cohortMembers");
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("name", new StringProperty()).required("name");
		model.property("description", new StringProperty());
		model.property("location", new RefProperty("#/definitions/LocationCreate"));
		model.property("startDate", new DateProperty());
		model.property("endDate", new DateProperty());
		model.property("cohortType", new RefProperty("#/definitions/CohortmCohorttypeCreate"));
		model.property("definitionHandlerClassname", new StringProperty());
		model.property("attributes", new ArrayProperty(new RefProperty("#/definitions/CohortmCohortmemberAttributeCreate")));
		model.addProperty("cohortMembers", new ArrayProperty(new RefProperty("#/definitions/CohortMembershipCreate")));
		model.property("voided", new BooleanProperty());
		model.property("groupCohort", new BooleanProperty());
		return model;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			model.property("name", new StringProperty());
			model.property("description", new StringProperty());
			model.property("startDate", new DateProperty());
			model.property("endDate", new DateProperty());
			model.property("groupCohort", new BooleanProperty());
			model.property("uuid", new StringProperty().example("uuid"));
			model.property("location", new RefProperty("#/definitions/LocationGetRef"));
			model.property("cohortType", new RefProperty("#/definitions/CohortmCohorttypeGetRef"));
			model.property("attributes", new RefProperty("#/definitions/CohortmCohortmemberAttributeGetRef"));
			model.property("voided", new BooleanProperty());
			model.property("voidReason", new StringProperty());
			model.property("display", new StringProperty());
		} else if (rep instanceof FullRepresentation) {
			model.property("name", new StringProperty());
			model.property("description", new StringProperty());
			model.property("startDate", new DateProperty());
			model.property("endDate", new DateProperty());
			model.property("groupCohort", new BooleanProperty());
			model.property("location", new RefProperty("#/definitions/LocationGetFull"));
			model.addProperty("cohortMembers", new ArrayProperty(new RefProperty("#/definitions/CohortMembershipGetFull")));
			model.property("cohortType", new RefProperty("#/definitions/CohortmCohorttypeGetFull"));
			model.property("attributes", new RefProperty("#/definitions/CohortmCohortmemberAttributeGetFull"));
			model.property("voided", new BooleanProperty());
			model.property("voidReason", new StringProperty());
			model.property("display", new StringProperty());
			model.property("auditInfo", new StringProperty());
			model.property("uuid", new StringProperty().example("uuid"));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) getCREATEModel(rep);
		model.property("voidReason", new StringProperty());
		return model;
	}
	
	@Override
	public CohortM save(CohortM cohort) {
		if (cohort.getVoided()) {
			//end memberships if cohort is voided.
			for (CohortMember cohortMember : cohort.getCohortMembers()) {
				cohortMember.setVoided(true);
				cohortMember.setVoidReason("Cohort voided");
				cohortMember.setEndDate(cohort.getEndDate());
			}
		}
		return Context.getService(CohortService.class).saveCohortM(cohort);
	}
	
	@Override
	protected void delete(CohortM cohort, String reason, RequestContext request) throws ResponseException {
		Context.getService(CohortService.class).voidCohortM(cohort, reason);
	}
	
	@Override
	public void purge(CohortM cohort, RequestContext request) throws ResponseException {
		Context.getService(CohortService.class).purgeCohortM(cohort);
	}
	
	@Override
	public CohortM newDelegate() {
		return new CohortM();
	}
	
	@Override
	public CohortM getByUniqueId(String uuid) {
		return Context.getService(CohortService.class).getCohortMByUuid(uuid);
	}
	
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		Collection<CohortM> cohort = Context.getService(CohortService.class).findAll();
		return new NeedsPaging<>(new ArrayList<>(cohort), context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String attributeQuery = context.getParameter("attributes");
		String cohortType = context.getParameter("cohortType");
		String location = context.getParameter("location");
		
		Map<String, String> attributes = null;
		CohortType type = null;
		
		if (StringUtils.isNotBlank(attributeQuery)) {
			try {
				attributes = new ObjectMapper().readValue("{" + attributeQuery + "}",
				    new TypeReference<Map<String, String>>() {
				    
				    });
			}
			catch (Exception e) {
				throw new APIException("Invalid format for parameter 'attributes'", e);
			}
		}
		
		if (StringUtils.isNotBlank(cohortType)) {
			CohortTypeService typeService = Context.getService(CohortTypeService.class);
			type = typeService.getCohortTypeByName(cohortType);
			if (type == null) {
				type = typeService.getCohortTypeByUuid(cohortType);
			}
			
			if (type == null) {
				throw new RuntimeException(
				        "Could not find a Cohort Type matching '" + cohortType + "' either by name or UUID");
			}
		}
		
		CohortService cohortService = Context.getService(CohortService.class);
		
		if (StringUtils.isNotBlank(location)) {
			Collection<CohortM> cohorts = cohortService.findCohortMByLocationUuid(location);
			return new NeedsPaging<>(new ArrayList<>(cohorts), context);
		}
		
		List<CohortM> cohort = cohortService.findMatchingCohortMs(context.getParameter("q"), attributes, type,
		    context.getIncludeAll());
		return new NeedsPaging<>(cohort, context);
		
	}
	
	/**
	 * Gets the active attributes of the cohort
	 */
	@PropertyGetter("attributes")
	public Collection<CohortAttribute> getCohortAttributes(CohortM cohort) {
		return cohort.getActiveAttributes();
	}
	
	/**
	 * Sets the attributes of a cohort.
	 *
	 * @param cohort the cohort whose attributes to set
	 * @param attributes attributes to be set
	 */
	@PropertySetter("attributes")
	public void setAttributes(CohortM cohort, List<CohortAttribute> attributes) {
		if (attributes != null) {
			User authenticatedUser = Context.getAuthenticatedUser();
			Set<CohortAttribute> attributeSet = new HashSet<>(attributes);
			cohort.getActiveAttributes().stream().filter(a -> !attributeSet.contains(a)).forEach(a -> {
				a.setVoided(true);
				a.setVoidReason("Attribute voided by API");
				a.setVoidedBy(authenticatedUser);
			});
			
			attributeSet.forEach(cohort::addAttribute);
		}
	}
	
	@PropertyGetter("display")
	public String getDisplay(CohortM cohort) {
		return cohort.getName();
	}
	
	@PropertyGetter("size")
	public int size(CohortM cohort) {
		return cohort.size();
	}
	
	@PropertyGetter("cohortMembers")
	public Set<CohortMember> getCohortMembers(CohortM cohort) {
		return cohort.getActiveCohortMembers();
	}
	
	@PropertySetter("cohortMembers")
	public void setCohortMembers(CohortM cohort, List<CohortMember> members) {
		if (members != null) {
			Set<CohortMember> memberSet = new HashSet<>(members);
			cohort.removeMemberships(
			    cohort.getActiveCohortMembers().stream().filter(m -> !memberSet.contains(m)).toArray(CohortMember[]::new));
			cohort.addMemberships(members.toArray(new CohortMember[0]));
		}
	}
}

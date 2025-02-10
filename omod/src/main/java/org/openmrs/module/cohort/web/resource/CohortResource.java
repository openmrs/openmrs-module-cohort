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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
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
        + "/cohort", supportedClass = CohortM.class, supportedOpenmrsVersions = { "1.8 - 2.*" })
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
	
	public Schema<?> getCREATESchema(Representation rep) {
		Schema<Object> model = new Schema<>();
		model.addProperty("name", new StringSchema()).addProperty("description", new StringSchema())
		        .addProperty("location", new Schema<>().$ref("#/components/schemas/LocationCreate"))
		        .addProperty("startDate", new DateSchema()).addProperty("endDate", new DateSchema())
		        .addProperty("cohortType", new Schema<CohortType>().$ref("#/components/schemas/CohortmCohorttypeCreate"))
		        .addProperty("definitionHandlerClassname", new StringSchema())
		        .addProperty("attributes",
		            new ArraySchema().items(new Schema<>().$ref("#/components/schemas/CohortmCohortmemberAttributeCreate")))
		        .addProperty("cohortMembers",
		            new ArraySchema().items(new Schema<CohortMember>().$ref("#/components/schemas/CohortMembershipCreate")))
		        .addProperty("voided", new BooleanSchema()).addProperty("groupCohort", new BooleanSchema());
		
		model.setRequired(Collections.singletonList("name"));
		
		return model;
	}
	
	@Override
	public Schema<?> getGETSchema(Representation rep) {
		Schema<?> model = super.getGETSchema(rep);
		if (rep instanceof DefaultRepresentation) {
			model.addProperty("name", new StringSchema()).addProperty("description", new StringSchema())
			        .addProperty("startDate", new DateSchema()).addProperty("endDate", new DateSchema())
			        .addProperty("groupCohort", new BooleanSchema()).addProperty("uuid", new StringSchema().example("uuid"))
			        .addProperty("location", new Schema<Location>().$ref("#/components/schemas/LocationGetRef"))
			        .addProperty("cohortType", new Schema<CohortType>().$ref("#/components/schemas/CohortmCohorttypeGetRef"))
			        .addProperty("attributes",
			            new Schema<>().$ref("#/components/schemas/CohortmCohortmemberAttributeGetRef"))
			        .addProperty("voided", new BooleanSchema()).addProperty("voidReason", new StringSchema())
			        .addProperty("display", new StringSchema());
		} else if (rep instanceof FullRepresentation) {
			model.addProperty("name", new StringSchema()).addProperty("description", new StringSchema())
			        .addProperty("startDate", new DateSchema()).addProperty("endDate", new DateSchema())
			        .addProperty("groupCohort", new BooleanSchema())
			        .addProperty("location", new Schema<Location>().$ref("#/components/schemas/LocationGetFull"))
			        .addProperty("cohortMembers",
			            new ArraySchema().items(new Schema<>().$ref("#/components/schemas/CohortMembershipGetFull")))
			        .addProperty("cohortType",
			            new Schema<CohortType>().$ref("#/components/schemas/CohortmCohorttypeGetFull"))
			        .addProperty("attributes",
			            new Schema<>().$ref("#/components/schemas/CohortmCohortmemberAttributeGetFull"))
			        .addProperty("voided", new BooleanSchema()).addProperty("voidReason", new StringSchema())
			        .addProperty("display", new StringSchema()).addProperty("auditInfo", new StringSchema())
			        .addProperty("uuid", new StringSchema().example("uuid"));
		}
		return model;
	}
	
	@Override
	public Schema<?> getUPDATESchema(Representation rep) {
		Schema<?> model = getCREATESchema(rep);
		model.addProperty("voidReason", new StringSchema());
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
			
			cohort.getActiveAttributes().addAll(attributeSet);
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

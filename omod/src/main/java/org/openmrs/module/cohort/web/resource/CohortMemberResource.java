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

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.api.CohortMemberService;
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
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@SuppressWarnings("unused")
@Resource(name = RestConstants.VERSION_1 + CohortMainRestController.COHORT_NAMESPACE
        + "/cohortmember", supportedClass = CohortMember.class, supportedOpenmrsVersions = { "1.8 - 9.*" })
public class CohortMemberResource extends DataDelegatingCrudResource<CohortMember> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (Context.isAuthenticated()) {
			if (rep instanceof DefaultRepresentation) {
				final DelegatingResourceDescription description = new DelegatingResourceDescription();
				description.addProperty("patient", Representation.REF);
				description.addProperty("startDate");
				description.addProperty("endDate");
				description.addProperty("uuid");
				description.addProperty("voided");
				description.addProperty("attributes", "activeAttributes", Representation.REF);
				description.addProperty("cohort", Representation.REF);
				description.addSelfLink();
				description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
				return description;
			} else if (rep instanceof FullRepresentation) {
				final DelegatingResourceDescription description = new DelegatingResourceDescription();
				description.addProperty("display");
				description.addProperty("startDate");
				description.addProperty("endDate");
				description.addProperty("uuid");
				description.addProperty("voided");
				description.addProperty("patient", Representation.FULL);
				description.addProperty("cohort", Representation.DEFAULT);
				description.addProperty("attributes", "activeAttributes", Representation.DEFAULT);
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
		description.addRequiredProperty("cohort");
		description.addProperty("endDate");
		description.addRequiredProperty("patient");
		description.addRequiredProperty("startDate");
		description.addProperty("voided");
		description.addProperty("attributes");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("startDate");
		description.addProperty("endDate");
		description.addProperty("voided");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl().property("cohort", new RefProperty("#/definitions/CohortCreate"))
		        .property("endDate", new DateProperty()).property("patient", new RefProperty("#/definitions/PatientCreate"))
		        .property("startDate", new DateProperty()).property("voided", new BooleanProperty())
		        .property("attributes", new RefProperty("#/definitions/CohortmCohortmemberAttributeCreate"));
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			model.property("patient", new RefProperty("#/definitions/PatientGetRef"));
			model.property("startDate", new DateProperty());
			model.property("endDate", new DateProperty());
			model.property("uuid", new StringProperty());
			model.property("voided", new BooleanProperty());
		} else if (rep instanceof FullRepresentation) {
			model.property("display", new StringProperty());
			model.property("startDate", new DateProperty());
			model.property("endDate", new DateProperty());
			model.property("uuid", new StringProperty());
			model.property("voided", new BooleanProperty());
			model.property("patient", new RefProperty("#/definitions/PatientGetFull"));
			model.property("auditInfo", new StringProperty());
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getUPDATEModel(rep);
		return model.property("endDate", new DateProperty()).property("startDate", new DateProperty()).property("voided",
		    new BooleanProperty());
	}
	
	@Override
	public CohortMember newDelegate() {
		return new CohortMember();
	}
	
	@Override
	public CohortMember save(CohortMember cohortMember) throws ResponseException {
		CohortM cohort = cohortMember.getCohort();
		Patient newPatient = cohortMember.getPatient();
		if (cohort.getVoided()) {
			throw new RuntimeException("Cannot add patient to ended group.");
		}
		for (CohortMember currentMember : cohort.getCohortMembers()) {
			if (currentMember.getPatient().getUuid().equals(newPatient.getUuid()) && !cohortMember.getVoided()) {
				if (currentMember.getEndDate() == null && cohortMember.getEndDate() == null) {
					throw new RuntimeException("Patient already exists in group.");
				}
				currentMember.setEndDate(cohortMember.getEndDate());
				currentMember.setVoided(false);
				cohortMember = currentMember;
				break;
			}
		}
		return Context.getService(CohortMemberService.class).saveCohortMember(cohortMember);
	}
	
	@Override
	public void delete(CohortMember cohortMember, String reason, RequestContext context) throws ResponseException {
		Context.getService(CohortMemberService.class).voidCohortMember(cohortMember, reason);
	}
	
	@Override
	public CohortMember getByUniqueId(String uuid) {
		return Context.getService(CohortMemberService.class).getCohortMemberByUuid(uuid);
	}
	
	@Override
	public void purge(CohortMember cohortMember, RequestContext context) throws ResponseException {
		Context.getService(CohortMemberService.class).purgeCohortMember(cohortMember);
	}
	
	@PropertySetter("attributes")
	public static void setAttributes(CohortMember cohortMember, Set<CohortMemberAttribute> attributes) {
		for (CohortMemberAttribute attribute : attributes) {
			attribute.setOwner(cohortMember);
		}
		cohortMember.setAttributes(attributes);
	}
	
	@PropertyGetter("display")
	public String getDisplayString(CohortMember cohortMember) {
		Patient patient = cohortMember.getPatient();
		if (patient != null) {
			PatientIdentifier identifier = patient.getPatientIdentifier();
			return identifier + "-" + patient.getPersonName().getFullName();
		}
		
		return null;
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String query = context.getParameter("q");
		String cohortUuid = context.getParameter("cohort");
		String patientUuid = context.getParameter("patient");
		
		if (isNotBlank(cohortUuid) && isNotBlank(query)) {
			Collection<CohortMember> cohortMembers = Context.getService(CohortMemberService.class)
			        .findCohortMembersByCohortAndPatientName(cohortUuid, query);
			return new NeedsPaging<>(new ArrayList<>(cohortMembers), context);
		} else if (isNotBlank(cohortUuid)) {
			return new NeedsPaging<>(
			        new ArrayList<>(Context.getService(CohortMemberService.class).findCohortMembersByCohortUuid(cohortUuid)),
			        context);
		} else if (isNotBlank(patientUuid)) {
			return new NeedsPaging<>(
			        new ArrayList<>(
			                Context.getService(CohortMemberService.class).findCohortMembersByPatientUuid(patientUuid)),
			        context);
		} else {
			throw new InvalidSearchException("No valid value specified for params query(q), cohort and/or patient");
		}
	}
}

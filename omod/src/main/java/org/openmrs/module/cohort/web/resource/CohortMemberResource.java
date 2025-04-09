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

import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;
import org.openmrs.Cohort;
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
        + "/cohortmember", supportedClass = CohortMember.class, supportedOpenmrsVersions = { "1.8 - 2.*" })
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
	public Schema<?> getCREATESchema(Representation rep) {
		return new Schema<>().addProperty("cohort", new Schema<Cohort>().$ref("#/components/schemas/CohortCreate"))
		        .addProperty("endDate", new DateSchema())
		        .addProperty("patient", new Schema<Patient>().$ref("#/components/schemas/PatientCreate"))
		        .addProperty("startDate", new DateSchema()).addProperty("voided", new BooleanSchema())
		        .addProperty("attributes", new Schema<>().$ref("#/components/schemas/CohortmCohortmemberAttributeCreate"));
	}
	
	@Override
	public Schema<?> getGETSchema(Representation rep) {
		Schema<?> model = super.getGETSchema(rep);
		if (rep instanceof DefaultRepresentation) {
			model.addProperty("patient", new Schema<Patient>().$ref("#/components/schemas/PatientGetRef"))
			        .addProperty("startDate", new DateSchema()).addProperty("endDate", new DateSchema())
			        .addProperty("uuid", new StringSchema()).addProperty("voided", new BooleanSchema());
		} else if (rep instanceof FullRepresentation) {
			model.addProperty("display", new StringSchema()).addProperty("startDate", new DateSchema())
			        .addProperty("endDate", new DateSchema()).addProperty("uuid", new UUIDSchema())
			        .addProperty("voided", new BooleanSchema())
			        .addProperty("patient", new Schema<Patient>().$ref("#/components/schemas/PatientGetFull"))
			        .addProperty("auditInfo", new StringSchema());
		}
		return model;
	}
	
	@Override
	public Schema<?> getUPDATESchema(Representation rep) {
		Schema<?> model = super.getUPDATESchema(rep);
		return model.addProperty("endDate", new DateSchema()).addProperty("startDate", new DateSchema())
		        .addProperty("voided", new BooleanSchema());
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

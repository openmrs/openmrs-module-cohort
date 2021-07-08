package org.openmrs.module.cohort.web.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortMemberAttribute;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.cohort.rest.v1_0.resource.CohortRest;
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
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + CohortRest.COHORT_NAMESPACE
		+ "/cohortmember", supportedClass = CohortMember.class, supportedOpenmrsVersions = { "1.8 - 2.*" })
public class CohortMemberRequestResource extends DataDelegatingCrudResource<CohortMember> {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

		DelegatingResourceDescription description = null;

		if (Context.isAuthenticated()) {
			description = new DelegatingResourceDescription();
			if (rep instanceof DefaultRepresentation) {
				description.addProperty("patient", Representation.REF);
				description.addProperty("role");
				description.addProperty("startDate");
				description.addProperty("endDate");
				description.addProperty("head");
				description.addProperty("uuid");
				description.addProperty("voided");
				description.addProperty("attributes", "activeAttributes", Representation.REF);
				description.addProperty("cohort", Representation.REF);
				description.addSelfLink();
			} else if (rep instanceof FullRepresentation) {
				description.addProperty("patient", Representation.FULL);
				description.addProperty("cohort", Representation.REF);
				description.addProperty("role");
				description.addProperty("startDate");
				description.addProperty("endDate");
				description.addProperty("head");
				description.addProperty("uuid");
				description.addProperty("voided");
				description.addProperty("attributes", "activeAttributes", Representation.DEFAULT);
				description.addProperty("auditInfo");
				description.addSelfLink();
			}
		}
		return description;
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("cohort");
		description.addProperty("endDate");
		description.addRequiredProperty("patient");
		description.addProperty("role");
		description.addRequiredProperty("startDate");
		description.addProperty("head");
		description.addProperty("voided");
		description.addProperty("attributes");
		return description;
	}

	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("startDate");
		description.addProperty("endDate");
		description.addProperty("role");
		description.addProperty("head");
		description.addProperty("voided");
		return description;
	}

	@Override
	public CohortMember newDelegate() {
		return new CohortMember();
	}

	@Override
	public CohortMember save(CohortMember cohortMember) throws ResponseException {
		CohortM cohort = cohortMember.getCohort();
		Patient new_patient = cohortMember.getPatient();
		if (cohort.getVoided()) {
			throw new RuntimeException("Cannot add patient to ended group.");
		}
		for (CohortMember member : cohort.getCohortMembers()) {
			if (member.getPatient().getUuid().equals(new_patient.getUuid()) && !cohortMember.getVoided()) {
				if (member.getEndDate() == null) {
					throw new RuntimeException("Patient already exists in group.");
				} else {
					member.setVoided(false);
					member.setEndDate(null);
					cohortMember = member;
				}
			}
		}
		return Context.getService(CohortService.class).saveCohortMember(cohortMember);
	}

	@Override
	public void delete(CohortMember cohortMember, String reason, RequestContext context) throws ResponseException {
		cohortMember.setVoided(true);
		cohortMember.setVoidReason(reason);
		Context.getService(CohortService.class).saveCohortMember(cohortMember);
	}

	@Override
	public CohortMember getByUniqueId(String uuid) {
		return Context.getService(CohortService.class).getCohortMemberByUuid(uuid);
	}

	@Override
	public void purge(CohortMember cohortMember, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException();
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
		String cohort = context.getParameter("cohort");
		String patientUuid = context.getParameter("patient");
		List<CohortMember> list = new ArrayList<CohortMember>();

		if (StringUtils.isNotBlank(cohort) && StringUtils.isNotBlank(patientUuid)) {
			throw new IllegalArgumentException(
					"Patient and Cohort Parameters can't both be declared in the url, search by either cohort or patient, not both");

		} else if (StringUtils.isNotBlank(cohort)) {
			CohortM cohorto = Context.getService(CohortService.class).getCohortByName(cohort);
			if (cohorto == null) {
				// check by uuid
				cohorto = Context.getService(CohortService.class).getCohortByUuid(cohort);
			}
			if (cohorto == null) {
				throw new IllegalArgumentException("No match found in cohort");
			}
			list = Context.getService(CohortService.class).findCohortMembersByCohort(cohorto.getCohortId());

		} else if (StringUtils.isNotBlank(patientUuid)) {
			Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
			if (patient == null) {
				throw new IllegalArgumentException("No patient with uuid " + patientUuid);
			}
			list = Context.getService(CohortService.class).findCohortMembersByPatient(patient.getId());

		} else {
			throw new IllegalArgumentException("No valid value specified for param cohort and/or patient");
		}

		return new NeedsPaging<>(list, context);

	}
}

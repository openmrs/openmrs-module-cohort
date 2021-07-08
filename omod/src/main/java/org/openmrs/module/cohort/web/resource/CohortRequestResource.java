package org.openmrs.module.cohort.web.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortLeader;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;
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
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;
import java.util.Map;

@Resource(name = RestConstants.VERSION_1 + CohortRest.COHORT_NAMESPACE
		+ "/cohort", supportedClass = CohortM.class, supportedOpenmrsVersions = { "1.8 - 2.*" })
public class CohortRequestResource extends DataDelegatingCrudResource<CohortM> {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (Context.isAuthenticated()) {

			DelegatingResourceDescription description = null;

			if (rep instanceof DefaultRepresentation) {
				description = new DelegatingResourceDescription();
				description.addProperty("name");
				description.addProperty("description");
				description.addProperty("location");
				description.addProperty("startDate");
				description.addProperty("endDate");
				description.addProperty("cohortType");
				description.addProperty("cohortProgram");
				description.addProperty("cohortLeaders");
				description.addProperty("cohortVisits");
				description.addProperty("attributes");
				description.addProperty("groupCohort");
				description.addProperty("uuid");
				description.addProperty("voided");
				description.addProperty("voidReason");
				description.addProperty("display");
				description.addSelfLink();
				return description;
			} else if (rep instanceof FullRepresentation) {

				description = new DelegatingResourceDescription();

				description.addProperty("name");
				description.addProperty("description");
				description.addProperty("location", Representation.REF);
				description.addProperty("startDate");
				description.addProperty("endDate");
				description.addProperty("cohortType");
				description.addProperty("cohortProgram");
				description.addProperty("cohortLeaders");
				description.addProperty("attributes");
				description.addProperty("groupCohort");
				description.addProperty("cohortMembers", Representation.FULL);
				description.addProperty("voided");
				description.addProperty("voidReason");
				description.addProperty("cohortVisits");
				description.addProperty("uuid");
				description.addProperty("auditInfo");
				description.addProperty("display");

				description.addSelfLink();

				return description;
			}

			return description;
		}
		return null;
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();

		description.addRequiredProperty("name");
		description.addProperty("description");
		description.addRequiredProperty("location");
		description.addRequiredProperty("startDate");
		description.addProperty("endDate");
		description.addRequiredProperty("cohortType");
		description.addProperty("cohortProgram");
		description.addProperty("attributes");
		description.addProperty("cohortMembers");
		description.addProperty("voided");
		description.addProperty("groupCohort");
		return description;
	}

	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();

		description.addRequiredProperty("name");
		description.addProperty("description");
		description.addRequiredProperty("location");
		description.addRequiredProperty("startDate");
		description.addProperty("endDate");
		description.addRequiredProperty("cohortType");
		description.addProperty("cohortProgram");
		description.addProperty("attributes");
		description.addProperty("cohortMembers");
		description.addProperty("groupCohort");
		description.addProperty("voided");
		description.addProperty("voidReason");
		return description;
	}

	@Override
	public CohortM save(CohortM cohort) {
		if (cohort.getVoided()) {
			for (CohortLeader cohortLeader : cohort.getActiveCohortLeaders()) {
				cohortLeader.setVoided(true);
				cohortLeader.setVoidReason("Cohort Ended");
				cohortLeader.setEndDate(cohort.getEndDate());
			}
			//end memberships if cohort is voided.
			for (CohortMember cohortMember : cohort.getCohortMembers()) {
				cohortMember.setVoided(true);
				cohortMember.setVoidReason("Cohort Ended");
				cohortMember.setEndDate(cohort.getEndDate());
			}
		}
		return Context.getService(CohortService.class).saveCohort(cohort);
	}

	@Override
	protected void delete(CohortM cohort, String reason, RequestContext request) throws ResponseException {
		cohort.setVoided(true);
		cohort.setVoidReason(reason);
		Context.getService(CohortService.class).saveCohort(cohort);
	}

	@Override
	public void purge(CohortM cohort, RequestContext request) throws ResponseException {
		Context.getService(CohortService.class).purgeCohort(cohort);
	}

	@Override
	public CohortM newDelegate() {
		return new CohortM();
	}

	@Override
	public CohortM getByUniqueId(String id) {
		CohortM obj = Context.getService(CohortService.class).getCohortByUuid(id);

		if (obj == null) {
			// TODO add to API
			// obj = Context.getService(CohortService.class).getCohortByName(id);
		}

		return obj;
	}

	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<CohortM> cohort = Context.getService(CohortService.class).getAllCohorts();
		return new NeedsPaging<CohortM>(cohort, context);
	}

	@Override
	protected PageableResult doSearch(RequestContext context) {
		String attributesStr = context.getParameter("attributes");
		String cohortType = context.getParameter("cohortType");
		String location = context.getParameter("location");

		Map<String, String> attributes = null;
		CohortType type = null;

		if (StringUtils.isNotBlank(attributesStr)) {
			try {
				attributes = new ObjectMapper()
						.readValue("{" + attributesStr + "}", new TypeReference<Map<String, String>>() {

						});
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Invalid format for parameter 'attributes'");
			}
		}
		if (StringUtils.isNotBlank(cohortType)) {
			type = Context.getService(CohortService.class).getCohortTypeByName(cohortType);
			if (type == null) {
				type = Context.getService(CohortService.class).getCohortTypeByUuid(cohortType);
			}
			if (type == null) {
				throw new RuntimeException("No Cohort Type By Name/Uuid Found Matching The Supplied Parameter");
			}
		}

		if (StringUtils.isNotBlank(location)) {
			Location cohortLocation = Context.getService(LocationService.class).getLocationByUuid(location);
			if (cohortLocation == null) {
				throw new RuntimeException("No Location found for that uuid");
			} else {
				int locationId = cohortLocation.getLocationId();
				List<CohortM> cohorts = Context.getService(CohortService.class).getCohortsByLocationId(locationId);
				return new NeedsPaging<CohortM>(cohorts, context);
			}
		}

		List<CohortM> cohort = Context.getService(CohortService.class)
				.findCohortsMatching(context.getParameter("q"), attributes, type);
		return new NeedsPaging<CohortM>(cohort, context);

	}

	/**
	 * Sets attributes on the given cohort.
	 *
	 * @param cohort
	 * @param attrs
	 */
	@PropertySetter("attributes")
	public static void setAttributes(CohortM cohort, List<CohortAttribute> attrs) {
		for (CohortAttribute attr : attrs) {
			CohortAttribute existingAttribute = cohort.getAttribute(Context.getService(CohortService.class)
					.getCohortAttributeTypeByUuid(attr.getCohortAttributeType().getUuid()));
			if (existingAttribute != null) {
				if (attr.getValue() == null) {
					cohort.removeAttribute(existingAttribute);
				} else {
					existingAttribute.setValue(attr.getValue());
				}
			} else {
				cohort.addAttribute(attr);
			}
		}
	}

	@PropertyGetter("display")
	public static String getDisplay(CohortM cohort) {
		return cohort.getName();
	}
}

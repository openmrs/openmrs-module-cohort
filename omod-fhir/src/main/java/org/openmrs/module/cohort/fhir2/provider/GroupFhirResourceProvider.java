/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.fhir2.provider;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Patch;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.api.CohortService;
import org.openmrs.module.cohort.fhir2.translator.GroupTranslator;
import org.openmrs.module.fhir2.api.util.JsonPatchUtils;
import org.openmrs.module.fhir2.api.util.XmlPatchUtils;
import org.openmrs.module.fhir2.providers.util.FhirProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FHIR resource provider for {@link Group} resources backed by {@link CohortM}.
 */
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class GroupFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	private CohortService cohortService;
	
	@Autowired
	private GroupTranslator groupTranslator;
	
	private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();
	
	@Override
	public Class<Group> getResourceType() {
		return Group.class;
	}
	
	@Search
	public IBundleProvider searchGroups(@OptionalParam(name = "name") StringParam name) {
		String nameMatch = name != null ? name.getValue() : null;
		List<CohortM> cohorts = cohortService.findMatchingCohortMs(nameMatch, null, null, false);
		List<Group> results = new ArrayList<>(cohorts.size());
		for (CohortM cohort : cohorts) {
			results.add(groupTranslator.toFhirResource(cohort));
		}
		return new SimpleBundleProvider(results);
	}
	
	@Read
	public Group getGroupById(@IdParam @Nonnull IdType id) {
		CohortM cohort = cohortService.getCohortMByUuid(id.getIdPart());
		if (cohort == null) {
			throw new ResourceNotFoundException("Could not find Group with Id " + id.getIdPart());
		}
		return groupTranslator.toFhirResource(cohort);
	}
	
	@Create
	@SuppressWarnings("unused")
	public MethodOutcome createGroup(@ResourceParam Group group) {
		CohortM cohort = groupTranslator.toOpenmrsType(Objects.requireNonNull(group));
		CohortM saved = cohortService.saveCohortM(cohort);
		return FhirProviderUtils.buildCreate(groupTranslator.toFhirResource(saved));
	}
	
	@Update
	@SuppressWarnings("unused")
	public MethodOutcome updateGroup(@IdParam IdType id, @ResourceParam Group group) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to update");
		}
		CohortM existing = cohortService.getCohortMByUuid(id.getIdPart());
		if (existing == null) {
			throw new ResourceNotFoundException("Could not find Group with Id " + id.getIdPart());
		}
		CohortM updated = groupTranslator.toOpenmrsType(existing, Objects.requireNonNull(group));
		CohortM saved = cohortService.saveCohortM(updated);
		return FhirProviderUtils.buildUpdate(groupTranslator.toFhirResource(saved));
	}
	
	@Patch
	@SuppressWarnings("unused")
	public MethodOutcome patchGroup(@IdParam IdType id, PatchTypeEnum patchType, @ResourceParam String body,
	        RequestDetails requestDetails) {
		if (id == null || id.getIdPart() == null) {
			throw new InvalidRequestException("id must be specified to patch");
		}
		if (patchType == null) {
			throw new InvalidRequestException("patch type must be specified");
		}
		if (body == null) {
			throw new InvalidRequestException("patch body must be specified");
		}
		
		CohortM existing = cohortService.getCohortMByUuid(id.getIdPart());
		if (existing == null) {
			throw new ResourceNotFoundException("Could not find Group with Id " + id.getIdPart());
		}
		
		Group existingGroup = groupTranslator.toFhirResource(existing);
		Group patchedGroup;
		switch (patchType) {
			case JSON_PATCH:
				if (isJsonMergePatch(requestDetails)) {
					patchedGroup = JsonPatchUtils.applyJsonMergePatch(FHIR_CONTEXT, existingGroup, body);
				} else {
					patchedGroup = JsonPatchUtils.applyJsonPatch(FHIR_CONTEXT, existingGroup, body);
				}
				break;
			case XML_PATCH:
				patchedGroup = XmlPatchUtils.applyXmlPatch(FHIR_CONTEXT, existingGroup, body);
				break;
			default:
				throw new InvalidRequestException("Unsupported patch type: " + patchType);
		}
		
		CohortM updated = groupTranslator.toOpenmrsType(existing, patchedGroup);
		CohortM saved = cohortService.saveCohortM(updated);
		return FhirProviderUtils.buildPatch(groupTranslator.toFhirResource(saved));
	}
	
	@Delete
	@SuppressWarnings("unused")
	public OperationOutcome deleteGroup(@IdParam @Nonnull IdType id) {
		CohortM cohort = cohortService.getCohortMByUuid(id.getIdPart());
		if (cohort == null) {
			throw new ResourceNotFoundException("Could not find Group with Id " + id.getIdPart());
		}
		cohortService.voidCohortM(cohort, "voided via FHIR request");
		return FhirProviderUtils.buildDeleteR4();
	}
	
	private boolean isJsonMergePatch(RequestDetails requestDetails) {
		if (requestDetails == null) {
			return false;
		}
		
		String contentType = requestDetails.getHeader(Constants.HEADER_CONTENT_TYPE);
		return contentType != null && contentType.equalsIgnoreCase("application/merge-patch+json");
	}
}

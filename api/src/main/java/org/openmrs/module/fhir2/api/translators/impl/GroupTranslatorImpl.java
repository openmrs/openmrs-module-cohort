/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators.impl;

import static org.apache.commons.lang3.Validate.notNull;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getLastUpdated;
import static org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils.getVersionId;

import javax.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupMemberComponent;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortTypeService;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.GroupMemberTranslator;
import org.openmrs.module.fhir2.api.translators.GroupTranslator;
import org.openmrs.module.fhir2.model.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link GroupTranslator}.
 */
@Slf4j
@Primary
@Component("cohortGroupTranslator")
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class GroupTranslatorImpl extends BaseGroupTranslator implements GroupTranslator {
	
	private static final String GROUP_LIST_TYPE_EXTENSION_URL = FhirConstants.OPENMRS_FHIR_EXT_PREFIX + "/group/list-type";
	
	private static final String GROUP_LOCATION_EXTENSION_URL = FhirConstants.OPENMRS_FHIR_EXT_PREFIX + "/group/location";
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private GroupMemberTranslator groupMemberTranslator;
	
	@Autowired
	private CohortTypeService cohortTypeService;
	
	@Autowired
	private LocationService locationService;
	
	@Override
	public Group toFhirResource(@Nonnull CohortM cohort) {
		notNull(cohort, "Cohort object should not be null");
		
		Cohort baseCohort = toBaseCohort(cohort);
		Group group = super.toFhirResource(baseCohort);
		group.setType(GroupType.PERSON);
		
		addListTypeExtension(group, cohort.getCohortType());
		addLocationExtension(group, cohort.getLocation());
		
		Set<CohortMember> members = cohort.getCohortMembers();
		group.setQuantity(members.size());
		for (CohortMember member : members) {
			if (member.getPatient() != null) {
				GroupMember groupMember = groupMemberTranslator.toFhirResource(member.getPatient().getId());
				GroupMemberComponent component = new GroupMemberComponent();
				component.setEntity(groupMember.getEntity());
				if (member.getStartDate() != null || member.getEndDate() != null) {
					Period period = new Period();
					period.setStart(member.getStartDate());
					period.setEnd(member.getEndDate());
					component.setPeriod(period);
				}
				component.setInactive(member.getVoided());
				group.addMember(component);
			}
		}
		
		group.getMeta().setLastUpdated(getLastUpdated(cohort));
		group.getMeta().setVersionId(getVersionId(cohort));
		
		return group;
	}
	
	@Override
	public CohortM toOpenmrsType(@Nonnull Group group) {
		notNull(group, "Group resource should not be null");
		return toOpenmrsType(new CohortM(), group);
	}
	
	@Override
	public CohortM toOpenmrsType(@Nonnull CohortM existing, @Nonnull Group group) {
		notNull(group, "Group resource should not be null");
		notNull(existing, "Existing cohort should not be null");
		
		Cohort base = super.toOpenmrsType(toBaseCohort(existing), group);
		applyBaseFields(existing, base);
		applyListType(existing, group);
		applyLocation(existing, group);
		
		existing.getCohortMembers().clear();
		if (group.hasMember()) {
			Set<CohortMember> members = new HashSet<>();
			for (GroupMemberComponent memberComponent : group.getMember()) {
				GroupMember groupMember = new GroupMember(memberComponent.getEntity());
				Integer patientId = groupMemberTranslator.toOpenmrsType(groupMember);
				Patient patient = patientService.getPatient(patientId);
				if (patient != null) {
					CohortMember cohortMember = new CohortMember(patient);
					cohortMember.setCohort(existing);
					if (memberComponent.hasPeriod()) {
						Period period = memberComponent.getPeriod();
						cohortMember.setStartDate(period.getStart());
						cohortMember.setEndDate(period.getEnd());
					}
					if (memberComponent.hasInactive()) {
						cohortMember.setVoided(memberComponent.getInactive());
					}
					members.add(cohortMember);
				}
			}
			existing.getCohortMembers().addAll(members);
		}
		return existing;
	}
	
	private Cohort toBaseCohort(CohortM cohort) {
		Cohort base = new Cohort();
		base.setUuid(cohort.getUuid());
		base.setName(cohort.getName());
		base.setDescription(cohort.getDescription());
		base.setVoided(cohort.getVoided());
		base.setCreator(cohort.getCreator());
		return base;
	}
	
	private void applyBaseFields(CohortM target, Cohort base) {
		target.setUuid(base.getUuid());
		target.setName(base.getName());
		target.setDescription(base.getDescription());
		target.setVoided(base.getVoided());
		target.setCreator(base.getCreator());
	}
	
	private void addListTypeExtension(Group group, CohortType cohortType) {
		if (cohortType == null || (cohortType.getUuid() == null && StringUtils.isBlank(cohortType.getName()))) {
			return;
		}
		
		CodeableConcept listTypeConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setSystem(GROUP_LIST_TYPE_EXTENSION_URL);
		if (StringUtils.isNotBlank(cohortType.getUuid())) {
			coding.setCode(cohortType.getUuid());
		}
		if (StringUtils.isNotBlank(cohortType.getName())) {
			coding.setDisplay(cohortType.getName());
			listTypeConcept.setText(cohortType.getName());
		}
		listTypeConcept.addCoding(coding);
		
		group.addExtension(new Extension(GROUP_LIST_TYPE_EXTENSION_URL, listTypeConcept));
	}
	
	private void addLocationExtension(Group group, Location location) {
		if (location == null || StringUtils.isBlank(location.getUuid())) {
			return;
		}
		
		Reference reference = new Reference();
		reference.setReference("Location/" + location.getUuid());
		if (StringUtils.isNotBlank(location.getName())) {
			reference.setDisplay(location.getName());
		}
		
		group.addExtension(new Extension(GROUP_LOCATION_EXTENSION_URL, reference));
	}
	
	private void applyListType(CohortM existing, Group group) {
		Extension extension = group.getExtensionByUrl(GROUP_LIST_TYPE_EXTENSION_URL);
		if (extension == null || !(extension.getValue() instanceof CodeableConcept)) {
			return;
		}
		
		CohortType cohortType = resolveCohortType((CodeableConcept) extension.getValue());
		if (cohortType != null) {
			existing.setCohortType(cohortType);
		}
	}
	
	private CohortType resolveCohortType(CodeableConcept concept) {
		if (concept == null) {
			return null;
		}
		
		CohortType cohortType = null;
		if (concept.hasCoding()) {
			for (Coding coding : concept.getCoding()) {
				if (StringUtils.isNotBlank(coding.getCode())) {
					cohortType = cohortTypeService.getCohortTypeByUuid(coding.getCode());
				}
				if (cohortType == null && StringUtils.isNotBlank(coding.getDisplay())) {
					cohortType = cohortTypeService.getCohortTypeByName(coding.getDisplay());
				}
				if (cohortType != null) {
					return cohortType;
				}
			}
		}
		
		if (cohortType == null && concept.hasText() && StringUtils.isNotBlank(concept.getText())) {
			cohortType = cohortTypeService.getCohortTypeByName(concept.getText());
		}
		
		return cohortType;
	}
	
	private void applyLocation(CohortM existing, Group group) {
		Extension extension = group.getExtensionByUrl(GROUP_LOCATION_EXTENSION_URL);
		if (extension == null || !(extension.getValue() instanceof Reference)) {
			return;
		}
		
		Location location = resolveLocation((Reference) extension.getValue());
		if (location != null) {
			existing.setLocation(location);
		}
	}
	
	private Location resolveLocation(Reference reference) {
		if (reference == null) {
			return null;
		}
		
		Location location = null;
		if (reference.hasReference()) {
			String uuid = reference.getReferenceElement().getIdPart();
			if (StringUtils.isNotBlank(uuid)) {
				location = locationService.getLocationByUuid(uuid);
			}
		}
		
		if (location == null && reference.hasDisplay() && StringUtils.isNotBlank(reference.getDisplay())) {
			location = locationService.getLocation(reference.getDisplay());
		}
		
		return location;
	}
}

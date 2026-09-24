/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.fhir2.translator;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupMemberComponent;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.api.CohortTypeService;
import org.openmrs.module.cohort.definition.CohortDefinitionHandler;
import org.openmrs.module.cohort.definition.ManualCohortDefinitionHandler;
import org.openmrs.module.cohort.exceptions.ManualChangeNotSupportedException;
import org.openmrs.module.cohort.fhir2.util.CohortFhirUtils;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.LocationReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link GroupTranslator} that keeps cohort membership changes aligned
 * with cohort semantics.
 */
@Slf4j
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
public class GroupTranslatorImpl implements GroupTranslator {
	
	static final String COHORT_TYPE_EXTENSION_URL = FhirConstants.OPENMRS_FHIR_EXT_PREFIX + "/group/cohort-type";
	
	static final String GROUP_LOCATION_EXTENSION_URL = FhirConstants.OPENMRS_FHIR_EXT_PREFIX + "/group/location";
	
	static final String GROUP_DESCRIPTION_EXTENSION_URL = FhirConstants.OPENMRS_FHIR_EXT_PREFIX + "/group/description";
	
	@Autowired
	private CohortTypeService cohortTypeService;
	
	@Autowired
	private LocationReferenceTranslator locationReferenceTranslator;
	
	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;
	
	@Override
	public Group toFhirResource(@Nonnull CohortM cohort) {
		Objects.requireNonNull(cohort, "Cohort object should not be null");
		ensureManualCohort(cohort);
		
		Group group = new Group();
		if (isNotBlank(cohort.getUuid())) {
			group.setId(new IdType(Group.class.getSimpleName(), cohort.getUuid()));
		}
		
		group.setActive(!Boolean.TRUE.equals(cohort.getVoided()));
		group.setActual(true);
		group.setType(GroupType.PERSON);
		group.setName(cohort.getName());
		long activeCount = cohort.getCohortMembers().stream().filter(Objects::nonNull)
		        .filter(member -> member.getEndDate() == null).count();
		group.setQuantity(Math.toIntExact(activeCount));
		
		Date lastUpdated = cohort.getDateChanged() != null ? cohort.getDateChanged() : cohort.getDateCreated();
		if (lastUpdated != null) {
			group.getMeta().setLastUpdated(lastUpdated);
			group.getMeta().setVersionId(String.valueOf(lastUpdated.getTime()));
		}
		
		addTypeExtension(group, cohort.getCohortType());
		addLocationExtension(group, cohort.getLocation());
		addDescriptionExtension(group, cohort.getDescription());
		
		for (CohortMember member : cohort.getCohortMembers()) {
			if (member.getPatient() == null) {
				continue;
			}
			
			GroupMemberComponent component = new GroupMemberComponent();
			component.setEntity(patientReferenceTranslator.toFhirResource(member.getPatient()));
			
			Period period = buildPeriod(member.getStartDate(), member.getEndDate());
			if (period.hasStart() || period.hasEnd()) {
				component.setPeriod(period);
			}
			
			component.setInactive(member.getEndDate() != null);
			group.addMember(component);
		}
		
		return group;
	}
	
	@Override
	public CohortM toOpenmrsType(@Nonnull Group group) {
		Objects.requireNonNull(group, "Group resource should not be null");
		
		CohortM cohort = new CohortM();
		if (group.hasIdElement() && isNotBlank(group.getIdElement().getIdPart())) {
			cohort.setUuid(group.getIdElement().getIdPart());
		}
		
		return toOpenmrsType(cohort, group);
	}
	
	@Override
	public CohortM toOpenmrsType(@Nonnull CohortM existing, @Nonnull Group group) {
		Objects.requireNonNull(group, "Group resource should not be null");
		Objects.requireNonNull(existing, "Existing cohort should not be null");
		
		ensureManualCohort(existing);
		
		if (group.hasIdElement() && isNotBlank(group.getIdElement().getIdPart())) {
			existing.setUuid(group.getIdElement().getIdPart());
		}
		existing.setName(group.getName());
		if (group.hasActive()) {
			existing.setVoided(!group.getActive());
		}
		
		applyType(existing, group.getExtensionByUrl(COHORT_TYPE_EXTENSION_URL));
		applyLocation(existing, group.getExtensionByUrl(GROUP_LOCATION_EXTENSION_URL));
		applyDescription(existing, group.getExtensionByUrl(GROUP_DESCRIPTION_EXTENSION_URL));
		
		updateMembership(existing, group.getMember() != null ? group.getMember() : emptyList());
		
		return existing;
	}
	
	private void addTypeExtension(Group group, CohortType cohortType) {
		if (cohortType == null) {
			return;
		}
		
		CodeableConcept concept = new CodeableConcept();
		Coding coding = concept.addCoding();
		coding.setSystem(COHORT_TYPE_EXTENSION_URL);
		if (isNotBlank(cohortType.getUuid())) {
			coding.setCode(cohortType.getUuid());
		}
		
		String display = CohortFhirUtils.getDataTranslation(cohortType);
		if (isBlank(display)) {
			display = cohortType.getName();
		}
		
		if (isNotBlank(display)) {
			coding.setDisplay(display);
			concept.setText(display);
		}
		
		group.addExtension(new Extension(COHORT_TYPE_EXTENSION_URL, concept));
	}
	
	private void addLocationExtension(Group group, Location location) {
		if (location == null) {
			return;
		}
		
		Reference reference = locationReferenceTranslator.toFhirResource(location);
		group.addExtension(new Extension(GROUP_LOCATION_EXTENSION_URL, reference));
	}
	
	private void addDescriptionExtension(Group group, String description) {
		if (isNotBlank(description)) {
			group.addExtension(new Extension(GROUP_DESCRIPTION_EXTENSION_URL, new StringType(description)));
		}
	}
	
	private void applyType(CohortM existing, Extension extension) {
		if (extension == null || !(extension.getValue() instanceof CodeableConcept)) {
			return;
		}
		
		CodeableConcept concept = (CodeableConcept) extension.getValue();
		if (concept == null || concept.getCoding().isEmpty()) {
			return;
		}
		
		for (Coding coding : concept.getCoding()) {
			if (isNotBlank(coding.getCode())) {
				CohortType type = cohortTypeService.getCohortTypeByUuid(coding.getCode());
				if (type != null) {
					existing.setCohortType(type);
					return;
				}
			}
		}
	}
	
	private void applyLocation(CohortM existing, Extension extension) {
		if (extension == null || !(extension.getValue() instanceof Reference)) {
			return;
		}
		
		Reference reference = (Reference) extension.getValue();
		Location translated = locationReferenceTranslator.toOpenmrsType(reference);
		if (translated != null) {
			existing.setLocation(translated);
		}
	}
	
	private void applyDescription(CohortM existing, Extension extension) {
		if (extension == null || !(extension.getValue() instanceof StringType)) {
			existing.setDescription(null);
			return;
		}
		
		existing.setDescription(((StringType) extension.getValue()).getValue());
	}
	
	private void updateMembership(CohortM existing, List<GroupMemberComponent> members) {
		Map<Integer, CohortMember> currentMembers = existing.getCohortMembers().stream()
		        .filter(cm -> cm.getPatient() != null && cm.getPatient().getId() != null)
		        .collect(toMap(cm -> cm.getPatient().getId(), cm -> cm, (left, right) -> left, HashMap::new));
		
		List<CohortMember> toAdd = new ArrayList<>();
		List<CohortMember> toRemove = new ArrayList<>();
		
		for (GroupMemberComponent memberComponent : members) {
			Reference entity = memberComponent.getEntity();
			if (entity == null) {
				continue;
			}
			
			Patient patient = patientReferenceTranslator.toOpenmrsType(entity);
			if (patient == null || patient.getId() == null) {
				continue;
			}
			
			CohortMember existingMember = currentMembers.remove(patient.getId());
			boolean inactive = resolveInactive(memberComponent);
			Period period = memberComponent.getPeriod();
			Date start = period != null ? period.getStart() : null;
			Date end = period != null ? period.getEnd() : null;
			
			if (existingMember == null) {
				if (inactive) {
					continue;
				}
				
				CohortMember newMember = new CohortMember(patient);
				newMember.setCohort(existing);
				newMember.setStartDate(start);
				newMember.setEndDate(end);
				toAdd.add(newMember);
			} else {
				if (start != null) {
					existingMember.setStartDate(start);
				}
				
				if (!inactive) {
					existingMember.setEndDate(end);
					existingMember.setVoided(false);
				} else {
					existingMember.setEndDate(end != null ? end : existingMember.getEndDate());
					toRemove.add(existingMember);
				}
			}
		}
		
		// Any members not referenced should be ended
		for (CohortMember member : currentMembers.values()) {
			toRemove.add(member);
		}
		
		if (!toAdd.isEmpty()) {
			existing.addMemberships(toAdd.toArray(new CohortMember[0]));
		}
		
		if (!toRemove.isEmpty()) {
			for (CohortMember member : toRemove) {
				if (member.getEndDate() == null) {
					member.setEndDate(new Date());
				}
			}
			existing.removeMemberships(toRemove.toArray(new CohortMember[0]));
		}
	}
	
	private Period buildPeriod(Date start, Date end) {
		Period period = new Period();
		if (start != null) {
			period.setStart(start);
		}
		if (end != null) {
			period.setEnd(end);
		}
		return period;
	}
	
	private boolean resolveInactive(GroupMemberComponent memberComponent) {
		if (memberComponent == null) {
			return false;
		}
		
		if (memberComponent.hasInactive()) {
			return memberComponent.getInactive();
		}
		
		return memberComponent.hasPeriod() && memberComponent.getPeriod().hasEnd();
	}
	
	private void ensureManualCohort(CohortM cohort) {
		CohortDefinitionHandler handler = cohort.getDefinitionHandler();
		if (!(handler instanceof ManualCohortDefinitionHandler)) {
			throw new ManualChangeNotSupportedException("Manual changes to this cohort aren't supported");
		}
	}
}

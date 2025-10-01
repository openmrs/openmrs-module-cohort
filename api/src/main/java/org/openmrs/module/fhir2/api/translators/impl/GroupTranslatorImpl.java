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
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Group.GroupMemberComponent;
import org.hl7.fhir.r4.model.Group.GroupType;
import org.hl7.fhir.r4.model.Period;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
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
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private GroupMemberTranslator groupMemberTranslator;
	
	@Override
	public Group toFhirResource(@Nonnull CohortM cohort) {
		notNull(cohort, "Cohort object should not be null");
		
		Cohort baseCohort = toBaseCohort(cohort);
		Group group = super.toFhirResource(baseCohort);
		group.setType(GroupType.PERSON);
		
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
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.definition.handler;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.definition.ManualCohortDefinitionHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultCohortDefinitionHandler implements ManualCohortDefinitionHandler {
	
	@Override
	public void addMembers(CohortM cohort, CohortMember... cohortMembers) {
		if (cohortMembers.length == 0) {
			return;
		}
		
		final Date now = new Date();
		Set<Patient> activePatients = getActivePatients(cohort);
		Arrays.stream(cohortMembers).filter(Objects::nonNull).filter(cm -> !activePatients.contains(cm.getPatient()))
		        .forEach(cm -> {
			        cm.setCohort(cohort);
			        cohort.getCohortMembers().add(cm);
			        
			        if (!cm.getVoided() && (cm.getStartDate() == null || cm.getStartDate().before(now))
			                && (cm.getEndDate() == null || cm.getEndDate().after(now))) {
				        cohort.getActiveCohortMembers().add(cm);
			        }
		        });
	}
	
	@Override
	public void removeMembers(CohortM cohort, CohortMember... cohortMembers) {
		if (cohortMembers.length == 0) {
			return;
		}
		
		// voided in memory, as addMembers adds, so the removal is saved with the rest of the cohort rather than committed
		// on its own before the cohort has been validated
		final Date now = new Date();
		User authenticatedUser = Context.getAuthenticatedUser();
		Set<Patient> activePatients = getActivePatients(cohort);
		Arrays.stream(cohortMembers).filter(Objects::nonNull).filter(cm -> activePatients.contains(cm.getPatient()))
		        .forEach(cm -> {
			        cm.setVoided(true);
			        cm.setVoidReason("Cohort member removed");
			        cm.setVoidedBy(authenticatedUser);
			        cm.setDateVoided(now);
			        
			        cohort.getActiveCohortMembers().remove(cm);
		        });
	}
	
	private Set<Patient> getActivePatients(CohortM cohort) {
		return cohort.getActiveCohortMembers().stream().map(CohortMember::getPatient).collect(Collectors.toSet());
	}
}

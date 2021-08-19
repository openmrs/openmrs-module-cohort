/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.validator;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.api.CohortService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@Qualifier("addCohortValidator")
public class AddCohortValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(CohortM.class);
	}
	
	@Override
	public void validate(Object command, Errors errors) {
		CohortService service = Context.getService(CohortService.class);
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "Cohort Name Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "Cohort Description Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDate", "Cohort Start Date Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endDate", "Cohort End Date Required");
		
		CohortM cohort = (CohortM) command;
		if (cohort.getStartDate().compareTo(cohort.getEndDate()) > 0) {
			errors.rejectValue("startDate", "Start date should be less than End date");
		}
		
		// TODO change it to find by name and then reject
		List<CohortM> allCohorts = service.getAllCohorts();
		for (CohortM checkCohort : allCohorts) {
			if (checkCohort.getName().equals(cohort.getName())) {
				errors.rejectValue("name", "A cohort with this name already exists");
			}
		}
	}
}

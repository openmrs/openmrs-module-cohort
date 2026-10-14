/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.definition;

import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;

public interface ManualCohortDefinitionHandler extends CohortDefinitionHandler {
	
	@Override
	default void update(CohortM cohort) {
		// Manual cohorts don't generally need to do anything to update themselves
	}
	
	void addMembers(CohortM cohort, CohortMember... cohortMembers);
	
	/**
	 * Removes the given members from the cohort. The change is made in memory and saved with the cohort
	 * by {@link org.openmrs.module.cohort.api.CohortService#saveCohortM(CohortM)}.
	 *
	 * @param cohort the cohort to remove the members from
	 * @param cohortMembers the members to remove
	 */
	void removeMembers(CohortM cohort, CohortMember... cohortMembers);
}

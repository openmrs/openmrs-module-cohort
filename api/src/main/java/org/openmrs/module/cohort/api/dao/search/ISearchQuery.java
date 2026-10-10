/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.api.dao.search;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;

/**
 * ISearchQuery interface
 */
public interface ISearchQuery {
	
	//Add cohort search methods
	List<CohortM> findCohorts(String nameMatching, Map<String, String> attributes, CohortType cohortType,
	        boolean includeVoided);
	
	Collection<CohortMember> findCohortMembersByPatientNames(@NotNull String name);
	
	Collection<CohortMember> findCohortMembersByCohortAndPatient(@NotNull String cohortUuid, @NotNull String query);
}

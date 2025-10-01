/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.translators;

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Group;
import org.openmrs.module.cohort.CohortM;

/**
 * Translates between {@link CohortM} and FHIR {@link Group} resources.
 */
public interface GroupTranslator extends OpenmrsFhirUpdatableTranslator<CohortM, Group> {
	
	/**
	 * Converts an OpenMRS {@link CohortM} to a FHIR {@link Group} resource.
	 *
	 * @param cohort the cohort to convert
	 * @return the corresponding FHIR Group
	 */
	@Override
	Group toFhirResource(@Nonnull CohortM cohort);
	
	/**
	 * Creates a new {@link CohortM} from the given FHIR {@link Group} resource.
	 *
	 * @param group the FHIR group
	 * @return the new cohort instance
	 */
	@Override
	CohortM toOpenmrsType(@Nonnull Group group);
	
	/**
	 * Updates an existing {@link CohortM} with the values from the given FHIR {@link Group} resource.
	 *
	 * @param existing the cohort to update
	 * @param group the FHIR group providing the new values
	 * @return the updated cohort
	 */
	@Override
	CohortM toOpenmrsType(@Nonnull CohortM existing, @Nonnull Group group);
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.fhir2.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.context.Context;

@Slf4j
public final class CohortFhirUtils {
	
	private CohortFhirUtils() {
	}
	
	/**
	 * Retrieves a localized display string for the provided cohort data element. If no translation
	 * exists, falls back to the object's inherent name when available.
	 *
	 * @param data the OpenMRS data object to translate
	 * @return a localized display string or {@code null} when none can be resolved
	 */
	public static String getDataTranslation(BaseOpenmrsData data) {
		if (data == null) {
			return null;
		}
		
		String uuid = data.getUuid();
		if (StringUtils.isBlank(uuid)) {
			return fallbackName(data);
		}
		
		String localization = getLocalization(data.getClass().getSimpleName(), uuid);
		
		if (StringUtils.isNotBlank(localization)) {
			return localization;
		}
		
		return fallbackName(data);
	}
	
	private static String fallbackName(BaseOpenmrsData data) {
		if (data instanceof OpenmrsMetadata) {
			return ((OpenmrsMetadata) data).getName();
		}
		
		return null;
	}
	
	private static String getLocalization(String shortClassName, String uuid) {
		int underscoreIndex = shortClassName.indexOf("_$");
		if (underscoreIndex > 0) {
			shortClassName = shortClassName.substring(0, underscoreIndex);
		}
		
		String code = String.format("ui.i18n.%s.name.%s", shortClassName, uuid);
		
		try {
			String localization = Context.getMessageSourceService().getMessage(code, null, Context.getLocale());
			if (StringUtils.isNotBlank(localization) && !StringUtils.equals(code, localization)) {
				return localization;
			}
		}
		catch (Exception e) {
			log.info("Caught exception while attempting to localize code [{}]", code, e);
		}
		
		return null;
	}
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.api.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Collection;
import java.util.function.BiFunction;

import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.cohort.api.dao.search.ISearchQuery;
import org.openmrs.module.cohort.api.dao.search.PropValue;

public interface GenericDao<W extends OpenmrsObject & Auditable> {
	
	ISearchQuery getSearchHandler();
	
	W get(final int id);
	
	W get(final String uuid);
	
	W get(final String uuid, boolean includeDeleted);
	
	W createOrUpdate(W object);
	
	void delete(W object);
	
	void delete(String uuid);
	
	Collection<W> findAll();
	
	Collection<W> findAll(boolean includeRetired);
	
	Collection<W> findBy(PropValue propValue);
	
	Collection<W> findBy(PropValue propValue, boolean includeRetired);
	
	W findByUniqueProp(PropValue propValue);
	
	W findByUniqueProp(PropValue propValue, boolean includeRetired);
	
	Collection<W> findByOr(BiFunction<CriteriaBuilder, Root<W>, Predicate>... predicates);
	
	Collection<W> findByAnd(BiFunction<CriteriaBuilder, Root<W>, Predicate>... predicates);
}

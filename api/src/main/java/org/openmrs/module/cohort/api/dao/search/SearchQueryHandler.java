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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortType;
import org.springframework.stereotype.Component;

@SuppressWarnings("unchecked")
@Component(value = "cohort.search.cohortSearchHandler")
public class SearchQueryHandler extends AbstractSearchHandler implements ISearchQuery {
	
	public List<CohortM> findCohorts(String nameMatching, Map<String, String> attributes, CohortType cohortType,
	        boolean includeVoided) {
		CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CohortM> cq = cb.createQuery(CohortM.class);
		Root<CohortM> root = cq.from(CohortM.class);
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("voided"), includeVoided));
		
		if (StringUtils.isNotBlank(nameMatching)) {
			predicates.add(cb.like(cb.lower(root.get("name")), "%" + nameMatching.toLowerCase() + "%"));
		}
		
		if (attributes != null && !attributes.isEmpty()) {
			Join<CohortM, ?> attribute = root.join("attributes");
			// "attributeType" resolves to the AttributeType interface through the generic BaseCustomizableData mapping,
			// so join the concrete entity explicitly to reach its name
			Root<CohortAttributeType> attrType = cq.from(CohortAttributeType.class);
			predicates.add(cb.equal(attribute.get("attributeType"), attrType));
			predicates.add(cb.equal(attribute.get("voided"), false));
			
			List<Predicate> disjunction = new ArrayList<>();
			for (String attributeName : attributes.keySet()) {
				disjunction.add(cb.and(cb.equal(attrType.get("name"), attributeName),
				    cb.like(attribute.get("valueReference"), "%" + attributes.get(attributeName) + "%")));
			}
			
			predicates.add(cb.or(disjunction.toArray(new Predicate[0])));
		}
		
		if (cohortType != null) {
			predicates.add(cb.equal(root.get("cohortType").get("cohortTypeId"), cohortType.getCohortTypeId()));
		}
		
		cq.select(root).distinct(true).where(predicates.toArray(new Predicate[0]));
		
		return getCurrentSession().createQuery(cq).getResultList();
	}
	
	@Override
	public Collection<CohortMember> findCohortMembersByPatientNames(String name) {
		CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CohortMember> cq = cb.createQuery(CohortMember.class);
		Root<CohortMember> root = cq.from(CohortMember.class);
		cq.where(handleNames(cb, root.join("patient"), name));
		return getCurrentSession().createQuery(cq).getResultList();
	}
	
	@Override
	public Collection<CohortMember> findCohortMembersByCohortAndPatient(String cohortUuid, String query) {
		CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<CohortMember> cq = cb.createQuery(CohortMember.class);
		Root<CohortMember> root = cq.from(CohortMember.class);
		cq.where(cb.equal(root.join("cohort").get("uuid"), cohortUuid), handleNames(cb, root.join("patient"), query));
		
		return getCurrentSession().createQuery(cq).getResultList();
	}
}

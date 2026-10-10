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
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.cohort.api.dao.search.PropValue;
import org.openmrs.module.cohort.api.dao.search.SearchQueryHandler;

@Slf4j
@SuppressWarnings("unchecked")
@Getter
@Setter
public abstract class AbstractGenericDao<W extends OpenmrsObject & Auditable> implements GenericDao<W> {
	
	private final SessionFactory sessionFactory;
	
	private final SearchQueryHandler searchHandler;
	
	private final Class<W> clazz;
	
	public AbstractGenericDao(SessionFactory sessionFactory, SearchQueryHandler searchHandler) {
		this.sessionFactory = sessionFactory;
		this.searchHandler = searchHandler;
		
		clazz = (Class<W>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public W get(int id) {
		return (W) getCurrentSession().get(clazz, id);
	}
	
	@Override
	public W get(String uuid) {
		return get(uuid, false);
	}
	
	@Override
	public W get(String uuid, boolean includeVoided) {
		CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<W> cq = cb.createQuery(clazz);
		Root<W> root = cq.from(clazz);
		List<Predicate> predicates = new ArrayList<>();
		includeDeletedObjects(cb, root, predicates, includeVoided);
		predicates.add(cb.equal(root.get("uuid"), uuid));
		cq.where(predicates.toArray(new Predicate[0]));
		return getCurrentSession().createQuery(cq).uniqueResult();
	}
	
	@Override
	public Collection<W> findAll() {
		return this.findAll(false);
	}
	
	@Override
	public Collection<W> findAll(boolean includeRetired) {
		CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<W> cq = cb.createQuery(clazz);
		Root<W> root = cq.from(clazz);
		List<Predicate> predicates = new ArrayList<>();
		includeDeletedObjects(cb, root, predicates, includeRetired);
		cq.where(predicates.toArray(new Predicate[0]));
		return getCurrentSession().createQuery(cq).getResultList();
	}
	
	@Override
	public W createOrUpdate(W entity) {
		return HibernateUtil.saveOrUpdate(getCurrentSession(), entity);
	}
	
	@Override
	public void delete(W entity) {
		Session session = getCurrentSession();
		session.remove(session.contains(entity) ? entity : session.merge(entity));
	}
	
	@Override
	public void delete(String uuid) {
		this.delete(this.get(uuid));
	}
	
	/**
	 * By default, retired/voided objects are excluded for searches
	 * 
	 * @param propValue Property and value
	 * @return A Collection of W objects
	 */
	@Override
	public Collection<W> findBy(PropValue propValue) {
		return this.findBy(propValue, false);
	}
	
	@Override
	public Collection<W> findBy(PropValue propValue, boolean includeRetired) {
		return getCurrentSession().createQuery(createPropValueQuery(propValue, includeRetired)).getResultList();
	}
	
	@Override
	public W findByUniqueProp(PropValue propValue) {
		return this.findByUniqueProp(propValue, false);
	}
	
	@Override
	public W findByUniqueProp(PropValue propValue, boolean includeRetired) {
		return getCurrentSession().createQuery(createPropValueQuery(propValue, includeRetired)).uniqueResult();
	}
	
	/**
	 * Builds a query matching {@code propValue}, inner joining its association path (if any) like the
	 * former {@code Criteria.createCriteria(associationPath, alias)} did
	 */
	private CriteriaQuery<W> createPropValueQuery(PropValue propValue, boolean includeRetired) {
		CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<W> cq = cb.createQuery(clazz);
		Root<W> root = cq.from(clazz);
		List<Predicate> predicates = new ArrayList<>();
		includeDeletedObjects(cb, root, predicates, includeRetired);
		predicates
		        .add(propValue.getAssociationPath().isPresent()
		                ? cb.equal(root.join(propValue.getAssociationPath().get()).get(propValue.getProperty()),
		                    propValue.getValue())
		                : cb.equal(root.get(propValue.getProperty()), propValue.getValue()));
		cq.where(predicates.toArray(new Predicate[0]));
		return cq;
	}
	
	@Override
	public Collection<W> findByOr(BiFunction<CriteriaBuilder, Root<W>, Predicate>... predicates) {
		CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<W> cq = cb.createQuery(clazz);
		Root<W> root = cq.from(clazz);
		cq.where(cb.or(toPredicates(cb, root, predicates)));
		return getCurrentSession().createQuery(cq).getResultList();
	}
	
	@Override
	public Collection<W> findByAnd(BiFunction<CriteriaBuilder, Root<W>, Predicate>... predicates) {
		CriteriaBuilder cb = getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<W> cq = cb.createQuery(clazz);
		Root<W> root = cq.from(clazz);
		cq.where(cb.and(toPredicates(cb, root, predicates)));
		return getCurrentSession().createQuery(cq).getResultList();
	}
	
	private Predicate[] toPredicates(CriteriaBuilder cb, Root<W> root,
	        BiFunction<CriteriaBuilder, Root<W>, Predicate>[] predicates) {
		Predicate[] result = new Predicate[predicates.length];
		for (int i = 0; i < predicates.length; i++) {
			result[i] = predicates[i].apply(cb, root);
		}
		return result;
	}
	
	protected boolean isVoidable() {
		return Voidable.class.isAssignableFrom(clazz);
	}
	
	protected boolean isRetireable() {
		return Retireable.class.isAssignableFrom(clazz);
	}
	
	protected void handleVoidable(CriteriaBuilder cb, Root<W> root, List<Predicate> predicates) {
		predicates.add(cb.equal(root.get("voided"), false));
	}
	
	protected void handleRetireable(CriteriaBuilder cb, Root<W> root, List<Predicate> predicates) {
		predicates.add(cb.equal(root.get("retired"), false));
	}
	
	protected void includeDeletedObjects(CriteriaBuilder cb, Root<W> root, List<Predicate> predicates,
	        boolean includeDeleted) {
		if (!includeDeleted) {
			if (isVoidable()) {
				handleVoidable(cb, root, predicates);
			} else if (isRetireable()) {
				handleRetireable(cb, root, predicates);
			}
		}
	}
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.api.db.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.openmrs.api.db.hibernate.PatientSearchCriteria;
import org.openmrs.module.cohort.CohortAttribute;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.cohort.CohortEncounter;
import org.openmrs.module.cohort.CohortLeader;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortMember;
import org.openmrs.module.cohort.CohortMemberVisit;
import org.openmrs.module.cohort.CohortObs;
import org.openmrs.module.cohort.CohortProgram;
import org.openmrs.module.cohort.CohortRole;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.cohort.CohortVisit;
import org.openmrs.module.cohort.api.db.CohortDAO;
import org.openmrs.module.cohort.api.db.EncounterSearchCriteria;

/**
 * It is a default implementation of {@link CohortDAO}.
 */
@SuppressWarnings("unchecked")
public class HibernateCohortDAO implements CohortDAO {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public CohortAttribute saveCohortAttributes(CohortAttribute att) {
		getCurrentSession().saveOrUpdate(att);
		return att;
	}
	
	@Override
	public List<CohortAttribute> findCohortAttribute(String name) {
		Criteria criteria = getCurrentSession().createCriteria(CohortAttribute.class);
		criteria.add(Restrictions.ilike("value", name, MatchMode.START));
		return criteria.list();
	}
	
	@Override
	public CohortMember saveCPatient(CohortMember cohortMember) {
		getCurrentSession().saveOrUpdate(cohortMember);
		return cohortMember;
	}
	
	@Override
	public CohortType saveCohortType(CohortType cohorttype) {
		getCurrentSession().saveOrUpdate(cohorttype);
		return cohorttype;
	}
	
	@Override
	public List<CohortType> findCohortType() {
		Query queryResult = getCurrentSession().createQuery("from CohortType");
		return queryResult.list();
	}
	
	@Override
	public List<CohortType> getAllCohortTypes() {
		Query queryResult = getCurrentSession().createQuery("from CohortType");
		return queryResult.list();
	}
	
	@Override
	public List<CohortType> findCohortType(String cohort_name) {
		Criteria criteria = getCurrentSession().createCriteria(CohortType.class);
		criteria.add(Restrictions.ilike("name", cohort_name, MatchMode.START));
		return criteria.list();
	}
	
	@Override
	public void purgeCohortType(CohortType cohort) {
		getCurrentSession().delete(cohort);
	}
	
	@Override
	public void purgeCohortAtt(CohortAttribute att) {
		getCurrentSession().delete(att);
	}
	
	@Override
	public List<CohortAttributeType> getAllCohortAttributes() {
		Query queryResult = getCurrentSession().createQuery("from CohortAttributeType");
		return queryResult.list();
	}
	
	@Override
	public CohortAttributeType findCohortAttributes(String attribute_type_name) {
		Criteria criteria = getCurrentSession().createCriteria(CohortAttributeType.class);
		criteria.add(Restrictions.eq("name", attribute_type_name));
		return (CohortAttributeType) criteria.uniqueResult();
	}
	
	@Override
	public void purgeCohortAttributes(CohortAttributeType attributes) {
		getCurrentSession().delete(attributes);
	}
	
	@Override
	public void purgeCohortEncounters(CohortEncounter cencounters) {
		getCurrentSession().delete(cencounters);
	}
	
	@Override
	public CohortType getCohortType(Integer id) {
		return (CohortType) getCurrentSession().get(CohortType.class, id);
	}
	
	@Override
	public CohortAttributeType saveCohortAttributes(CohortAttributeType attributes) {
		getCurrentSession().saveOrUpdate(attributes);
		return attributes;
	}
	
	@Override
	public CohortAttributeType getCohortAttributes(Integer cohort_attribute_type_id) {
		return (CohortAttributeType) getCurrentSession().get(CohortAttributeType.class, cohort_attribute_type_id);
	}
	
	@Override
	public CohortEncounter saveCohortEncounters(CohortEncounter cencounters) {
		getCurrentSession().saveOrUpdate(cencounters);
		return cencounters;
	}
	
	@Override
	public List<CohortEncounter> findCohortEncounters() {
		Query queryResult = getCurrentSession().createQuery("from CohortEncounter");
		return queryResult.list();
	}
	
	@Override
	public CohortM saveCohort(CohortM cohort) {
		getCurrentSession().saveOrUpdate(cohort);
		return cohort;
	}
	
	@Override
	public void purgeCohort(CohortM cohort) {
		getCurrentSession().delete(cohort);
	}
	
	@Override
	public CohortM getCohort(Integer id) {
		return (CohortM) getCurrentSession().get(CohortM.class, id);
	}
	
	@Override
	public List<CohortM> findCohorts() {
		return (List<CohortM>) getCurrentSession().createQuery("from CohortM").list();
	}
	
	@Override
	public List<CohortM> findCohorts(String nameMatching, Map<String, String> attributes, CohortType cohortType) {
		Criteria criteria = getCurrentSession().createCriteria(CohortM.class);
		
		if (StringUtils.isNotBlank(nameMatching)) {
			criteria.add(Restrictions.ilike("name", nameMatching, MatchMode.ANYWHERE));
		}
		
		if (attributes != null && !attributes.isEmpty()) {
			Criteria cri = criteria.createCriteria("attributes").add(Restrictions.eq("voided", false))
			        .createAlias("cohortAttributeType", "attrType");
			
			Disjunction dis = Restrictions.disjunction();
			for (String k : attributes.keySet()) {
				dis.add(Restrictions.conjunction(Restrictions.eq("attrType.name", k),
				    Restrictions.like("value", attributes.get(k), MatchMode.ANYWHERE)));
			}
			
			cri.add(dis);
		}
		
		if (cohortType != null) {
			criteria.add(Restrictions.eq("cohortType.cohortTypeId", cohortType.getCohortTypeId()));
		}
		
		criteria.setProjection(null).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return criteria.list();
	}
	
	@Override
	public CohortVisit saveCohortVisit(CohortVisit cvisit) {
		getCurrentSession().saveOrUpdate(cvisit);
		return cvisit;
	}
	
	@Override
	public void purgeCohortVisit(CohortVisit cvisit) {
		getCurrentSession().delete(cvisit);
	}
	
	@Override
	public CohortLeader getCohortLeaderByUuid(String uuid) {
		return (CohortLeader) getCurrentSession().createQuery("from CohortLeader t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortLeader getCohortLeaderById(Integer id) {
		return (CohortLeader) getCurrentSession().get(CohortLeader.class, id);
	}
	
	@Override
	public List<CohortLeader> getCohortLeadersByCohortId(Integer id) {
		return getCurrentSession().createCriteria(CohortLeader.class, "cohortLeader")
		        .createAlias("cohortLeader.cohort", "cohort").add(Restrictions.eq("cohort.cohortId", id))
		        .add(Restrictions.eq("cohortLeader.voided", false)).list();
	}
	
	@Override
	public CohortLeader saveCohortLeader(CohortLeader cohortLeader) {
		getCurrentSession().saveOrUpdate(cohortLeader);
		return cohortLeader;
	}
	
	@Override
	public void purgeCohortLeader(CohortLeader cohortLeader) {
		getCurrentSession().delete(cohortLeader);
	}
	
	@Override
	public CohortMemberVisit getCohortMemberVisitByUuid(String uuid) {
		return (CohortMemberVisit) getCurrentSession().createQuery("from CohortMemberVisit t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortMemberVisit saveCohortMemberVisit(CohortMemberVisit cohortMemberVisit) {
		getCurrentSession().saveOrUpdate(cohortMemberVisit);
		return cohortMemberVisit;
	}
	
	@Override
	public List<CohortMember> getCohortMembersByPatientId(int patientId) {
		return (List<CohortMember>) getCurrentSession().createQuery("from CohortMember t where t.patient.patientId = :id")
		        .setInteger("id", patientId).list();
	}
	
	@Override
	public List<CohortAttribute> getCohortAttributesByAttributeType(Integer attributeTypeId) {
		return (List<CohortAttribute>) getCurrentSession()
		        .createQuery("from CohortAttribute t where t.cohortAttributeType.cohortAttributeTypeId = :attributeTypeId")
		        .setInteger("attributeTypeId", attributeTypeId).list();
	}
	
	@Override
	public List<CohortVisit> findCohortVisitByVisitType(Integer visitType) {
		Query queryResult = getCurrentSession().createQuery("from CohortVisit");
		return queryResult.list();
	}
	
	@Override
	public CohortM getCohortByName(String name) {
		return (CohortM) getCurrentSession().createQuery("from CohortM t where t.name = :name").setString("name", name)
		        .uniqueResult();
	}
	
	@Override
	public CohortM getCohortUuid(String uuid) {
		return (CohortM) getCurrentSession().createCriteria(CohortM.class).add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	@Override
	public List<CohortEncounter> findCohortEncounters(String name) {
		Criteria criteria = getCurrentSession().createCriteria(CohortEncounter.class);
		criteria.add(Restrictions.ilike("encounterType", name, MatchMode.START));
		criteria.add(Restrictions.eq("voided", false));
		return criteria.list();
	}
	
	@Override
	public List<CohortVisit> findCohortVisit(String name) {
		Criteria criteria = getCurrentSession().createCriteria(CohortVisit.class);
		criteria.add(Restrictions.ilike("visitType", name, MatchMode.START));
		criteria.add(Restrictions.eq("voided", false));
		return criteria.list();
	}
	
	@Override
	public CohortEncounter getCohortEncounterUuid(String uuid) {
		return (CohortEncounter) getCurrentSession().createQuery("from CohortEncounter t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortMember getCohortMemUuid(String uuid) {
		return (CohortMember) getCurrentSession().createQuery("from CohortMember t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortType getCohortTypeByName(String name) {
		return (CohortType) getCurrentSession().createQuery("from CohortType t where t.name = :name").setString("name", name)
		        .uniqueResult();
	}
	
	@Override
	public CohortAttributeType getCohortAttributeTypeUuid(String uuid) {
		return (CohortAttributeType) getCurrentSession().createQuery("from CohortAttributeType t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public List<CohortMember> findCohortMember() {
		Session session = getCurrentSession();
		Query queryResult = session.createQuery("from CohortMember");
		return queryResult.list();
		
	}
	
	@Override
	public CohortVisit findCohortVisit(Integer id) {
		return (CohortVisit) getCurrentSession().get(CohortVisit.class, id);
	}
	
	@Override
	public CohortType findCohortType(Integer id) {
		return (CohortType) getCurrentSession().get(CohortType.class, id);
	}
	
	@Override
	public CohortAttribute getCohortAttribute(Integer id) {
		return (CohortAttribute) getCurrentSession().get(CohortAttribute.class, id);
	}
	
	@Override
	public CohortAttributeType findCohortAttributeType(Integer id) {
		return (CohortAttributeType) getCurrentSession().get(CohortAttributeType.class, id);
	}
	
	@Override
	public CohortEncounter findCohortEncounter(Integer id) {
		return (CohortEncounter) getCurrentSession().get(CohortEncounter.class, id);
	}
	
	@Override
	public CohortProgram saveCohortProgram(CohortProgram cohort) {
		getCurrentSession().saveOrUpdate(cohort);
		return cohort;
	}
	
	@Override
	public void purgeCohortProgram(CohortProgram cvisit) {
		getCurrentSession().delete(cvisit);
	}
	
	@Override
	public List<CohortProgram> findCohortProg() {
		Session session = getCurrentSession();
		Query queryResult = session.createQuery("from CohortProgram");
		return queryResult.list();
	}
	
	@Override
	public CohortProgram findCohortProgram(String name) {
		Criteria criteria = getCurrentSession().createCriteria(CohortProgram.class);
		criteria.add(Restrictions.eq("name", name));
		return (CohortProgram) criteria.uniqueResult();
	}
	
	@Override
	public CohortProgram getCohortProgramUuid(String uuid) {
		return (CohortProgram) getCurrentSession().createQuery("from CohortProgram t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortProgram findCohortProgram(Integer id) {
		return (CohortProgram) getCurrentSession().get(CohortProgram.class, id);
	}
	
	@Override
	public CohortObs saveCohortObs(CohortObs cobs) {
		getCurrentSession().saveOrUpdate(cobs);
		return cobs;
	}
	
	@Override
	public void purgeCohortObs(CohortObs cobs) {
		getCurrentSession().delete(cobs);
	}
	
	@Override
	public List<CohortObs> findCohortObs() {
		Session session = getCurrentSession();
		Query queryResult = session.createQuery("from CohortObs");
		return queryResult.list();
	}
	
	@Override
	public CohortObs findCohortObs(Integer id) {
		return (CohortObs) getCurrentSession().get(CohortObs.class, id);
	}
	
	@Override
	public CohortObs getCohortObsUuid(String uuid) {
		return (CohortObs) getCurrentSession().createQuery("from CohortObs t where t.uuid = :uuid").setString("uuid", uuid)
		        .uniqueResult();
	}
	
	@Override
	public CohortRole saveCohortRole(CohortRole cohort) {
		getCurrentSession().saveOrUpdate(cohort);
		return cohort;
	}
	
	@Override
	public List<CohortRole> findRoles(String name) {
		Session session = getCurrentSession();
		Query queryResult = session
		        .createQuery("from CohortRole r where r.cohortType=(select m.cohortType from CohortM m where m.name=:name)")
		        .setParameter("name", name);
		return queryResult.list();
	}
	
	@Override
	public List<CohortRole> findCohortRoles(String name) {
		Session session = getCurrentSession();
		Query queryResult = session.createQuery("from CohortRole where name=:name").setParameter("name", name);
		return queryResult.list();
	}
	
	@Override
	public CohortRole getCohortRoleUuid(String uuid) {
		return (CohortRole) getCurrentSession().createQuery("from CohortRole t where t.uuid = :uuid").setString("uuid", uuid)
		        .uniqueResult();
	}
	
	@Override
	public CohortEncounter getCohortEncounter(Integer id) {
		return (CohortEncounter) getCurrentSession().get(CohortEncounter.class, id);
	}
	
	@Override
	public List<CohortEncounter> getEncounters(EncounterSearchCriteria searchCriteria) {
		Criteria crit = getCurrentSession().createCriteria(CohortEncounter.class);
		
		if (searchCriteria.getCohort() != null && searchCriteria.getCohort().getCohortId() != null) {
			crit.add(Restrictions.eq("cohort", searchCriteria.getCohort()));
		}
		if (searchCriteria.getLocation() != null && searchCriteria.getLocation().getLocationId() != null) {
			crit.add(Restrictions.eq("location", searchCriteria.getLocation()));
		}
		if (searchCriteria.getFromDate() != null) {
			crit.add(Restrictions.ge("encounterDatetime", searchCriteria.getFromDate()));
		}
		if (searchCriteria.getToDate() != null) {
			crit.add(Restrictions.le("encounterDatetime", searchCriteria.getToDate()));
		}
		if (searchCriteria.getEnteredViaForms() != null && searchCriteria.getEnteredViaForms().size() > 0) {
			crit.add(Restrictions.in("form", searchCriteria.getEnteredViaForms()));
		}
		if (searchCriteria.getEncounterTypes() != null && searchCriteria.getEncounterTypes().size() > 0) {
			crit.add(Restrictions.in("encounterType", searchCriteria.getEncounterTypes()));
		}
		if (searchCriteria.getVisits() != null && searchCriteria.getVisits().size() > 0) {
			crit.add(Restrictions.in("visit", searchCriteria.getVisits()));
		}
		crit.addOrder(Order.asc("encounterDatetime"));
		return crit.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CohortEncounter> getEncounters(String query, Integer cohortId, Integer start, Integer length,
	        boolean includeVoided) {
		if (StringUtils.isBlank(query) && cohortId == null) {
			return Collections.emptyList();
		}
		
		Criteria criteria = createEncounterByQueryCriteria(query, cohortId, includeVoided, true);
		
		if (start != null) {
			criteria.setFirstResult(start);
		}
		if (length != null && length > 0) {
			criteria.setMaxResults(length);
		}
		
		return criteria.list();
	}
	
	private Criteria createEncounterByQueryCriteria(String query, Integer cohortId, boolean includeVoided,
	        boolean orderByNames) {
		Criteria criteria = getCurrentSession().createCriteria(CohortEncounter.class, "enc");
		if (!includeVoided) {
			criteria.add(Restrictions.eq("enc.voided", false));
		}
		
		criteria = criteria.createCriteria("cohort", "coh");
		if (cohortId != null) {
			criteria.add(Restrictions.eq("coh.cohortId", cohortId));
			if (StringUtils.isNotBlank(query)) {
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				//match on location.name, encounterType.name, form.name
				//provider.name, provider.identifier, provider.person.names
				MatchMode mode = MatchMode.ANYWHERE;
				criteria.createAlias("enc.location", "loc");
				criteria.createAlias("enc.encounterType", "encType");
				criteria.createAlias("enc.form", "form");
				criteria.createAlias("enc.encounterProviders", "enc_prov");
				criteria.createAlias("enc_prov.provider", "prov");
				criteria.createAlias("prov.person", "person", JoinType.LEFT_OUTER_JOIN);
				criteria.createAlias("person.names", "personName", JoinType.LEFT_OUTER_JOIN);
				
				Disjunction or = Restrictions.disjunction();
				or.add(Restrictions.ilike("loc.name", query, mode));
				or.add(Restrictions.ilike("encType.name", query, mode));
				or.add(Restrictions.ilike("form.name", query, mode));
				or.add(Restrictions.ilike("prov.name", query, mode));
				or.add(Restrictions.ilike("prov.identifier", query, mode));
				
				String[] splitNames = query.split(" ");
				Disjunction nameOr = Restrictions.disjunction();
				for (String splitName : splitNames) {
					nameOr.add(Restrictions.ilike("personName.givenName", splitName, mode));
					nameOr.add(Restrictions.ilike("personName.middleName", splitName, mode));
					nameOr.add(Restrictions.ilike("personName.familyName", splitName, mode));
					nameOr.add(Restrictions.ilike("personName.familyName2", splitName, mode));
				}
				//OUTPUT for provider criteria: 
				//prov.name like '%query%' OR prov.identifier like '%query%'
				//OR ( personName.voided = false 
				//		 AND (  personName.givenName like '%query%' 
				//			OR personName.middleName like '%query%' 
				//			OR personName.familyName like '%query%'
				//			OR personName.familyName2 like '%query%'
				//			)
				//	 )
				Conjunction personNameConjuction = Restrictions.conjunction();
				personNameConjuction.add(Restrictions.eq("personName.voided", false));
				personNameConjuction.add(nameOr);
				
				or.add(personNameConjuction);
				
				criteria.add(or);
			}
		} else {
			String name = null;
			String identifier = null;
			if (query.matches(".*\\d+.*")) {
				identifier = query;
			} else {
				// there is no number in the string, search on name
				name = query;
			}
			criteria = new PatientSearchCriteria(sessionFactory, criteria).prepareCriteria(name, identifier,
			    new ArrayList<>(), false, orderByNames, false);
		}
		
		return criteria;
	}
	
	@Override
	public List<CohortRole> findCohortRole(String cohort_name) {
		Criteria criteria = getCurrentSession().createCriteria(CohortRole.class);
		criteria.add(Restrictions.ilike("name", cohort_name, MatchMode.START));
		criteria.add(Restrictions.eq("voided", false));
		return criteria.list();
	}
	
	@Override
	public CohortRole findCohortRole(Integer id) {
		return (CohortRole) getCurrentSession().get(CohortRole.class, id);
	}
	
	@Override
	public void purgeCohortRole(CohortRole crole) {
		getCurrentSession().delete(crole);
	}
	
	@Override
	public Long getCount(String name) {
		return (Long) getCurrentSession()
		        .createQuery("select count(*) from CohortMember c left outer join c.cohort m where m.name=:name")
		        .setParameter("name", name).uniqueResult();
	}
	
	@Override
	public CohortObs saveObs(CohortObs obs) {
		if (obs.hasGroupMembers() && obs.getCohortObsId() != null) {
			// hibernate has a problem updating child collections
			// if the parent object was already saved so we do it
			// explicitly here
			for (CohortObs member : obs.getGroupMembers()) {
				if (member.getCohortObsId() == null) {
					saveObs(member);
				}
			}
		}
		
		getCurrentSession().saveOrUpdate(obs);
		
		return obs;
	}
	
	@Override
	public CohortMember getCohortMember(Integer id) {
		return (CohortMember) getCurrentSession().get(CohortMember.class, id);
	}
	
	@Override
	// FIXME Does this work?
	public List<CohortMember> findCohortMember(String name) {
		Session session = getCurrentSession();
		Query queryResult = session
		        .createQuery("from CohortMember where patient=(select patientId from Person where names=:name")
		        .setParameter("name", name);
		return queryResult.list();
	}
	
	@Override
	public List<CohortEncounter> findCohortEncounter(String cohort, String location) {
		Criteria criteria = getCurrentSession().createCriteria(CohortEncounter.class);
		criteria.add(Restrictions.ilike("cohort", cohort, MatchMode.START));
		criteria.add(Restrictions.ilike("location", location, MatchMode.START));
		return criteria.list();
	}
	
	@Override
	public List<CohortMember> findCohortMembersByCohortId(Integer cohortId) {
		return getCurrentSession().createQuery("from CohortMember where cohort = :cohortId")
		        .setParameter("cohortId", cohortId).list();
	}
	
	/**
	 * Gets the current hibernate session while taking care of the hibernate 3 and 4 differences.
	 *
	 * @return the current hibernate session.
	 */
	private org.hibernate.Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@Override
	public CohortAttribute getCohortAttributeById(Integer id) {
		return (CohortAttribute) getCurrentSession().get(CohortAttribute.class, id);
	}
	
	@Override
	public CohortAttribute getCohortAttributeByUuid(String uuid) {
		return (CohortAttribute) getCurrentSession().createQuery("from CohortAttribute t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortAttributeType getCohortAttributeTypeById(Integer id) {
		return (CohortAttributeType) getCurrentSession().get(CohortAttributeType.class, id);
	}
	
	@Override
	public CohortAttributeType getCohortAttributeTypeByUuid(String uuid) {
		return (CohortAttributeType) getCurrentSession().createQuery("from CohortAttributeType t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortEncounter getCohortEncounterById(Integer id) {
		return (CohortEncounter) getCurrentSession().get(CohortEncounter.class, id);
	}
	
	@Override
	public CohortEncounter getCohortEncounterByUuid(String uuid) {
		return (CohortEncounter) getCurrentSession().createQuery("from CohortEncounter t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortM getCohortMById(Integer id) {
		return (CohortM) getCurrentSession().get(CohortM.class, id);
	}
	
	@Override
	public CohortM getCohortMByUuid(String uuid) {
		return (CohortM) getCurrentSession().createQuery("from CohortM t where t.uuid = :uuid").setString("uuid", uuid)
		        .uniqueResult();
	}
	
	@Override
	public CohortMember getCohortMemberById(Integer id) {
		return (CohortMember) getCurrentSession().get(CohortMember.class, id);
	}
	
	@Override
	public CohortMember getCohortMemberByUuid(String uuid) {
		return (CohortMember) getCurrentSession().createQuery("from CohortMember t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortObs getCohortObsById(Integer id) {
		return (CohortObs) getCurrentSession().get(CohortObs.class, id);
	}
	
	@Override
	public CohortObs getCohortObsByUuid(String uuid) {
		return (CohortObs) getCurrentSession().createQuery("from CohortObs t where t.uuid = :uuid").setString("uuid", uuid)
		        .uniqueResult();
	}
	
	@Override
	public CohortProgram getCohortProgramById(Integer id) {
		return (CohortProgram) getCurrentSession().get(CohortProgram.class, id);
	}
	
	@Override
	public CohortProgram getCohortProgramByUuid(String uuid) {
		return (CohortProgram) getCurrentSession().createQuery("from CohortProgram t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public CohortRole getCohortRoleById(Integer id) {
		return (CohortRole) getCurrentSession().get(CohortRole.class, id);
	}
	
	@Override
	public CohortRole getCohortRoleByUuid(String uuid) {
		return (CohortRole) getCurrentSession().createQuery("from CohortRole t where t.uuid = :uuid").setString("uuid", uuid)
		        .uniqueResult();
	}
	
	@Override
	public CohortType getCohortTypeById(Integer id) {
		return (CohortType) getCurrentSession().get(CohortType.class, id);
	}
	
	@Override
	public CohortType getCohortTypeByUuid(String uuid) {
		return (CohortType) getCurrentSession().createQuery("from CohortType t where t.uuid = :uuid").setString("uuid", uuid)
		        .uniqueResult();
	}
	
	@Override
	public CohortVisit getCohortVisitById(Integer id) {
		return (CohortVisit) getCurrentSession().get(CohortVisit.class, id);
	}
	
	@Override
	public CohortVisit getCohortVisitByUuid(String uuid) {
		return (CohortVisit) getCurrentSession().createQuery("from CohortVisit t where t.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	public List<CohortM> getCohortsByLocationId(Integer id) {
		return (List<CohortM>) getCurrentSession().createQuery("from CohortM t where t.location.locationId = :id")
		        .setString("id", id.toString()).list();
	}
	
	@Override
	public List<CohortM> getCohortByCohortTypeId(Integer id) {
		return (List<CohortM>) getCurrentSession().createQuery("from CohortM t where t.cohortType.cohortTypeId = :id")
		        .setString("id", id.toString()).list();
	}
	
	@Override
	public List<CohortM> getCohortByCohortProgramId(Integer id) {
		return (List<CohortM>) getCurrentSession().createQuery("from CohortM t where t.cohortProgram.cohortProgramId = :id")
		        .setString("id", id.toString()).list();
	}
	
	@Override
	public List<CohortMember> getCohortMembersByCohortRoleId(Integer id) {
		return (List<CohortMember>) getCurrentSession().createQuery("from CohortM t where t.role.cohortProgramId = :id")
		        .setString("id", id.toString()).list();
	}
	
	@Override
	public CohortM getCohort(Integer locationId, Integer programId, Integer typeId) {
		Criteria criteria = getCurrentSession().createCriteria(CohortM.class, "cohort");
		if (locationId != null) {
			criteria.createAlias("cohort.location", "loc");
			criteria.add(Restrictions.eq("loc.locationId", locationId));
		}
		if (programId != null) {
			criteria.createAlias("cohort.cohortProgram", "program");
			criteria.add(Restrictions.eq("program.programId", programId));
		}
		if (typeId != null) {
			criteria.createAlias("cohort.cohortType", "type");
			criteria.add(Restrictions.eq("type.cohortTypeId", typeId));
		}
		return (CohortM) criteria.uniqueResult();
	}
	
	@Override
	public List<CohortMember> getCohortMembersByCohortId(Integer id) {
		Criteria criteria = getCurrentSession().createCriteria(CohortMember.class, "cohortMember")
		        .createAlias("cohortMember.cohort", "cohort");
		criteria.add(Restrictions.eq("cohort.cohortId", id));
		return criteria.list();
	}
	
	@Override
	public List<CohortRole> getAllCohortRoles() {
		return getCurrentSession().createCriteria(CohortRole.class).list();
	}
	
	@Override
	public CohortRole getCohortRoleByName(String name) {
		return (CohortRole) getCurrentSession().createCriteria(CohortRole.class).add(Restrictions.eq("name", name))
		        .uniqueResult();
	}
	
	@Override
	public void deleteCohortRoleById(Integer id) {
		CohortRole cr = (CohortRole) getCurrentSession().createCriteria(CohortRole.class)
		        .add(Restrictions.eq("cohortRoleId", id)).uniqueResult();
		getCurrentSession().delete(cr);
	}
	
	@Override
	public List<CohortAttribute> findCohortAttributes(Integer cohortId, Integer attributeTypeId) {
		Criteria cri = getCurrentSession().createCriteria(CohortAttribute.class);
		if (cohortId != null) {
			cri.createAlias("cohort", "c");
			cri.add(Restrictions.eq("c.cohortId", cohortId));
		}
		
		if (attributeTypeId != null) {
			cri.createAlias("cohortAttributeType", "a");
			cri.add(Restrictions.eq("a.cohortAttributeTypeId", attributeTypeId));
		}
		return cri.list();
	}
	
	@Override
	public List<CohortVisit> getCohortVisitsByLocationId(Integer id) {
		return (List<CohortVisit>) getCurrentSession().createCriteria(CohortVisit.class, "cv")
		        .createAlias("cv.location", "location").add(Restrictions.eq("location.locationId", id)).list();
	}
	
	@Override
	public List<CohortMember> getAllHeadCohortMembers() {
		return getCurrentSession().createCriteria(CohortMember.class).add(Restrictions.eq("head", true)).list();
	}
	
	@Override
	public List<CohortObs> getCohortObsByEncounterId(Integer id) {
		return getCurrentSession().createCriteria(CohortObs.class, "cohortObs")
		        .createAlias("cohortObs.encounter", "encounter").add(Restrictions.eq("encounter.encounterId", id)).list();
	}
	
	@Override
	public List<CohortVisit> getCohortVisitsByDate(Date startDate, Date endDate) {
		return getCurrentSession().createCriteria(CohortVisit.class).add(Restrictions.ge("startDate", startDate))
		        .add(Restrictions.le("endDate", endDate)).list();
	}
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.OpenmrsService;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured
 * in moduleApplicationContext.xml. It can be accessed only via Context:<br>
 * <code>
 * Context.getService(cohortService.class).someMethod();
 * </code>
 *
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface CohortService extends OpenmrsService {
	
	CohortM getCohortById(Integer id);
	
	CohortM getCohortByUuid(String uuid);
	
	CohortM getCohortByName(String name);
	
	List<CohortM> getAllCohorts();
	
	List<CohortM> findCohortsMatching(String nameMatching, Map<String, String> attributes, CohortType cohortType);
	
	CohortM saveCohort(CohortM cohort);
	
	void purgeCohort(CohortM cohort);
	
	CohortM getCohort(Integer locationId, Integer programId, Integer typeId);
	
	CohortMember getCohortMemberByUuid(String uuid);
	
	List<CohortMember> findCohortMemberByName(String name);
	
	List<CohortMember> findCohortMembersByCohort(Integer cohortId);
	
	CohortMember getCohortMemberById(Integer id);
	
	List<CohortMember> getCohortMembersByCohortId(Integer id);
	
	List<CohortMember> getCohortMembersByCohortRoleId(Integer id);
	
	List<CohortMember> getAllHeadCohortMembers();
	
	CohortMember saveCohortMember(CohortMember cohortmember);
	
	List<CohortMember> findCohortMembersByPatient(int patientId);
	
	CohortAttributeType getCohortAttributeType(Integer id);
	
	List<CohortAttributeType> getAllCohortAttributeTypes();
	
	CohortAttributeType getCohortAttributeTypeByName(String attribute_type_name);
	
	CohortAttributeType getCohortAttributeTypeByUuid(String uuid);
	
	CohortAttributeType saveCohort(CohortAttributeType a);
	
	void purgeCohortAttributes(CohortAttributeType attributes);
	
	CohortAttribute getCohortAttributeByUuid(String uuid);
	
	CohortAttribute getCohortAttributeById(Integer id);
	
	CohortAttribute saveCohortAttribute(CohortAttribute att);
	
	void purgeCohortAtt(CohortAttribute att);
	
	List<CohortAttribute> findCohortAttributes(Integer cohortId, Integer attributeTypeId);
	
	CohortRole getCohortRoleByUuid(String uuid);
	
	CohortRole getCohortRoleByName(String name);
	
	CohortRole getCohortRoleById(Integer id);
	
	List<CohortRole> getAllCohortRoles();
	
	CohortRole saveCohortRole(CohortRole cohort);
	
	void purgeCohortRole(CohortRole crole);
	
	CohortType getCohortTypeById(Integer id);
	
	CohortType getCohortTypeByUuid(String uuid);
	
	CohortType getCohortTypeByName(String name);
	
	List<CohortType> getAllCohortTypes();
	
	CohortType saveCohort(CohortType cohort);
	
	void purgeCohortType(CohortType type);
	
	CohortProgram getCohortProgramByUuid(String uuid);
	
	CohortProgram getCohortProgramById(Integer id);
	
	CohortProgram getCohortProgramByName(String name);
	
	List<CohortProgram> getAllCohortPrograms();
	
	CohortProgram saveCohortProgram(CohortProgram cohort);
	
	void purgeCohortProgram(CohortProgram cvisit);
	
	CohortVisit getCohortVisitById(Integer id);
	
	List<CohortVisit> getCohortVisitByType(Integer visitType);
	
	CohortVisit getCohortVisitByUuid(String uuid);
	
	CohortVisit saveCohortVisit(CohortVisit cvisit);
	
	List<CohortVisit> getCohortVisitsByLocation(Integer id);
	
	List<CohortVisit> getCohortVisitsByDate(Date startDate, Date endDate);
	
	void purgeCohortVisit(CohortVisit cvisit);
	
	CohortEncounter getCohortEncounterByUuid(String uuid);
	
	CohortEncounter getCohortEncounterById(Integer id);
	
	List<CohortEncounter> filterEncountersByViewPermissions(List<CohortEncounter> encounters, User user);
	
	List<CohortEncounter> findCohortEncounter(String cohort, String location);
	
	List<CohortEncounter> getEncounters(CohortM who, Location loc, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, boolean includeVoided);
	
	List<CohortEncounter> getEncounters(CohortM who, Location loc, Date fromDate, Date toDate,
	        Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes, Collection<User> providers,
	        boolean includeVoided);
	
	List<CohortEncounter> getEncountersByCohort(String query, Integer cohortId, boolean includeVoided);
	
	CohortEncounter saveCohortEncounter(CohortEncounter cencounters);
	
	void purgeCohortEncounter(CohortEncounter cencounters);
	
	CohortObs getCohortObsByUuid(String uuid);
	
	CohortObs getCohortObsById(Integer id);
	
	List<CohortObs> getObservations(List<CohortM> whom, List<CohortEncounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<Location> locations, List<String> sort, Integer mostRecentN, Integer obsGroupId,
	        Date fromDate, Date toDate, boolean includeVoidedObs);
	
	List<CohortObs> getObservationsByCohortAndConcept(CohortM who, Concept question);
	
	CohortObs saveCohortObs(CohortObs cobs);
	
	CohortObs voidObs(CohortObs obs, String reason);
	
	List<CohortObs> getCohortObsByEncounterId(Integer id);
	
	void purgeCohortObs(CohortObs cobs);
	
	Long getCount(String name);
	
	CohortLeader getCohortLeaderByUuid(String uuid);
	
	CohortLeader getCohortLeaderById(Integer id);
	
	List<CohortLeader> getCohortLeadersByCohortId(Integer id);
	
	CohortLeader saveCohortLeader(CohortLeader cohortLeader);
	
	CohortLeader voidCohortLeader(CohortLeader cohortLeader, String reason);
	
	void purgeCohortLeader(CohortLeader cohortLeader);
	
	CohortMemberVisit getCohortMemberVisitByUuid(String uuid);
	
	CohortMemberVisit saveCohortMemberVisit(CohortMemberVisit cohortMemberVisit);
	
	List<CohortAttribute> getCohortAttributesByAttributeType(Integer attributeId);
	
	List<CohortM> getCohortsByLocationId(int locationId);
}

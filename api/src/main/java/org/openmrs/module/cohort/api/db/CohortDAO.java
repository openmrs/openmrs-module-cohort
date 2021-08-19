/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort.api.db;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import org.openmrs.module.cohort.api.CohortService;

/**
 * Database methods for {@link CohortService}.
 */
public interface CohortDAO {
	
	CohortAttribute getCohortAttributeById(Integer id);
	
	CohortAttribute getCohortAttributeByUuid(String uuid);
	
	CohortAttribute saveCohortAttributes(CohortAttribute att);
	
	List<CohortAttribute> findCohortAttributes(Integer cohortId, Integer attributeTypeId);
	
	CohortAttributeType getCohortAttributeTypeById(Integer id);
	
	CohortAttributeType getCohortAttributeTypeByUuid(String uuid);
	
	CohortAttributeType getCohortAttributeTypeUuid(String uuid);
	
	CohortAttributeType getCohortAttributes(Integer cohort_attribute_type_id);
	
	CohortAttributeType saveCohortAttributes(CohortAttributeType attributes);
	
	CohortEncounter getCohortEncounter(Integer id);
	
	CohortEncounter getCohortEncounterById(Integer id);
	
	CohortEncounter getCohortEncounterByUuid(String uuid);
	
	CohortEncounter getCohortEncounterUuid(String uuid);
	
	CohortEncounter saveCohortEncounters(CohortEncounter cencounters);
	
	CohortM getCohortByName(String name);
	
	CohortM getCohortMById(Integer id);
	
	CohortM getCohortMByUuid(String uuid);
	
	CohortM getCohortUuid(String uuid);
	
	CohortM saveCohort(CohortM cohort);
	
	CohortM getCohort(Integer locationId, Integer ProgramId, Integer TypeId);
	
	CohortMember getCohortMemUuid(String uuid);
	
	CohortMember getCohortMemberById(Integer id);
	
	CohortMember getCohortMemberByUuid(String uuid);
	
	CohortMember saveCPatient(CohortMember cohort);
	
	CohortObs getCohortObsById(Integer id);
	
	CohortObs getCohortObsByUuid(String uuid);
	
	CohortObs getCohortObsUuid(String uuid);
	
	CohortObs saveCohortObs(CohortObs cobs);
	
	List<CohortObs> getCohortObsByEncounterId(Integer id);
	
	CohortObs saveObs(CohortObs obs);
	
	CohortProgram getCohortProgramById(Integer id);
	
	CohortProgram getCohortProgramByUuid(String uuid);
	
	CohortProgram getCohortProgramUuid(String uuid);
	
	CohortProgram saveCohortProgram(CohortProgram cohort);
	
	CohortRole getCohortRoleById(Integer id);
	
	CohortRole getCohortRoleByUuid(String uuid);
	
	CohortRole getCohortRoleUuid(String uuid);
	
	CohortRole saveCohortRole(CohortRole cohort);
	
	List<CohortRole> getAllCohortRoles();
	
	CohortRole getCohortRoleByName(String name);
	
	void deleteCohortRoleById(Integer id);
	
	CohortType getCohortType(Integer id);
	
	CohortType getCohortTypeById(Integer id);
	
	CohortType getCohortTypeByUuid(String uuid);
	
	CohortType getCohortTypeByName(String name);
	
	CohortType saveCohortType(CohortType cohorttype);
	
	CohortVisit getCohortVisitById(Integer id);
	
	CohortVisit getCohortVisitByUuid(String uuid);
	
	List<CohortVisit> getCohortVisitsByLocationId(Integer id);
	
	List<CohortVisit> getCohortVisitsByDate(Date startDate, Date endDate);
	
	CohortVisit saveCohortVisit(CohortVisit cvisit);
	
	CohortAttribute getCohortAttribute(Integer id);
	
	List<CohortAttribute> getCohortAttributesByAttributeType(Integer attributeTypeId);
	
	List<CohortAttribute> findCohortAttribute(String name);
	
	CohortAttributeType findCohortAttributeType(Integer id);
	
	List<CohortAttributeType> getAllCohortAttributes();
	
	CohortAttributeType findCohortAttributes(String attribute_type_name);
	
	CohortEncounter findCohortEncounter(Integer id);
	
	List<CohortEncounter> findCohortEncounter(String cohort, String location);
	
	List<CohortEncounter> findCohortEncounters();
	
	List<CohortEncounter> findCohortEncounters(String name);
	
	List<CohortEncounter> getEncounters(EncounterSearchCriteria searchCriteria);
	
	List<CohortEncounter> getEncounters(String query, Integer cohortId, Integer start, Integer length,
	        boolean includeVoided);
	
	List<CohortM> findCohorts();
	
	List<CohortM> findCohorts(String nameMatching, Map<String, String> attributes, CohortType type);
	
	CohortM getCohort(Integer id);
	
	List<CohortM> getCohortsByLocationId(Integer id);
	
	List<CohortM> getCohortByCohortTypeId(Integer id);
	
	List<CohortM> getCohortByCohortProgramId(Integer id);
	
	List<CohortMember> findCohortMember();
	
	List<CohortMember> findCohortMember(String name);
	
	List<CohortMember> findCohortMembersByCohortId(Integer cohortId);
	
	List<CohortMember> getCohortMembersByCohortId(Integer id);
	
	CohortMember getCohortMember(Integer id);
	
	List<CohortMember> getAllHeadCohortMembers();
	
	List<CohortMember> getCohortMembersByCohortRoleId(Integer id);
	
	List<CohortMember> getCohortMembersByPatientId(int patientId);
	
	List<CohortObs> findCohortObs();
	
	CohortObs findCohortObs(Integer id);
	
	List<CohortProgram> findCohortProg();
	
	CohortProgram findCohortProgram(Integer id);
	
	CohortProgram findCohortProgram(String name);
	
	CohortRole findCohortRole(Integer id);
	
	List<CohortRole> findCohortRole(String cohort_name);
	
	List<CohortRole> findCohortRoles(String name);
	
	List<CohortRole> findRoles(String name);
	
	List<CohortType> findCohortType();
	
	CohortType findCohortType(Integer id);
	
	List<CohortType> findCohortType(String cohort_name);
	
	List<CohortType> getAllCohortTypes();
	
	List<CohortVisit> findCohortVisitByVisitType(Integer visitType);
	
	CohortVisit findCohortVisit(Integer id);
	
	List<CohortVisit> findCohortVisit(String name);
	
	CohortLeader getCohortLeaderByUuid(String uuid);
	
	CohortLeader getCohortLeaderById(Integer id);
	
	List<CohortLeader> getCohortLeadersByCohortId(Integer id);
	
	CohortLeader saveCohortLeader(CohortLeader cohortLeader);
	
	CohortMemberVisit getCohortMemberVisitByUuid(String uuid);
	
	CohortMemberVisit saveCohortMemberVisit(CohortMemberVisit cohortMemberVisit);
	
	Long getCount(String name);
	
	void purgeCohort(CohortM cohort);
	
	void purgeCohortAtt(CohortAttribute att);
	
	void purgeCohortAttributes(CohortAttributeType attributes);
	
	void purgeCohortEncounters(CohortEncounter cencounters);
	
	void purgeCohortObs(CohortObs cobs);
	
	void purgeCohortProgram(CohortProgram cvisit);
	
	void purgeCohortRole(CohortRole crole);
	
	void purgeCohortType(CohortType cohort);
	
	void purgeCohortVisit(CohortVisit cvisit);
	
	void purgeCohortLeader(CohortLeader cohortLeader);
}

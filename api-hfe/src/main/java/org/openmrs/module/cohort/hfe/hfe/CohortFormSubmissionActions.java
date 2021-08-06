package org.openmrs.module.cohort.hfe;

import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortEncounter;
import org.openmrs.module.cohort.CohortM;
import org.openmrs.module.cohort.CohortObs;
import org.openmrs.module.htmlformentry.FormSubmissionActions;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.InvalidActionException;
import org.openmrs.util.OpenmrsUtil;

public class CohortFormSubmissionActions extends FormSubmissionActions {
	
	/**
	 * Logger to use with this class
	 */
	protected final Log log = LogFactory.getLog(getClass());
	
	private Boolean cohortUpdateRequired = false;
	
	private List<CohortM> cohortsToCreate = new Vector<CohortM>();
	
	private CohortEncounter cohortencounter;
	
	private CohortObs cobs;
	
	private List<Encounter> encountersToCreate = new Vector<Encounter>();
	
	private List<Encounter> encountersToEdit = new Vector<Encounter>();
	
	private List<Obs> obsToCreate = new Vector<Obs>();
	
	private List<Obs> obsToVoid = new Vector<Obs>();
	
	/**
	 * The stack where state is stored
	 */
	private Stack<Object> stack = new Stack<Object>(); // a snapshot might look something like { Patient, Encounter, ObsGroup }
	
	public void beginCohort(CohortM cohort) throws InvalidActionException {
		// person has to be at the top of the stack
		if (stack.size() > 0) {
			throw new InvalidActionException("Person can only go on the top of the stack");
		}
		if (cohort.getCohortId() == null && !cohortsToCreate.contains(cohort)) {
			cohortsToCreate.add(cohort);
		}
		stack.push(cohort);
	}
	
	/**
	 * Removes the most recently added Person from the submission stack. All other objects added
	 * after that Person are removed as well.
	 * <p/>
	 * (So, in the current one-person-per-form model, this would empty the entire submission stack)
	 *
	 * @throws InvalidActionException
	 */
	public void endCohort() throws InvalidActionException {
		if (!stackContains(CohortM.class)) {
			throw new InvalidActionException("No cohort on the stack");
		}
		while (true) {
			Object o = stack.pop();
			if (o instanceof CohortM) {
				break;
			}
		}
	}
	
	/**
	 * Adds an Encounter to the submission stack
	 *
	 * @param encounter the Encounter to add
	 * @throws InvalidActionException
	 */
	public void beginEncounter(Encounter encounter) throws InvalidActionException {
		// there needs to be a Person on the stack before this
		if (!stackContains(CohortM.class)) {
			throw new InvalidActionException("No Person on the stack");
		}
		if (encounter.getEncounterId() == null && !encountersToCreate.contains(encounter)) {
			encountersToCreate.add(encounter);
		}
		// encounter.setPatient(highestOnStack(Patient.class));
		//cohortencounter.setCohort(highestOnStack(CohortM.class));
		// encounter=CohortFormEntrySession.convertToEncounter(cohortencounter);
		stack.push(encounter);
	}
	
	/**
	 * Removes the most recently added Encounter from the submission stack. All objects added after
	 * that Encounter are removed as well.
	 *
	 * @throws InvalidActionException
	 */
	public void endEncounter() throws InvalidActionException {
		if (!stackContains(Encounter.class)) {
			throw new InvalidActionException("No Encounter on the stack");
		}
		while (true) {
			Object o = stack.pop();
			if (o instanceof Encounter) {
				break;
			}
		}
	}
	
	/**
	 * Adds an Obs Group to the submission stack
	 *
	 * @param group the Obs Group to add
	 * @throws InvalidActionException
	 */
	public void beginObsGroup(Obs group) throws InvalidActionException {
		//there needs to be a Cohort on the stack before this
		if (!stackContains(CohortM.class)) {
			throw new InvalidActionException("No cohort on the stack");
		}
		if (group.getObsId() == null && !obsToCreate.contains(group)) {
			obsToCreate.add(group);
		}
		//Person person = highestOnStack(Person.class);
		Encounter encounter = highestOnStack(Encounter.class);
		//group.setPerson(person);
		if (encounter != null) {
			addObsToEncounterIfNotAlreadyThere(encounter, group);
		}
		//this is for obs groups within obs groups
		Object o = stack.peek();
		if (o instanceof Obs) {
			Obs oParent = (Obs) o;
			oParent.addGroupMember(group);
		}
		stack.push(group);
		
	}
	
	/**
	 * Utility function that adds a set of Obs to an Encounter, skipping Obs that are already part
	 * of the Encounter
	 *
	 * @param encounter
	 * @param group
	 */
	private void addObsToEncounterIfNotAlreadyThere(Encounter encounter, Obs group) {
		for (Obs obs : encounter.getObsAtTopLevel(true)) {
			if (obs.equals(group)) {
				return;
			}
		}
		encounter.addObs(group);
	}
	
	/**
	 * Removes the most recently added ObsGroup from the submission stack. All objects added after
	 * that ObsGroup are removed as well.
	 *
	 * @throws InvalidActionException
	 */
	public void endObsGroup() throws InvalidActionException {
		// there needs to be an Obs on the stack before this
		if (!stackContains(Obs.class)) {
			throw new InvalidActionException("No Obs on the stack");
		}
		while (true) {
			Object o = stack.pop();
			if (o instanceof Obs) {
				break;
			}
		}
	}
	
	/**
	 * Returns the Person that was most recently added to the stack
	 *
	 * @return the Person most recently added to the stack
	 */
	public CohortM getCurrentCohort() {
		return highestOnStack(CohortM.class);
	}
	
	/**
	 * Returns the Encounter that was most recently added to the stack
	 *
	 * @return the Encounter most recently added to the stack
	 */
	public Encounter getCurrentEncounter() {
		return highestOnStack(Encounter.class);
	}
	
	/**
	 * Utility method that returns the object of a specified class that was most recently added to
	 * the stack
	 */
	@SuppressWarnings("unchecked")
	private <T> T highestOnStack(Class<T> clazz) {
		for (ListIterator<Object> iter = stack.listIterator(stack.size()); iter.hasPrevious(); ) {
			Object o = iter.previous();
			if (clazz.isAssignableFrom(o.getClass())) {
				return (T) o;
			}
		}
		return null;
	}
	
	/**
	 * Utility method that tests whether there is an object of the specified type on the stack
	 */
	private boolean stackContains(Class<?> clazz) {
		for (Object o : stack) {
			if (clazz.isAssignableFrom(o.getClass())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates an new Obs and associates with the most recently added Person, Encounter, and
	 * ObsGroup (if applicable) on the stack.
	 * <p/>
	 * Note that this method does not actually commit the Obs to the database, but instead adds the
	 * Obs to a list of Obs to be added. The changes are applied elsewhere in the framework.
	 *
	 * @param concept         concept associated with the Obs
	 * @param value           value for the Obs
	 * @param datetime        date information for the Obs
	 * @param accessionNumber accession number for the Obs
	 * @param comment         comment for the obs
	 * @return the Obs to create
	 */
	public Obs createObs(Concept concept, Object value, Date datetime, String accessionNumber, String comment) {
		if (value == null || "".equals(value)) {
			throw new IllegalArgumentException("Cannot create Obs with null or blank value");
		}
		Obs obs = HtmlFormEntryUtil.createObs(concept, value, datetime, accessionNumber);
		CohortM cohort = highestOnStack(CohortM.class);
		if (cohort == null) {
			throw new IllegalArgumentException("Cannot create an Obs outside of a Cohort.");
		}
		Encounter encounter = highestOnStack(Encounter.class);
		Obs obsGroup = highestOnStack(Obs.class);
		
		/*if (cohort != null)
		{
			cobs.setCohort(cohort);
			obs=CohortFormEntrySession.convertToObs(cobs);
		}*/
		if (StringUtils.isNotBlank(comment)) {
			obs.setComment(comment);
		}
		
		if (encounter != null) {
			encounter.addObs(obs);
		}
		if (obsGroup != null) {
			obsGroup.addGroupMember(obs);
		} else {
			obsToCreate.add(obs);
		}
		return obs;
	}
	
	/**
	 * Legacy createObs methods without the comment argument
	 */
	public Obs createObs(Concept concept, Object value, Date datetime, String accessionNumber) {
		return createObs(concept, value, datetime, accessionNumber, null);
	}
	
	
	/**
	 * Modifies an existing Obs.
	 * <p/>
	 * This method works by adding the current Obs to a list of Obs to void, and then adding the new
	 * Obs to a list of Obs to create. Note that this method does not commit the changes to the
	 * database--the changes are applied elsewhere in the framework.
	 *
	 * @param existingObs     the Obs to modify
	 * @param concept         concept associated with the Obs
	 * @param newValue        the new value of the Obs
	 * @param newDatetime     the new date information for the Obs
	 * @param accessionNumber new accession number for the Obs
	 * @param comment         comment for the obs
	 */
	public void modifyObs(Obs existingObs, Concept concept, Object newValue, Date newDatetime, String accessionNumber, String comment) {
		if (newValue == null || "".equals(newValue)) {
			// we want to delete the existing obs
			if (log.isDebugEnabled()) {
				log.debug("VOID: " + printObsHelper(existingObs));
			}
			obsToVoid.add(existingObs);
			return;
		}
		if (concept == null) {
			// we want to delete the existing obs
			if (log.isDebugEnabled()) {
				log.debug("VOID: " + printObsHelper(existingObs));
			}
			obsToVoid.add(existingObs);
			return;
		}
		Obs newObs = HtmlFormEntryUtil.createObs(concept, newValue, newDatetime, accessionNumber);
		String oldString = existingObs.getValueAsString(Context.getLocale());
		String newString = newObs.getValueAsString(Context.getLocale());
		if (log.isDebugEnabled() && concept != null) {
			log.debug("For concept " + concept.getName(Context.getLocale(), false) + ": " + oldString + " -> " + newString);
		}
		boolean valueChanged = !newString.equals(oldString);
		// TODO: handle dates that may equal encounter date
		boolean dateChanged = dateChangedHelper(existingObs.getObsDatetime(), newObs.getObsDatetime());
		boolean accessionNumberChanged = accessionNumberChangedHelper(existingObs.getAccessionNumber(),
				newObs.getAccessionNumber());
		boolean conceptsHaveChanged = false;
		if (!existingObs.getConcept().getConceptId().equals(concept.getConceptId())) {
			conceptsHaveChanged = true;
		}
		if (valueChanged || dateChanged || accessionNumberChanged || conceptsHaveChanged) {
			if (log.isDebugEnabled()) {
				log.debug("CHANGED: " + printObsHelper(existingObs));
			}
			// TODO: really the voided obs should link to the new one, but this is a pain to implement due to the dreaded error: org.hibernate.NonUniqueObjectException: a different object with the same identifier value was already associated with the session
			obsToVoid.add(existingObs);
			createObs(concept, newValue, newDatetime, accessionNumber, comment);
		} else {
			if (existingObs != null && StringUtils.isNotBlank(comment)) {
				existingObs.setComment(comment);
			}
			
			if (log.isDebugEnabled()) {
				log.debug("SAME: " + printObsHelper(existingObs));
			}
		}
	}
	
	/**
	 * Legacy modifyObs methods without the comment argument
	 */
	public void modifyObs(Obs existingObs, Concept concept, Object newValue, Date newDatetime, String accessionNumber) {
		modifyObs(existingObs, concept, newValue, newDatetime, accessionNumber, null);
	}
	
	/**
	 * This method compares Timestamps to plain Dates by dropping the nanosecond precision
	 */
	private boolean dateChangedHelper(Date oldVal, Date newVal) {
		if (newVal == null) {
			return false;
		} else {
			return oldVal.getTime() != newVal.getTime();
		}
	}
	
	private boolean accessionNumberChangedHelper(String oldVal, String newVal) {
		return !OpenmrsUtil.nullSafeEquals(oldVal, newVal);
	}
	
	private String printObsHelper(Obs obs) {
		return obs.getConcept().getName(Context.getLocale(), false) + " = " + obs.getValueAsString(Context.getLocale());
	}
	
	/**
	 * Returns true/false if we need to save the patient record during form submissiosn
	 *
	 * @return
	 */
	public Boolean getCohortUpdateRequired() {
		return cohortUpdateRequired;
	}
	
	/**
	 * Set whether we need to save the patient record during form submission
	 *
	 * @param cohortUpdateRequired
	 */
	
	public void setCohortUpdateRequired(Boolean cohortUpdateRequired) {
		this.cohortUpdateRequired = cohortUpdateRequired;
	}
	
	/**
	 * Returns a list of all the Cohorts that need to be created to process form submission
	 *
	 * @return a list of all Cohorts to create
	 */
	public List<CohortM> getCohortsToCreate() {
		return cohortsToCreate;
	}
	
	
	/**
	 * Sets the list of Cohorts that need to be created to process form submission
	 *
	 * @param cohortsToCreate the list of Cohorts to create
	 */
	public void setCohortsToCreate(List<CohortM> cohortsToCreate) {
		this.cohortsToCreate = cohortsToCreate;
	}
	
	/**
	 * Returns a list of all the Encounters that need to be created to process form submissions
	 *
	 * @return a list of Encounters to create
	 */
	public List<Encounter> getEncountersToCreate() {
		return encountersToCreate;
	}
	
	/**
	 * Sets the list of Encounters that need to be created to process form submission
	 *
	 * @param encountersToCreate the list of Encounters to create
	 */
	public void setEncountersToCreate(List<Encounter> encountersToCreate) {
		this.encountersToCreate = encountersToCreate;
	}
	
	/**
	 * Returns the list of Encounters that need to be edited to process form submission
	 *
	 * @return the list of Encounters to edit
	 */
	public List<Encounter> getEncountersToEdit() {
		return encountersToEdit;
	}
	
	/**
	 * Sets the list of Encounters that need to be editing to process form submission
	 *
	 * @param encountersToEdit the list of Encounters to edit
	 */
	public void setEncountersToEdit(List<Encounter> encountersToEdit) {
		this.encountersToEdit = encountersToEdit;
	}
	
	/**
	 * Returns the list of Obs that need to be created to process form submission
	 *
	 * @return the list of Obs to create
	 */
	public List<Obs> getObsToCreate() {
		return obsToCreate;
	}
	
	/**
	 * Sets the list of Obs that need to be created to process form submission
	 *
	 * @param obsToCreate the list of Obs to create
	 */
	public void setObsToCreate(List<Obs> obsToCreate) {
		this.obsToCreate = obsToCreate;
	}
	
	/**
	 * Returns the list of Os that need to be voided to process form submission
	 *
	 * @return the list of Obs to void
	 */
	public List<Obs> getObsToVoid() {
		return obsToVoid;
	}
	
	/**
	 * Sets the list Obs that need to be voided to process form submission
	 *
	 * @param obsToVoid the list of Obs to void
	 */
	public void setObsToVoid(List<Obs> obsToVoid) {
		this.obsToVoid = obsToVoid;
	}
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cohort;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.BatchSize;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.util.Format;
import org.openmrs.util.Format.FORMAT_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "cohort_obs")
public class CohortObs extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(CohortObs.class);
	
	private static DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	
	private static DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "obs_id")
	private Integer cohortObsId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "concept_id")
	protected Concept concept;
	
	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	@JoinColumn(name = "encounter_id")
	private CohortEncounter encounter;
	
	@ManyToOne
	@JoinColumn(name = "obs_group_id")
	protected CohortObs obsGroup;
	
	@Column(name = "obs_datetime")
	private Date obsDateTime;
	
	@OneToMany(cascade = CascadeType.ALL)
	@BatchSize(size = 25)
	@OrderBy("cohort_obs_id")
	protected Set<CohortObs> groupMembers;
	
	@ManyToOne
	@JoinColumn(name = "value_coded")
	protected Concept valueCoded;
	
	@Transient
	protected transient ConceptName valueCodedName;
	
	@Column(name = "value_group_id")
	protected Integer valueGroupId;
	
	@Column(name = "value_datetime")
	protected Date valueDatetime;
	
	@Column(name = "value_numeric")
	protected Double valueNumeric;
	
	@Column(name = "value_modifier", length = 2)
	protected String valueModifier;
	
	@Column(name = "value_text", length = 65535)
	protected String valueText;
	
	@Column(name = "value_complex")
	protected String valueComplex;
	
	@Column(name = "accession_number")
	protected String accessionNumber;
	
	protected String comment;
	
	@Transient
	protected transient ComplexData complexData;
	
	public Integer getCohortObsId() {
		return cohortObsId;
	}
	
	public void setCohortObsId(Integer cohortObsId) {
		this.cohortObsId = cohortObsId;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	public CohortEncounter getEncounterId() {
		return encounter;
	}
	
	public void setEncounterId(CohortEncounter encounterId) {
		this.encounter = encounterId;
	}
	
	public Date getObsDateTime() {
		return obsDateTime;
	}
	
	public void setObsDateTime(Date obsDateTime) {
		this.obsDateTime = obsDateTime;
	}
	
	public CohortObs getObsGroup() {
		return obsGroup;
	}
	
	public void setObsGroup(CohortObs obsGroup) {
		this.obsGroup = obsGroup;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Concept getValueCoded() {
		return valueCoded;
	}
	
	public void setValueCoded(Concept valueCoded) {
		this.valueCoded = valueCoded;
	}
	
	public ConceptName getValueCodedName() {
		return valueCodedName;
	}
	
	public void setValueCodedName(ConceptName valueCodedName) {
		this.valueCodedName = valueCodedName;
	}
	
	public Integer getValueGroupId() {
		return valueGroupId;
	}
	
	public void setValueGroupId(Integer valueGroupId) {
		this.valueGroupId = valueGroupId;
	}
	
	public Date getValueDatetime() {
		return valueDatetime;
	}
	
	public void setValueDatetime(Date valueDatetime) {
		this.valueDatetime = valueDatetime;
	}
	
	public Double getValueNumeric() {
		return valueNumeric;
	}
	
	public void setValueNumeric(Double valueNumeric) {
		this.valueNumeric = valueNumeric;
	}
	
	public String getValueModifier() {
		return valueModifier;
	}
	
	public void setValueModifier(String valueModifier) {
		this.valueModifier = valueModifier;
	}
	
	public String getValueText() {
		return valueText;
	}
	
	public void setValueText(String valueText) {
		this.valueText = valueText;
	}
	
	public ComplexData getComplexData() {
		return complexData;
	}
	
	public void setComplexData(ComplexData complexData) {
		this.complexData = complexData;
	}
	
	public String getValueComplex() {
		return this.valueComplex;
	}
	
	/**
	 * Set the value for the ComplexData. This method is used by the ComplexObsHandler. The valueComplex
	 * has two parts separated by a bar '|' character: part A) the title; and part B) the URI. The title
	 * is the readable description of the valueComplex that is returned by Obs.getValueAsString(). The
	 * URI is the location where the ComplexData is stored.
	 *
	 * @param valueComplex readable title and URI for the location of the ComplexData binary object.
	 * @since 1.5
	 */
	public void setValueComplex(String valueComplex) {
		this.valueComplex = valueComplex;
	}
	
	public String getAccessionNumber() {
		return accessionNumber;
	}
	
	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}
	
	public void setGroupMembers(Set<CohortObs> groupMembers) {
		this.groupMembers = groupMembers;
	}
	
	@Override
	public Integer getId() {
		return getCohortObsId();
	}
	
	@Override
	public void setId(Integer id) {
		setCohortObsId(id);
	}
	
	public boolean hasGroupMembers(boolean includeVoided) {
		// ! symbol used because if it's not empty, we want true
		return !org.springframework.util.CollectionUtils.isEmpty(getGroupMembers(includeVoided));
	}
	
	public boolean hasGroupMembers() {
		return hasGroupMembers(false);
	}
	
	public Set<CohortObs> getGroupMembers() {
		return getGroupMembers(false); //same as just returning groupMembers
	}
	
	public boolean isObsGrouping() {
		return hasGroupMembers(true);
	}
	
	public Set<CohortObs> getGroupMembers(boolean includeVoided) {
		if (includeVoided) {
			//just return all group members
			return groupMembers;
		}
		if (groupMembers == null) {
			//Empty set so return null
			return null;
		}
		Set<CohortObs> nonVoided = new LinkedHashSet<CohortObs>(groupMembers);
		Iterator<CohortObs> i = nonVoided.iterator();
		while (i.hasNext()) {
			CohortObs obs = i.next();
			if (obs.getVoided() != null && obs.getVoided()) {
				i.remove();
			}
		}
		return nonVoided;
	}
	
	public String getValueAsString(Locale locale) {
		// formatting for the return of numbers of type double
		NumberFormat nf = NumberFormat.getNumberInstance(locale);
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern("#0.0#####"); // formatting style up to 6 digits
		//branch on hl7 abbreviations
		if (getConcept() != null) {
			String abbrev = getConcept().getDatatype().getHl7Abbreviation();
			if ("BIT".equals(abbrev)) {
				return getValueAsBoolean() == null ? "" : getValueAsBoolean().toString();
			} else if ("CWE".equals(abbrev)) {
				if (getValueCoded() == null) {
					return "";
				} else {
					ConceptName valueCodedName = getValueCodedName();
					if (valueCodedName != null) {
						return getValueCoded().getName(locale, false).getName();
					} else {
						ConceptName fallbackName = getValueCoded().getName();
						if (fallbackName != null) {
							return fallbackName.getName();
						} else {
							return "";
						}
						
					}
				}
			} else if ("NM".equals(abbrev) || "SN".equals(abbrev)) {
				if (getValueNumeric() == null) {
					return "";
				} else {
					if (getConcept() instanceof ConceptNumeric) {
						ConceptNumeric cn = (ConceptNumeric) getConcept();
						if (cn.getAllowDecimal()) {
							double d = getValueNumeric();
							int i = (int) d;
							return Integer.toString(i);
						} else {
							df.format(getValueNumeric());
						}
					}
				}
			} else if ("DT".equals(abbrev)) {
				return (getValueDatetime() == null ? "" : DATE_FORMAT.format(getValueDatetime()));
			} else if ("TM".equals(abbrev)) {
				return (getValueDatetime() == null ? "" : Format.format(getValueDatetime(), locale, FORMAT_TYPE.TIME));
			} else if ("TS".equals(abbrev)) {
				return (getValueDatetime() == null ? "" : Format.format(getValueDatetime(), locale, FORMAT_TYPE.TIMESTAMP));
			} else if ("ST".equals(abbrev)) {
				return getValueText();
			} else if ("ED".equals(abbrev) && getValueComplex() != null) {
				String[] valueComplex = getValueComplex().split("\\|");
				for (int i = 0; i < valueComplex.length; i++) {
					if (StringUtils.isNotEmpty(valueComplex[i])) {
						return valueComplex[i].trim();
					}
				}
			}
		}
		
		// if the datatype is 'unknown', default to just returning what is not null
		if (getValueNumeric() != null) {
			return df.format(getValueNumeric());
		} else if (getValueCoded() != null) {
			ConceptName valudeCodedName = getValueCodedName();
			if (valudeCodedName != null) {
				return valudeCodedName.getName();
			} else {
				return "";
			}
		} else if (getValueDatetime() != null) {
			return Format.format(getValueDatetime(), locale, FORMAT_TYPE.DATE);
		} else if (getValueText() != null) {
			return getValueText();
		} else if (hasGroupMembers()) {
			// all of the values are null and we're an obs group...so loop
			// over the members and just do a getValueAsString on those
			// this could potentially cause an infinite loop if an obs group
			// is a member of its own group at some point in the hierarchy
			StringBuilder sb = new StringBuilder();
			for (CohortObs groupMember : getGroupMembers()) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(groupMember.getValueAsString(locale));
			}
			return sb.toString();
		}
		
		// returns the title portion of the valueComplex
		// which is everything before the first bar '|' character.
		if (getValueComplex() != null) {
			String[] valueComplex = getValueComplex().split("\\|");
			for (int i = 0; i < valueComplex.length; i++) {
				if (StringUtils.isNotEmpty(valueComplex[i])) {
					return valueComplex[i].trim();
				}
			}
		}
		
		return "";
	}
	
	public Boolean getValueAsBoolean() {
		
		if (getValueCoded() != null) {
			if (getValueCoded().equals(Context.getConceptService().getTrueConcept())) {
				return Boolean.TRUE;
			} else if (getValueCoded().equals(Context.getConceptService().getFalseConcept())) {
				return Boolean.FALSE;
			}
		} else if (getValueNumeric() != null) {
			if (getValueNumeric() == 1) {
				return Boolean.TRUE;
			} else if (getValueNumeric() == 0) {
				return Boolean.FALSE;
			}
		}
		//returning null is preferred to defaulting to false to support validation of user input is from a form
		return null;
	}
	
	/**
	 * Returns the boolean value if the concept of this obs is of boolean datatype
	 *
	 * @return true or false if value is set otherwise null
	 * @should return true if value coded answer concept is true concept
	 * @should return false if value coded answer concept is false concept
	 */
	public Boolean getValueBoolean() {
		if (getConcept() != null && valueCoded != null && getConcept().getDatatype().isBoolean()) {
			Concept trueConcept = Context.getConceptService().getTrueConcept();
			return trueConcept != null && valueCoded.getId().equals(trueConcept.getId());
		}
		
		return null;
	}
	
	public void setValueAsString(String s) throws ParseException {
		if (log.isDebugEnabled()) {
			log.debug("getConcept() == " + getConcept());
		}
		
		if (getConcept() != null && !StringUtils.isBlank(s)) {
			String abbrev = getConcept().getDatatype().getHl7Abbreviation();
			if ("BIT".equals(abbrev)) {
				setValueBoolean(Boolean.valueOf(s));
			} else if ("CWE".equals(abbrev)) {
				throw new RuntimeException("Not Yet Implemented");
			} else if ("NM".equals(abbrev) || "SN".equals(abbrev)) {
				setValueNumeric(Double.valueOf(s));
			} else if ("DT".equals(abbrev)) {
				setValueDatetime(DATE_FORMAT.parse(s));
			} else if ("TM".equals(abbrev)) {
				setValueDatetime(TIME_FORMAT.parse(s));
			} else if ("TS".equals(abbrev)) {
				setValueDatetime(DATE_TIME_FORMAT.parse(s));
			} else if ("ST".equals(abbrev)) {
				setValueText(s);
			} else {
				throw new RuntimeException("Don't know how to handle " + abbrev);
			}
			
		} else {
			throw new RuntimeException("concept is null for " + this);
		}
	}
	
	@Deprecated
	public Map<Locale, String> getValueAsString() {
		Map<Locale, String> localeMap = new HashMap<Locale, String>();
		Locale[] locales = Locale.getAvailableLocales(); // ABKTODO: get actual available locales
		for (int i = 0; i < locales.length; i++) {
			localeMap.put(locales[i], getValueAsString(locales[i]));
		}
		return localeMap;
	}
	
	public void setValueBoolean(Boolean valueBoolean) {
		if (valueBoolean != null && getConcept() != null && getConcept().getDatatype().isBoolean()) {
			setValueCoded(valueBoolean.booleanValue() ? Context.getConceptService().getTrueConcept()
			        : Context.getConceptService().getFalseConcept());
		} else if (valueBoolean == null) {
			setValueCoded(null);
		}
	}
}

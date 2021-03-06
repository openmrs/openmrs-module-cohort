package org.openmrs.module.cohort;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.api.context.Context;

@Entity
@Table(name = "cohort_attribute")
public class CohortAttribute extends BaseOpenmrsData {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "cohort_attribute_id")
	private Integer cohortAttributeId;

	@ManyToOne
	@JoinColumn(name = "cohort_id")
	private CohortM cohort;

	private String value;

	@ManyToOne
	@JoinColumn(name = "cohort_attribute_type_id")
	private CohortAttributeType cohortAttributeType;

	public Integer getCohortAttributeId() {
		return cohortAttributeId;
	}

	public void setCohortAttributeId(Integer cohortAttributeId) {
		this.cohortAttributeId = cohortAttributeId;
	}

	public CohortM getCohort() {
		return cohort;
	}

	public void setCohort(CohortM cohort) {
		this.cohort = cohort;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public CohortAttributeType getCohortAttributeType() {
		return cohortAttributeType;
	}

	public void setCohortAttributeType(CohortAttributeType cohortAttributeType) {
		this.cohortAttributeType = cohortAttributeType;
	}

	/**
	 * Convenience method for voiding this attribute
	 *
	 * @param reason
	 * @should set voided bit to true
	 */
	public void voidAttribute(String reason) {
		setVoided(true);
		setVoidedBy(Context.getAuthenticatedUser());
		setVoidReason(reason);
		setDateVoided(new Date());
	}

	@Override
	public Integer getId() {
		return getCohortAttributeId();
	}

	@Override
	public void setId(Integer arg0) {
		setCohortAttributeId(arg0);

	}

}

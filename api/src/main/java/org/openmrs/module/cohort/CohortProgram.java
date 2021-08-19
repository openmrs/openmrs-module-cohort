package org.openmrs.module.cohort;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.openmrs.BaseOpenmrsData;

@Entity
@Table(name = "cohort_program")
public class CohortProgram extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "cohort_program_id")
	private int cohortProgramId;
	
	private String name;
	
	private String description;
	
	@Override
	public Integer getId() {
		return getCohortProgramId();
	}
	
	@Override
	public void setId(Integer id) {
		setCohortProgramId(id);
	}
	
	public int getCohortProgramId() {
		return cohortProgramId;
	}
	
	public void setCohortProgramId(int cohortProgramId) {
		this.cohortProgramId = cohortProgramId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}

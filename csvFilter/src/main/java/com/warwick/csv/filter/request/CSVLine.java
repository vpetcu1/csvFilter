package com.warwick.csv.filter.request;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CSVLine {

	public int id;
	public List<Integer> vars;
	public int decision;

	public List<Integer> getVars() {
		return vars;
	}

	public void setVars(List<Integer> vars) {
		this.vars = vars;
	}

	public int getId() {
		return id;
	}

	public int getDecision() {
		return decision;
	}

	public void setId(int id) {
		this.id = id;
	}


	public void setDecision(int decision) {
		this.decision = decision;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + decision;
		result = prime * result + id;
		result = prime * result + ((vars == null) ? 0 : vars.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CSVLine other = (CSVLine) obj;
		if (decision != other.decision)
			return false;
		if (id != other.id)
			return false;
		if (vars == null) {
			if (other.vars != null)
				return false;
		} else if (!vars.equals(other.vars))
			return false;
		return true;
	}



}

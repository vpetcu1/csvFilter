package com.warwick.test.pojo;

public class CSVLine {

	private int id;
	private int var;
	private int decision;

	public int getId() {
		return id;
	}

	public int getVar() {
		return var;
	}

	public int getDecision() {
		return decision;
	}

	public CSVLine(int id, int var, int decision) {
		super();
		this.id = id;
		this.var = var;
		this.decision = decision;
	}
	
	public CSVLine(String... values) throws IllegalArgumentException {
			this.id = new Integer(values[0]);
			this.var = new Integer(values[1]);
			this.decision = new Integer(values[2]);
	}

}

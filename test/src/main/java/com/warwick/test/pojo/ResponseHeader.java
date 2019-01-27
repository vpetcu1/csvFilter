package com.warwick.test.pojo;

import java.util.List;

public class ResponseHeader {

	private boolean ok;
	private List<ResponseError> errors;

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public List<ResponseError> getErrors() {
		return errors;
	}

	public void setErrors(List<ResponseError> errors) {
		this.errors = errors;
	}

}

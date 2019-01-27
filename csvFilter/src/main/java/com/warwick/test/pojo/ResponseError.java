package com.warwick.test.pojo;

public class ResponseError {
	
	private int errorCode;
	private String message;

	public int getErrorCode() {
		return errorCode;
	}

	public ResponseError setErrorCode(int errorCode) {
		this.errorCode = errorCode;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public ResponseError setMessage(String message) {
		this.message = message;
		return this;
	}

}

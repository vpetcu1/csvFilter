package com.warwick.test.pojo;

public enum ErrorCode {
	
	INTERNAL_SERVER_ERROR(1), BAD_REQUEST(2);
	
	private int code;
	
	ErrorCode(int code) {
		this.code = code;
    }

	public int getCode() {
		return code;
	}
	
}

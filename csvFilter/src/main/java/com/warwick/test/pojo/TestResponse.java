package com.warwick.test.pojo;

import java.util.List;

public class TestResponse {

	private ResponseHeader header;
	private List<CSVLine> result;

	public ResponseHeader getHeader() {
		return header;
	}

	public void setHeader(ResponseHeader header) {
		this.header = header;
	}

	public List<CSVLine> getResult() {
		return result;
	}

	public void setResult(List<CSVLine> result) {
		this.result = result;
	}

}

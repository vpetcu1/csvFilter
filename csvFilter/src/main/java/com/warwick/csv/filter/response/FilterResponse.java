package com.warwick.csv.filter.response;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.warwick.csv.filter.request.CSVLine;

@XmlRootElement
public class FilterResponse {

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

package com.warwick.test.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SerializedFile {

	private String fileName;
	private String fileContent;

	/**
	 * @return the fileContent
	 */
	public String getFileContent() {
		return this.fileContent;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * @param fileContent
	 *            the fileContent to set
	 */
	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}

package com.warwick.test.pojo;

import javax.xml.bind.annotation.XmlRootElement;

public class SerializedFile {

	private String base64;
	private String filename;
	private int filesize;
	private String filetype;

	/**
	 * @return the base64
	 */
	public String getBase64() {
		return this.base64;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * @return the filesize
	 */
	public int getFilesize() {
		return this.filesize;
	}

	/**
	 * @return the filetype
	 */
	public String getFiletype() {
		return this.filetype;
	}

	/**
	 * @param base64
	 *            the base64 to set
	 */
	public void setBase64(String base64) {
		this.base64 = base64;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @param filesize
	 *            the filesize to set
	 */
	public void setFilesize(int filesize) {
		this.filesize = filesize;
	}

	/**
	 * @param filetype
	 *            the filetype to set
	 */
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}

}

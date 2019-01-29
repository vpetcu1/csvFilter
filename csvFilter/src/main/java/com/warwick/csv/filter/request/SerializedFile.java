package com.warwick.csv.filter.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SerializedFile {

	@NotNull(message="base64 must not be null")
	private String base64;
	@NotNull(message="filename must not be null")
	private String filename;
	@Max(22000)
	@Min(1)
	private int filesize;
	@NotNull(message="filetype must not be null")
	@Pattern(regexp = "application/vnd.ms-excel", flags = Pattern.Flag.CASE_INSENSITIVE)
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
	public SerializedFile setBase64(String base64) {
		this.base64 = base64;
		return this;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public SerializedFile setFilename(String filename) {
		this.filename = filename;
		return this;
	}

	/**
	 * @param filesize
	 *            the filesize to set
	 */
	public SerializedFile setFilesize(int filesize) {
		this.filesize = filesize;
		return this;
	}

	/**
	 * @param filetype
	 *            the filetype to set
	 */
	public SerializedFile setFiletype(String filetype) {
		this.filetype = filetype;
		return this;
	}

}

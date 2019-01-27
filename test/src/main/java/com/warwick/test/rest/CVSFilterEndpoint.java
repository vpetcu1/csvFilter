package com.warwick.test.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.opencsv.CSVReader;
import com.warwick.test.pojo.ResponseError;
import com.warwick.test.pojo.ResponseHeader;
import com.warwick.test.pojo.SerializedFile;
import com.warwick.test.pojo.CSVLine;
import com.warwick.test.pojo.TestResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author E035
 *
 */
/**
 * @author E035
 *
 */
@Path("/cvs-filter")
@Api(protocols = "http", value = "/test", description = "Get filtered Results of the uploaded file", tags = "test")
@Produces(MediaType.APPLICATION_JSON)
public class CVSFilterEndpoint {

	final static String FILES_PATH = System.getProperty("java.io.tmpdir");
	private static final String FILE = "file";

	@POST
	@Path("/upload-base64")
	@ApiOperation(value = "Upload Base64 file", notes = "Returns the time as a string", response = Response.class)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response processCSVFileBase64(SerializedFile file) throws IOException {
		String fileName = saveFile(file.getBase64(), file.getFilename());
		TestResponse testResponse = processCVSFile(fileName);
		return Response.ok().entity(testResponse).build();
	}

	@POST
	@Path("/upload")
	@ApiOperation(value = "Get the current time", notes = "Returns the time as a string", response = Response.class)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response processCSVFile(MultipartFormDataInput input) throws IOException {
		System.out.println("input:" + input);
		String fileName = saveFile(input);
		TestResponse testResponse = processCVSFile(fileName);
		return Response.ok().entity(testResponse).build();
	}

	/**
	 * @param input Multipart Form that has the containing file
	 * @return
	 * @throws IOException
	 */
	private String saveFile(@NotNull MultipartFormDataInput input) throws IOException {
		List<String> fileNames = new ArrayList<String>();
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get(FILE);
		for (InputPart inputPart : inputParts) {
			System.out.println("inputPart:" + inputPart);
			MultivaluedMap<String, String> header = inputPart.getHeaders();
			String fileName = getFileName(header);
			InputStream inputStream = inputPart.getBody(InputStream.class, null);
			byte[] bytes = IOUtils.toByteArray(inputStream);
			fileNames.add(writeFile(bytes, fileName));
		}
		if(fileNames.size() == 0)
			throw new IOException("There are no parts for input named \"" + FILE + "\"");
		System.out.println(fileNames);
		System.out.println(fileNames.get(0));
		return fileNames.get(0);
		
	}

	/**
	 * @param base64FileContent content of file in Base64 format
	 * @param                   fileName.
	 * @return complete path of File
	 * @throws IOException
	 */
	private String saveFile(String base64FileContent, String fileName) throws IOException {
		byte[] bytes = Base64.getMimeDecoder().decode(base64FileContent);
		return writeFile(bytes, fileName);
	}

	/**
	 * @param content  of the file
	 * @param fileName of saved Files
	 * @return complete path of File
	 * @throws IOException
	 */
	private String writeFile(byte[] content, String fileName) throws IOException {
		File file = new File(FILES_PATH + File.separator + fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		try (FileOutputStream fop = new FileOutputStream(file)) {
			fop.write(content);
		}
		return file.getPath();
	}

	/**
	 * header sample { Content-Type=[image/png], Content-Disposition=[form-data;
	 * name="file"; filename="filename.extension"] }
	 * 
	 * @param header
	 * @return fileName
	 */
	private String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
		String fileName = null;
		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {
				String[] name = filename.split("=");
				fileName = name[1].trim().replaceAll("\"", "");
			}
		}
		if(fileName == null)
			throw new IllegalArgumentException("Invalid Multipart submit. Could not determine File Name.");
		return fileName;
	}

	/**
	 * @param fileName
	 * @return List of POJO's
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	private List<CSVLine> mapCSVtoPOJO(String fileName, boolean hasHeader) throws IOException {
		List<CSVLine> rows = new ArrayList<CSVLine>();
		try (CSVReader csvReader = new CSVReader(new FileReader(fileName));) {
			String[] values = null;
			if (hasHeader)
				csvReader.readNext();
			while ((values = csvReader.readNext()) != null) {
				try {
					rows.add(new CSVLine(values));
				} catch (Exception e) {
					throw new IllegalArgumentException("CSV File has invalid content.");
				}
			}
		}
		return rows;
	}

	/**
	 * @param fileNam. The complete path of file
	 * @return
	 * @throws IOException
	 */
	private TestResponse processCVSFile(String fileName) throws IOException {
		TestResponse testResponse = new TestResponse();
		List<ResponseError> errors = new ArrayList<ResponseError>();
		ResponseHeader responseHeader = new ResponseHeader();
		testResponse.setHeader(responseHeader);
		responseHeader.setErrors(errors);
		List<CSVLine> filteredLines = filterLines(mapCSVtoPOJO(fileName, true));
		testResponse.setResult(filteredLines);
		return testResponse;
	}

	/**
	 * @param lines
	 * @return A List with objects that pass-through filter
	 */
	private List<CSVLine> filterLines(List<CSVLine> lines) {
		return lines;
	}

}

package com.warwick.test.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("/test")
@Api(protocols = "http", value = "/test", description = "Get filtered Results of the uploaded file", tags = "test")
@Produces(MediaType.APPLICATION_JSON)
public class TestEndpoint {

	final static String FILES_PATH = System.getProperty("java.io.tmpdir");

	// Convert a Base64 string and create a file
	private String convertFile(String dataBase64) throws IOException {
		byte[] bytes = Base64.getDecoder().decode(dataBase64);
		String uuid = UUID.randomUUID().toString();
		File file = new File(FILES_PATH + File.separator + uuid);

		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(bytes);
			fos.flush();
		}

		return uuid;
	}

	@GET
	@Path("/hello")
	@Produces("text/plain")
	public Response doGet() {
		return Response.ok("Hello from Thorntail!").build();
	}

	@POST
	@Path("/upload-b64")
	@ApiOperation(value = "Upload Base64 file", notes = "Returns the time as a string", response = Response.class)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response processCSVFileBase64(SerializedFile file, @Context UriInfo uriInfo) throws IOException {
		String id = convertFile(file.getFileContent());
		// String fileName = json.getString("file_name");
		// String mimeType = json.getString("mime_type");

		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		builder.path(id);
		return Response.created(builder.build()).build();
	}

	@POST
	@Path("/upload")
	@ApiOperation(value = "Get the current time", notes = "Returns the time as a string", response = Response.class)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFile(MultipartFormDataInput input) {

		String fileName = "";
		
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get("file");

		for (InputPart inputPart : inputParts) {

		 try {

			MultivaluedMap<String, String> header = inputPart.getHeaders();
			fileName = getFileName(header);

			//convert the uploaded file to inputstream
			InputStream inputStream = inputPart.getBody(InputStream.class,null);

			byte [] bytes = IOUtils.toByteArray(inputStream);
				
			//constructs upload file path
			fileName = FILES_PATH + fileName;
				
			writeFile(bytes,fileName);
				
			System.out.println("Done");

		  } catch (IOException e) {
			e.printStackTrace();
		  }

		}

		return Response.status(200)
		    .entity("{\"message\": \"success\", \"status\": 200}").build();

	}

	/**
	 * header sample
	 * {
	 * 	Content-Type=[image/png], 
	 * 	Content-Disposition=[form-data; name="file"; filename="filename.extension"]
	 * }
	 **/
	//get uploaded filename, is there a easy way in RESTEasy?
	private String getFileName(MultivaluedMap<String, String> header) {

		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
		
		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");
				
				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

	//save to somewhere
	private void writeFile(byte[] content, String filename) throws IOException {

		File file = new File(filename);

		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream fop = new FileOutputStream(file);

		fop.write(content);
		fop.flush();
		fop.close();

	}	
}

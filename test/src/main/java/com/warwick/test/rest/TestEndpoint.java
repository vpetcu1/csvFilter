package com.warwick.test.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

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
	@Path("/process")
	@ApiOperation(value = "Get the current time", notes = "Returns the time as a string", response = Response.class)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response processCSVFile(SerializedFile file, @Context UriInfo uriInfo) throws IOException {
		String id = convertFile(file.getFileContent());
		// String fileName = json.getString("file_name");
		// String mimeType = json.getString("mime_type");

		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
		builder.path(id);
		return Response.created(builder.build()).build();
	}

}

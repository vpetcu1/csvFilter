package com.warwick.csv.filter.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import com.opencsv.CSVReader;
import com.warwick.csv.filter.request.CSVLine;
import com.warwick.csv.filter.request.SerializedFile;
import com.warwick.csv.filter.response.FilterResponse;

@RunWith(Arquillian.class)
public class RestArquilianTest {

	private static final String RESOURCE_PREFIX = CSVFilterRestApplication.class.getAnnotation(ApplicationPath.class)
			.value().substring(1);

	@Deployment(testable = false)
	public static Archive createDeployment() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("project-test-defaults-path.yml");
		assertNotNull(url);
		File projectDefaults = new File(url.toURI());
		JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "csvFilter.war");
		deployment.addClass(CSVFilterEndpoint.class);
		deployment.addClass(SerializedFile.class);
		deployment.addPackage("com.warwick.csv.filter.exception");
		deployment.addPackage("com.warwick.csv.filter.request");
		deployment.addPackage("com.warwick.csv.filter.response");
		deployment.addPackage("com.warwick.csv.filter.rest");
		deployment.setContextRoot("/");
		deployment.addAsResource(projectDefaults, "/project-defaults.yml");
		deployment.addAllDependencies();
		return deployment;
	}

	@ArquillianResource
	URL deploymentUrl;

	@Test
	public void testUploadBase64EmptySerializedFile() throws Exception {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/csv-filter/upload-base64");
		Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
		Entity<SerializedFile> entity = Entity.entity(new SerializedFile(), MediaType.APPLICATION_JSON);
		Response response = builder.post(entity);
		assertEquals(400, response.getStatus());
		FilterResponse filterResponse = response.readEntity(FilterResponse.class);
		assertEquals(false, filterResponse.getHeader().isOk());
		assertEquals(4, filterResponse.getHeader().getErrors().size());
		for (int i = 0; i < filterResponse.getHeader().getErrors().size(); i++) {
			assertEquals(2, filterResponse.getHeader().getErrors().get(i).getErrorCode());
		}
	}

	@Test
	public void testUploadBase64InvalidFile() throws Exception {
		String fileName = "project-test-defaults-path.yml";
		String path = "/csv-filter/upload-base64";
		Response response = uploadFile(fileName, path);
		assertEquals(400, response.getStatus());
		FilterResponse filterResponse = response.readEntity(FilterResponse.class);
		assertEquals(false, filterResponse.getHeader().isOk());
		assertEquals(1, filterResponse.getHeader().getErrors().size());
		for (int i = 0; i < filterResponse.getHeader().getErrors().size(); i++) {
			assertEquals(2, filterResponse.getHeader().getErrors().get(i).getErrorCode());
		}
	}

	@Test
	public void testUploadBase64ExampleA() throws Exception {
		String fileName = "exampleA_input.csv";
		String path = "/csv-filter/upload-base64";
		Response response = uploadFile(fileName, path);
		assertEquals(200, response.getStatus());
		FilterResponse filterResponse = response.readEntity(FilterResponse.class);
		assertTrue(isOuputExpected(filterResponse, "exampleA_output.csv"));
	}
	
	@Test
	public void testUploadBase64ExampleB() throws Exception {
		String fileName = "exampleB_input.csv";
		String path = "/csv-filter/upload-base64";
		Response response = uploadFile(fileName, path);
		assertEquals(200, response.getStatus());
		FilterResponse filterResponse = response.readEntity(FilterResponse.class);
		assertTrue(isOuputExpected(filterResponse, "exampleB_output.csv"));
	}
	
	@Test
	public void testUploadBase64ExampleC() throws Exception {
		String fileName = "exampleC_input.csv";
		String path = "/csv-filter/upload-base64";
		Response response = uploadFile(fileName, path);
		assertEquals(200, response.getStatus());
		FilterResponse filterResponse = response.readEntity(FilterResponse.class);
		assertTrue(isOuputExpected(filterResponse, "exampleC_output.csv"));
	}

	@Test
	public void testUploadEmptyRequest() throws Exception {
		Client client = new ResteasyClientBuilder().build();
		WebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/csv-filter/upload");
		MultipartFormDataOutput mdo = new MultipartFormDataOutput();
		GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(mdo) {
		};
		Response response = target.request().post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
		assertEquals(400, response.getStatus());
	}

	@Test
	public void testUploadMultipartExampleA() throws Exception {
		Client client = new ResteasyClientBuilder().build();
		WebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/csv-filter/upload");
		MultipartFormDataOutput mdo = new MultipartFormDataOutput();
		mdo.addFormData("file", this.getClass().getClassLoader().getResourceAsStream("exampleA_output.csv"),
				MediaType.APPLICATION_OCTET_STREAM_TYPE, "exampleA_output.csv");
		GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(mdo) {
		};
		Response response = target.request().post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));
		assertEquals(200, response.getStatus());
		FilterResponse filterResponse = response.readEntity(FilterResponse.class);
		assertTrue(isOuputExpected(filterResponse, "exampleA_output.csv"));
	}

	private boolean isOuputExpected(FilterResponse filterResponse, String outputFileName) throws IOException {
		boolean isOutputExpected = false;
		List<CSVLine> outputServiceLines = filterResponse.getResult();
		List<CSVLine> outputFileLines = mapCSVtoPOJO(outputFileName, true);
		if (outputServiceLines.size() == outputFileLines.size()) {
			isOutputExpected = true;
			for (int i = 0; i < outputServiceLines.size(); i++) {
				if (!outputServiceLines.get(i).equals(outputFileLines.get(i))) {
					isOutputExpected = true;
				}
			}
		}
		return isOutputExpected;
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
		try (CSVReader csvReader = new CSVReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(fileName)))) {
			String[] values = null;
			if (hasHeader)
				csvReader.readNext();
			while ((values = csvReader.readNext()) != null) {
				try {
					CSVLine line = new CSVLine();
					line.setId(new Integer(values[0]));
					List<Integer> vars = new ArrayList<Integer>();
					//fill the first and the last column
					line.setVars(vars);
					line.setDecision(new Integer(values[values.length-1]));
					for (int i = 1; i < values.length-1; i++) {
						vars.add(new Integer(values[i]));
					}
					rows.add(line);
				} catch (Exception e) {
					throw new IllegalArgumentException("CSV File has invalid content.");
				}
			}
		}
		return rows;
	}

	private Response uploadFile(String fileName, String path) throws IOException {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + path);
		Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);

		byte[] file = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(fileName));
		String base64 = Base64.getMimeEncoder().encodeToString(file);
		SerializedFile serializedFile = new SerializedFile();
		serializedFile.setFilename(fileName);
		serializedFile.setBase64(base64);
		serializedFile.setFilesize(file.length);
		serializedFile.setFiletype("application/vnd.ms-excel");

		Entity<SerializedFile> entity = Entity.entity(serializedFile, MediaType.APPLICATION_JSON);
		Response response = builder.post(entity);
		return response;
	}

}

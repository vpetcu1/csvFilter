package com.warwick.csv.filter.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
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
import com.warwick.csv.filter.request.CSVLine;
import com.warwick.csv.filter.request.SerializedFile;
import com.warwick.csv.filter.response.FilterResponse;
import com.warwick.csv.filter.response.ResponseError;
import com.warwick.csv.filter.response.ResponseHeader;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

@Path("/csv-filter")
@Api(protocols = "http", value = "/csv-filter", description = "Get filtered Results of the uploaded file", tags = "csv-filter")
@SwaggerDefinition (
info = @Info (
        title = "CSV Filter Example Service",
        description = "A simple example here",
        version = "1.0.0-SNAPSHOT",
        contact = @Contact (
            name = "Vasilica Petcu",
            email = "vpetcu1@gmail.com"
        )
    ),
    host = "localhost",
    basePath = "/api/",
    schemes = {SwaggerDefinition.Scheme.HTTP}
)
public class CSVFilterEndpoint {

	final static String FILES_PATH = System.getProperty("java.io.tmpdir");
	private static final String FILE = "file";

	@POST
	@Path("/upload-base64")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload Base64 file", notes = "Returns filtered lines", response = Response.class )
	@ApiResponses(value = {
			   @ApiResponse(code = 200, message = "Success, Returning filtered lines"),
			   @ApiResponse(code = 400, message = "Bad Request"),
			   @ApiResponse(code = 500, message = "Internal Server Error") })
	public Response processCSVFileBase64(@Valid @ApiParam(value = "test", 
            examples = @Example(value = { 
                    @ExampleProperty(mediaType="application/json", value="{\n" + 
                    		"  \"base64\": \"SWQsVmFyMSxEZWNpc2lvbg0KMSwxMCwwDQoyLDIwLDENCjMsMzAsMA0KNCw0MCwxDQo1LDUwLDANCg==\",\n" + 
                    		"  \"filename\": \"exampleA_input.csv\",\n" + 
                    		"  \"filesize\": 58,\n" + 
                    		"  \"filetype\": \"application/vnd.ms-excel\"\n" + 
                    		"}") 
            })) SerializedFile file) throws IOException {
		String fileName = saveFile(file.getBase64(), file.getFilename());
		FilterResponse filterResponse = processCSVFile(fileName);
		return Response.ok().entity(filterResponse).build();
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload Multipart file", notes = "Returns filtered lines", response = Response.class )
	@ApiResponses(value = {
			   @ApiResponse(code = 200, message = "Success, Returning filtered lines"),
			   @ApiResponse(code = 400, message = "Bad Request"),
			   @ApiResponse(code = 500, message = "Internal Server Error") })
	public Response processCSVFile(@NotNull MultipartFormDataInput input) throws IOException {
		String fileName = saveFile(input);
		FilterResponse filterResponse = processCSVFile(fileName);
		return Response.ok().entity(filterResponse).build();
	}

	/**
	 * @param input Multipart Form that has the containing file
	 * @return
	 * @throws IOException
	 */
	private String saveFile(MultipartFormDataInput input) throws IOException {
		List<String> fileNames = new ArrayList<String>();
		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get(FILE);
		if (inputParts == null || inputParts.size() == 0)
			throw new IllegalArgumentException("There are no parts for input named \"" + FILE + "\"");
		for (InputPart inputPart : inputParts) {
			MultivaluedMap<String, String> header = inputPart.getHeaders();
			String fileName = getFileName(header);
			InputStream inputStream = inputPart.getBody(InputStream.class, null);
			byte[] bytes = IOUtils.toByteArray(inputStream);
			fileNames.add(writeFile(bytes, fileName));
		}
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
		if (fileName == null)
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
					CSVLine line = new CSVLine();
					line.setId(new Integer(values[0]));
					List<Integer> vars = new ArrayList<Integer>();
					// fill the first and the last column
					line.setVars(vars);
					line.setDecision(new Integer(values[values.length - 1]));
					for (int i = 1; i < values.length - 1; i++) {
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

	/**
	 * @param fileNam. The complete path of file
	 * @return
	 * @throws IOException
	 */
	private FilterResponse processCSVFile(String fileName) throws IOException {
		FilterResponse filterResponse = new FilterResponse();
		List<ResponseError> errors = new ArrayList<ResponseError>();
		ResponseHeader responseHeader = new ResponseHeader();
		filterResponse.setHeader(responseHeader);
		responseHeader.setErrors(errors);
		List<CSVLine> filteredLines = filterLines(mapCSVtoPOJO(fileName, true));
		filterResponse.setResult(filteredLines);
		return filterResponse;
	}

	/**
	 * @param lines
	 * @return A List with objects that pass-through filter
	 */
	private List<CSVLine> filterLines(List<CSVLine> lines) {

		List<IntSummaryStatistics> summaries = new ArrayList<IntSummaryStatistics>();
		for (int i = 0; i < lines.get(0).getVars().size(); i++) {
			final Integer wrapI = new Integer(i);
			IntSummaryStatistics lineSummary = lines.stream().filter(p -> p.decision == 1)
					.collect(Collectors.summarizingInt(p -> p.vars.get(wrapI)));
			summaries.add(lineSummary);
		}

		List<CSVLine> output = lines
				.stream().filter(
						p -> (p.decision == 1 || (
								IntStream.range(0, summaries.size())
								.filter(j -> p.vars.get(j) >= summaries.get(j).getMin() && p.vars.get(j) <= summaries.get(j).getMax() ? true : false)
								.count() > 0)
						)
				).collect(Collectors.toList());
		return output;
	}

	public static void main(String[] args) throws IOException {
		String fileName = "C:\\Users\\E035\\git\\csvFilter\\csvFilter\\src\\test\\resources\\exampleB_input.csv";
		FilterResponse filterResponse = new CSVFilterEndpoint().processCSVFile(fileName);
	}
}

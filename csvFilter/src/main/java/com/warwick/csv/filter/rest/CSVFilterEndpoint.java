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
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.opencsv.CSVReader;
import com.warwick.csv.filter.request.CSVLine;
import com.warwick.csv.filter.request.SerializedFile;
import com.warwick.csv.filter.response.FilterResponse;
import com.warwick.csv.filter.response.ResponseError;
import com.warwick.csv.filter.response.ResponseHeader;

@Path("/csv-filter")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "CSV Filtering Service", description = "Gets a csv file, applies a filter a devolves the records")
public class CSVFilterEndpoint {

	final static String FILES_PATH = System.getProperty("java.io.tmpdir");
	private static final String FILE = "file";

	@POST
	@Path("/upload-base64")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(description = "Returns a a filtered list of records for a base64 input file")
	@APIResponses({
        @APIResponse(content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = FilterResponse.class))),
        @APIResponse(responseCode = "400", description = "Bad Request, input invalid"),
        @APIResponse(responseCode = "500", description = "Internal Server Error")
    })
	public Response processCSVFileBase64(@Valid  @Parameter(description = "Serialized Base64 file", required = true) SerializedFile file) throws IOException {
		String fileName = saveFile(file.getBase64(), file.getFilename());
		FilterResponse filterResponse = processCSVFile(fileName);
		return Response.ok().entity(filterResponse).build();
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(description = "Returns a a filtered list of records for a multipart input file")
	@APIResponses({
        @APIResponse(content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = FilterResponse.class))),
        @APIResponse(responseCode = "400", description = "Bad Request, input invalid"),
        @APIResponse(responseCode = "500", description = "Internal Server Error")
    })
	public Response processCSVFile(@Parameter(description = "Multipartform with File", required = true) MultipartFormDataInput input) throws IOException {
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
				.stream().peek(s -> System.out.println("s.id:" + s.id)).filter(
						p -> (p.decision == 1 || (IntStream.range(0, summaries.size())
								.filter(j -> p.vars.get(j) >= summaries.get(j).getMin()
										&& p.vars.get(j) <= summaries.get(j).getMax() ? true : false)
								.count() > 0)))
				.peek(s -> {
					System.out.print("filtered:" + s.id);
					for (int i = 0; i < s.vars.size(); i++) {
						System.out.println(s.vars.get(i));
					}
				}).collect(Collectors.toList());
		System.out.println(output.size());
		System.out.println(output);
		return output;
	}

	public static void main(String[] args) throws IOException {
		String fileName = "C:\\Users\\E035\\git\\csvFilter\\csvFilter\\src\\test\\resources\\exampleB_input.csv";
		FilterResponse filterResponse = new CSVFilterEndpoint().processCSVFile(fileName);
	}
}

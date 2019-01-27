package com.warwick.test.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class TestExceptionMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(Throwable throwable) {
		throwable.printStackTrace();
		List<ResponseError> errors = new ArrayList<ResponseError>();
		ResponseHeader responseHeader = new ResponseHeader();
		TestResponse testResponse = new TestResponse();
		responseHeader.setErrors(errors);
		testResponse.setHeader(responseHeader);
		Status status = null;
		if (throwable instanceof IllegalArgumentException) {
			status = Status.BAD_REQUEST;
			responseHeader.setOk(false);
			errors.add(new ResponseError().setErrorCode(ErrorCode.BAD_REQUEST.getCode())
					.setMessage(throwable.getMessage()));
		} else if (throwable instanceof javax.ws.rs.NotSupportedException) {
			status = Status.BAD_REQUEST;
			responseHeader.setOk(false);
			errors.add(new ResponseError().setErrorCode(ErrorCode.BAD_REQUEST.getCode())
					.setMessage(throwable.getMessage()));
		} else if (throwable instanceof ConstraintViolationException) {
			status = Status.BAD_REQUEST;
			responseHeader.setOk(false);
			ConstraintViolationException cvs = (ConstraintViolationException)throwable; 
			for (ConstraintViolation<?> cv : cvs.getConstraintViolations()) {
				errors.add(new ResponseError().setErrorCode(ErrorCode.BAD_REQUEST.getCode())
						.setMessage(cv.getPropertyPath() + " " + cv.getMessage()));
			}
		} else if (throwable instanceof IOException) {
			status = Status.INTERNAL_SERVER_ERROR;
			responseHeader.setOk(false);
			errors.add(new ResponseError().setErrorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
					.setMessage(throwable.getMessage()));
		}
		return Response.status(status).entity(testResponse).build();
	}

}

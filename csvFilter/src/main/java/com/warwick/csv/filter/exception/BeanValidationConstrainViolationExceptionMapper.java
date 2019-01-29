package com.warwick.csv.filter.exception;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.warwick.csv.filter.response.ErrorCode;
import com.warwick.csv.filter.response.FilterResponse;
import com.warwick.csv.filter.response.ResponseError;
import com.warwick.csv.filter.response.ResponseHeader;

/**
 * ExceptionMapper used for java-bean validations. Treats @ConstraintViolationException
 *
 */
@Provider
public class BeanValidationConstrainViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
	@Override
	public Response toResponse(ConstraintViolationException e) {
		List<ResponseError> errors = new ArrayList<ResponseError>();
		ResponseHeader responseHeader = new ResponseHeader();
		FilterResponse filterResponse = new FilterResponse();
		responseHeader.setErrors(errors);
		filterResponse.setHeader(responseHeader);
		Status status = Status.BAD_REQUEST;
		responseHeader.setOk(false);
		ConstraintViolationException cvs = (ConstraintViolationException) e;
		for (ConstraintViolation<?> cv : cvs.getConstraintViolations()) {
			errors.add(new ResponseError().setErrorCode(ErrorCode.BAD_REQUEST.getCode())
					.setMessage(cv.getPropertyPath() + " " + cv.getMessage()));
		}
		return Response.status(status).entity(filterResponse).build();
	}
}
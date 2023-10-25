package com.example.user_info_service.controller.exception;

import com.example.user_info_service.exception.ErrorDetail;
import com.example.user_info_service.exception.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class ControllerAdvice {

  @ExceptionHandler(HttpClientErrorException.BadRequest.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleBadRequest(HttpClientErrorException ex)
      throws JsonProcessingException {
    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setMessage("Bad Request");
    ErrorDetail errorDetail = new ErrorDetail();
    String str = ex.getMessage();
    String errorMessage = null;
    if (str != null) {
      int startIndex = str.indexOf('{');
      String jsonString = str.substring(startIndex);
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(jsonString);
      // Extract the first error message
      if (jsonNode.has("errors")) {
        JsonNode errorsNode = jsonNode.get("errors");
        if (errorsNode.isArray() && !errorsNode.isEmpty()) {
          errorMessage = errorsNode.get(0).get("message").asText();
        }
      } else {
        errorMessage = jsonNode.get("message").asText();
      }
    }

    errorDetail.setMessage(errorMessage);
    errorDetail.setField("Error");
    List<ErrorDetail> errorDetails = Collections.singletonList(errorDetail);
    errorResponse.setErrors(errorDetails);
    return ResponseEntity.badRequest().body(errorResponse);

  }

  @ExceptionHandler(HttpClientErrorException.NotFound.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ErrorResponse> handleNotFound(HttpClientErrorException ex) {
    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setMessage("Resource Not Found");
    ErrorDetail errorDetail = new ErrorDetail();
    errorDetail.setMessage("The requested resource is not found.");
    errorDetail.setField("path");
    List<ErrorDetail> errorDetails = Collections.singletonList(errorDetail);
    errorResponse.setErrors(errorDetails);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  @ExceptionHandler(Throwable.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ErrorResponse> handleServerError(Throwable ex) {
    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setMessage("Internal Server Error");
    ErrorDetail errorDetail = new ErrorDetail();
    errorDetail.setMessage(ex.getMessage());
    errorDetail.setField("Error");
    List<ErrorDetail> errorDetails = Collections.singletonList(errorDetail);
    errorResponse.setErrors(errorDetails);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleIllegalException(IllegalArgumentException ex) {
    ErrorResponse errorResponse = new ErrorResponse();
    errorResponse.setMessage("Bad Request");
    ErrorDetail errorDetail = new ErrorDetail();
    errorDetail.setMessage(ex.getMessage());
    errorDetail.setField("error");
    List<ErrorDetail> errorDetails = Collections.singletonList(errorDetail);
    errorResponse.setErrors(errorDetails);
    return ResponseEntity.badRequest().body(errorResponse);
  }
}
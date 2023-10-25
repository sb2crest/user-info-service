package com.example.user_info_service.controller.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.user_info_service.exception.ErrorDetail;
import com.example.user_info_service.exception.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ControllerAdviceTest {

    @InjectMocks
    private ControllerAdvice controllerAdvice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleBadRequest() throws JsonProcessingException {
        HttpClientErrorException httpClientErrorException = mock(HttpClientErrorException.class);
        String exceptionMessage = "400 Bad Request {\"message\":\"Sample Error\",\"errors\":[{\"message\":\"Error Message\"}]}";
        when(httpClientErrorException.getMessage()).thenReturn(exceptionMessage);
        ResponseEntity<ErrorResponse> responseEntity = controllerAdvice.handleBadRequest(
                httpClientErrorException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals("Bad Request", errorResponse.getMessage());
        assertEquals(1, errorResponse.getErrors().size());
        ErrorDetail errorDetail = errorResponse.getErrors().get(0);
        assertEquals("Error", errorDetail.getField());
        assertEquals("Error Message", errorDetail.getMessage());
    }

    @Test
    void testHandleBadRequest1() throws JsonProcessingException {
        HttpClientErrorException httpClientErrorException = mock(HttpClientErrorException.class);
        String exceptionMessage = "400 Bad Request {\"message\":\"Sample Error\",\"\":[{\"message\":\"Error Message\"}]}";
        when(httpClientErrorException.getMessage()).thenReturn(exceptionMessage);
        ResponseEntity<ErrorResponse> responseEntity = controllerAdvice.handleBadRequest(
                httpClientErrorException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void testHandleNotFoundException() {
        HttpClientErrorException httpClientErrorException = mock(HttpClientErrorException.class);
        ResponseEntity<ErrorResponse> responseEntity = controllerAdvice.handleNotFound(
                httpClientErrorException);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals("Resource Not Found", errorResponse.getMessage());
        assertEquals(1, errorResponse.getErrors().size());
        ErrorDetail errorDetail = errorResponse.getErrors().get(0);
        assertEquals("The requested resource is not found.", errorDetail.getMessage());
        assertEquals("path", errorDetail.getField());
    }

    @Test
    void testHandleServerError() {
        Throwable throwable = new Throwable("Sample error message");

        ResponseEntity<ErrorResponse> responseEntity = controllerAdvice.handleServerError(throwable);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals("Internal Server Error", errorResponse.getMessage());
        List<ErrorDetail> errorDetails = errorResponse.getErrors();
        assertEquals(1, errorDetails.size());
        assertEquals("Error", errorDetails.get(0).getField());
        assertEquals("Sample error message", errorDetails.get(0).getMessage());
    }

    @Test
    void testHandleIllegalException() {
        IllegalArgumentException throwable = new IllegalArgumentException("Sample error message");

        ResponseEntity<ErrorResponse> responseEntity = controllerAdvice.handleIllegalException(throwable);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals("Bad Request", errorResponse.getMessage());
        List<ErrorDetail> errorDetails = errorResponse.getErrors();
        assertEquals(1, errorDetails.size());
        assertEquals("error", errorDetails.get(0).getField());
        assertEquals("Sample error message", errorDetails.get(0).getMessage());
    }

    @Test
    public void testHandleBadRequestWithoutErrorMessage() throws JsonProcessingException {
        HttpClientErrorException httpClientErrorException = mock(HttpClientErrorException.class);
        when(httpClientErrorException.getMessage()).thenReturn(null);

        ResponseEntity<ErrorResponse> responseEntity = controllerAdvice.handleBadRequest(httpClientErrorException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals("Bad Request", errorResponse.getMessage());
        assertEquals(1, errorResponse.getErrors().size());
        assertEquals(null, errorResponse.getErrors().get(0).getMessage());
        assertEquals("Error", errorResponse.getErrors().get(0).getField());
    }

    @Test
    public void testHandleBadRequestWithEmptyErrorsNode() throws JsonProcessingException {
        HttpClientErrorException mockHttpClientErrorException = Mockito.mock(HttpClientErrorException.class);
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        ArrayNode errorsNode = JsonNodeFactory.instance.arrayNode();
        rootNode.set("errors", errorsNode);
        String errorJson = rootNode.toString();

        when(mockHttpClientErrorException.getMessage()).thenReturn(errorJson);
        ResponseEntity<ErrorResponse> responseEntity = controllerAdvice.handleBadRequest(mockHttpClientErrorException);

        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
        assertEquals("Bad Request", errorResponse.getMessage());
        assertEquals(1, errorResponse.getErrors().size());
        assertEquals(null, errorResponse.getErrors().get(0).getMessage());
        assertEquals("Error", errorResponse.getErrors().get(0).getField());
    }

    @Test
    void testHandleBadRequestWithNonArrayErrorsNode() throws Exception {
        HttpClientErrorException mockHttpClientErrorException = mock(HttpClientErrorException.class);

        String errorJson = "400 Bad Request {\"message\":\"Sample Error\",\"errors\":{\"message\":\"Sample error message\"}}";
        when(mockHttpClientErrorException.getMessage()).thenReturn(errorJson);

        ResponseEntity<ErrorResponse> responseEntity = controllerAdvice.handleBadRequest(mockHttpClientErrorException);

        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Bad Request", errorResponse.getMessage());
        assertEquals(1, errorResponse.getErrors().size());
    }

}









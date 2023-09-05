package com.example.user_info_service.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -93116643889493326L;

    public BusinessException(String errorCode, String errorDesc, HttpStatus httpStatus) {
        super(errorCode + ": " + errorDesc + ": " +httpStatus);
    }

}

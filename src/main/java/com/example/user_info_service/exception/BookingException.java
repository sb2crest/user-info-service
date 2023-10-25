package com.example.user_info_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class BookingException extends BusinessException {

    @Serial
    private static final long serialVersionUID = -5383000840940136245L;

    public BookingException(ResStatus resStatus) {
        super(resStatus.getCode(), resStatus.getDesc(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


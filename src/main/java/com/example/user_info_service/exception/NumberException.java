package com.example.user_info_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NumberException extends BusinessException {

    @Serial
    private static final long serialVersionUID = -5383000840940136245L;

    public NumberException(ResStatus resStatus) {
        super(resStatus.getCode(), resStatus.getDesc(), HttpStatus.BAD_REQUEST);
    }
}


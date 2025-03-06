package com.user.info.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class VehicleNumberException extends BusinessException {

    private static final long serialVersionUID = -5383000840940136245L;

    public VehicleNumberException(ResStatus resStatus) {
        super(resStatus.getCode(), resStatus.getDesc(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


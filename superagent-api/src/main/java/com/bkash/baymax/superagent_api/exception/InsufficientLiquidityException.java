package com.bkash.baymax.superagent_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientLiquidityException extends RuntimeException {

    public InsufficientLiquidityException(String message) {
        super(message);
    }
}

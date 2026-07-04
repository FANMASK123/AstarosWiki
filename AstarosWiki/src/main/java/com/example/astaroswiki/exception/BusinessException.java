package com.example.astaroswiki.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private static final Integer DEFAULT_ERROR_CODE = 500;

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = DEFAULT_ERROR_CODE;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

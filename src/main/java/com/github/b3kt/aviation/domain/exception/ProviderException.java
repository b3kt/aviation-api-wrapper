package com.github.b3kt.aviation.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProviderException extends RuntimeException{
    private final HttpStatus httpStatus;

    public ProviderException(HttpStatus httpStatus) {
        super(String.format("Failed while fetching data: '%s'. provider not responding", httpStatus));
        this.httpStatus = httpStatus;
    }
}

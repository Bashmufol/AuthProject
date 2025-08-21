package com.bash.authproject.model;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class ResponseModel<T> {
    private HttpStatus code;
    private String message;
    private T data;
}

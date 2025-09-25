package com.kshrd.jpahibernate02_homework.base;

import com.kshrd.jpahibernate02_homework.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public abstract class BaseController {
    //    this is for no payload
    protected ResponseEntity<ApiResponse<Object>> response(String message) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(message)
                .instant(LocalDateTime.now())
                .build());
    }

    //    this is for http ok mostly just use for get
    protected <T> ResponseEntity<ApiResponse<T>> response(String message, T payload) {
        ApiResponse<T> apiResponse = ApiResponse.<T>builder()
                .status(HttpStatus.OK)
                .message(message)
                .payload(payload)
                .instant(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    //    this is for create
    protected <T> ResponseEntity<ApiResponse<T>> response(String message, HttpStatus httpStatus, T payload) {
        ApiResponse<T> apiResponse = ApiResponse.<T>builder()
                .status(httpStatus)
                .message(message)
                .payload(payload)
                .instant(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}

package com.iteci.cobro.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.iteci.cobro.exceptions.IteciPrinterException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IteciPrinterException.class)
    public ResponseEntity<String> handlePrinterException(IteciPrinterException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Printer error: " + ex.getMessage());
    }
}
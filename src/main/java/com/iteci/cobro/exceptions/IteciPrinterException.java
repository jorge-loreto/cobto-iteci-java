package com.iteci.cobro.exceptions;

public class IteciPrinterException extends Exception {

    public IteciPrinterException(String message) {
        super(message);
    }

    public IteciPrinterException(String message, Throwable cause) {
        super(message, cause);
    }
}
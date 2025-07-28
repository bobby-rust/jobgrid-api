package com.robrustdev.jobgrid.exceptions;

public class EmailInUseException extends RuntimeException {
    public EmailInUseException(String message) {
        super(message);
    }
}

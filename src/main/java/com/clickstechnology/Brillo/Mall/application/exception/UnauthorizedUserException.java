package com.clickstechnology.Brillo.Mall.application.exception;

public class UnauthorizedUserException extends RuntimeException {
    public UnauthorizedUserException() {
        super("Unauthorized User");
    }
}

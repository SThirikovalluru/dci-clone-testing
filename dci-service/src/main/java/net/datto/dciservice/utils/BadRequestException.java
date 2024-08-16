package net.datto.dciservice.utils;

public class BadRequestException extends Exception {
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public BadRequestException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public BadRequestException() {
        super();
    }
}
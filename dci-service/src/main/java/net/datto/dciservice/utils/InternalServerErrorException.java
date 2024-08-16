package net.datto.dciservice.utils;

public class InternalServerErrorException extends Exception {
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public InternalServerErrorException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public InternalServerErrorException() {
        super();
    }
}

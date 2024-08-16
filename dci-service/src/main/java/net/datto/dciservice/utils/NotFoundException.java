package net.datto.dciservice.utils;

public class NotFoundException extends Exception {
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public NotFoundException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public NotFoundException() {
        super();
    }
}

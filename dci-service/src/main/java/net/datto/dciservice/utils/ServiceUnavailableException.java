package net.datto.dciservice.utils;

public class ServiceUnavailableException extends Exception {
    private String errorMessage;

    public ServiceUnavailableException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public ServiceUnavailableException() {
        super();
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

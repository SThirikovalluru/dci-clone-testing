package net.datto.dciservice.utils;

public class UnauthorizedException extends Exception {
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public UnauthorizedException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public UnauthorizedException() {
        super();
    }
}

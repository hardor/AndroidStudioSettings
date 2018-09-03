package ru.profapp.RanobeReader.Common;

public class ErrorConnectionException extends Exception {

    public ErrorConnectionException(String message) {
        super(message);
    }

    public ErrorConnectionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ErrorConnectionException() {
        super();
    }

    public ErrorConnectionException(Throwable cause) {
        super(cause);
    }
}
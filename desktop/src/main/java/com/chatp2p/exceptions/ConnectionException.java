package com.chatp2p.exceptions;

public class ConnectionException extends AppException {
    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
} 
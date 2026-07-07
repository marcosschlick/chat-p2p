package com.chatp2p.exceptions;

public class NetworkException extends AppException {
    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
} 
package com.lixin.capability.oss.exception;

public class OssCapabilityException extends RuntimeException {
    public OssCapabilityException(String message) {
        super(message);
    }

    public OssCapabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}

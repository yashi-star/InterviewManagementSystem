package com.interview.management.exception;

public class ResourceAlreadyExistsException extends BusinessException {
    public ResourceAlreadyExistsException(String resourceName, String field, String value) {
        super(String.format("%s already exists with %s: %s", resourceName, field, value));
    }
    
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}

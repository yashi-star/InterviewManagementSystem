package com.interview.management.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id));
    }
    
    public ResourceNotFoundException(String resourceName, String identifier, String value) {
        super(String.format("%s not found with %s: %s", resourceName, identifier, value));
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }}
package com.interview.management.exception;

/**
 * Thrown when external service calls fail
 * HTTP Status: 503 SERVICE UNAVAILABLE
 */
class ExternalServiceException extends BusinessException {
    private final String serviceName;
    
    public ExternalServiceException(String serviceName, String message) {
        super(String.format("External service '%s' failed: %s", serviceName, message));
        this.serviceName = serviceName;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("External service '%s' failed: %s", serviceName, message), cause);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}

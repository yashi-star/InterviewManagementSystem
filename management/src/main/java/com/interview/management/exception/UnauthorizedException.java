package com.interview.management.exception;


/**
 * Thrown when user doesn't have permission
 * HTTP Status: 403 FORBIDDEN
 */
class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String user, String action) {
        super(String.format("User '%s' is not authorized to perform action: %s", user, action));
    }
}
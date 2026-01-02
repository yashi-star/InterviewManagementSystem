package com.interview.management.exception;

/**
 * Thrown when an operation is not allowed in current state
 * HTTP Status: 400 BAD REQUEST
 */
public class InvalidStateException extends BusinessException {
    public InvalidStateException(String message) {
        super(message);
    }
    
    public InvalidStateException(String entity, String currentState, String attemptedOperation) {
        super(String.format("Cannot perform '%s' on %s in state '%s'", 
            attemptedOperation, entity, currentState));
    }
}
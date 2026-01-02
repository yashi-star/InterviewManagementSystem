package com.interview.management.exception;


/**
 * Thrown when AI screening fails
 * HTTP Status: 500 INTERNAL SERVER ERROR
 */
public class AIScreeningException extends BusinessException {
    public AIScreeningException(String message) {
        super(message);
    }
    
    public AIScreeningException(String message, Throwable cause) {
        super(message, cause);
    }
}

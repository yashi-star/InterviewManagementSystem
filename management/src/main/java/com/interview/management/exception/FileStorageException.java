package com.interview.management.exception;


/**
 * Thrown when file operations fail
 * HTTP Status: 500 INTERNAL SERVER ERROR
 */
public class FileStorageException extends BusinessException {
    public FileStorageException(String message) {
        super(message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

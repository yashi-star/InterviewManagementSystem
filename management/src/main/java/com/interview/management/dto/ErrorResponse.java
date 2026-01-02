package com.interview.management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standard error response structure
 * Returned for all API errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Timestamp when error occurred
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * HTTP status text (e.g., "Bad Request")
     */
    private String error;
    
    /**
     * Main error message
     */
    private String message;
    
    /**
     * API endpoint path where error occurred
     */
    private String path;
    
    /**
     * Detailed error information (optional)
     */
    private String details;
    
    /**
     * List of validation errors (for field-level errors)
     */
    private List<FieldError> fieldErrors;
    
    /**
     * Additional error metadata (optional)
     */
    private Map<String, Object> metadata;
    
    /**
     * Request ID for tracking (optional)
     */
    private String requestId;
    
    /**
     * Represents a field-level validation error
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        /**
         * Field name that failed validation
         */
        private String field;
        
        /**
         * Rejected value
         */
        private Object rejectedValue;
        
        /**
         * Error message
         */
        private String message;
    }
    
    /**
     * Create error response with minimal information
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
    
    /**
     * Create error response with details
     */
    public static ErrorResponse of(int status, String error, String message, String details, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .details(details)
                .path(path)
                .build();
    }
}

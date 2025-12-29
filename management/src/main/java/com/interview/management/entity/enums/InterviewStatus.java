package com.interview.management.entity.enums;

public /**
 * Interview status lifecycle
 */
public enum InterviewStatus {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    RESCHEDULED("Rescheduled");
    
    private final String displayName;
    
    InterviewStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 

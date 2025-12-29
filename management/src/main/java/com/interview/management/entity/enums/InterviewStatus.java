package com.interview.management.entity.enums;

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
    
    /**
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }
} 


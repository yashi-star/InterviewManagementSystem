package com.interview.management.entity.enums;

public enum CandidateStage {
    APPLIED("Application Received"),
    SCREENING("Under AI Screening"),
    INTERVIEW_SCHEDULED("Interview Scheduled"),
    INTERVIEW_COMPLETED("Interview Completed"),
    HIRED("Hired"),
    REJECTED("Rejected");
    
    private final String displayName;
    
    CandidateStage(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}


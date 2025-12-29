package com.interview.management.entity.enums;

public enum InterviewType {
    TECHNICAL("Technical Interview"),
    HR("HR Interview"),
    MANAGERIAL("Managerial Interview"),
    CULTURAL_FIT("Cultural Fit Interview");
    
    private final String displayName;
    
    InterviewType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
package com.interview.management.entity.enums;

public enum Recommendation {
    STRONG_HIRE("Strong Hire - Excellent candidate"),
    HIRE("Hire - Good candidate"),
    MAYBE("Maybe - Borderline candidate"),
    NO_HIRE("No Hire - Not suitable");
    
    private final String displayName;
    
    Recommendation(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
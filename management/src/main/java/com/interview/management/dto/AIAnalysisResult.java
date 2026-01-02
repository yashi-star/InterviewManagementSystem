package com.interview.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI Analysis Result DTO
 * Contains structured output from AI resume analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResult {
    
    /**
     * Technical skills identified in resume
     * Format: JSON array string or plain text list
     * Example: "[{\"skill\": \"Java\", \"proficiency\": \"Advanced\", \"years\": 5}]"
     */
    private String skillsMatched;
    
    /**
     * Total years of professional experience
     */
    private Double experienceYears;
    
    /**
     * Education level and details
     * Example: "B.Tech Computer Science, XYZ University, 2020"
     */
    private String educationLevel;
    
    /**
     * Cultural fit indicators
     * Format: JSON string or plain text
     * Example: "{\"teamwork\": \"High\", \"leadership\": \"Medium\", \"communication\": \"High\"}"
     */
    private String culturalFit;
    
    /**
     * Overall match score (0-100)
     * Used for ranking candidates
     */
    private Integer matchScore;
    
    /**
     * Free-form AI analysis and summary
     * The AI's overall assessment and recommendation
     */
    private String analysisText;
    
    /**
     * Recommendation: STRONG_HIRE, HIRE, MAYBE, NO_HIRE
     */
    private String recommendation;
    
    /**
     * AI model used for analysis
     * Example: "llama2", "mistral"
     */
    private String modelUsed;
    
    /**
     * Processing time in milliseconds
     */
    private Long processingTimeMs;
    
    /**
     * Indicates if this is a fallback analysis (when AI fails)
     */
    private Boolean isFallback;
}
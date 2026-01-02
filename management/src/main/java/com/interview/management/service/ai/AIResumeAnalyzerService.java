package com.interview.management.service.ai;

import com.interview.management.dto.AIAnalysisResult;
import com.interview.management.exception.AIScreeningException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AI Resume Analyzer Service
 * Uses Spring AI + Ollama to analyze resume content
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIResumeAnalyzerService {
    
    private final ChatClient.Builder chatClientBuilder;
    
    /**
     * Analyze resume text using AI
     */
    public AIAnalysisResult analyzeResume(String resumeText, String jobDescription) {
        log.info("Starting AI resume analysis - Resume length: {} chars", resumeText.length());
        
        try {
            // Build the chat client
            ChatClient chatClient = chatClientBuilder.build();
            
            // Create the analysis prompt
            String prompt = buildAnalysisPrompt(resumeText, jobDescription);
            
            log.debug("Sending prompt to Ollama (length: {} chars)", prompt.length());
            
            // Call Ollama AI
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            log.debug("Received AI response (length: {} chars)", aiResponse.length());
            
            // Parse AI response into structured data
            AIAnalysisResult result = parseAIResponse(aiResponse, resumeText);
            
            log.info("AI analysis complete - Match Score: {}", result.getMatchScore());
            return result;
            
        } catch (Exception e) {
            log.error("AI resume analysis failed", e);
            throw new AIScreeningException("Failed to analyze resume with AI", e);
        }
    }
    
    /**
     * Build the AI prompt for resume analysis
     */
    private String buildAnalysisPrompt(String resumeText, String jobDescription) {
        // Truncate resume if too long (Ollama has token limits)
        String truncatedResume = resumeText.length() > 4000 
                ? resumeText.substring(0, 4000) + "..." 
                : resumeText;
        
        String promptTemplate = """
                You are an expert HR recruiter analyzing a candidate's resume.
                
                RESUME CONTENT:
                {resume}
                
                JOB REQUIREMENTS:
                {jobDescription}
                
                Please analyze this resume and provide a structured response in the following format:
                
                SKILLS:
                List the technical skills found (one per line, format: "Skill - Proficiency Level - Years")
                
                EXPERIENCE:
                Total years of professional experience (just a number)
                
                EDUCATION:
                Highest education level and field (one line)
                
                CULTURAL_FIT:
                Rate teamwork, leadership, and communication (High/Medium/Low for each)
                
                MATCH_SCORE:
                Overall match score from 0-100 (just the number)
                
                ANALYSIS:
                Brief summary (2-3 sentences) explaining the match score and key strengths/weaknesses.
                
                RECOMMENDATION:
                One of: STRONG_HIRE, HIRE, MAYBE, REJECT
                
                Be concise and format your response EXACTLY as shown above with the section headers.
                """;
        
        PromptTemplate template = new PromptTemplate(promptTemplate);
        Prompt prompt = template.create(Map.of(
                "resume", truncatedResume,
                "jobDescription", jobDescription != null ? jobDescription : "General software engineering position"
        ));
        
        return prompt.getContents();
    }
    
    /**
     * Parse AI response into structured AIAnalysisResult
     */
    private AIAnalysisResult parseAIResponse(String aiResponse, String originalResume) {
        log.debug("Parsing AI response into structured format");
        
        AIAnalysisResult result = new AIAnalysisResult();
        
        try {
            // Extract sections using regex patterns
            result.setSkillsMatched(extractSection(aiResponse, "SKILLS:", "EXPERIENCE:"));
            result.setExperienceYears(extractExperience(aiResponse));
            result.setEducationLevel(extractSection(aiResponse, "EDUCATION:", "CULTURAL_FIT:"));
            result.setCulturalFit(extractSection(aiResponse, "CULTURAL_FIT:", "MATCH_SCORE:"));
            result.setMatchScore(extractMatchScore(aiResponse));
            result.setAnalysisText(extractSection(aiResponse, "ANALYSIS:", "RECOMMENDATION:"));
            result.setRecommendation(extractRecommendation(aiResponse));
            
            // Validate the result
            validateResult(result);
            
        } catch (Exception e) {
            log.error("Failed to parse AI response, using fallback analysis", e);
            // Fallback to basic keyword-based analysis
            return createFallbackAnalysis(originalResume);
        }
        
        return result;
    }
    
    /**
     * Extract a section between two headers
     */
    private String extractSection(String text, String startMarker, String endMarker) {
        int startIdx = text.indexOf(startMarker);
        if (startIdx == -1) return "";
        
        startIdx += startMarker.length();
        
        int endIdx = text.indexOf(endMarker, startIdx);
        if (endIdx == -1) {
            endIdx = text.length();
        }
        
        return text.substring(startIdx, endIdx).trim();
    }
    
    /**
     * Extract years of experience
     */
    private Double extractExperience(String text) {
        String expSection = extractSection(text, "EXPERIENCE:", "EDUCATION:");
        
        // Try to find a number in the experience section
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)");
        java.util.regex.Matcher matcher = pattern.matcher(expSection);
        
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Could not parse experience years: {}", matcher.group(1));
            }
        }
        
        return 0.0;
    }
    
    /**
     * Extract match score (0-100)
     */
    private Integer extractMatchScore(String text) {
        String scoreSection = extractSection(text, "MATCH_SCORE:", "ANALYSIS:");
        
        // Try to find a number
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(scoreSection);
        
        if (matcher.find()) {
            try {
                int score = Integer.parseInt(matcher.group(1));
                // Clamp between 0-100
                return Math.max(0, Math.min(100, score));
            } catch (NumberFormatException e) {
                log.warn("Could not parse match score: {}", matcher.group(1));
            }
        }
        
        return 50; // Default score
    }
    
    /**
     * Extract recommendation
     */
    private String extractRecommendation(String text) {
        String recSection = extractSection(text, "RECOMMENDATION:", "");
        String rec = recSection.toUpperCase().trim();
        
        if (rec.contains("STRONG_HIRE") || rec.contains("STRONG HIRE")) {
            return "STRONG_HIRE";
        } else if (rec.contains("NO_HIRE") || rec.contains("REJECT")) {
            return "NO_HIRE";
        } else if (rec.contains("MAYBE")) {
            return "MAYBE";
        } else if (rec.contains("HIRE")) {
            return "HIRE";
        }
        
        return "MAYBE"; // Default
    }
    
    /**
     * Validate that we got meaningful data
     */
    private void validateResult(AIAnalysisResult result) {
        if (result.getMatchScore() == null || result.getMatchScore() < 0 || result.getMatchScore() > 100) {
            result.setMatchScore(50);
        }
        
        if (result.getAnalysisText() == null || result.getAnalysisText().isEmpty()) {
            result.setAnalysisText("AI analysis completed successfully.");
        }
        
        if (result.getSkillsMatched() == null || result.getSkillsMatched().isEmpty()) {
            result.setSkillsMatched("Skills analysis pending manual review.");
        }
    }
    
    /**
     * Create fallback analysis if AI fails
     */
    private AIAnalysisResult createFallbackAnalysis(String resumeText) {
        log.warn("Using fallback keyword-based analysis");
        
        AIAnalysisResult result = new AIAnalysisResult();
        
        // Basic keyword matching
        String lowerResume = resumeText.toLowerCase();
        
        // Count technical keywords
        int skillCount = 0;
        String[] techKeywords = {"java", "python", "javascript", "react", "spring", "sql", 
                                "aws", "docker", "kubernetes", "git", "api", "microservices"};
        StringBuilder skills = new StringBuilder();
        
        for (String keyword : techKeywords) {
            if (lowerResume.contains(keyword)) {
                skillCount++;
                skills.append(keyword).append(" - Mentioned\n");
            }
        }
        
        result.setSkillsMatched(skills.toString());
        
        // Estimate experience (very rough)
        if (lowerResume.contains("senior") || lowerResume.contains("lead")) {
            result.setExperienceYears(5.0);
        } else if (lowerResume.contains("junior") || lowerResume.contains("intern")) {
            result.setExperienceYears(1.0);
        } else {
            result.setExperienceYears(3.0);
        }
        
        // Education
        if (lowerResume.contains("master") || lowerResume.contains("phd")) {
            result.setEducationLevel("Master's degree or higher");
        } else if (lowerResume.contains("bachelor") || lowerResume.contains("b.tech") || lowerResume.contains("b.e")) {
            result.setEducationLevel("Bachelor's degree");
        } else {
            result.setEducationLevel("Education information not clearly specified");
        }
        
        // Cultural fit (default)
        result.setCulturalFit("Teamwork: Medium, Leadership: Medium, Communication: Medium");
        
        // Calculate basic match score
        int baseScore = 40;
        int skillBonus = Math.min(skillCount * 5, 30); // Max 30 points from skills
        result.setMatchScore(baseScore + skillBonus);
        
        result.setAnalysisText(String.format(
            "Basic analysis completed. Found %d relevant technical skills. " +
            "Resume shows %s years of experience. Further manual review recommended.",
            skillCount, result.getExperienceYears()
        ));
        
        result.setRecommendation(result.getMatchScore() >= 70 ? "HIRE" : "MAYBE");
        
        return result;
    }
}

package com.interview.management.service;

import com.interview.management.entity.Feedback;
import com.interview.management.entity.Interview;
import com.interview.management.entity.Interviewer;
import com.interview.management.entity.enums.InterviewStatus;
import com.interview.management.entity.enums.Recommendation;
import com.interview.management.repository.FeedbackRepository;
import com.interview.management.repository.InterviewRepository;
import com.interview.management.repository.InterviewerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feedback Service
 * Handles feedback collection after interviews
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewerRepository interviewerRepository;
    
    /**
     * Submit feedback for an interview
     */
    public Feedback submitFeedback(Long interviewId, Long interviewerId,
                                   Integer technicalScore, Integer communicationScore,
                                   Integer problemSolvingScore, Integer culturalFitScore,
                                   String strengths, String weaknesses,
                                   String additionalComments, Recommendation recommendation) {
        
        log.info("Submitting feedback for interview: {} by interviewer: {}", interviewId, interviewerId);
        
        // Validate interview exists and is completed
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found: " + interviewId));
        
        if (interview.getCurrentStatus() != InterviewStatus.COMPLETED) {
            throw new IllegalStateException("Cannot submit feedback for non-completed interview");
        }
        
        // Validate interviewer exists
        Interviewer interviewer = interviewerRepository.findById(interviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Interviewer not found: " + interviewerId));
        
        // Validate scores are in range 1-5
        validateScore(technicalScore, "Technical score");
        validateScore(communicationScore, "Communication score");
        validateScore(problemSolvingScore, "Problem solving score");
        if (culturalFitScore != null) {
            validateScore(culturalFitScore, "Cultural fit score");
        }
        
        // Create feedback
        Feedback feedback = Feedback.builder()
                .interview(interview)
                .interviewer(interviewer)
                .technicalScore(technicalScore)
                .communicationScore(communicationScore)
                .problemSolvingScore(problemSolvingScore)
                .culturalFitScore(culturalFitScore)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .additionalComments(additionalComments)
                .recommendation(recommendation)
                .build();
        
        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback submitted successfully with ID: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * Validate score is in range 1-5
     */
    private void validateScore(Integer score, String fieldName) {
        if (score == null || score < 1 || score > 5) {
            throw new IllegalArgumentException(fieldName + " must be between 1 and 5");
        }
    }
    
    /**
     * Get feedback by ID
     */
    @Transactional(readOnly = true)
    public Feedback getFeedbackById(Long id) {
        log.debug("Fetching feedback: {}", id);
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + id));
    }
    
    /**
     * Get all feedback for an interview
     */
    @Transactional(readOnly = true)
    public List<Feedback> getFeedbackForInterview(Long interviewId) {
        log.debug("Fetching feedback for interview: {}", interviewId);
        return feedbackRepository.findByInterviewId(interviewId);
    }
    
    /**
     * Get all feedback given by an interviewer
     */
    @Transactional(readOnly = true)
    public List<Feedback> getFeedbackByInterviewer(Long interviewerId) {
        log.debug("Fetching feedback by interviewer: {}", interviewerId);
        return feedbackRepository.findByInterviewerId(interviewerId);
    }
    
    /**
     * Get average scores for a candidate (across all interviews)
     */
    @Transactional(readOnly = true)
    public Map<String, Double> getAverageScoresForCandidate(Long candidateId) {
        log.debug("Calculating average scores for candidate: {}", candidateId);
        
        List<Object[]> results = feedbackRepository.getAverageScoresForCandidate(candidateId);
        
        if (results.isEmpty()) {
            throw new IllegalArgumentException("No feedback found for candidate: " + candidateId);
        }
        
        Object[] scores = results.get(0);
        Map<String, Double> averages = new HashMap<>();
        averages.put("technical", (Double) scores[0]);
        averages.put("communication", (Double) scores[1]);
        averages.put("problemSolving", (Double) scores[2]);
        
        return averages;
    }
    
    /**
     * Get positive feedback (STRONG_HIRE or HIRE)
     */
    @Transactional(readOnly = true)
    public List<Feedback> getPositiveFeedback() {
        log.debug("Fetching positive feedback");
        return feedbackRepository.findPositiveFeedback();
    }
    
    /**
     * Get interviewer statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInterviewerStatistics(Long interviewerId) {
        log.debug("Fetching statistics for interviewer: {}", interviewerId);
        
        Object[] stats = feedbackRepository.getInterviewerStatistics(interviewerId);
        
        if (stats == null) {
            throw new IllegalArgumentException("No statistics found for interviewer: " + interviewerId);
        }
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("avgTechnicalScore", stats[0]);
        statistics.put("avgCommunicationScore", stats[1]);
        statistics.put("totalFeedbacks", stats[2]);
        statistics.put("strongHireCount", stats[3]);
        
        return statistics;
    }
    
    /**
     * Update feedback
     */
    public Feedback updateFeedback(Long feedbackId, Integer technicalScore, 
                                   Integer communicationScore, Integer problemSolvingScore,
                                   Integer culturalFitScore, String strengths,
                                   String weaknesses, String additionalComments,
                                   Recommendation recommendation) {
        
        log.info("Updating feedback: {}", feedbackId);
        
        Feedback feedback = getFeedbackById(feedbackId);
        
        if (technicalScore != null) {
            validateScore(technicalScore, "Technical score");
            feedback.setTechnicalScore(technicalScore);
        }
        if (communicationScore != null) {
            validateScore(communicationScore, "Communication score");
            feedback.setCommunicationScore(communicationScore);
        }
        if (problemSolvingScore != null) {
            validateScore(problemSolvingScore, "Problem solving score");
            feedback.setProblemSolvingScore(problemSolvingScore);
        }
        if (culturalFitScore != null) {
            validateScore(culturalFitScore, "Cultural fit score");
            feedback.setCulturalFitScore(culturalFitScore);
        }
        if (strengths != null) {
            feedback.setStrengths(strengths);
        }
        if (weaknesses != null) {
            feedback.setWeaknesses(weaknesses);
        }
        if (additionalComments != null) {
            feedback.setAdditionalComments(additionalComments);
        }
        if (recommendation != null) {
            feedback.setRecommendation(recommendation);
        }
        
        Feedback updated = feedbackRepository.save(feedback);
        log.info("Feedback {} updated successfully", feedbackId);
        return updated;
    }
    
    /**
     * Delete feedback
     */
    public void deleteFeedback(Long feedbackId) {
        log.info("Deleting feedback: {}", feedbackId);
        
        Feedback feedback = getFeedbackById(feedbackId);
        feedbackRepository.delete(feedback);
        
        log.info("Feedback {} deleted successfully", feedbackId);
    }
    
    /**
     * Calculate overall score for feedback
     */
    @Transactional(readOnly = true)
    public Double calculateOverallScore(Long feedbackId) {
        Feedback feedback = getFeedbackById(feedbackId);
        return feedback.getOverallScore();
    }
}
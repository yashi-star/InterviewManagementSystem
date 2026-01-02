package com.interview.management.service;

import com.interview.management.entity.enums.CandidateStage;
import com.interview.management.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dashboard Service
 * Provides statistics and analytics for the recruiter dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {
    
    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final AIScreeningRepository screeningRepository;
    private final FeedbackRepository feedbackRepository;
    private final CandidateStageHistoryRepository stageHistoryRepository;
    
    /**
     * Get complete dashboard statistics
     */
    public Map<String, Object> getDashboardStatistics() {
        log.info("Generating dashboard statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total candidates applied
        long totalCandidates = candidateRepository.count();
        stats.put("totalCandidates", totalCandidates);
        
        // Candidates this month
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0);
        long candidatesThisMonth = candidateRepository.countCandidatesCreatedAfter(startOfMonth);
        stats.put("candidatesThisMonth", candidatesThisMonth);
        
        // Interviews scheduled today
        long interviewsToday = interviewRepository.countInterviewsScheduledToday(LocalDateTime.now());
        stats.put("interviewsScheduledToday", interviewsToday);
        
        // Pending feedback count
        long pendingFeedbacks = interviewRepository.countPendingFeedbacks();
        stats.put("pendingFeedbackCount", pendingFeedbacks);
        
        // Candidate distribution by stage
        Map<CandidateStage, Long> candidatesByStage = getCandidateCountByStage();
        stats.put("candidatesByStage", candidatesByStage);
        
        // Recent activity (last 7 days)
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        List<Object> recentActivity = getRecentActivity(lastWeek);
        stats.put("recentActivity", recentActivity);
        
        // High-scoring candidates
        List<Object> topCandidates = getTopScoredCandidates(80, 5);
        stats.put("topCandidates", topCandidates);
        
        log.info("Dashboard statistics generated successfully");
        return stats;
    }
    
    /**
     * Get candidate count by stage
     */
    public Map<CandidateStage, Long> getCandidateCountByStage() {
        log.debug("Fetching candidate count by stage");
        
        List<Object[]> results = candidateRepository.countByStage();
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (CandidateStage) row[0],
                        row -> (Long) row[1]
                ));
    }
    
    /**
     * Get recent activity (stage changes in last N days)
     */
    public List<Object> getRecentActivity(LocalDateTime since) {
        log.debug("Fetching recent activity since: {}", since);
        
        return stageHistoryRepository.findRecentStageChanges(since)
                .stream()
                .map(history -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("candidateName", history.getCandidate().getName());
                    activity.put("fromStage", history.getFromStage());
                    activity.put("toStage", history.getToStage());
                    activity.put("changedBy", history.getChangedBy());
                    activity.put("changedAt", history.getChangedAt());
                    activity.put("reason", history.getReason());
                    return activity;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get top-scored candidates from AI screening
     */
    public List<Object> getTopScoredCandidates(int minScore, int limit) {
        log.debug("Fetching top candidates with minScore: {}, limit: {}", minScore, limit);
        
        return screeningRepository.findHighScoreCandidates(minScore)
                .stream()
                .limit(limit)
                .map(screening -> {
                    Map<String, Object> candidate = new HashMap<>();
                    candidate.put("candidateId", screening.getCandidate().getId());
                    candidate.put("candidateName", screening.getCandidate().getName());
                    candidate.put("email", screening.getCandidate().getEmail());
                    candidate.put("matchScore", screening.getMatchScore());
                    candidate.put("currentStage", screening.getCandidate().getCurrentStage());
                    candidate.put("screenedAt", screening.getScreenedAt());
                    return candidate;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get interview statistics
     */
    public Map<String, Object> getInterviewStatistics() {
        log.debug("Generating interview statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Today's interviews
        stats.put("todayCount", interviewRepository.countInterviewsScheduledToday(LocalDateTime.now()));
        
        // Pending feedback
        stats.put("pendingFeedback", interviewRepository.countPendingFeedbacks());
        
        // Interview stats by type
        List<Object[]> statsByType = interviewRepository.getInterviewStatsByType();
        Map<String, Object> byType = new HashMap<>();
        for (Object[] row : statsByType) {
            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("total", row[1]);
            typeStats.put("completed", row[2]);
            byType.put(row[0].toString(), typeStats);
        }
        stats.put("byType", byType);
        
        return stats;
    }
    
    /**
     * Get AI screening statistics
     */
    public Map<String, Object> getScreeningStatistics() {
        log.debug("Generating screening statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total screenings
        stats.put("totalScreenings", screeningRepository.count());
        
        // Average score by stage
        List<Object[]> avgByStage = screeningRepository.getAverageScoreByStage();
        Map<String, Double> averagesByStage = new HashMap<>();
        for (Object[] row : avgByStage) {
            averagesByStage.put(row[0].toString(), (Double) row[1]);
        }
        stats.put("averageScoreByStage", averagesByStage);
        
        // High-scoring count (score >= 80)
        int highScoreCount = screeningRepository.findHighScoreCandidates(80).size();
        stats.put("highScoreCandidates", highScoreCount);
        
        return stats;
    }
    
    /**
     * Get feedback statistics
     */
    public Map<String, Object> getFeedbackStatistics() {
        log.debug("Generating feedback statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total feedback
        stats.put("totalFeedback", feedbackRepository.count());
        
        // Positive feedback count
        int positiveCount = feedbackRepository.findPositiveFeedback().size();
        stats.put("positiveFeedbackCount", positiveCount);
        
        return stats;
    }
    
    /**
     * Get hiring funnel statistics
     */
    public Map<String, Object> getHiringFunnelStatistics() {
        log.debug("Generating hiring funnel statistics");
        
        Map<CandidateStage, Long> countsByStage = getCandidateCountByStage();
        
        Map<String, Object> funnel = new HashMap<>();
        funnel.put("applied", countsByStage.getOrDefault(CandidateStage.APPLIED, 0L));
        funnel.put("screening", countsByStage.getOrDefault(CandidateStage.SCREENING, 0L));
        funnel.put("interviewScheduled", countsByStage.getOrDefault(CandidateStage.INTERVIEW_SCHEDULED, 0L));
        funnel.put("interviewCompleted", countsByStage.getOrDefault(CandidateStage.INTERVIEW_COMPLETED, 0L));
        funnel.put("hired", countsByStage.getOrDefault(CandidateStage.HIRED, 0L));
        funnel.put("rejected", countsByStage.getOrDefault(CandidateStage.REJECTED, 0L));
        
        // Calculate conversion rates
        long totalApplied = countsByStage.values().stream().mapToLong(Long::longValue).sum();
        long hired = countsByStage.getOrDefault(CandidateStage.HIRED, 0L);
        
        if (totalApplied > 0) {
            double conversionRate = (hired * 100.0) / totalApplied;
            funnel.put("overallConversionRate", String.format("%.2f%%", conversionRate));
        }
        
        return funnel;
    }
    
    /**
     * Get recent candidates (last N days)
     */
    public List<Map<String, Object>> getRecentCandidates(int days) {
        log.debug("Fetching candidates from last {} days", days);
        
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        return candidateRepository.findByCreatedAtAfter(since)
                .stream()
                .map(candidate -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", candidate.getId());
                    info.put("name", candidate.getName());
                    info.put("email", candidate.getEmail());
                    info.put("currentStage", candidate.getCurrentStage());
                    info.put("appliedDate", candidate.getCreatedAt());
                    return info;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get upcoming interviews (next N days)
     */
    public List<Map<String, Object>> getUpcomingInterviews(int days) {
        log.debug("Fetching interviews for next {} days", days);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);
        
        return interviewRepository.findInterviewsInDateRange(now, future)
                .stream()
                .map(interview -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", interview.getId());
                    info.put("candidateName", interview.getCandidate().getName());
                    info.put("interviewerName", interview.getInterviewer().getName());
                    info.put("scheduledAt", interview.getScheduledAt());
                    info.put("type", interview.getInterviewType());
                    info.put("status", interview.getCurrentStatus());
                    info.put("location", interview.getLocation());
                    return info;
                })
                .collect(Collectors.toList());
    }
}
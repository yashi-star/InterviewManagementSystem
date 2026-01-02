package com.interview.management.service;

import com.interview.management.entity.Interviewer;
import com.interview.management.repository.InterviewerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interviewer Service
 * Handles interviewer management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterviewerService {
    
    private final InterviewerRepository interviewerRepository;
    
    /**
     * Create new interviewer
     */
    public Interviewer createInterviewer(Interviewer interviewer) {
        log.info("Creating interviewer: {}", interviewer.getEmail());
        
        // Check if email already exists
        if (interviewerRepository.existsByEmail(interviewer.getEmail())) {
            log.error("Interviewer with email {} already exists", interviewer.getEmail());
            throw new IllegalArgumentException("Interviewer with this email already exists");
        }
        
        Interviewer saved = interviewerRepository.save(interviewer);
        log.info("Interviewer created successfully with ID: {}", saved.getId());
        return saved;
    }
    
    /**
     * Get interviewer by ID
     */
    @Transactional(readOnly = true)
    public Interviewer getInterviewerById(Long id) {
        log.debug("Fetching interviewer: {}", id);
        return interviewerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interviewer not found: " + id));
    }
    
    /**
     * Get interviewer by email
     */
    @Transactional(readOnly = true)
    public Interviewer getInterviewerByEmail(String email) {
        log.debug("Fetching interviewer with email: {}", email);
        return interviewerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Interviewer not found: " + email));
    }
    
    /**
     * Get all interviewers
     */
    @Transactional(readOnly = true)
    public List<Interviewer> getAllInterviewers() {
        log.debug("Fetching all interviewers");
        return interviewerRepository.findAll();
    }
    
    /**
     * Get interviewers by department
     */
    @Transactional(readOnly = true)
    public List<Interviewer> getInterviewersByDepartment(String department) {
        log.debug("Fetching interviewers in department: {}", department);
        return interviewerRepository.findByDepartment(department);
    }
    
    /**
     * Find interviewers by expertise/skill
     */
    @Transactional(readOnly = true)
    public List<Interviewer> findInterviewersByExpertise(String skill) {
        log.debug("Searching interviewers with expertise in: {}", skill);
        return interviewerRepository.findByExpertiseContaining(skill);
    }
    
    /**
     * Find available interviewers in a time slot
     */
    @Transactional(readOnly = true)
    public List<Interviewer> findAvailableInterviewers(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("Finding available interviewers between {} and {}", startTime, endTime);
        return interviewerRepository.findAvailableInterviewers(startTime, endTime);
    }
    
    /**
     * Update interviewer details
     */
    public Interviewer updateInterviewer(Long interviewerId, Interviewer updates) {
        log.info("Updating interviewer: {}", interviewerId);
        
        Interviewer interviewer = getInterviewerById(interviewerId);
        
        if (updates.getName() != null) {
            interviewer.setName(updates.getName());
        }
        if (updates.getDepartment() != null) {
            interviewer.setDepartment(updates.getDepartment());
        }
        if (updates.getDesignation() != null) {
            interviewer.setDesignation(updates.getDesignation());
        }
        if (updates.getExpertise() != null) {
            interviewer.setExpertise(updates.getExpertise());
        }
        // Email cannot be updated as it's unique identifier
        
        Interviewer updated = interviewerRepository.save(interviewer);
        log.info("Interviewer {} updated successfully", interviewerId);
        return updated;
    }
    
    /**
     * Delete interviewer
     */
    public void deleteInterviewer(Long interviewerId) {
        log.info("Deleting interviewer: {}", interviewerId);
        
        Interviewer interviewer = getInterviewerById(interviewerId);
        
        // Check if interviewer has associated interviews
        if (!interviewer.getInterviews().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete interviewer with existing interviews. " +
                    "Archive the interviewer instead.");
        }
        
        interviewerRepository.delete(interviewer);
        log.info("Interviewer {} deleted successfully", interviewerId);
    }
    
    /**
     * Check if interviewer is available at a specific time
     */
    @Transactional(readOnly = true)
    public boolean isInterviewerAvailable(Long interviewerId, LocalDateTime startTime, 
                                          LocalDateTime endTime) {
        log.debug("Checking availability for interviewer {} from {} to {}", 
                interviewerId, startTime, endTime);
        
        List<Interviewer> available = findAvailableInterviewers(startTime, endTime);
        return available.stream().anyMatch(i -> i.getId().equals(interviewerId));
    }
}
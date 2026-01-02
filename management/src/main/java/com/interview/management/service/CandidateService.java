package com.interview.management.service;

import com.interview.management.entity.Candidate;
import com.interview.management.entity.enums.CandidateStage;
import com.interview.management.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Candidate Service
 * Handles all candidate-related business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CandidateService {
    
    private final CandidateRepository candidateRepository;
    private static final String RESUME_UPLOAD_DIR = "uploads/resumes/";
    
    /**
     * Create new candidate with resume upload
     */
    public Candidate createCandidate(Candidate candidate, MultipartFile resume) {
        log.info("Creating candidate: {}", candidate.getEmail());
        
        // Check if email already exists
        if (candidateRepository.existsByEmail(candidate.getEmail())) {
            log.error("Candidate with email {} already exists", candidate.getEmail());
            throw new IllegalArgumentException("Candidate with this email already exists");
        }
        
        // Handle resume upload if provided
        if (resume != null && !resume.isEmpty()) {
            String resumePath = saveResume(resume, candidate.getEmail());
            candidate.setResumePath(resumePath);
        }
        
        // Set initial stage
        candidate.setCurrentStage(CandidateStage.APPLIED);
        
        Candidate saved = candidateRepository.save(candidate);
        log.info("Candidate created successfully with ID: {}", saved.getId());
        return saved;
    }
    
    /**
     * Save resume file to disk
     */
    private String saveResume(MultipartFile file, String email) {
        try {
            // Create directory if not exists
            Path uploadPath = Paths.get(RESUME_UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = email.replace("@", "_") + "_" + UUID.randomUUID() + extension;
            
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            log.info("Resume saved: {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            log.error("Failed to save resume for {}", email, e);
            throw new RuntimeException("Failed to save resume file", e);
        }
    }
    
    /**
     * Get candidate by ID
     */
    @Transactional(readOnly = true)
    public Candidate getCandidateById(Long id) {
        log.debug("Fetching candidate with ID: {}", id);
        return candidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + id));
    }
    
    /**
     * Get candidate by email
     */
    @Transactional(readOnly = true)
    public Candidate getCandidateByEmail(String email) {
        log.debug("Fetching candidate with email: {}", email);
        return candidateRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with email: " + email));
    }
    
    /**
     * Get all candidates with pagination
     */
    @Transactional(readOnly = true)
    public Page<Candidate> getAllCandidates(Pageable pageable) {
        log.debug("Fetching all candidates with pagination");
        return candidateRepository.findAll(pageable);
    }
    
    /**
     * Get candidates by stage
     */
    @Transactional(readOnly = true)
    public Page<Candidate> getCandidatesByStage(CandidateStage stage, Pageable pageable) {
        log.debug("Fetching candidates in stage: {}", stage);
        return candidateRepository.findByCurrentStage(stage, pageable);
    }
    
    /**
     * Search candidates
     */
    @Transactional(readOnly = true)
    public Page<Candidate> searchCandidates(String name, String email, CandidateStage stage, Pageable pageable) {
        log.debug("Searching candidates with filters - name: {}, email: {}, stage: {}", name, email, stage);
        return candidateRepository.searchCandidates(name, email, stage, pageable);
    }
    
    /**
     * Update candidate stage with history tracking
     */
    public Candidate updateCandidateStage(Long candidateId, CandidateStage newStage, 
                                          String changedBy, String reason) {
        log.info("Updating candidate {} stage to {}", candidateId, newStage);
        
        Candidate candidate = getCandidateById(candidateId);
        
        if (candidate.getCurrentStage() == newStage) {
            log.warn("Candidate {} is already in stage {}", candidateId, newStage);
            throw new IllegalArgumentException("Candidate is already in this stage");
        }
        
        // Update stage and create history entry
        candidate.updateStage(newStage, changedBy, reason);
        
        Candidate updated = candidateRepository.save(candidate);
        log.info("Candidate {} stage updated to {}", candidateId, newStage);
        return updated;
    }
    
    /**
     * Update candidate details
     */
    public Candidate updateCandidate(Long candidateId, Candidate updates) {
        log.info("Updating candidate details: {}", candidateId);
        
        Candidate candidate = getCandidateById(candidateId);
        
        if (updates.getName() != null) {
            candidate.setName(updates.getName());
        }
        if (updates.getPhone() != null) {
            candidate.setPhone(updates.getPhone());
        }
        // Email cannot be updated as it's unique identifier
        
        Candidate updated = candidateRepository.save(candidate);
        log.info("Candidate {} updated successfully", candidateId);
        return updated;
    }
    
    /**
     * Delete candidate
     */
    public void deleteCandidate(Long candidateId) {
        log.info("Deleting candidate: {}", candidateId);
        
        Candidate candidate = getCandidateById(candidateId);
        
        // Delete resume file if exists
        if (candidate.getResumePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(candidate.getResumePath()));
                log.debug("Resume file deleted: {}", candidate.getResumePath());
            } catch (IOException e) {
                log.error("Failed to delete resume file: {}", candidate.getResumePath(), e);
            }
        }
        
        candidateRepository.delete(candidate);
        log.info("Candidate {} deleted successfully", candidateId);
    }
    
    /**
     * Get candidates without AI screening
     */
    @Transactional(readOnly = true)
    public List<Candidate> getCandidatesWithoutScreening() {
        log.debug("Fetching candidates without AI screening");
        return candidateRepository.findCandidatesWithoutScreening();
    }
    
    /**
     * Get top candidates by AI score
     */
    @Transactional(readOnly = true)
    public List<Candidate> getTopCandidatesByAIScore(int minScore, int limit) {
        log.debug("Fetching top candidates with minScore: {}, limit: {}", minScore, limit);
        return candidateRepository.findTopCandidatesByAIScore(minScore, limit);
    }
    
    /**
     * Get candidate statistics by stage
     */
    @Transactional(readOnly = true)
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
     * Get candidates created after date
     */
    @Transactional(readOnly = true)
    public List<Candidate> getCandidatesCreatedAfter(LocalDateTime date) {
        log.debug("Fetching candidates created after: {}", date);
        return candidateRepository.findByCreatedAtAfter(date);
    }
    
    /**
     * Count candidates this month
     */
    @Transactional(readOnly = true)
    public Long countCandidatesThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        log.debug("Counting candidates from: {}", startOfMonth);
        return candidateRepository.countCandidatesCreatedAfter(startOfMonth);
    }
    
    /**
     * Get resume file path
     */
    @Transactional(readOnly = true)
    public String getResumePath(Long candidateId) {
        Candidate candidate = getCandidateById(candidateId);
        if (candidate.getResumePath() == null) {
            throw new IllegalArgumentException("Candidate has no resume uploaded");
        }
        return candidate.getResumePath();
    }
}
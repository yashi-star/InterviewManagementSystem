package com.interview.management.controller.Repository;

import com.interview.management.repository.CandidateRepository;

import com.interview.management.entity.Candidate;
import com.interview.management.entity.enums.CandidateStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository tests for CandidateRepository
 * Uses H2 in-memory database for testing
 */
@DataJpaTest
@DisplayName("CandidateRepository Tests")
class CandidateRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    private Candidate candidate1;
    private Candidate candidate2;
    
    @BeforeEach
    void setUp() {
        candidate1 = Candidate.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .currentStage(CandidateStage.APPLIED)
                .build();
        
        candidate2 = Candidate.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .phone("0987654321")
                .currentStage(CandidateStage.SCREENING)
                .build();
        
        entityManager.persist(candidate1);
        entityManager.persist(candidate2);
        entityManager.flush();
    }
    
    // ============================================
    // BASIC CRUD TESTS
    // ============================================
    
    @Test
    @DisplayName("Should save candidate successfully")
    void save_Success() {
        // Given
        Candidate newCandidate = Candidate.builder()
                .name("Bob Johnson")
                .email("bob@example.com")
                .currentStage(CandidateStage.APPLIED)
                .build();
        
        // When
        Candidate saved = candidateRepository.save(newCandidate);
        
        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Bob Johnson");
    }
    
    @Test
    @DisplayName("Should find candidate by ID")
    void findById_Success() {
        // When
        Optional<Candidate> found = candidateRepository.findById(candidate1.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should find all candidates")
    void findAll_Success() {
        // When
        List<Candidate> candidates = candidateRepository.findAll();
        
        // Then
        assertThat(candidates).hasSize(2);
    }
    
    // ============================================
    // CUSTOM QUERY TESTS
    // ============================================
    
    @Test
    @DisplayName("Should find candidate by email")
    void findByEmail_Success() {
        // When
        Optional<Candidate> found = candidateRepository.findByEmail("john.doe@example.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should return empty when email not found")
    void findByEmail_NotFound() {
        // When
        Optional<Candidate> found = candidateRepository.findByEmail("nonexistent@example.com");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("Should check if email exists")
    void existsByEmail_True() {
        // When
        boolean exists = candidateRepository.existsByEmail("john.doe@example.com");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("Should return false when email doesn't exist")
    void existsByEmail_False() {
        // When
        boolean exists = candidateRepository.existsByEmail("nonexistent@example.com");
        
        // Then
        assertThat(exists).isFalse();
    }
    
    @Test
    @DisplayName("Should find candidates by stage")
    void findByCurrentStage_Success() {
        // When
        List<Candidate> appliedCandidates = candidateRepository.findByCurrentStage(CandidateStage.APPLIED);
        
        // Then
        assertThat(appliedCandidates).hasSize(1);
        assertThat(appliedCandidates.get(0).getName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should find candidates by stage with pagination")
    void findByCurrentStageWithPagination_Success() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        
        // When
        Page<Candidate> page = candidateRepository.findByCurrentStage(CandidateStage.APPLIED, pageable);
        
        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Should find candidates by name containing (case insensitive)")
    void findByNameContainingIgnoreCase_Success() {
        // When
        List<Candidate> found = candidateRepository.findByNameContainingIgnoreCase("john");
        
        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should find candidates created after date")
    void findByCreatedAtAfter_Success() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        
        // When
        List<Candidate> found = candidateRepository.findByCreatedAtAfter(yesterday);
        
        // Then
        assertThat(found).hasSize(2); // Both created today
    }
    
    @Test
    @DisplayName("Should find candidates by stage and created after date")
    void findByCurrentStageAndCreatedAtAfter_Success() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        
        // When
        List<Candidate> found = candidateRepository.findByCurrentStageAndCreatedAtAfter(
                CandidateStage.APPLIED, yesterday);
        
        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCurrentStage()).isEqualTo(CandidateStage.APPLIED);
    }
    
    // ============================================
    // CUSTOM JPQL QUERY TESTS
    // ============================================
    
    @Test
    @DisplayName("Should count candidates by stage")
    void countByStage_Success() {
        // When
        List<Object[]> counts = candidateRepository.countByStage();
        
        // Then
        assertThat(counts).isNotEmpty();
        assertThat(counts).hasSize(2); // APPLIED and SCREENING
    }
    
    @Test
    @DisplayName("Should count candidates created after date")
    void countCandidatesCreatedAfter_Success() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        
        // When
        Long count = candidateRepository.countCandidatesCreatedAfter(yesterday);
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should search candidates with multiple criteria")
    void searchCandidates_Success() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        
        // When
        Page<Candidate> found = candidateRepository.searchCandidates(
                "John", null, CandidateStage.APPLIED, pageable);
        
        // Then
        assertThat(found.getContent()).hasSize(1);
        assertThat(found.getContent().get(0).getName()).isEqualTo("John Doe");
    }
    
    @Test
    @DisplayName("Should search candidates with null criteria (find all)")
    void searchCandidates_NullCriteria_FindsAll() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        
        // When
        Page<Candidate> found = candidateRepository.searchCandidates(
                null, null, null, pageable);
        
        // Then
        assertThat(found.getContent()).hasSize(2);
    }
    
    // ============================================
    // DELETE TESTS
    // ============================================
    
    @Test
    @DisplayName("Should delete candidate successfully")
    void delete_Success() {
        // When
        candidateRepository.delete(candidate1);
        entityManager.flush();
        
        // Then
        Optional<Candidate> found = candidateRepository.findById(candidate1.getId());
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("Should delete candidate by ID")
    void deleteById_Success() {
        // When
        candidateRepository.deleteById(candidate1.getId());
        entityManager.flush();
        
        // Then
        Optional<Candidate> found = candidateRepository.findById(candidate1.getId());
        assertThat(found).isEmpty();
    }
}
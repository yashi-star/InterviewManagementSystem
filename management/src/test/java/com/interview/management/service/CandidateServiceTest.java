package com.interview.management.service;

import com.interview.management.entity.Candidate;
import com.interview.management.entity.enums.CandidateStage;
import com.interview.management.exception.ResourceAlreadyExistsException;
import com.interview.management.exception.ResourceNotFoundException;
import com.interview.management.exception.ValidationException;
import com.interview.management.repository.CandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CandidateService
 * Tests business logic without Spring context
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CandidateService Unit Tests")
class CandidateServiceTest {
    
    @Mock
    private CandidateRepository candidateRepository;
    
    @InjectMocks
    private CandidateService candidateService;
    
    private Candidate testCandidate;
    private MultipartFile mockResume;
    
    @BeforeEach
    void setUp() {
        // Initialize test data
        testCandidate = Candidate.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .currentStage(CandidateStage.APPLIED)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Mock resume file
        mockResume = new MockMultipartFile(
                "resume",
                "resume.pdf",
                "application/pdf",
                "Mock PDF content".getBytes()
        );
    }
    
    // ============================================
    // CREATE CANDIDATE TESTS
    // ============================================
    
    @Test
    @DisplayName("Should create candidate successfully")
    void createCandidate_Success() {
        // Given
        when(candidateRepository.existsByEmail(testCandidate.getEmail())).thenReturn(false);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(testCandidate);
        
        // When
        Candidate result = candidateService.createCandidate(testCandidate, null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getCurrentStage()).isEqualTo(CandidateStage.APPLIED);
        
        verify(candidateRepository).existsByEmail(testCandidate.getEmail());
        verify(candidateRepository).save(any(Candidate.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void createCandidate_DuplicateEmail_ThrowsException() {
        // Given
        when(candidateRepository.existsByEmail(testCandidate.getEmail())).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> candidateService.createCandidate(testCandidate, null))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("email");
        
        verify(candidateRepository).existsByEmail(testCandidate.getEmail());
        verify(candidateRepository, never()).save(any(Candidate.class));
    }
    
    @Test
    @DisplayName("Should throw exception when name is empty")
    void createCandidate_EmptyName_ThrowsException() {
        // Given
        testCandidate.setName("");
        when(candidateRepository.existsByEmail(testCandidate.getEmail())).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> candidateService.createCandidate(testCandidate, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name");
    }
    
    @Test
    @DisplayName("Should throw exception when name is null")
    void createCandidate_NullName_ThrowsException() {
        // Given
        testCandidate.setName(null);
        when(candidateRepository.existsByEmail(testCandidate.getEmail())).thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> candidateService.createCandidate(testCandidate, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("name");
    }
    
    // ============================================
    // GET CANDIDATE TESTS
    // ============================================
    
    @Test
    @DisplayName("Should get candidate by ID successfully")
    void getCandidateById_Success() {
        // Given
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        
        // When
        Candidate result = candidateService.getCandidateById(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        
        verify(candidateRepository).findById(1L);
    }
    
    @Test
    @DisplayName("Should throw exception when candidate not found")
    void getCandidateById_NotFound_ThrowsException() {
        // Given
        when(candidateRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> candidateService.getCandidateById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Candidate")
                .hasMessageContaining("999");
        
        verify(candidateRepository).findById(999L);
    }
    
    @Test
    @DisplayName("Should get candidate by email successfully")
    void getCandidateByEmail_Success() {
        // Given
        when(candidateRepository.findByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(testCandidate));
        
        // When
        Candidate result = candidateService.getCandidateByEmail("john.doe@example.com");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        
        verify(candidateRepository).findByEmail("john.doe@example.com");
    }
    
    // ============================================
    // UPDATE CANDIDATE STAGE TESTS
    // ============================================
    
    @Test
    @DisplayName("Should update candidate stage successfully")
    void updateCandidateStage_Success() {
        // Given
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        when(candidateRepository.save(any(Candidate.class))).thenReturn(testCandidate);
        
        // When
        Candidate result = candidateService.updateCandidateStage(
                1L, 
                CandidateStage.SCREENING, 
                "recruiter@company.com", 
                "Moving to screening"
        );
        
        // Then
        assertThat(result).isNotNull();
        verify(candidateRepository).findById(1L);
        verify(candidateRepository).save(any(Candidate.class));
    }
    
    @Test
    @DisplayName("Should throw exception when stage is already current")
    void updateCandidateStage_SameStage_ThrowsException() {
        // Given
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        
        // When & Then
        assertThatThrownBy(() -> candidateService.updateCandidateStage(
                1L, 
                CandidateStage.APPLIED, // Same as current
                "recruiter@company.com", 
                "test"
        )).hasMessageContaining("already in stage");
        
        verify(candidateRepository).findById(1L);
        verify(candidateRepository, never()).save(any(Candidate.class));
    }
    
    @Test
    @DisplayName("Should throw exception when invalid stage transition")
    void updateCandidateStage_InvalidTransition_ThrowsException() {
        // Given
        testCandidate.setCurrentStage(CandidateStage.HIRED);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        
        // When & Then
        assertThatThrownBy(() -> candidateService.updateCandidateStage(
                1L, 
                CandidateStage.SCREENING, 
                "recruiter@company.com", 
                "test"
        )).hasMessageContaining("stage transition");
    }
    
    // ============================================
    // DELETE CANDIDATE TESTS
    // ============================================
    
    @Test
    @DisplayName("Should delete candidate successfully")
    void deleteCandidate_Success() {
        // Given
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        doNothing().when(candidateRepository).delete(any(Candidate.class));
        
        // When
        candidateService.deleteCandidate(1L);
        
        // Then
        verify(candidateRepository).findById(1L);
        verify(candidateRepository).delete(testCandidate);
    }
    
    @Test
    @DisplayName("Should throw exception when deleting hired candidate")
    void deleteCandidate_HiredCandidate_ThrowsException() {
        // Given
        testCandidate.setCurrentStage(CandidateStage.HIRED);
        when(candidateRepository.findById(1L)).thenReturn(Optional.of(testCandidate));
        
        // When & Then
        assertThatThrownBy(() -> candidateService.deleteCandidate(1L))
                .hasMessageContaining("Cannot delete hired candidates");
        
        verify(candidateRepository).findById(1L);
        verify(candidateRepository, never()).delete(any(Candidate.class));
    }
    
    @Test
    @DisplayName("Should throw exception when deleting non-existent candidate")
    void deleteCandidate_NotFound_ThrowsException() {
        // Given
        when(candidateRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> candidateService.deleteCandidate(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(candidateRepository).findById(999L);
        verify(candidateRepository, never()).delete(any(Candidate.class));
    }
}

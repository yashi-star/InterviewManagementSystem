package com.interview.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.management.entity.Candidate;
import com.interview.management.entity.enums.CandidateStage;
import com.interview.management.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CandidateController
 * Tests HTTP layer with MockMvc
 */
@WebMvcTest(CandidateController.class)
@DisplayName("CandidateController Integration Tests")
class CandidateControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private CandidateService candidateService;
    
    private Candidate testCandidate;
    
    @BeforeEach
    void setUp() {
        testCandidate = Candidate.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .currentStage(CandidateStage.APPLIED)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    // ============================================
    // CREATE CANDIDATE TESTS
    // ============================================
    
    @Test
    @DisplayName("POST /api/candidates - Should create candidate successfully")
    void createCandidate_Success() throws Exception {
        // Given
        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "resume.pdf",
                "application/pdf",
                "Mock PDF content".getBytes()
        );
        
        when(candidateService.createCandidate(any(Candidate.class), any()))
                .thenReturn(testCandidate);
        
        // When & Then
        mockMvc.perform(multipart("/api/candidates")
                        .file(resume)
                        .param("name", "John Doe")
                        .param("email", "john.doe@example.com")
                        .param("phone", "1234567890"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.currentStage", is("APPLIED")));
        
        verify(candidateService).createCandidate(any(Candidate.class), any());
    }
    
    @Test
    @DisplayName("POST /api/candidates - Should return 400 when validation fails")
    void createCandidate_ValidationError_Returns400() throws Exception {
        // When & Then - missing required fields
        mockMvc.perform(multipart("/api/candidates")
                        .param("email", "invalid-email"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    // ============================================
    // GET CANDIDATE TESTS
    // ============================================
    
    @Test
    @DisplayName("GET /api/candidates/{id} - Should return candidate when found")
    void getCandidateById_Success() throws Exception {
        // Given
        when(candidateService.getCandidateById(1L)).thenReturn(testCandidate);
        
        // When & Then
        mockMvc.perform(get("/api/candidates/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
        
        verify(candidateService).getCandidateById(1L);
    }
    
    @Test
    @DisplayName("GET /api/candidates/{id} - Should return 404 when not found")
    void getCandidateById_NotFound_Returns404() throws Exception {
        // Given
        when(candidateService.getCandidateById(999L))
                .thenThrow(new com.interview.management.exception.ResourceNotFoundException("Candidate", 999L));
        
        // When & Then
        mockMvc.perform(get("/api/candidates/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Candidate not found")));
        
        verify(candidateService).getCandidateById(999L);
    }
    
    @Test
    @DisplayName("GET /api/candidates - Should return paginated candidates")
    void getAllCandidates_Success() throws Exception {
        // Given
        List<Candidate> candidates = Arrays.asList(testCandidate);
        Page<Candidate> page = new PageImpl<>(candidates);
        
        when(candidateService.getAllCandidates(any())).thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/api/candidates")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("John Doe")));
        
        verify(candidateService).getAllCandidates(any());
    }
    
    @Test
    @DisplayName("GET /api/candidates/stage/{stage} - Should return candidates by stage")
    void getCandidatesByStage_Success() throws Exception {
        // Given
        List<Candidate> candidates = Arrays.asList(testCandidate);
        Page<Candidate> page = new PageImpl<>(candidates);
        
        when(candidateService.getCandidatesByStage(eq(CandidateStage.APPLIED), any()))
                .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/api/candidates/stage/APPLIED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].currentStage", is("APPLIED")));
        
        verify(candidateService).getCandidatesByStage(eq(CandidateStage.APPLIED), any());
    }
    
    // ============================================
    // UPDATE CANDIDATE TESTS
    // ============================================
    
    @Test
    @DisplayName("PUT /api/candidates/{id}/stage - Should update stage successfully")
    void updateCandidateStage_Success() throws Exception {
        // Given
        testCandidate.setCurrentStage(CandidateStage.SCREENING);
        when(candidateService.updateCandidateStage(
                eq(1L), eq(CandidateStage.SCREENING), anyString(), anyString()))
                .thenReturn(testCandidate);
        
        // When & Then
        mockMvc.perform(put("/api/candidates/1/stage")
                        .param("newStage", "SCREENING")
                        .param("changedBy", "recruiter@company.com")
                        .param("reason", "Screening started"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStage", is("SCREENING")));
        
        verify(candidateService).updateCandidateStage(
                eq(1L), eq(CandidateStage.SCREENING), anyString(), anyString());
    }
    
    @Test
    @DisplayName("PUT /api/candidates/{id}/stage - Should return 400 for invalid stage transition")
    void updateCandidateStage_InvalidTransition_Returns400() throws Exception {
        // Given
        when(candidateService.updateCandidateStage(
                eq(1L), eq(CandidateStage.HIRED), anyString(), anyString()))
                .thenThrow(new com.interview.management.exception.BusinessRuleViolationException(
                        "Invalid stage transition"));
        
        // When & Then
        mockMvc.perform(put("/api/candidates/1/stage")
                        .param("newStage", "HIRED")
                        .param("changedBy", "recruiter"))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Invalid stage transition")));
    }
    
    // ============================================
    // DELETE CANDIDATE TESTS
    // ============================================
    
    @Test
    @DisplayName("DELETE /api/candidates/{id} - Should delete candidate successfully")
    void deleteCandidate_Success() throws Exception {
        // Given
        doNothing().when(candidateService).deleteCandidate(1L);
        
        // When & Then
        mockMvc.perform(delete("/api/candidates/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
        
        verify(candidateService).deleteCandidate(1L);
    }
    
    @Test
    @DisplayName("DELETE /api/candidates/{id} - Should return 404 when not found")
    void deleteCandidate_NotFound_Returns404() throws Exception {
        // Given
        doThrow(new com.interview.management.exception.ResourceNotFoundException("Candidate", 999L))
                .when(candidateService).deleteCandidate(999L);
        
        // When & Then
        mockMvc.perform(delete("/api/candidates/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
        
        verify(candidateService).deleteCandidate(999L);
    }
    
    // ============================================
    // SEARCH TESTS
    // ============================================
    
    @Test
    @DisplayName("GET /api/candidates/search - Should search candidates")
    void searchCandidates_Success() throws Exception {
        // Given
        List<Candidate> candidates = Arrays.asList(testCandidate);
        Page<Candidate> page = new PageImpl<>(candidates);
        
        when(candidateService.searchCandidates(eq("John"), any(), any(), any()))
                .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/api/candidates/search")
                        .param("name", "John"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", containsString("John")));
        
        verify(candidateService).searchCandidates(eq("John"), any(), any(), any());
    }
    
    // ============================================
    // STATISTICS TESTS
    // ============================================
    
    @Test
    @DisplayName("GET /api/candidates/statistics - Should return statistics")
    void getCandidateStatistics_Success() throws Exception {
        // Given
        when(candidateService.getCandidateCountByStage()).thenReturn(
                java.util.Map.of(
                        CandidateStage.APPLIED, 10L,
                        CandidateStage.SCREENING, 5L
                )
        );
        when(candidateService.countCandidatesThisMonth()).thenReturn(15L);
        
        // When & Then
        mockMvc.perform(get("/api/candidates/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countByStage", notNullValue()))
                .andExpect(jsonPath("$.totalThisMonth", is(15)));
        
        verify(candidateService).getCandidateCountByStage();
        verify(candidateService).countCandidatesThisMonth();
    }
}
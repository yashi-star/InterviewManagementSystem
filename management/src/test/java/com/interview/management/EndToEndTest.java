package com.interview.management;

import com.interview.management.entity.*;
import com.interview.management.entity.enums.*;
import com.interview.management.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Test
 * Tests complete candidate journey from application to hiring
 * 
 * Flow:
 * 1. Create Candidate with Resume
 * 2. AI Screen Candidate
 * 3. Create Interviewer
 * 4. Schedule Interview
 * 5. Complete Interview
 * 6. Submit Feedback
 * 7. Hire Candidate
 * 8. Verify History/Audit Trail
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("End-to-End Integration Test - Complete Candidate Journey")
class EndToEndIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private InterviewerRepository interviewerRepository;
    
    @Autowired
    private InterviewRepository interviewRepository;
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private AIScreeningRepository screeningRepository;
    
    private static Long candidateId;
    private static Long interviewerId;
    private static Long interviewId;
    
    @BeforeEach
    void setUp() {
        // Clean up database before each test
        feedbackRepository.deleteAll();
        interviewRepository.deleteAll();
        screeningRepository.deleteAll();
        interviewerRepository.deleteAll();
        candidateRepository.deleteAll();
    }
    
    // ============================================
    // STEP 1: CREATE CANDIDATE
    // ============================================
    
    @Test
    @Order(1)
    @DisplayName("Step 1: Create candidate with resume")
    void step1_CreateCandidate() throws Exception {
        // Given
        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "john_resume.pdf",
                "application/pdf",
                "Mock resume content for John Doe".getBytes()
        );
        
        // When & Then
        MvcResult result = mockMvc.perform(multipart("/api/candidates")
                        .file(resume)
                        .param("name", "John Doe")
                        .param("email", "john.doe@example.com")
                        .param("phone", "1234567890"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.currentStage").value("APPLIED"))
                .andReturn();
        
        // Extract candidate ID for next steps
        String response = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        candidateId = mapper.readTree(response)
                .get("id")
                .asLong();
        
        // Verify in database
        Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();
        assertThat(candidate.getName()).isEqualTo("John Doe");
        assertThat(candidate.getCurrentStage()).isEqualTo(CandidateStage.APPLIED);
    }
    
    // ============================================
    // STEP 2: AI SCREENING (Mock - Skip Ollama)
    // ============================================
    
    @Test
    @Order(2)
    @DisplayName("Step 2: AI screen candidate (manual - skip AI call)")
    @Transactional
    void step2_ScreenCandidate() {
        // For E2E test, create screening manually to avoid Ollama dependency
        Candidate candidate = candidateRepository.findAll().get(0);
        candidateId = candidate.getId();
        
        AIScreening screening = AIScreening.builder()
                .candidate(candidate)
                .skillsMatched("Java - Advanced - 5 years, Spring Boot - Advanced - 4 years")
                .experienceYears(5.0)
                .educationLevel("B.Tech Computer Science, XYZ University, 2018")
                .culturalFit("Teamwork: High, Leadership: Medium, Communication: High")
                .matchScore(85)
                .analysisText("Strong technical candidate with excellent Java skills")
                .aiModelUsed("test-model")
                .build();
        
        screeningRepository.save(screening);
        
        // Update candidate stage
        candidate.updateStage(CandidateStage.SCREENING, "AI_SYSTEM", "AI screening completed");
        candidateRepository.save(candidate);
        
        // Verify
        AIScreening saved = screeningRepository.findLatestScreeningForCandidate(candidateId).orElseThrow();
        assertThat(saved.getMatchScore()).isEqualTo(85);
        assertThat(candidate.getCurrentStage()).isEqualTo(CandidateStage.SCREENING);
    }
    
    // ============================================
    // STEP 3: CREATE INTERVIEWER
    // ============================================
    
    @Test
    @Order(3)
    @DisplayName("Step 3: Create interviewer")
    void step3_CreateInterviewer() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/interviewers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Jane Smith",
                                    "email": "jane.smith@company.com",
                                    "department": "Engineering",
                                    "designation": "Senior Software Engineer",
                                    "expertise": "Java, Spring Boot, Microservices"
                                }
                                """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andReturn();
        
        // Extract interviewer ID
        String response = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        interviewerId = mapper.readTree(response)
                .get("id")
                .asLong();
        
        // Verify in database
        Interviewer interviewer = interviewerRepository.findById(interviewerId).orElseThrow();
        assertThat(interviewer.getName()).isEqualTo("Jane Smith");
    }
    
    // ============================================
    // STEP 4: SCHEDULE INTERVIEW
    // ============================================
    
    @Test
    @Order(4)
    @DisplayName("Step 4: Schedule interview")
    void step4_ScheduleInterview() throws Exception {
        // Get IDs from previous steps
        if (candidateId == null) {
            candidateId = candidateRepository.findAll().get(0).getId();
        }
        if (interviewerId == null) {
            interviewerId = interviewerRepository.findAll().get(0).getId();
        }
        
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/interviews")
                        .param("candidateId", candidateId.toString())
                        .param("interviewerId", interviewerId.toString())
                        .param("scheduledAt", futureTime.toString())
                        .param("durationMinutes", "60")
                        .param("type", "TECHNICAL")
                        .param("location", "Conference Room A")
                        .param("notes", "Technical round - Java and Spring Boot")
                        .param("scheduledBy", "recruiter@company.com"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.currentStatus").value("SCHEDULED"))
                .andExpect(jsonPath("$.interviewType").value("TECHNICAL"))
                .andReturn();
        
        // Extract interview ID
        String response = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        interviewId = mapper.readTree(response)
                .get("id")
                .asLong();
        
        // Verify candidate stage updated
        Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();
        assertThat(candidate.getCurrentStage()).isEqualTo(CandidateStage.INTERVIEW_SCHEDULED);
    }
    
    // ============================================
    // STEP 5: COMPLETE INTERVIEW
    // ============================================
    
    @Test
    @Order(5)
    @DisplayName("Step 5: Complete interview")
    void step5_CompleteInterview() throws Exception {
        // Get IDs from previous steps
        if (interviewId == null) {
            interviewId = interviewRepository.findAll().get(0).getId();
        }
        
        // When & Then
        mockMvc.perform(put("/api/interviews/" + interviewId + "/status")
                        .param("newStatus", "COMPLETED")
                        .param("changedBy", "interviewer@company.com")
                        .param("notes", "Interview completed successfully"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("COMPLETED"));
        
        // Verify candidate stage updated
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        Candidate candidate = interview.getCandidate();
        assertThat(candidate.getCurrentStage()).isEqualTo(CandidateStage.INTERVIEW_COMPLETED);
    }
    
    // ============================================
    // STEP 6: SUBMIT FEEDBACK
    // ============================================
    
    @Test
    @Order(6)
    @DisplayName("Step 6: Submit interview feedback")
    void step6_SubmitFeedback() throws Exception {
        // Get IDs from previous steps
        if (interviewId == null) {
            interviewId = interviewRepository.findAll().get(0).getId();
        }
        if (interviewerId == null) {
            interviewerId = interviewerRepository.findAll().get(0).getId();
        }
        
        // When & Then
        mockMvc.perform(post("/api/feedback")
                        .param("interviewId", interviewId.toString())
                        .param("interviewerId", interviewerId.toString())
                        .param("technicalScore", "5")
                        .param("communicationScore", "4")
                        .param("problemSolvingScore", "5")
                        .param("culturalFitScore", "4")
                        .param("strengths", "Excellent technical skills, strong problem-solving")
                        .param("weaknesses", "Could improve on system design knowledge")
                        .param("additionalComments", "Great candidate, highly recommended")
                        .param("recommendation", "STRONG_HIRE"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technicalScore").value(5))
                .andExpect(jsonPath("$.recommendation").value("STRONG_HIRE"));
        
        // Verify in database
        List<Feedback> feedbacks = feedbackRepository.findByInterviewId(interviewId);
        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).getTechnicalScore()).isEqualTo(5);
    }
    
    // ============================================
    // STEP 7: HIRE CANDIDATE
    // ============================================
    
    @Test
    @Order(7)
    @DisplayName("Step 7: Hire candidate")
    void step7_HireCandidate() throws Exception {
        // Get candidate ID from previous steps
        if (candidateId == null) {
            candidateId = candidateRepository.findAll().get(0).getId();
        }
        
        // When & Then
        mockMvc.perform(put("/api/candidates/" + candidateId + "/stage")
                        .param("newStage", "HIRED")
                        .param("changedBy", "hr@company.com")
                        .param("reason", "Excellent performance in technical interview"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStage").value("HIRED"));
        
        // Verify final state
        Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();
        assertThat(candidate.getCurrentStage()).isEqualTo(CandidateStage.HIRED);
    }
    
    // ============================================
    // STEP 8: VERIFY AUDIT TRAIL
    // ============================================
    
    @Test
    @Order(8)
    @DisplayName("Step 8: Verify complete audit trail")
    void step8_VerifyAuditTrail() throws Exception {
        // Get candidate ID from previous steps
        if (candidateId == null) {
            candidateId = candidateRepository.findAll().get(0).getId();
        }
        
        // Verify candidate stage history
        mockMvc.perform(get("/api/history/candidates/" + candidateId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
        
        // Verify interview status history
        if (interviewId != null) {
            mockMvc.perform(get("/api/history/interviews/" + interviewId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
        
        // Verify dashboard statistics
        mockMvc.perform(get("/api/dashboard"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCandidates").exists())
                .andExpect(jsonPath("$.candidatesByStage").exists());
    }
    
    // ============================================
    // NEGATIVE TEST: PREVENT INVALID OPERATIONS
    // ============================================
    
    @Test
    @Order(9)
    @DisplayName("Step 9: Verify cannot delete hired candidate")
    void step9_CannotDeleteHiredCandidate() throws Exception {
        // Get candidate ID from previous steps
        if (candidateId == null) {
            candidateId = candidateRepository.findAll().get(0).getId();
        }
        
        // Try to delete hired candidate - should fail
        mockMvc.perform(delete("/api/candidates/" + candidateId))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Cannot delete hired")));
    }
    
    @Test
    @Order(10)
    @DisplayName("Step 10: Verify scheduling conflict detection")
    void step10_DetectSchedulingConflict() throws Exception {
        // Get interviewer ID from previous steps
        if (interviewerId == null) {
            interviewerId = interviewerRepository.findAll().get(0).getId();
        }
        
        // Create a new candidate
        Candidate newCandidate = Candidate.builder()
                .name("Alice Johnson")
                .email("alice@example.com")
                .currentStage(CandidateStage.SCREENING)
                .build();
        newCandidate = candidateRepository.save(newCandidate);
        
        // Get existing interview time
        Interview existingInterview = interviewRepository.findAll().get(0);
        LocalDateTime conflictTime = existingInterview.getScheduledAt().plusMinutes(30);
        
        // Try to schedule overlapping interview - should fail
        mockMvc.perform(post("/api/interviews")
                        .param("candidateId", newCandidate.getId().toString())
                        .param("interviewerId", interviewerId.toString())
                        .param("scheduledAt", conflictTime.toString())
                        .param("durationMinutes", "60")
                        .param("type", "HR")
                        .param("scheduledBy", "recruiter@company.com"))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("conflict")));
    }
}
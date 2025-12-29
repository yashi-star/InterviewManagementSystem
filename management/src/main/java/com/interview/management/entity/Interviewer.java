package com.interview.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Interviewer Entity
 * Represents employees who conduct interviews
 */
@Entity
@Table(name = "interviewers", indexes = {
    @Index(name = "idx_interviewer_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interviewer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(length = 100)
    private String department;
    
    /**
     * Job title
     * Example: "Senior Software Engineer", "HR Manager"
     */
    @Column(length = 100)
    private String designation;
    
    /**
     * Areas of expertise for conducting interviews
     * Example: "Java, Spring Boot, Microservices"
     */
    @Column(columnDefinition = "TEXT")
    private String expertise;
    
    /**
     * All interviews conducted by this interviewer
     */
    @OneToMany(mappedBy = "interviewer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Interview> interviews = new ArrayList<>();
    
    /**
     * All feedback provided by this interviewer
     */
    @OneToMany(mappedBy = "interviewer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Feedback> feedbacksProvided = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
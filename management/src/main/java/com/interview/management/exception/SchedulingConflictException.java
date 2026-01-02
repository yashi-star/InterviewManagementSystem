package com.interview.management.exception;

public class SchedulingConflictException extends BusinessException {
    private final Long interviewerId;
    private final String conflictTime;
    
    public SchedulingConflictException(Long interviewerId, String conflictTime) {
        super(String.format("Interviewer %d has a scheduling conflict at %s", interviewerId, conflictTime));
        this.interviewerId = interviewerId;
        this.conflictTime = conflictTime;
    }
    
    public SchedulingConflictException(String message) {
        super(message);
        this.interviewerId = null;
        this.conflictTime = null;
    }
    
    public Long getInterviewerId() {
        return interviewerId;
    }
    
    public String getConflictTime() {
        return conflictTime;
    }}

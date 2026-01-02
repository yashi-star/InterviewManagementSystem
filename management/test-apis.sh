#!/bin/bash

# ============================================
# API Test Script for Interview Management System
# ============================================
# This script tests all major APIs
# Make sure the application is running on http://localhost:8087

BASE_URL="http://localhost:8087"
CANDIDATE_ID=""
INTERVIEWER_ID=""
INTERVIEW_ID=""

echo "============================================"
echo "Testing Interview Management System APIs"
echo "============================================"
echo ""

echo "[1/8] Testing Dashboard API..."
curl -s -X GET "$BASE_URL/api/dashboard" -H "Content-Type: application/json" | jq '.' 2>/dev/null || echo "Response received"
echo ""

echo "[2/8] Creating Candidate..."
CANDIDATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/candidates" \
  -F "name=Test Candidate" \
  -F "email=test.candidate@example.com" \
  -F "phone=1234567890")
echo "$CANDIDATE_RESPONSE" | jq '.' 2>/dev/null || echo "$CANDIDATE_RESPONSE"
CANDIDATE_ID=$(echo "$CANDIDATE_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
echo "Candidate ID: $CANDIDATE_ID"
echo ""

echo "[3/8] Creating Interviewer..."
INTERVIEWER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/interviewers" \
  -d "name=Test Interviewer&email=test.interviewer@company.com&department=Engineering&expertise=Java,Spring")
echo "$INTERVIEWER_RESPONSE" | jq '.' 2>/dev/null || echo "$INTERVIEWER_RESPONSE"
INTERVIEWER_ID=$(echo "$INTERVIEWER_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
echo "Interviewer ID: $INTERVIEWER_ID"
echo ""

echo "[4/8] Running AI Screening (this may take 30-60 seconds)..."
SCREENING_RESPONSE=$(curl -s -X POST "$BASE_URL/api/ai-screening/screen/$CANDIDATE_ID?jobDescription=Java Developer with Spring Boot experience")
echo "$SCREENING_RESPONSE" | jq '.' 2>/dev/null || echo "$SCREENING_RESPONSE"
echo ""

echo "[5/8] Scheduling Interview..."
SCHEDULED_TIME="2024-12-20T10:00:00"
INTERVIEW_RESPONSE=$(curl -s -X POST "$BASE_URL/api/interviews" \
  -d "candidateId=$CANDIDATE_ID&interviewerId=$INTERVIEWER_ID&scheduledAt=$SCHEDULED_TIME&durationMinutes=60&type=TECHNICAL&location=Conference Room A&scheduledBy=recruiter@company.com")
echo "$INTERVIEW_RESPONSE" | jq '.' 2>/dev/null || echo "$INTERVIEW_RESPONSE"
INTERVIEW_ID=$(echo "$INTERVIEW_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
echo "Interview ID: $INTERVIEW_ID"
echo ""

echo "[6/8] Getting Candidate Details..."
curl -s -X GET "$BASE_URL/api/candidates/$CANDIDATE_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/api/candidates/$CANDIDATE_ID"
echo ""

echo "[7/8] Getting Interview Details..."
curl -s -X GET "$BASE_URL/api/interviews/$INTERVIEW_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/api/interviews/$INTERVIEW_ID"
echo ""

echo "[8/8] Getting Candidate Stage History..."
curl -s -X GET "$BASE_URL/api/history/candidates/$CANDIDATE_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/api/history/candidates/$CANDIDATE_ID"
echo ""

echo "============================================"
echo "API Testing Complete!"
echo "============================================"
echo ""
echo "To view all candidates:"
echo "curl -X GET $BASE_URL/api/candidates"
echo ""
echo "To view dashboard:"
echo "curl -X GET $BASE_URL/api/dashboard"
echo ""


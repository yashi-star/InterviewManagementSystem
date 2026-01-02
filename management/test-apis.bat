@echo off
REM ============================================
REM API Test Script for Interview Management System
REM ============================================
REM This script tests all major APIs
REM Make sure the application is running on http://localhost:8087

echo ============================================
echo Testing Interview Management System APIs
echo ============================================
echo.

set BASE_URL=http://localhost:8087
set CANDIDATE_ID=
set INTERVIEWER_ID=
set INTERVIEW_ID=

echo [1/8] Testing Dashboard API...
curl -s -X GET "%BASE_URL%/api/dashboard" -H "Content-Type: application/json" | echo.
echo.

echo [2/8] Creating Candidate...
curl -s -X POST "%BASE_URL%/api/candidates" ^
  -F "name=Test Candidate" ^
  -F "email=test.candidate@example.com" ^
  -F "phone=1234567890" > candidate_response.json
echo Candidate created. Response saved to candidate_response.json
for /f "tokens=2 delims=:,}" %%a in ('findstr /C:"\"id\"" candidate_response.json') do set CANDIDATE_ID=%%a
set CANDIDATE_ID=%CANDIDATE_ID: =%
echo Candidate ID: %CANDIDATE_ID%
echo.

echo [3/8] Creating Interviewer...
curl -s -X POST "%BASE_URL%/api/interviewers" ^
  -d "name=Test Interviewer&email=test.interviewer@company.com&department=Engineering&expertise=Java,Spring" > interviewer_response.json
echo Interviewer created. Response saved to interviewer_response.json
for /f "tokens=2 delims=:,}" %%a in ('findstr /C:"\"id\"" interviewer_response.json') do set INTERVIEWER_ID=%%a
set INTERVIEWER_ID=%INTERVIEWER_ID: =%
echo Interviewer ID: %INTERVIEWER_ID%
echo.

echo [4/8] Running AI Screening (this may take 30-60 seconds)...
curl -s -X POST "%BASE_URL%/api/ai-screening/screen/%CANDIDATE_ID%?jobDescription=Java Developer with Spring Boot experience" > screening_response.json
echo AI Screening completed. Response saved to screening_response.json
echo.

echo [5/8] Scheduling Interview...
set SCHEDULED_TIME=2024-12-20T10:00:00
curl -s -X POST "%BASE_URL%/api/interviews" ^
  -d "candidateId=%CANDIDATE_ID%&interviewerId=%INTERVIEWER_ID%&scheduledAt=%SCHEDULED_TIME%&durationMinutes=60&type=TECHNICAL&location=Conference Room A&scheduledBy=recruiter@company.com" > interview_response.json
echo Interview scheduled. Response saved to interview_response.json
for /f "tokens=2 delims=:,}" %%a in ('findstr /C:"\"id\"" interview_response.json') do set INTERVIEW_ID=%%a
set INTERVIEW_ID=%INTERVIEW_ID: =%
echo Interview ID: %INTERVIEW_ID%
echo.

echo [6/8] Getting Candidate Details...
curl -s -X GET "%BASE_URL%/api/candidates/%CANDIDATE_ID%" | echo.
echo.

echo [7/8] Getting Interview Details...
curl -s -X GET "%BASE_URL%/api/interviews/%INTERVIEW_ID%" | echo.
echo.

echo [8/8] Getting Candidate Stage History...
curl -s -X GET "%BASE_URL%/api/history/candidates/%CANDIDATE_ID%" | echo.
echo.

echo ============================================
echo API Testing Complete!
echo ============================================
echo.
echo Check the following files for detailed responses:
echo - candidate_response.json
echo - interviewer_response.json
echo - screening_response.json
echo - interview_response.json
echo.
echo To view all candidates:
echo curl -X GET "%BASE_URL%/api/candidates"
echo.
echo To view dashboard:
echo curl -X GET "%BASE_URL%/api/dashboard"
echo.
pause


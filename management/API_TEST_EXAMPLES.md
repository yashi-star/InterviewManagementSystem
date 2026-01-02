# 📡 API Test Examples

Complete API testing guide for Interview Management System.

**Base URL:** `http://localhost:8087`

---

## 🎯 Quick Test (All Features)

### Option 1: Use Test Script
```bash
# Windows
test-apis.bat

# Linux/Mac
chmod +x test-apis.sh
./test-apis.sh
```

### Option 2: Use Swagger UI
Open: **http://localhost:8087/swagger-ui.html**

---

## 📋 API Endpoints

### 1. Dashboard APIs

#### Get Dashboard Statistics
```bash
curl -X GET "http://localhost:8087/api/dashboard"
```

**Response:**
```json
{
  "totalCandidates": 10,
  "interviewsScheduledToday": 3,
  "pendingFeedbackCount": 2,
  "candidatesByStage": {
    "APPLIED": 5,
    "SCREENING": 2,
    "INTERVIEW_SCHEDULED": 2,
    "HIRED": 1
  }
}
```

---

### 2. Candidate APIs

#### Create Candidate
```bash
curl -X POST "http://localhost:8087/api/candidates" \
  -F "name=John Doe" \
  -F "email=john.doe@example.com" \
  -F "phone=1234567890" \
  -F "resume=@sample-resume.pdf"
```

#### Get All Candidates
```bash
curl -X GET "http://localhost:8087/api/candidates?page=0&size=10"
```

#### Get Candidate by ID
```bash
curl -X GET "http://localhost:8087/api/candidates/1"
```

#### Search Candidates
```bash
curl -X GET "http://localhost:8087/api/candidates/search?name=John&stage=APPLIED"
```

#### Update Candidate Stage
```bash
curl -X PUT "http://localhost:8087/api/candidates/1/stage" \
  -d "newStage=SCREENING&changedBy=recruiter@company.com&reason=Resume reviewed"
```

---

### 3. AI Screening APIs

#### Screen Single Candidate
```bash
curl -X POST "http://localhost:8087/api/ai-screening/screen/1?jobDescription=Java Developer with 5+ years experience"
```

**Note:** This takes 30-60 seconds as it calls Ollama AI.

#### Bulk Screen Candidates
```bash
curl -X POST "http://localhost:8087/api/ai-screening/bulk" \
  -H "Content-Type: application/json" \
  -d '{"candidateIds": [1, 2, 3], "jobDescription": "Full Stack Developer"}'
```

#### Get Screening by ID
```bash
curl -X GET "http://localhost:8087/api/ai-screening/1"
```

#### Get All Screenings for Candidate
```bash
curl -X GET "http://localhost:8087/api/ai-screening/candidate/1"
```

#### Get High-Score Candidates
```bash
curl -X GET "http://localhost:8087/api/ai-screening/high-score?minScore=80"
```

---

### 4. Interviewer APIs

#### Create Interviewer
```bash
curl -X POST "http://localhost:8087/api/interviewers" \
  -d "name=Jane Smith&email=jane.smith@company.com&department=Engineering&expertise=Java,Spring Boot,Microservices"
```

#### Get All Interviewers
```bash
curl -X GET "http://localhost:8087/api/interviewers"
```

#### Get Available Interviewers
```bash
curl -X GET "http://localhost:8087/api/interviewers/available?startTime=2024-12-20T10:00:00&endTime=2024-12-20T11:00:00"
```

---

### 5. Interview APIs

#### Schedule Interview
```bash
curl -X POST "http://localhost:8087/api/interviews" \
  -d "candidateId=1&interviewerId=1&scheduledAt=2024-12-20T10:00:00&durationMinutes=60&type=TECHNICAL&location=Conference Room A&scheduledBy=recruiter@company.com"
```

#### Get Interview by ID
```bash
curl -X GET "http://localhost:8087/api/interviews/1"
```

#### Get Today's Interviews
```bash
curl -X GET "http://localhost:8087/api/interviews/today"
```

#### Update Interview Status
```bash
curl -X PUT "http://localhost:8087/api/interviews/1/status" \
  -d "status=COMPLETED&changedBy=interviewer@company.com&notes=Interview completed successfully"
```

#### Reschedule Interview
```bash
curl -X PUT "http://localhost:8087/api/interviews/1/reschedule" \
  -d "newScheduledAt=2024-12-21T14:00:00&reason=Interviewer unavailable"
```

---

### 6. Feedback APIs

#### Submit Feedback
```bash
curl -X POST "http://localhost:8087/api/feedback" \
  -d "interviewId=1&interviewerId=1&technicalScore=5&communicationScore=4&problemSolvingScore=5&culturalFitScore=4&strengths=Strong technical skills&weaknesses=None&recommendation=STRONG_HIRE"
```

#### Get Feedback by ID
```bash
curl -X GET "http://localhost:8087/api/feedback/1"
```

#### Get Feedback for Interview
```bash
curl -X GET "http://localhost:8087/api/feedback/interview/1"
```

---

### 7. History/Audit APIs

#### Get Candidate Stage History
```bash
curl -X GET "http://localhost:8087/api/history/candidates/1"
```

#### Get Interview Status History
```bash
curl -X GET "http://localhost:8087/api/history/interviews/1"
```

#### Get Recent Activity
```bash
curl -X GET "http://localhost:8087/api/history/candidates/recent?days=7"
```

---

## 🧪 Complete Test Flow

### Step-by-Step Complete Journey

```bash
# 1. Create Candidate
CANDIDATE_RESPONSE=$(curl -s -X POST "http://localhost:8087/api/candidates" \
  -F "name=John Doe" \
  -F "email=john.doe@example.com" \
  -F "phone=1234567890")
CANDIDATE_ID=$(echo $CANDIDATE_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
echo "Created Candidate ID: $CANDIDATE_ID"

# 2. AI Screen Candidate
curl -X POST "http://localhost:8087/api/ai-screening/screen/$CANDIDATE_ID?jobDescription=Java Developer"

# 3. Create Interviewer
INTERVIEWER_RESPONSE=$(curl -s -X POST "http://localhost:8087/api/interviewers" \
  -d "name=Jane Smith&email=jane@company.com&department=Engineering")
INTERVIEWER_ID=$(echo $INTERVIEWER_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
echo "Created Interviewer ID: $INTERVIEWER_ID"

# 4. Schedule Interview
INTERVIEW_RESPONSE=$(curl -s -X POST "http://localhost:8087/api/interviews" \
  -d "candidateId=$CANDIDATE_ID&interviewerId=$INTERVIEWER_ID&scheduledAt=2024-12-20T10:00:00&type=TECHNICAL")
INTERVIEW_ID=$(echo $INTERVIEW_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | head -1)
echo "Scheduled Interview ID: $INTERVIEW_ID"

# 5. Complete Interview
curl -X PUT "http://localhost:8087/api/interviews/$INTERVIEW_ID/status" \
  -d "status=COMPLETED&changedBy=interviewer@company.com"

# 6. Submit Feedback
curl -X POST "http://localhost:8087/api/feedback" \
  -d "interviewId=$INTERVIEW_ID&interviewerId=$INTERVIEWER_ID&technicalScore=5&communicationScore=4&problemSolvingScore=5&recommendation=STRONG_HIRE"

# 7. Hire Candidate
curl -X PUT "http://localhost:8087/api/candidates/$CANDIDATE_ID/stage" \
  -d "newStage=HIRED&changedBy=hr@company.com&reason=Excellent interview performance"

# 8. View Dashboard
curl -X GET "http://localhost:8087/api/dashboard"
```

---

## 🔍 Using Postman

1. **Import Collection**: Create a new collection in Postman
2. **Set Base URL**: `http://localhost:8087`
3. **Add Requests**: Use the curl commands above as reference
4. **Set Variables**: 
   - `{{baseUrl}}` = `http://localhost:8087`
   - `{{candidateId}}` = (from create candidate response)
   - `{{interviewerId}}` = (from create interviewer response)
   - `{{interviewId}}` = (from schedule interview response)

---

## 📊 Expected Responses

### Success Response
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "currentStage": "APPLIED"
}
```

### Error Response
```json
{
  "timestamp": "2024-12-19T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Candidate not found with id: 999",
  "path": "/api/candidates/999"
}
```

---

## 🐛 Troubleshooting

### Connection Refused
- Make sure application is running: `mvn spring-boot:run`
- Check port: Application runs on `8087` by default

### 404 Not Found
- Check endpoint URL is correct
- Verify resource ID exists

### 500 Internal Server Error
- Check application logs: `logs/application.log`
- Verify Ollama is running: `docker-compose ps ollama`
- Check database connection: `docker-compose ps postgres`

---

## 📝 Notes

- **AI Screening**: Takes 30-60 seconds (calls Ollama AI)
- **Date Format**: Use ISO 8601: `2024-12-20T10:00:00`
- **File Upload**: Use `-F` flag for multipart/form-data
- **JSON**: Use `-H "Content-Type: application/json"` and `-d` with JSON


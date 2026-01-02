# 🚀 How to Run Interview Management System

## Prerequisites

- **Java 21** (JDK 21)
- **Maven 3.8+**
- **Docker & Docker Compose** (for database and Ollama AI)
- **PostgreSQL** (optional, if not using Docker)

---

## 📋 Quick Start (3 Steps)

### Step 1: Start Infrastructure (Database + AI)

```bash
# Navigate to project directory
cd management

# Start PostgreSQL and Ollama AI services
docker-compose up -d postgres ollama

# Wait for services to be ready (about 30-60 seconds)
# Check status:
docker-compose ps
```

**Note:** First time running Ollama will download the `llama2` model (~4GB), which takes 5-10 minutes.

### Step 2: Build and Run Application

```bash
# Build the project
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run

# OR run the JAR directly:
# java -jar target/management-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8087**

### Step 3: Verify It's Running

```bash
# Check health (if actuator is enabled)
curl http://localhost:8087/actuator/health

# Or open in browser:
# http://localhost:8087/swagger-ui.html
```

---

## 🧪 Running Tests

### Run All Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

### Run Specific Test Classes

```bash
# Run end-to-end integration test (tests complete flow)
mvn test -Dtest=EndToEndIntegrationTest

# Run service tests
mvn test -Dtest=*ServiceTest

# Run controller tests
mvn test -Dtest=*ControllerTest
```

### Run Tests with Detailed Output

```bash
# Verbose output
mvn test -X

# Show test output
mvn test -Dtest=EndToEndIntegrationTest -Dmaven.test.failure.ignore=true
```

---

## 🐳 Running with Docker (Full Stack)

### Start Everything (App + Database + AI)

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Check all services status
docker-compose ps
```

### Stop Everything

```bash
# Stop all services
docker-compose down

# Stop and remove all data
docker-compose down -v
```

---

## 📡 API Testing

### Option 1: Swagger UI (Recommended)

1. Start the application
2. Open browser: **http://localhost:8087/swagger-ui.html**
3. Test all APIs interactively

### Option 2: Use the Test Script

See `test-apis.sh` (Linux/Mac) or `test-apis.bat` (Windows) for automated API testing.

### Option 3: Manual cURL Commands

See `API_TEST_EXAMPLES.md` for complete API examples.

---

## 🔍 Verify Features

### 1. AI Resume Screening
```bash
# Create candidate with resume
curl -X POST "http://localhost:8087/api/candidates" \
  -F "name=John Doe" \
  -F "email=john@example.com" \
  -F "resume=@sample-resume.pdf"

# Run AI screening
curl -X POST "http://localhost:8087/api/ai-screening/screen/1?jobDescription=Java Developer"
```

### 2. Interview Scheduling
```bash
# Create interviewer
curl -X POST "http://localhost:8087/api/interviewers" \
  -d "name=Jane Smith&email=jane@company.com&department=Engineering"

# Schedule interview
curl -X POST "http://localhost:8087/api/interviews" \
  -d "candidateId=1&interviewerId=1&scheduledAt=2024-12-20T10:00:00&type=TECHNICAL"
```

### 3. Dashboard
```bash
# Get dashboard statistics
curl http://localhost:8087/api/dashboard
```

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Change port in application.properties:
server.port=8088
```

### Database Connection Failed
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check connection
docker exec -it ims_postgres psql -U postgres -d interview_db
```

### Ollama Not Responding
```bash
# Check Ollama logs
docker-compose logs ollama

# Restart Ollama
docker-compose restart ollama

# Verify model is downloaded
docker exec -it ims_ollama ollama list
```

### Application Won't Start
```bash
# Clean and rebuild
mvn clean install

# Check Java version
java -version  # Should be 21

# Check Maven version
mvn -version
```

---

## 📊 Monitoring

### View Application Logs
```bash
# If running with Maven
tail -f logs/application.log

# If running with Docker
docker-compose logs -f app
```

### Database Access
```bash
# Connect to PostgreSQL
docker exec -it ims_postgres psql -U postgres -d interview_db

# Useful SQL queries:
# \dt                    # List tables
# SELECT * FROM candidates;
# SELECT * FROM interviews;
```

---

## 🎯 Next Steps

1. **Test All Features**: Run `mvn test` to verify everything works
2. **Explore APIs**: Use Swagger UI at http://localhost:8087/swagger-ui.html
3. **Check Logs**: Monitor `logs/application.log` for detailed execution
4. **Review Dashboard**: Access http://localhost:8087/api/dashboard

---

## 📝 Notes

- **First Run**: Ollama model download takes 5-10 minutes
- **Database**: Tables are auto-created on first run (Hibernate DDL)
- **Resume Storage**: Resumes are stored in `./resumes` directory
- **Logs**: Application logs are in `logs/application.log`


# Interview Management System

AI-powered interview management system with complete candidate journey from application to hiring.

## 🚀 Quick Start

See [QUICK_START.md](QUICK_START.md) for 3-command setup.

## 📚 Documentation

- **[RUN_GUIDE.md](RUN_GUIDE.md)** - Complete setup and running instructions
- **[API_TEST_EXAMPLES.md](API_TEST_EXAMPLES.md)** - API testing examples
- **[QUICK_START.md](QUICK_START.md)** - Quick start guide

## ✨ Features

- ✅ **AI-Powered Resume Screening** - Uses Ollama AI to analyze resumes
- ✅ **Interview Scheduling** - With conflict detection
- ✅ **Candidate Pipeline Management** - Track stages with audit trail
- ✅ **Feedback Collection** - Rating system (1-5) for technical, communication skills
- ✅ **Recruiter Dashboard** - Statistics and analytics

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.5.9, Java 21
- **Database**: PostgreSQL
- **AI/ML**: Spring AI + Ollama
- **ORM**: JPA/Hibernate
- **Security**: Spring Security + JWT
- **Documentation**: SpringDoc (Swagger)
- **Testing**: JUnit 5 + Mockito

## 📋 Prerequisites

- Java 21
- Maven 3.8+
- Docker & Docker Compose

## 🏃 Running the Application

### Option 1: Docker (Recommended)
```bash
docker-compose up -d
```

### Option 2: Local Development
```bash
# Start infrastructure
docker-compose up -d postgres ollama

# Run application
mvn spring-boot:run
```

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run End-to-End Test
```bash
mvn test -Dtest=EndToEndIntegrationTest
```

### Test APIs
```bash
# Windows
test-apis.bat

# Linux/Mac
./test-apis.sh
```

## 📡 API Documentation

Once running, access Swagger UI at:
**http://localhost:8087/swagger-ui.html**

## 🐳 Docker Services

- **PostgreSQL**: Port 5432
- **Ollama AI**: Port 11434
- **Application**: Port 8087
- **pgAdmin** (optional): Port 5050

## 📝 Notes

- First Ollama run downloads ~4GB model (5-10 minutes)
- Database tables auto-created on first run
- Resumes stored in `./resumes` directory
- Logs in `logs/application.log`


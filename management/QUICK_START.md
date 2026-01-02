# ⚡ Quick Start Guide

## 🚀 Run in 3 Commands

```bash
# 1. Start Database & AI (Docker)
docker-compose up -d postgres ollama

# 2. Build & Run Application
mvn clean install -DskipTests && mvn spring-boot:run

# 3. Test APIs (in new terminal)
# Windows:
test-apis.bat

# Linux/Mac:
chmod +x test-apis.sh && ./test-apis.sh
```

## ✅ Verify It's Working

1. **Check Application**: http://localhost:8087/swagger-ui.html
2. **Check Dashboard**: http://localhost:8087/api/dashboard
3. **Run Tests**: `mvn test -Dtest=EndToEndIntegrationTest`

## 📚 Full Documentation

- **Complete Guide**: See `RUN_GUIDE.md`
- **API Examples**: See `API_TEST_EXAMPLES.md`
- **Docker Setup**: See `docker-compose.yml`

## 🎯 What Gets Tested

The `EndToEndIntegrationTest` covers:
- ✅ Candidate creation with resume
- ✅ AI-powered resume screening
- ✅ Interviewer management
- ✅ Interview scheduling with conflict detection
- ✅ Interview completion
- ✅ Feedback submission
- ✅ Candidate stage transitions
- ✅ Audit trail/history tracking
- ✅ Dashboard statistics

## 🐛 Troubleshooting

**Port 8087 in use?**
```bash
# Change in application.properties:
server.port=8088
```

**Ollama not responding?**
```bash
# Check Ollama is running
docker-compose ps ollama

# Restart if needed
docker-compose restart ollama
```

**Database connection failed?**
```bash
# Check PostgreSQL
docker-compose ps postgres

# View logs
docker-compose logs postgres
```


# 🔧 Troubleshooting Guide

## Docker Desktop Not Running

### Error:
```
unable to get image 'postgres:15-alpine': error during connect: 
Get "http://%2F%2F.%2Fpipe%2FdockerDesktopLinuxEngine/v1.51/images/postgres:15-alpine/json": 
open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified.
```

### Solution 1: Start Docker Desktop (Recommended)

1. **Open Docker Desktop** from Start Menu
2. **Wait for it to start** (whale icon in system tray should be steady)
3. **Then run:**
   ```bash
   cd management
   docker-compose up -d postgres ollama
   ```

### Solution 2: Run Without Docker (Local PostgreSQL)

If you don't want to use Docker, you can install PostgreSQL locally:

1. **Install PostgreSQL** from: https://www.postgresql.org/download/windows/
2. **Create database:**
   ```sql
   CREATE DATABASE interview_db;
   ```
3. **Update `application.properties`:**
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/interview_db
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```
4. **Skip Ollama** (AI features won't work, but rest will)
5. **Run backend:**
   ```bash
   cd management
   mvn spring-boot:run
   ```

### Solution 3: Use H2 In-Memory Database (For Testing Only)

1. **Add H2 dependency** to `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

2. **Update `application.properties`:**
   ```properties
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.driver-class-name=org.h2.Driver
   spring.h2.console.enabled=true
   ```

3. **Note:** Data will be lost when app stops (in-memory database)

---

## Other Common Issues

### Port Already in Use

**Backend (8087):**
```properties
# Change in: management/src/main/resources/application.properties
server.port=8088
```

**Frontend (3000):**
- React automatically uses next available port (3001, 3002, etc.)

### Java Version Error

**Check Java version:**
```bash
java -version
```

**Should be Java 21.** If not:
- Download from: https://www.oracle.com/java/technologies/downloads/#java21
- Or use: https://adoptium.net/

### Maven Not Found

**Install Maven:**
- Download from: https://maven.apache.org/download.cgi
- Or use Maven wrapper: `./mvnw` (included in project)

### Node.js Not Found

**Install Node.js:**
- Download from: https://nodejs.org/
- Should be Node 16+ and npm

---

## Quick Fixes

### Clean and Rebuild Backend
```bash
cd management
mvn clean install
mvn spring-boot:run
```

### Clean and Rebuild Frontend
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm start
```

### Reset Docker
```bash
cd management
docker-compose down -v
docker-compose up -d postgres ollama
```

---

## Check Services Status

### Docker Services
```bash
docker-compose ps
```

### Backend Health
```bash
curl http://localhost:8087/api/dashboard
```

### Frontend
- Open: http://localhost:3000

---

## Still Having Issues?

1. **Check logs:**
   - Backend: `management/logs/application.log`
   - Docker: `docker-compose logs`

2. **Verify installations:**
   - Java: `java -version`
   - Maven: `mvn -version`
   - Node: `node -v` and `npm -v`
   - Docker: `docker --version`

3. **Check ports:**
   - Backend: 8087
   - Frontend: 3000
   - PostgreSQL: 5432
   - Ollama: 11434


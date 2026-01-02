# 🚀 Run Without Docker

## Option 1: Use Local PostgreSQL

### Step 1: Install PostgreSQL
1. Download from: https://www.postgresql.org/download/windows/
2. Install with default settings
3. Remember your password (default username: `postgres`)

### Step 2: Create Database
```sql
-- Open pgAdmin or psql
CREATE DATABASE interview_db;
```

### Step 3: Update Backend Configuration

Edit: `management/src/main/resources/application.properties`

```properties
# Change these lines:
spring.datasource.url=jdbc:postgresql://localhost:5432/interview_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD
```

### Step 4: Disable Ollama (Optional)
Comment out Ollama configuration or skip AI features.

### Step 5: Run Backend
```bash
cd management
mvn spring-boot:run
```

### Step 6: Run Frontend
```bash
cd frontend
npm start
```

---

## Option 2: Use H2 In-Memory Database (Testing Only)

### Step 1: Add H2 Dependency

Edit: `management/pom.xml`

Add this dependency (if not already there):
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Step 2: Update Configuration

Edit: `management/src/main/resources/application.properties`

```properties
# Replace PostgreSQL config with:
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create-drop
```

### Step 3: Run Backend
```bash
cd management
mvn spring-boot:run
```

### Step 4: Run Frontend
```bash
cd frontend
npm start
```

**Note:** H2 is in-memory - data is lost when app stops!

---

## Quick Commands (Without Docker)

### Backend Only
```bash
cd management
mvn spring-boot:run
```

### Frontend Only
```bash
cd frontend
npm start
```

---

## What Works Without Docker

✅ **All Features Work:**
- Candidate management
- Interview scheduling
- Feedback collection
- Dashboard
- Stage tracking

❌ **AI Features Won't Work:**
- Resume screening (needs Ollama)
- AI analysis (needs Ollama)

---

## Recommended: Start Docker Desktop

For full functionality including AI features:
1. Install Docker Desktop: https://www.docker.com/products/docker-desktop
2. Start Docker Desktop
3. Wait for it to fully start
4. Then run: `docker-compose up -d postgres ollama`


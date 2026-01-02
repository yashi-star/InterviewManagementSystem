# ⚡ Quick Fix: Docker Desktop Not Running

## Immediate Solution

### Step 1: Start Docker Desktop
1. **Open Start Menu**
2. **Search "Docker Desktop"**
3. **Click to open**
4. **Wait for it to start** (whale icon in system tray)

### Step 2: Verify Docker is Running
```bash
docker --version
```
Should show Docker version (not an error)

### Step 3: Start Services
```bash
cd management
docker-compose up -d postgres ollama
```

### Step 4: Start Backend
```bash
mvn spring-boot:run
```

### Step 5: Start Frontend (New Terminal)
```bash
cd frontend
npm start
```

---

## Alternative: Run Without Docker

If you can't use Docker right now:

### Use Local PostgreSQL:
1. Install PostgreSQL: https://www.postgresql.org/download/windows/
2. Create database: `CREATE DATABASE interview_db;`
3. Update `application.properties` with your PostgreSQL password
4. Run: `mvn spring-boot:run`

**Note:** AI features (resume screening) won't work without Ollama, but everything else will!

---

## Check Docker Status

```bash
# Check if Docker is running
docker ps

# If error, Docker Desktop is not running
# Start Docker Desktop first!
```

---

## Still Stuck?

See `TROUBLESHOOTING.md` for detailed solutions.


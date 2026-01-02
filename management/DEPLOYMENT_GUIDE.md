# 🚀 Complete Deployment Guide for Freshers

## 📋 Table of Contents
1. [Overview](#overview)
2. [Frontend Deployment (Vercel)](#frontend-deployment-vercel)
3. [Backend Deployment (Railway)](#backend-deployment-railway)
4. [Database Setup](#database-setup)
5. [Environment Variables](#environment-variables)
6. [Connecting Frontend to Backend](#connecting-frontend-to-backend)
7. [Testing Production](#testing-production)
8. [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

### What We'll Deploy:
- **Frontend**: React app → Vercel (FREE)
- **Backend**: Spring Boot → Railway (FREE tier)
- **Database**: PostgreSQL → Railway (included)

### Why These Services:
- ✅ **FREE** for small projects
- ✅ **Easy to use** (no server management)
- ✅ **Automatic deployments** from GitHub
- ✅ **Good for freshers** (no complex setup)

---

## 🌐 Frontend Deployment (Vercel)

### Step 1: Prepare Frontend for Production

#### 1.1 Update API URL

**File: `frontend/src/components/Dashboard.js`**

**Before:**
```javascript
const API_BASE_URL = 'http://localhost:8087/api';
```

**After (we'll update after backend is deployed):**
```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8087/api';
```

#### 1.2 Create Environment File

**Create: `frontend/.env.production`**
```
REACT_APP_API_URL=https://your-backend.railway.app/api
```

**Note:** We'll update this URL after backend is deployed.

#### 1.3 Build Frontend

```bash
cd frontend
npm run build
```

**What this does:**
- Creates optimized production build
- Minifies code
- Creates `build/` folder
- Ready to deploy

**Verify:**
- Check `build/` folder exists
- Contains `index.html` and `static/` folder

---

### Step 2: Deploy to Vercel

#### Option A: Via Vercel Website (Easiest)

1. **Go to**: https://vercel.com
2. **Sign up** with GitHub account
3. **Click**: "New Project"
4. **Import** your GitHub repository
5. **Configure**:
   - **Framework Preset**: Create React App
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `build`
6. **Click**: "Deploy"

**Result:** Your frontend is live! (e.g., `your-project.vercel.app`)

#### Option B: Via Vercel CLI

```bash
# Install Vercel CLI
npm install -g vercel

# Login
vercel login

# Deploy
cd frontend
vercel

# Follow prompts
```

---

### Step 3: Update Environment Variables in Vercel

1. Go to your project on Vercel
2. Click **Settings** → **Environment Variables**
3. Add:
   - **Name**: `REACT_APP_API_URL`
   - **Value**: `https://your-backend.railway.app/api` (update after backend deploy)
4. Click **Save**
5. **Redeploy** (Vercel will auto-redeploy)

---

## 🚂 Backend Deployment (Railway)

### Step 1: Prepare Backend for Production

#### 1.1 Create Procfile

**Create: `management/Procfile`**
```
web: java -jar target/management-0.0.1-SNAPSHOT.jar
```

**What this does:**
- Tells Railway how to run your app
- `web:` means it's a web service
- Command runs the JAR file

#### 1.2 Update Application Properties

**File: `management/src/main/resources/application.properties`**

**Add/Update:**
```properties
# Use environment variables (Railway will provide)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Server port (Railway provides PORT env var)
server.port=${PORT:8087}

# CORS - Update with your frontend URL
# We'll handle this in code
```

#### 1.3 Update CORS in Code

**File: `management/src/main/java/.../config/SecurityConfig.java`** (if exists)

Or update each controller:
```java
@CrossOrigin(origins = {"https://your-frontend.vercel.app", "http://localhost:3000"})
```

---

### Step 2: Deploy to Railway

#### 2.1 Create Railway Account

1. **Go to**: https://railway.app
2. **Sign up** with GitHub
3. **Click**: "New Project"

#### 2.2 Deploy Backend

1. **Click**: "Deploy from GitHub repo"
2. **Select** your repository
3. **Select** root directory: `management`
4. Railway auto-detects Spring Boot

#### 2.3 Add PostgreSQL Database

1. **Click**: "+ New" → "Database" → "PostgreSQL"
2. Railway creates database automatically
3. **Note the connection details**

#### 2.4 Configure Environment Variables

**In Railway project settings:**

1. **Click**: "Variables" tab
2. **Add variables**:

| Variable | Value | Source |
|----------|-------|--------|
| `DATABASE_URL` | (auto-provided) | Railway PostgreSQL |
| `DB_USERNAME` | (auto-provided) | Railway PostgreSQL |
| `DB_PASSWORD` | (auto-provided) | Railway PostgreSQL |
| `PORT` | (auto-provided) | Railway |
| `SPRING_AI_OLLAMA_BASE_URL` | (optional) | Your Ollama URL |

**Note:** Railway provides `DATABASE_URL` automatically when you add PostgreSQL.

#### 2.5 Deploy

1. Railway automatically builds and deploys
2. **Wait** for deployment (5-10 minutes first time)
3. **Get your URL**: `https://your-app.railway.app`

---

### Step 3: Update Frontend API URL

#### 3.1 Get Backend URL from Railway

- Go to Railway project
- Click on your service
- Copy the URL (e.g., `https://your-app.railway.app`)

#### 3.2 Update Frontend

**Option A: Update in Vercel Environment Variables**

1. Go to Vercel project
2. Settings → Environment Variables
3. Update `REACT_APP_API_URL`:
   - Value: `https://your-app.railway.app/api`
4. Redeploy

**Option B: Update Code**

**File: `frontend/src/components/Dashboard.js`** (and other components):

```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'https://your-app.railway.app/api';
```

Then commit and push (Vercel auto-deploys).

---

## 🗄️ Database Setup

### Option 1: Railway PostgreSQL (Recommended)

**Already done** when you added PostgreSQL in Railway!

**Connection:**
- Railway provides `DATABASE_URL` automatically
- Backend connects automatically
- No manual setup needed

### Option 2: External Database (Supabase - FREE)

#### Step 1: Create Supabase Project

1. **Go to**: https://supabase.com
2. **Sign up** (FREE)
3. **Create** new project
4. **Wait** for setup (2-3 minutes)

#### Step 2: Get Connection String

1. Go to **Settings** → **Database**
2. Copy **Connection string**
3. Format: `postgresql://user:password@host:port/database`

#### Step 3: Update Railway Environment Variables

1. In Railway, add:
   - `DATABASE_URL`: Your Supabase connection string
   - `DB_USERNAME`: From Supabase
   - `DB_PASSWORD`: From Supabase

#### Step 4: Run Migrations

**Option A: Let Hibernate create tables (automatic)**
- Set: `spring.jpa.hibernate.ddl-auto=update`
- Tables created automatically on first run

**Option B: Manual SQL**
- Connect to Supabase SQL editor
- Run table creation scripts

---

## 🔐 Environment Variables Summary

### Frontend (Vercel)

| Variable | Value | Purpose |
|----------|-------|---------|
| `REACT_APP_API_URL` | `https://your-backend.railway.app/api` | Backend API URL |

### Backend (Railway)

| Variable | Value | Purpose |
|----------|-------|---------|
| `DATABASE_URL` | (auto) | PostgreSQL connection |
| `DB_USERNAME` | (auto) | Database username |
| `DB_PASSWORD` | (auto) | Database password |
| `PORT` | (auto) | Server port |
| `SPRING_AI_OLLAMA_BASE_URL` | (optional) | Ollama URL |

---

## 🔗 Connecting Frontend to Backend

### Step 1: Update All Components

**Files to update:**
- `frontend/src/components/Dashboard.js`
- `frontend/src/components/Candidates.js`
- `frontend/src/components/Interviews.js`

**Change:**
```javascript
// OLD
const API_BASE_URL = 'http://localhost:8087/api';

// NEW
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8087/api';
```

**Why:**
- Uses environment variable in production
- Falls back to localhost for development

### Step 2: Set Environment Variable in Vercel

1. Vercel Dashboard → Your Project
2. Settings → Environment Variables
3. Add: `REACT_APP_API_URL` = `https://your-backend.railway.app/api`
4. Redeploy

### Step 3: Update CORS in Backend

**Update all controllers:**
```java
@CrossOrigin(origins = {
    "https://your-frontend.vercel.app",
    "http://localhost:3000"
})
```

Or create `SecurityConfig.java`:
```java
@Configuration
public class SecurityConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins(
                        "https://your-frontend.vercel.app",
                        "http://localhost:3000"
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

---

## ✅ Testing Production

### Step 1: Test Backend

```bash
# Test backend health
curl https://your-backend.railway.app/api/dashboard

# Should return JSON
```

### Step 2: Test Frontend

1. **Open**: `https://your-frontend.vercel.app`
2. **Check**: Dashboard loads
3. **Check**: Data appears (not loading forever)
4. **Check**: No CORS errors in browser console

### Step 3: Test Full Flow

1. **Frontend**: Open dashboard
2. **Should show**: Statistics from backend
3. **Navigate**: To candidates page
4. **Should show**: Candidates list
5. **Check browser console**: No errors

---

## 🐛 Troubleshooting

### Frontend Shows "Loading..." Forever

**Problem:** Can't connect to backend

**Solutions:**
1. Check backend URL is correct in Vercel env vars
2. Check backend is running (Railway dashboard)
3. Check CORS is configured correctly
4. Check browser console for errors

### CORS Error

**Error:** `Access to fetch blocked by CORS policy`

**Solution:**
1. Update backend CORS to include frontend URL
2. Redeploy backend
3. Clear browser cache

### Database Connection Failed

**Error:** `Connection refused` or database errors

**Solutions:**
1. Check `DATABASE_URL` in Railway
2. Verify PostgreSQL is running
3. Check database credentials
4. Review Railway logs

### Build Fails

**Frontend Build Fails:**
- Check `package.json` has all dependencies
- Check for syntax errors
- Review Vercel build logs

**Backend Build Fails:**
- Check Java version (should be 21)
- Check Maven can build locally
- Review Railway build logs

---

## 📝 Deployment Checklist

### Before Deployment:
- [ ] Frontend builds locally (`npm run build`)
- [ ] Backend builds locally (`mvn clean install`)
- [ ] All tests pass
- [ ] Code committed to GitHub

### Frontend Deployment:
- [ ] Vercel account created
- [ ] Repository connected
- [ ] Build settings configured
- [ ] Environment variables set
- [ ] Deployed successfully
- [ ] Frontend URL works

### Backend Deployment:
- [ ] Railway account created
- [ ] Repository connected
- [ ] PostgreSQL database added
- [ ] Environment variables set
- [ ] Procfile created
- [ ] Deployed successfully
- [ ] Backend URL works

### After Deployment:
- [ ] Frontend API URL updated
- [ ] CORS configured
- [ ] Database tables created
- [ ] Tested end-to-end
- [ ] Shared URL with others

---

## 🎉 You're Live!

### Your URLs:
- **Frontend**: `https://your-project.vercel.app`
- **Backend**: `https://your-app.railway.app`
- **API Docs**: `https://your-app.railway.app/swagger-ui.html`

### Share with Others:
Just share your frontend URL! Everything is connected.

---

## 💡 Tips for Freshers

1. **Start with Frontend**: Deploy frontend first (easier)
2. **Then Backend**: Deploy backend and get URL
3. **Update Frontend**: Add backend URL to frontend
4. **Test Everything**: Make sure it all works
5. **Share URL**: Give frontend URL to others

### Common Mistakes to Avoid:
- ❌ Forgetting to update API URL in frontend
- ❌ Not setting CORS correctly
- ❌ Using localhost URLs in production
- ❌ Not setting environment variables
- ❌ Forgetting to commit code to GitHub

---

**You now have a live, production-ready application! 🚀**


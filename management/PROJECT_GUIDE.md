# 📚 Complete Project Guide - Interview Management System

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Layered Architecture](#layered-architecture)
4. [Database Design](#database-design)
5. [React Topics Used](#react-topics-used)
6. [Frontend Flow & Architecture](#frontend-flow--architecture)
7. [Frontend Creation Step-by-Step](#frontend-creation-step-by-step)
8. [Frontend-Backend Integration](#frontend-backend-integration)
9. [Deployment Guide](#deployment-guide)
10. [Interview Preparation](#interview-preparation)

---

## 🎯 Project Overview

### What is This Project?
A **complete Interview Management System (Mini ATS)** that helps recruiters manage the entire candidate journey from application to hiring.

### Key Features
- ✅ **AI-Powered Resume Screening** - Automatically analyzes resumes using Ollama AI
- ✅ **Interview Scheduling** - Schedule interviews with conflict detection
- ✅ **Candidate Pipeline** - Track candidates through different stages
- ✅ **Feedback Collection** - Collect and store interview feedback
- ✅ **Recruiter Dashboard** - View statistics and analytics

### Project Structure
```
management/
├── management/          # Backend (Spring Boot)
│   ├── src/
│   │   ├── main/java/   # Java source code
│   │   └── resources/   # Configuration files
│   └── pom.xml          # Maven dependencies
├── frontend/            # Frontend (React)
│   ├── src/
│   │   ├── components/  # React components
│   │   └── App.js       # Main app
│   └── package.json     # npm dependencies
└── docker-compose.yml   # Docker services
```

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 3.5.9 | Framework |
| **Spring Data JPA** | - | Database ORM |
| **PostgreSQL** | 15 | Database |
| **Spring AI** | 1.0.0-M4 | AI integration |
| **Ollama** | Latest | Local AI model |
| **Spring Security** | - | Authentication |
| **JWT** | 0.12.3 | Token-based auth |
| **Maven** | - | Build tool |
| **Docker** | - | Containerization |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 19.2.3 | UI library |
| **JavaScript** | ES6+ | Programming language |
| **Tailwind CSS** | 4.1.18 | Styling framework |
| **Axios** | 1.13.2 | HTTP client |
| **React Router** | 7.11.0 | Navigation |
| **Lucide React** | 0.562.0 | Icons |
| **Create React App** | 5.0.1 | Project setup |

---

## 🏗️ Layered Architecture

### Backend Architecture (Spring Boot)

```
┌─────────────────────────────────────────┐
│         CONTROLLER LAYER                │
│  (REST API Endpoints - @RestController) │
│  - DashboardController                  │
│  - CandidateController                  │
│  - InterviewController                  │
│  - AIScreeningController                 │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│          SERVICE LAYER                  │
│  (Business Logic - @Service)            │
│  - DashboardService                     │
│  - CandidateService                     │
│  - InterviewService                     │
│  - AIScreeningService                    │
│  - AIResumeAnalyzerService               │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│        REPOSITORY LAYER                  │
│  (Data Access - @Repository)            │
│  - CandidateRepository                   │
│  - InterviewRepository                   │
│  - AIScreeningRepository                 │
│  - FeedbackRepository                    │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         DATABASE LAYER                  │
│  (PostgreSQL Database)                   │
│  - Tables: candidates, interviews, etc.  │
└─────────────────────────────────────────┘
```

### Layer Responsibilities

#### 1. Controller Layer
- **Purpose**: Handle HTTP requests/responses
- **Annotations**: `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`
- **Responsibilities**:
  - Receive HTTP requests
  - Validate input
  - Call service layer
  - Return JSON responses

**Example:**
```java
@RestController
@RequestMapping("/api/candidates")
public class CandidateController {
    @GetMapping
    public ResponseEntity<List<Candidate>> getAllCandidates() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }
}
```

#### 2. Service Layer
- **Purpose**: Business logic
- **Annotations**: `@Service`, `@Transactional`
- **Responsibilities**:
  - Implement business rules
  - Call repository layer
  - Handle exceptions
  - Transform data

**Example:**
```java
@Service
public class CandidateService {
    public Candidate createCandidate(Candidate candidate) {
        // Business logic here
        return candidateRepository.save(candidate);
    }
}
```

#### 3. Repository Layer
- **Purpose**: Database operations
- **Annotations**: `@Repository`
- **Responsibilities**:
  - CRUD operations
  - Custom queries
  - Data persistence

**Example:**
```java
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByCurrentStage(CandidateStage stage);
}
```

#### 4. Entity Layer
- **Purpose**: Database table mapping
- **Annotations**: `@Entity`, `@Table`, `@Column`
- **Responsibilities**:
  - Define database schema
  - Map Java objects to tables
  - Define relationships

**Example:**
```java
@Entity
@Table(name = "candidates")
public class Candidate {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(nullable = false)
    private String name;
}
```

---

## 🗄️ Database Design

### Entity Relationship Diagram (ERD)

```
┌──────────────┐         ┌──────────────┐
│  Candidate   │────────▶│ AIScreening  │
│              │ 1:N     │              │
└──────────────┘         └──────────────┘
       │
       │ 1:N
       │
       ▼
┌──────────────┐         ┌──────────────┐
│  Interview   │────────▶│  Feedback   │
│              │ 1:N     │              │
└──────────────┘         └──────────────┘
       │
       │ N:1
       │
       ▼
┌──────────────┐
│ Interviewer  │
└──────────────┘
```

### Database Tables

#### 1. `candidates` Table
```sql
CREATE TABLE candidates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    resume_path VARCHAR(255),
    current_stage VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Fields:**
- `id`: Primary key
- `name`: Candidate name
- `email`: Unique email
- `phone`: Contact number
- `resume_path`: Path to uploaded resume
- `current_stage`: Current pipeline stage (APPLIED, SCREENING, etc.)
- `created_at`, `updated_at`: Audit timestamps

#### 2. `interviews` Table
```sql
CREATE TABLE interviews (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT REFERENCES candidates(id),
    interviewer_id BIGINT REFERENCES interviewers(id),
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INTEGER DEFAULT 60,
    current_status VARCHAR(50) NOT NULL,
    interview_type VARCHAR(50) NOT NULL,
    location VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Fields:**
- `id`: Primary key
- `candidate_id`: Foreign key to candidates
- `interviewer_id`: Foreign key to interviewers
- `scheduled_at`: Interview date/time
- `duration_minutes`: Interview duration
- `current_status`: Status (SCHEDULED, COMPLETED, etc.)
- `interview_type`: Type (TECHNICAL, HR, etc.)

#### 3. `ai_screenings` Table
```sql
CREATE TABLE ai_screenings (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT REFERENCES candidates(id),
    match_score INTEGER,
    skills_matched TEXT,
    experience_years DOUBLE PRECISION,
    education_level VARCHAR(255),
    cultural_fit TEXT,
    analysis_text TEXT,
    recommendation VARCHAR(50),
    model_used VARCHAR(100),
    processing_time_ms BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Fields:**
- Stores AI analysis results
- Links to candidate
- Contains match score, skills, etc.

#### 4. `feedbacks` Table
```sql
CREATE TABLE feedbacks (
    id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT REFERENCES interviews(id),
    interviewer_id BIGINT REFERENCES interviewers(id),
    technical_score INTEGER NOT NULL,
    communication_score INTEGER NOT NULL,
    problem_solving_score INTEGER NOT NULL,
    cultural_fit_score INTEGER,
    strengths TEXT,
    weaknesses TEXT,
    additional_comments TEXT,
    recommendation VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 5. `candidate_stage_history` Table
```sql
CREATE TABLE candidate_stage_history (
    id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT REFERENCES candidates(id),
    from_stage VARCHAR(50) NOT NULL,
    to_stage VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason TEXT
);
```

**Purpose**: Audit trail of stage changes

#### 6. `interview_status_history` Table
```sql
CREATE TABLE interview_status_history (
    id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT REFERENCES interviews(id),
    status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);
```

**Purpose**: Audit trail of status changes

### Relationships

1. **Candidate → AIScreening**: One-to-Many (one candidate can have multiple screenings)
2. **Candidate → Interview**: One-to-Many (one candidate can have multiple interviews)
3. **Interview → Feedback**: One-to-Many (one interview can have multiple feedbacks)
4. **Interviewer → Interview**: One-to-Many (one interviewer can conduct multiple interviews)

---

## ⚛️ React Topics Used

### 1. **React Hooks** (Most Important!)

#### useState Hook
**What it does**: Stores data that can change (state)

**Example in our code:**
```javascript
// In Dashboard.js
const [dashboardData, setDashboardData] = useState({
    totalCandidates: 0,
    interviewsScheduledToday: 0
});

// Update state
setDashboardData(newData);
```

**Interview Question**: "What is useState and when do you use it?"
**Answer**: useState is a hook that lets you add state to functional components. We use it to store data that changes, like API responses, form inputs, etc.

#### useEffect Hook
**What it does**: Runs code when component loads or when dependencies change

**Example in our code:**
```javascript
// In Dashboard.js
useEffect(() => {
    fetchDashboardData(); // Runs when component loads
}, []); // Empty array = run only once
```

**Interview Question**: "What is useEffect and what does the dependency array do?"
**Answer**: useEffect runs side effects (like API calls) after render. Empty array `[]` means run once on mount. Array with values means run when those values change.

### 2. **Component Structure**

#### Functional Components
**What it is**: Modern way to write React components

**Example:**
```javascript
function Dashboard() {
    return <div>Dashboard Content</div>;
}
```

**Interview Question**: "What's the difference between functional and class components?"
**Answer**: Functional components are simpler, use hooks, and are the modern standard. Class components use `this` and lifecycle methods.

### 3. **Props (Properties)**

**What it is**: Data passed from parent to child component

**Example:**
```javascript
// Parent component
<StatCard title="Total Candidates" value={10} />

// Child component receives props
function StatCard({ title, value }) {
    return <div>{title}: {value}</div>;
}
```

**Interview Question**: "What are props in React?"
**Answer**: Props are read-only data passed from parent to child. They make components reusable.

### 4. **Event Handling**

**What it is**: Handling user interactions (clicks, input changes)

**Example:**
```javascript
// In Candidates.js
<input
    value={searchTerm}
    onChange={(e) => setSearchTerm(e.target.value)}
/>
```

**Interview Question**: "How do you handle events in React?"
**Answer**: Use event handlers like `onClick`, `onChange`. They receive an event object with target information.

### 5. **Conditional Rendering**

**What it is**: Showing different content based on conditions

**Example:**
```javascript
{loading ? (
    <p>Loading...</p>
) : (
    <div>Data loaded!</div>
)}
```

**Interview Question**: "How do you conditionally render in React?"
**Answer**: Use ternary operator `? :` or `&&` operator. We use it to show loading states, empty states, etc.

### 6. **Lists and Keys**

**What it is**: Rendering arrays of data

**Example:**
```javascript
{candidates.map((candidate) => (
    <div key={candidate.id}>
        {candidate.name}
    </div>
))}
```

**Interview Question**: "Why do you need keys in lists?"
**Answer**: Keys help React identify which items changed. They should be unique and stable.

### 7. **React Router**

**What it is**: Navigation between pages

**Example:**
```javascript
// In App.js
<Routes>
    <Route path="/" element={<Dashboard />} />
    <Route path="/candidates" element={<Candidates />} />
</Routes>
```

**Interview Question**: "How does React Router work?"
**Answer**: React Router enables client-side routing. It changes the URL without page reload and renders different components.

### 8. **API Integration (Axios)**

**What it is**: Making HTTP requests to backend

**Example:**
```javascript
const fetchCandidates = async () => {
    const response = await axios.get('http://localhost:8087/api/candidates');
    setCandidates(response.data);
};
```

**Interview Question**: "How do you fetch data in React?"
**Answer**: Use `axios` or `fetch` API. We use `useEffect` to call APIs when component loads. `async/await` handles promises.

### 9. **CSS-in-JS / Tailwind CSS**

**What it is**: Styling components

**Example:**
```javascript
<div className="bg-gradient-to-r from-purple-500 to-blue-500 text-white">
    Content
</div>
```

**Interview Question**: "How do you style React components?"
**Answer**: We use Tailwind CSS utility classes. You can also use CSS modules, styled-components, or inline styles.

### 10. **Component Lifecycle**

**What it is**: Understanding when components mount, update, unmount

**Example:**
```javascript
useEffect(() => {
    // Component mounted - fetch data
    fetchData();
    
    return () => {
        // Component unmounting - cleanup
        // Cancel API calls, remove listeners
    };
}, []);
```

**Interview Question**: "What is component lifecycle?"
**Answer**: Components have lifecycle: mount (created), update (re-rendered), unmount (destroyed). useEffect handles these.

---

## 🔄 Frontend Flow & Architecture

### How Frontend Works (Simple Explanation)

```
1. User opens browser → http://localhost:3000
   ↓
2. React app loads → index.js runs
   ↓
3. App.js renders → Shows Navbar + Routes
   ↓
4. User clicks "Dashboard" → React Router navigates
   ↓
5. Dashboard component loads → useEffect runs
   ↓
6. useEffect calls API → axios.get('/api/dashboard')
   ↓
7. Backend responds → Returns JSON data
   ↓
8. State updates → setDashboardData(response.data)
   ↓
9. Component re-renders → Shows data on screen
```

### Frontend Architecture

```
┌─────────────────────────────────────────┐
│           BROWSER                        │
│  http://localhost:3000                  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         REACT APP                       │
│  ┌──────────────────────────────────┐  │
│  │  App.js (Main Component)         │  │
│  │  - Sets up routing               │  │
│  │  - Wraps all pages              │  │
│  └──────────┬───────────────────────┘  │
│             │                            │
│  ┌──────────▼───────────────────────┐  │
│  │  Navbar.js                       │  │
│  │  - Navigation menu               │  │
│  └──────────────────────────────────┘  │
│             │                            │
│  ┌──────────▼───────────────────────┐  │
│  │  Pages (Components)              │  │
│  │  - Dashboard.js                  │  │
│  │  - Candidates.js                 │  │
│  │  - Interviews.js                 │  │
│  └──────────┬───────────────────────┘  │
└─────────────┼───────────────────────────┘
              │
              │ HTTP Requests (Axios)
              │
┌─────────────▼───────────────────────────┐
│         BACKEND API                      │
│  http://localhost:8087/api              │
│  - /dashboard                           │
│  - /candidates                          │
│  - /interviews                          │
└─────────────────────────────────────────┘
```

### Data Flow Example: Dashboard

```
1. User visits Dashboard page
   ↓
2. Dashboard.js component renders
   ↓
3. useEffect hook runs (because component mounted)
   ↓
4. fetchDashboardData() function called
   ↓
5. axios.get('http://localhost:8087/api/dashboard')
   ↓
6. HTTP GET request sent to backend
   ↓
7. Backend processes request
   - DashboardService.getDashboardStatistics()
   - Queries database
   - Returns JSON
   ↓
8. Frontend receives response
   ↓
9. setDashboardData(response.data) updates state
   ↓
10. React re-renders component with new data
   ↓
11. User sees statistics on screen
```

---

## 🛠️ Frontend Creation Step-by-Step

### Step 1: Create React App

**Command:**
```bash
npx create-react-app frontend
```

**What happened:**
- Created `frontend` folder
- Installed React, React DOM, React Scripts
- Created basic project structure
- Added `package.json` with dependencies

**Files created:**
- `package.json` - Lists all dependencies
- `src/index.js` - Entry point
- `src/App.js` - Main component
- `public/index.html` - HTML template

### Step 2: Install Tailwind CSS

**Commands:**
```bash
cd frontend
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

**What happened:**
- Installed Tailwind CSS and dependencies
- Created `tailwind.config.js` - Tailwind configuration
- Created `postcss.config.js` - PostCSS configuration

**Files created:**
- `tailwind.config.js` - Defines colors, animations, etc.
- `postcss.config.js` - Processes CSS

### Step 3: Configure Tailwind

**File: `src/index.css`**
```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

**What this does:**
- Imports Tailwind's base styles
- Imports Tailwind's component classes
- Imports Tailwind's utility classes

**File: `tailwind.config.js`**
```javascript
module.exports = {
  content: ["./src/**/*.{js,jsx}"], // Where to look for classes
  theme: {
    extend: {
      colors: { /* Custom colors */ },
      animation: { /* Custom animations */ }
    }
  }
}
```

**What this does:**
- Tells Tailwind where to find classes (so it can remove unused ones)
- Defines custom colors, animations, etc.

### Step 4: Install Additional Libraries

**Commands:**
```bash
npm install axios react-router-dom lucide-react
```

**What each does:**
- `axios` - Makes HTTP requests to backend
- `react-router-dom` - Handles navigation/routing
- `lucide-react` - Provides icons

### Step 5: Create Component Structure

**Created folder: `src/components/`**

**Why:** To organize components in one place

**Files created:**
1. `Navbar.js` - Navigation bar
2. `Dashboard.js` - Dashboard page
3. `Candidates.js` - Candidates page
4. `Interviews.js` - Interviews page

### Step 6: Set Up Routing

**File: `src/App.js`**
```javascript
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import Candidates from './components/Candidates';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/candidates" element={<Candidates />} />
      </Routes>
    </BrowserRouter>
  );
}
```

**What this does:**
- `BrowserRouter` - Enables routing
- `Routes` - Container for routes
- `Route` - Defines a path and component
- When user visits `/`, shows Dashboard
- When user visits `/candidates`, shows Candidates

### Step 7: Create Navbar Component

**File: `src/components/Navbar.js`**

**What it contains:**
```javascript
// 1. Import React and React Router
import { Link, useLocation } from 'react-router-dom';

// 2. Create component function
function Navbar() {
    // 3. Get current location (to highlight active link)
    const location = useLocation();
    
    // 4. Return JSX (what to display)
    return (
        <nav>
            <Link to="/">Dashboard</Link>
            <Link to="/candidates">Candidates</Link>
        </nav>
    );
}
```

**How it works:**
- `Link` component creates navigation links
- `useLocation` hook gets current URL
- Compares URL to highlight active link

### Step 8: Create Dashboard Component

**File: `src/components/Dashboard.js`**

**Step-by-step breakdown:**

```javascript
// 1. Import React hooks and Axios
import React, { useState, useEffect } from 'react';
import axios from 'axios';

// 2. Define API base URL
const API_BASE_URL = 'http://localhost:8087/api';

// 3. Create component
function Dashboard() {
    // 4. Create state to store data
    const [dashboardData, setDashboardData] = useState({
        totalCandidates: 0,
        interviewsScheduledToday: 0
    });
    
    // 5. Create loading state
    const [loading, setLoading] = useState(true);
    
    // 6. Fetch data when component loads
    useEffect(() => {
        fetchDashboardData();
    }, []);
    
    // 7. Function to get data from API
    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            // Make HTTP GET request
            const response = await axios.get(`${API_BASE_URL}/dashboard`);
            // Update state with response data
            setDashboardData(response.data);
        } catch (error) {
            console.error('Error:', error);
        } finally {
            setLoading(false);
        }
    };
    
    // 8. Show loading or data
    if (loading) {
        return <div>Loading...</div>;
    }
    
    // 9. Return JSX to display
    return (
        <div>
            <h1>Dashboard</h1>
            <p>Total Candidates: {dashboardData.totalCandidates}</p>
        </div>
    );
}
```

**Explanation:**
1. **useState**: Creates state variables to store data
2. **useEffect**: Runs when component loads (empty array `[]` means run once)
3. **async/await**: Handles asynchronous API calls
4. **axios.get**: Makes HTTP GET request
5. **setDashboardData**: Updates state, which triggers re-render
6. **Conditional rendering**: Shows loading or data based on state

### Step 9: Style with Tailwind CSS

**Example styling:**
```javascript
<div className="bg-gradient-to-r from-purple-500 to-blue-500 text-white p-6 rounded-xl">
    Content
</div>
```

**Class breakdown:**
- `bg-gradient-to-r` - Gradient background (left to right)
- `from-purple-500` - Start color (purple)
- `to-blue-500` - End color (blue)
- `text-white` - White text
- `p-6` - Padding (24px)
- `rounded-xl` - Rounded corners

### Step 10: Add Animations

**In `tailwind.config.js`:**
```javascript
animation: {
    'fade-in': 'fadeIn 0.5s ease-in',
    'slide-up': 'slideUp 0.5s ease-out'
}
```

**Usage:**
```javascript
<div className="animate-fade-in">
    Content fades in
</div>
```

---

## 🔗 Frontend-Backend Integration

### How They Connect

```
┌─────────────────┐         HTTP/REST         ┌─────────────────┐
│   FRONTEND      │ ────────────────────────▶ │    BACKEND      │
│   (React)       │                            │  (Spring Boot)  │
│                 │                            │                 │
│  Port: 3000     │                            │  Port: 8087     │
│  localhost:3000 │                            │  localhost:8087 │
└─────────────────┘                            └─────────────────┘
```

### API Endpoints Used

#### 1. Dashboard API
**Frontend Code:**
```javascript
// In Dashboard.js
const response = await axios.get('http://localhost:8087/api/dashboard');
```

**Backend Code:**
```java
// In DashboardController.java
@GetMapping
public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
    return ResponseEntity.ok(dashboardService.getDashboardStatistics());
}
```

**Flow:**
1. Frontend: `axios.get('/api/dashboard')`
2. HTTP GET request sent to `http://localhost:8087/api/dashboard`
3. Backend: `@GetMapping` receives request
4. Backend: Calls `dashboardService.getDashboardStatistics()`
5. Backend: Returns JSON response
6. Frontend: Receives JSON, updates state, displays data

#### 2. Candidates API
**Frontend Code:**
```javascript
// In Candidates.js
const response = await axios.get('http://localhost:8087/api/candidates?page=0&size=100');
setCandidates(response.data.content);
```

**Backend Code:**
```java
// In CandidateController.java
@GetMapping
public ResponseEntity<Page<Candidate>> getAllCandidates(Pageable pageable) {
    return ResponseEntity.ok(candidateService.getAllCandidates(pageable));
}
```

#### 3. Interviews API
**Frontend Code:**
```javascript
// In Interviews.js
const response = await axios.get('http://localhost:8087/api/interviews');
setInterviews(response.data);
```

**Backend Code:**
```java
// In InterviewController.java
@GetMapping
public ResponseEntity<List<Interview>> getAllInterviews() {
    return ResponseEntity.ok(interviewService.getAllInterviews());
}
```

### CORS Configuration

**Why needed:** Browser security prevents frontend (port 3000) from calling backend (port 8087) without CORS.

**Backend Solution:**
```java
@CrossOrigin(origins = "*")  // Allows all origins
@RestController
public class DashboardController {
    // ...
}
```

**What this does:**
- Allows frontend to make requests
- Without this, browser blocks requests

### Request/Response Flow

```
1. User clicks button in frontend
   ↓
2. Event handler calls function
   ↓
3. Function calls axios.get('/api/endpoint')
   ↓
4. Axios creates HTTP request
   ↓
5. Request sent to backend (port 8087)
   ↓
6. Backend controller receives request
   ↓
7. Controller calls service
   ↓
8. Service calls repository
   ↓
9. Repository queries database
   ↓
10. Data flows back: DB → Repository → Service → Controller
   ↓
11. Controller returns JSON response
   ↓
12. Frontend receives response
   ↓
13. useState updates state
   ↓
14. React re-renders component
   ↓
15. User sees updated data
```

---

## 🚀 Deployment Guide

### Option 1: Vercel (Frontend) + Railway/Render (Backend) - Easiest for Freshers

#### Deploy Frontend to Vercel (FREE)

**Step 1: Build Frontend**
```bash
cd frontend
npm run build
```
This creates `build/` folder with production files.

**Step 2: Deploy to Vercel**
1. Go to: https://vercel.com
2. Sign up with GitHub
3. Click "New Project"
4. Import your repository
5. Set build command: `npm run build`
6. Set output directory: `build`
7. Click "Deploy"

**Result:** Your frontend is live! (e.g., `your-project.vercel.app`)

#### Deploy Backend to Railway (FREE tier available)

**Step 1: Prepare Backend**
1. Create `Procfile` in `management/` folder:
```
web: java -jar target/management-0.0.1-SNAPSHOT.jar
```

2. Update `application.properties`:
```properties
# Use environment variables for production
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

**Step 2: Deploy to Railway**
1. Go to: https://railway.app
2. Sign up with GitHub
3. Click "New Project"
4. Select "Deploy from GitHub repo"
5. Select your repository
6. Railway auto-detects Spring Boot
7. Add PostgreSQL database (Railway provides)
8. Set environment variables:
   - `DATABASE_URL` (auto-provided)
   - `SPRING_AI_OLLAMA_BASE_URL` (if using Ollama)

**Result:** Your backend is live! (e.g., `your-app.railway.app`)

**Step 3: Update Frontend API URL**
In `frontend/src/components/Dashboard.js` (and other components):
```javascript
// Change from:
const API_BASE_URL = 'http://localhost:8087/api';

// To:
const API_BASE_URL = 'https://your-backend.railway.app/api';
```

**Step 4: Redeploy Frontend**
- Push changes to GitHub
- Vercel auto-deploys

---

### Option 2: Netlify (Frontend) + Render (Backend)

#### Deploy Frontend to Netlify

1. Build: `npm run build`
2. Go to: https://netlify.com
3. Drag & drop `build/` folder
4. Done! Get URL like `your-app.netlify.app`

#### Deploy Backend to Render

1. Go to: https://render.com
2. Create "Web Service"
3. Connect GitHub repo
4. Set:
   - Build: `mvn clean install`
   - Start: `java -jar target/management-0.0.1-SNAPSHOT.jar`
5. Add PostgreSQL database
6. Set environment variables

---

### Option 3: Full Stack on Render (Both Frontend & Backend)

**Deploy Both to Render:**

1. **Frontend:**
   - Create "Static Site"
   - Build: `npm run build`
   - Publish: `build/`

2. **Backend:**
   - Create "Web Service"
   - Build: `mvn clean install`
   - Start: `java -jar target/management-0.0.1-SNAPSHOT.jar`

---

### Environment Variables for Production

**Backend (`application.properties`):**
```properties
# Database (from hosting provider)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Ollama (if using)
spring.ai.ollama.base-url=${OLLAMA_URL}

# CORS (update with your frontend URL)
# In SecurityConfig.java, update allowed origins
```

**Frontend:**
Create `.env` file:
```
REACT_APP_API_URL=https://your-backend.railway.app/api
```

Update components:
```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8087/api';
```

---

### Database Setup for Production

**Option 1: Hosting Provider Database**
- Railway, Render provide PostgreSQL
- Just connect using provided URL

**Option 2: External Database (Supabase - FREE)**
1. Go to: https://supabase.com
2. Create project
3. Get connection string
4. Use in backend environment variables

---

### Step-by-Step Deployment (Vercel + Railway)

#### Frontend Deployment:

```bash
# 1. Build frontend
cd frontend
npm run build

# 2. Commit and push to GitHub
git add .
git commit -m "Ready for deployment"
git push

# 3. Go to Vercel.com
# 4. Import repository
# 5. Deploy (automatic)
```

#### Backend Deployment:

```bash
# 1. Create Procfile
echo "web: java -jar target/management-0.0.1-SNAPSHOT.jar" > management/Procfile

# 2. Commit and push
git add management/Procfile
git commit -m "Add Procfile for deployment"
git push

# 3. Go to Railway.app
# 4. New Project → Deploy from GitHub
# 5. Add PostgreSQL database
# 6. Deploy
```

#### Update Frontend API URL:

```bash
# 1. Get backend URL from Railway (e.g., https://app.railway.app)

# 2. Update frontend components
# Change API_BASE_URL in:
# - Dashboard.js
# - Candidates.js
# - Interviews.js

# 3. Commit and push
git add .
git commit -m "Update API URL for production"
git push
# Vercel auto-deploys
```

---

## 💼 Interview Preparation

### Common React Questions & Answers

#### Q1: "Explain useState hook"
**Answer:**
"useState is a React hook that lets you add state to functional components. It returns an array with two elements: the current state value and a function to update it. For example, in our Dashboard component, we use `useState` to store dashboard data that we fetch from the API. When the API response comes back, we call `setDashboardData` to update the state, which triggers React to re-render the component with the new data."

#### Q2: "What is useEffect and when do you use it?"
**Answer:**
"useEffect is a hook that lets you perform side effects in functional components, like fetching data, setting up subscriptions, or manually changing the DOM. We use it in our Dashboard component to fetch data when the component first loads. The empty dependency array `[]` means it runs only once when the component mounts. If we had dependencies like `[userId]`, it would run whenever `userId` changes."

#### Q3: "How does React Router work?"
**Answer:**
"React Router enables client-side routing in React applications. Instead of making server requests for each page, it changes the URL and renders different components based on the route. In our app, we use `BrowserRouter` to enable routing, `Routes` to define route containers, and `Route` components to map URLs to components. When a user clicks a link, React Router updates the URL and renders the corresponding component without a page reload."

#### Q4: "How do you fetch data from an API in React?"
**Answer:**
"We use the `axios` library to make HTTP requests. In our components, we create an async function that calls `axios.get()` with the API endpoint. We call this function inside `useEffect` so it runs when the component loads. We use `async/await` to handle the asynchronous nature of API calls. When the response comes back, we update the component's state using `useState`, which triggers a re-render with the new data."

#### Q5: "What is the difference between props and state?"
**Answer:**
"Props are data passed from a parent component to a child component - they're read-only and flow down. State is data that belongs to a component and can change over time - it's managed within the component using `useState`. In our app, we pass props to child components like `StatCard`, but we use state to store data fetched from APIs, like `dashboardData` in the Dashboard component."

#### Q6: "Explain component lifecycle"
**Answer:**
"Components have three main phases: mounting (when created), updating (when re-rendered), and unmounting (when removed). In functional components, we use `useEffect` to handle these. The effect runs after render. With an empty dependency array, it runs on mount. With dependencies, it runs when those values change. The cleanup function (return statement) runs on unmount, which is useful for canceling API calls or removing event listeners."

#### Q7: "What is JSX?"
**Answer:**
"JSX is a syntax extension for JavaScript that looks like HTML but is actually JavaScript. It lets us write HTML-like code in React. For example, `<div className="card">` is JSX. React compiles JSX into JavaScript function calls. JSX makes it easier to write and understand component structure."

#### Q8: "How do you handle forms in React?"
**Answer:**
"We use controlled components, where form inputs are controlled by React state. We set the input's `value` to a state variable and use `onChange` to update that state. For example, in our search functionality, we have `value={searchTerm}` and `onChange={(e) => setSearchTerm(e.target.value)}`. This gives React full control over the form data."

---

### Project-Specific Questions

#### Q1: "Walk me through your project architecture"
**Answer:**
"Our project follows a layered architecture. The frontend is built with React and uses component-based structure. We have a Navbar component for navigation and separate page components like Dashboard, Candidates, and Interviews. Each component uses React hooks for state management and makes API calls using Axios. The backend follows Spring Boot's layered architecture with Controllers handling HTTP requests, Services containing business logic, Repositories for data access, and Entities mapping to database tables. The frontend communicates with the backend through REST APIs."

#### Q2: "How does the AI resume screening work?"
**Answer:**
"When a candidate uploads a resume, our backend uses Apache PDFBox to extract text from the PDF. This text is then sent to Ollama AI (running locally via Docker) using Spring AI. We send a structured prompt asking the AI to analyze the resume and extract skills, experience, education, and provide a match score. The AI response is parsed and stored in our database. The frontend displays this analysis to recruiters, helping them make informed decisions."

#### Q3: "How do you handle state management?"
**Answer:**
"We use React's built-in state management with `useState` hook. Each component manages its own local state. For example, the Dashboard component has state for dashboard data and loading status. The Candidates component has state for the candidates list and search term. We don't use Redux or Context API because the state is simple and component-specific. If the app grows, we could add Context API for shared state."

#### Q4: "Explain the data flow in your application"
**Answer:**
"Data flows in a unidirectional manner. When a user interacts with the frontend, an event handler is triggered. This handler makes an API call using Axios to the backend. The backend controller receives the request, calls the service layer for business logic, which then calls the repository layer to query the database. The response flows back through the same layers, and the backend returns JSON. The frontend receives this JSON, updates the component state using `setState`, which triggers React to re-render the component with the new data."

---

## 📝 Summary

### What You Built

1. **Backend**: Spring Boot REST API with AI integration
2. **Frontend**: React SPA with Tailwind CSS
3. **Database**: PostgreSQL with proper relationships
4. **Deployment**: Ready for production deployment

### Key Technologies Learned

- **React**: Hooks, Components, Routing, API Integration
- **Spring Boot**: REST APIs, JPA, Service Layer
- **PostgreSQL**: Database design, Relationships
- **Docker**: Containerization
- **Tailwind CSS**: Utility-first CSS

### Interview Confidence

You can confidently explain:
- ✅ React hooks (useState, useEffect)
- ✅ Component structure
- ✅ API integration
- ✅ State management
- ✅ Project architecture
- ✅ Database design
- ✅ Deployment process

---

## 🎯 Next Steps

1. **Deploy to Production** - Use Vercel + Railway
2. **Add More Features** - Forms, Modals, Charts
3. **Improve UI** - More animations, better UX
4. **Add Tests** - Unit tests, integration tests
5. **Documentation** - API documentation, user guide

---

**Good luck with your interviews! 🚀**


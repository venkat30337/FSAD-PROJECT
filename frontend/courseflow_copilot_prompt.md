# GitHub Copilot Prompt — CourseFlow Full-Stack Application
## FSAD-PS48 | React + Vite · Spring Boot 3.x · MySQL

---

## MASTER CONTEXT — READ BEFORE GENERATING ANY CODE

You are building **CourseFlow**, a production-grade academic course selection and scheduling
web application. Two actor roles exist: **STUDENT** and **ADMIN**. Every feature described
below must be fully wired end-to-end — from MySQL table → Spring Boot API → React component.
No mocked data. No hardcoded responses. All state persisted in MySQL.

**Tech Stack (locked — do not substitute):**
- Frontend: React 18 + Vite 5, React Router v6, Axios, React Hook Form + Yup, Zustand
- Backend: Spring Boot 3.x, Java 17, Spring Web, Spring Data JPA, Spring Security, JWT (jjwt 0.12.x)
- Database: MySQL 8.x
- Build: Maven (backend), npm (frontend)

---

## PART 1 — MYSQL DATABASE SCHEMA

Generate complete `schema.sql` with the following tables. Use InnoDB, UTF8MB4, proper FK
constraints, and indexes on all foreign keys and frequently-queried columns.

### Tables Required

```sql
-- 1. users
CREATE TABLE users (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name    VARCHAR(120) NOT NULL,
  email        VARCHAR(150) NOT NULL UNIQUE,
  password     VARCHAR(255) NOT NULL,        -- BCrypt encoded
  role         ENUM('STUDENT','ADMIN') NOT NULL DEFAULT 'STUDENT',
  student_id   VARCHAR(30) UNIQUE,           -- NULL for ADMIN
  department   VARCHAR(100),
  phone        VARCHAR(20),
  semester     INT,                           -- current semester, NULL for ADMIN
  max_credits  INT NOT NULL DEFAULT 24,
  is_active    BOOLEAN NOT NULL DEFAULT TRUE,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. courses
CREATE TABLE courses (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  code          VARCHAR(20) NOT NULL UNIQUE,
  title         VARCHAR(200) NOT NULL,
  description   TEXT,
  credits       INT NOT NULL,
  department    VARCHAR(100) NOT NULL,
  instructor    VARCHAR(120) NOT NULL,
  status        ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  created_by    BIGINT NOT NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 3. sections (one course → many sections/timeslots)
CREATE TABLE sections (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id       BIGINT NOT NULL,
  section_code    VARCHAR(20) NOT NULL,
  room            VARCHAR(60),
  days_of_week    VARCHAR(50) NOT NULL,      -- e.g. "MON,WED,FRI"
  start_time      TIME NOT NULL,
  end_time        TIME NOT NULL,
  max_seats       INT NOT NULL DEFAULT 60,
  enrolled_count  INT NOT NULL DEFAULT 0,
  academic_year   VARCHAR(10) NOT NULL,      -- e.g. "2025-26"
  semester_term   ENUM('ODD','EVEN') NOT NULL,
  FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
  UNIQUE KEY uq_section (course_id, section_code)
);

-- 4. prerequisites
CREATE TABLE prerequisites (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id           BIGINT NOT NULL,
  required_course_id  BIGINT NOT NULL,
  FOREIGN KEY (course_id)          REFERENCES courses(id) ON DELETE CASCADE,
  FOREIGN KEY (required_course_id) REFERENCES courses(id) ON DELETE CASCADE,
  UNIQUE KEY uq_prereq (course_id, required_course_id)
);

-- 5. enrollments
CREATE TABLE enrollments (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id    BIGINT NOT NULL,
  section_id    BIGINT NOT NULL,
  status        ENUM('ENROLLED','WAITLISTED','DROPPED','COMPLETED') NOT NULL DEFAULT 'ENROLLED',
  waitlist_pos  INT,                          -- NULL if ENROLLED
  enrolled_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  dropped_at    DATETIME,
  FOREIGN KEY (student_id)  REFERENCES users(id),
  FOREIGN KEY (section_id)  REFERENCES sections(id),
  UNIQUE KEY uq_enrollment (student_id, section_id)
);

-- 6. degree_requirements (maps program → required courses)
CREATE TABLE degree_requirements (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  program     VARCHAR(100) NOT NULL,
  course_id   BIGINT NOT NULL,
  category    ENUM('CORE','ELECTIVE','LAB') NOT NULL,
  FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- 7. notifications
CREATE TABLE notifications (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     BIGINT NOT NULL,
  title       VARCHAR(200) NOT NULL,
  message     TEXT NOT NULL,
  type        ENUM('ENROLLMENT','WAITLIST','DROP','ADMIN','SYSTEM') NOT NULL,
  is_read     BOOLEAN NOT NULL DEFAULT FALSE,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 8. audit_logs (admin actions)
CREATE TABLE audit_logs (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  admin_id    BIGINT NOT NULL,
  action      VARCHAR(100) NOT NULL,
  entity_type VARCHAR(60) NOT NULL,
  entity_id   BIGINT,
  detail      TEXT,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (admin_id) REFERENCES users(id)
);
```

Seed the database with:
- 1 ADMIN user: `admin@courseflow.edu` / `Admin@123`
- 1 STUDENT user: `student@courseflow.edu` / `Student@123`
- 6 sample PUBLISHED courses across 3 departments, each with 1–2 sections
- 2 prerequisite relationships

---

## PART 2 — SPRING BOOT BACKEND

### 2.1 Project Structure

```
src/main/java/com/courseflow/
├── config/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── JwtConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── CourseController.java
│   ├── SectionController.java
│   ├── EnrollmentController.java
│   ├── NotificationController.java
│   └── AdminController.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── CourseService.java
│   ├── EnrollmentService.java
│   └── NotificationService.java
├── repository/
│   └── (interface per entity, extends JpaRepository)
├── model/
│   └── (JPA entity per table)
├── dto/
│   ├── request/
│   └── response/
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── (custom exceptions)
└── CourseflowApplication.java
```

### 2.2 application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/courseflow_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate.dialect: org.hibernate.dialect.MySQL8Dialect
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
server:
  port: 8080
jwt:
  secret: courseflow_jwt_secret_key_must_be_at_least_256_bits_long_for_hs256
  expiration: 86400000   # 24 hours
```

### 2.3 Security Configuration

- Permit without auth: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/courses/public`
- ADMIN-only: all `/api/admin/**` routes
- Authenticated (any role): everything else
- CORS: allow `http://localhost:5173`
- Stateless session, no CSRF
- Password encoding: BCryptPasswordEncoder

### 2.4 JWT Utility

Generate token with claims: `userId`, `email`, `role`. Validate on every secured request via
`JwtAuthFilter extends OncePerRequestFilter`. Extract user details from token, set
SecurityContext. Token expiry: 24 hours.

### 2.5 Complete REST API Endpoints

**Base URL: `http://localhost:8080/api`**

#### AUTH — `/api/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | Public | Register student account |
| POST | `/auth/login` | Public | Login, returns JWT + user info |
| POST | `/auth/logout` | Any | Invalidate token (client-side) |

**POST /auth/register — Request Body:**
```json
{
  "fullName": "Aryan Sharma",
  "email": "aryan@example.com",
  "password": "Aryan@123",
  "confirmPassword": "Aryan@123",
  "studentId": "CS2024001",
  "department": "Computer Science",
  "phone": "9876543210",
  "semester": 3
}
```
Validations: email unique, password min 8 chars with 1 uppercase + 1 digit + 1 special char,
confirmPassword match, studentId unique. On success return `{ message, userId }`.

**POST /auth/login — Request Body:**
```json
{ "email": "aryan@example.com", "password": "Aryan@123" }
```
On success return:
```json
{
  "token": "eyJ...",
  "tokenType": "Bearer",
  "userId": 2,
  "email": "aryan@example.com",
  "fullName": "Aryan Sharma",
  "role": "STUDENT",
  "department": "Computer Science"
}
```

#### COURSES — `/api/courses`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/courses` | STUDENT/ADMIN | All published courses, paginated + filterable |
| GET | `/courses/public` | Public | Published courses (for landing) |
| GET | `/courses/{id}` | Any | Course detail with sections + prerequisites |
| POST | `/courses` | ADMIN | Create course (DRAFT) |
| PUT | `/courses/{id}` | ADMIN | Update course |
| PATCH | `/courses/{id}/publish` | ADMIN | Publish course |
| PATCH | `/courses/{id}/archive` | ADMIN | Archive course |
| DELETE | `/courses/{id}` | ADMIN | Soft delete (set ARCHIVED) |

**GET /courses query params:** `department`, `status`, `search` (title/code),
`instructor`, `credits`, `page` (default 0), `size` (default 10), `sort`

**POST /courses — Request Body:**
```json
{
  "code": "CS401",
  "title": "Machine Learning",
  "description": "Introduction to ML algorithms...",
  "credits": 4,
  "department": "Computer Science",
  "instructor": "Dr. Mehta",
  "prerequisiteIds": [1, 2]
}
```

#### SECTIONS — `/api/sections`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/sections/course/{courseId}` | Any | Sections for a course |
| POST | `/sections` | ADMIN | Add section to course |
| PUT | `/sections/{id}` | ADMIN | Update section |
| DELETE | `/sections/{id}` | ADMIN | Delete section |
| GET | `/sections/{id}/seats` | Any | Live seat availability |

**POST /sections — Request Body:**
```json
{
  "courseId": 1,
  "sectionCode": "A",
  "room": "LH-301",
  "daysOfWeek": "MON,WED,FRI",
  "startTime": "09:00",
  "endTime": "10:00",
  "maxSeats": 60,
  "academicYear": "2025-26",
  "semesterTerm": "ODD"
}
```

#### ENROLLMENTS — `/api/enrollments`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/enrollments` | STUDENT | Enroll in section |
| DELETE | `/enrollments/{id}` | STUDENT | Drop enrollment |
| GET | `/enrollments/my` | STUDENT | My current enrollments + timetable data |
| GET | `/enrollments/my/schedule` | STUDENT | Timetable view — sections with timing |
| GET | `/enrollments/section/{sectionId}` | ADMIN | Students in a section |
| GET | `/enrollments/conflicts` | ADMIN | All students with conflicts |
| POST | `/enrollments/check-conflict` | STUDENT | Pre-check before enrolling |

**POST /enrollments — Request Body:**
```json
{ "sectionId": 3 }
```

Server-side validation order (fail fast, return specific error message per step):
1. Student not already enrolled in this section
2. Course is PUBLISHED
3. Student has completed all prerequisites (check enrollments with status COMPLETED or ENROLLED)
4. No time conflict with existing enrolled sections (query by days_of_week overlap + time overlap)
5. Credit limit not exceeded (sum enrolled credits + new course credits <= max_credits)
6. Seats available: if `enrolled_count < max_seats` → ENROLLED; else → WAITLISTED (set waitlist_pos)
7. Use `SELECT ... FOR UPDATE` on the sections row inside `@Transactional` to prevent race condition

**POST /enrollments/check-conflict — Request Body:**
```json
{ "sectionId": 3 }
```
Returns: `{ "hasConflict": true/false, "conflictingSection": {...} }`

**DELETE /enrollments/{id}:**
- Set status to DROPPED, set dropped_at
- Decrement enrolled_count on section
- If waitlisted students exist: promote position-1 waitlisted student to ENROLLED,
  increment enrolled_count, send notification

#### ADMIN — `/api/admin`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/admin/users` | ADMIN | All users paginated, filterable by role |
| GET | `/admin/users/{id}` | ADMIN | User detail + enrollments |
| PATCH | `/admin/users/{id}/toggle-active` | ADMIN | Activate/deactivate student |
| GET | `/admin/dashboard` | ADMIN | Aggregate stats |
| GET | `/admin/conflicts` | ADMIN | All conflicting enrollments |
| POST | `/admin/enroll` | ADMIN | Manually enroll a student (override) |
| DELETE | `/admin/enrollments/{id}` | ADMIN | Force drop enrollment |
| GET | `/admin/audit-logs` | ADMIN | Paginated audit trail |

**GET /admin/dashboard response:**
```json
{
  "totalStudents": 245,
  "totalCourses": 48,
  "activeEnrollments": 1230,
  "waitlistedStudents": 67,
  "publishedCourses": 42,
  "draftCourses": 6,
  "topCoursesByEnrollment": [...],
  "recentActivity": [...]
}
```

#### NOTIFICATIONS — `/api/notifications`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/notifications` | Any | My notifications (paginated) |
| PATCH | `/notifications/{id}/read` | Any | Mark single as read |
| PATCH | `/notifications/read-all` | Any | Mark all as read |
| GET | `/notifications/unread-count` | Any | Badge count |

#### DEGREE AUDIT — `/api/degree`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/degree/audit` | STUDENT | My progress against requirements |
| GET | `/degree/requirements` | STUDENT | All requirements for my department/program |

### 2.6 Global Exception Handler

Handle all of the following with consistent JSON response `{ "status", "error", "message", "timestamp" }`:
- `ResourceNotFoundException` → 404
- `ConflictException` (schedule conflict, duplicate enroll) → 409
- `PrerequisiteNotMetException` → 422
- `SeatFullException` (when trying to force non-waitlist enroll) → 409
- `CreditLimitExceededException` → 422
- `MethodArgumentNotValidException` (Bean Validation) → 400 with field-level error map
- `AccessDeniedException` → 403
- `BadCredentialsException` → 401

### 2.7 Service-Layer Business Rules

**EnrollmentService.enroll():**
```java
@Transactional
public EnrollmentResponse enroll(Long studentId, Long sectionId) {
  // 1. Load section with pessimistic write lock: @Lock(PESSIMISTIC_WRITE)
  // 2. Check duplicate enrollment
  // 3. Check prerequisites
  // 4. Check time conflicts via JPQL: select e from Enrollment e join e.section s
  //    where e.student.id = :studentId and e.status = 'ENROLLED'
  //    and s.daysOfWeek overlaps section.daysOfWeek
  //    and s.startTime < section.endTime and s.endTime > section.startTime
  // 5. Check credit limit
  // 6. If seats: set ENROLLED, increment enrolled_count
  //    Else: set WAITLISTED, set waitlist_pos = current max + 1
  // 7. Create notification
  // 8. Write audit_log entry
  // 9. Return response
}
```

---

## PART 3 — REACT + VITE FRONTEND

### 3.1 Project Setup

```
src/
├── api/            # Axios instance + service functions per domain
├── components/
│   ├── common/     # Button, Input, Modal, Table, Badge, Spinner, Navbar, Sidebar
│   ├── auth/       # LoginForm, RegisterForm
│   ├── courses/    # CourseCard, CourseList, CourseDetail, CourseForm
│   ├── schedule/   # WeeklyTimetable, ScheduleConflictModal
│   ├── enrollment/ # EnrollButton, WaitlistBadge, EnrollmentList
│   ├── admin/      # UserTable, ConflictDashboard, AuditLogTable, StatsCard
│   └── notifications/ # NotificationBell, NotificationList
├── pages/
│   ├── auth/           # LoginPage, RegisterPage
│   ├── student/        # DashboardPage, CataloguePage, SchedulePage, DegreeAuditPage
│   └── admin/          # AdminDashboardPage, UsersPage, CoursesPage,
│                        # SectionsPage, EnrollmentsPage, AuditPage
├── store/          # Zustand stores: authStore, notificationStore
├── hooks/          # useAuth, useEnrollment, useCourses, useNotifications
├── utils/          # axiosInstance, timeUtils, validators
├── router/         # AppRouter with protected routes
└── styles/         # global.css, variables.css
```

### 3.2 Axios Configuration

```js
// src/utils/axiosInstance.js
const axiosInstance = axios.create({ baseURL: 'http://localhost:8080/api' });

// Request interceptor: attach Bearer token from localStorage
axiosInstance.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Response interceptor: on 401 → clear auth state → redirect /login
axiosInstance.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);
```

### 3.3 Route Structure

```jsx
// All routes
/                    → redirect to /login if not authenticated, else /dashboard
/login               → LoginPage (public)
/register            → RegisterPage (public) → on success redirect to /login
/dashboard           → StudentDashboardPage (STUDENT only)
/catalogue           → CataloguePage (STUDENT)
/schedule            → SchedulePage (STUDENT) — visual timetable
/degree-audit        → DegreeAuditPage (STUDENT)
/admin               → AdminDashboardPage (ADMIN only)
/admin/users         → UsersPage (ADMIN)
/admin/users/:id     → UserDetailPage (ADMIN)
/admin/courses       → AdminCoursesPage (ADMIN)
/admin/courses/new   → CourseFormPage (ADMIN)
/admin/courses/:id   → CourseFormPage in edit mode (ADMIN)
/admin/enrollments   → AdminEnrollmentsPage — conflict dashboard (ADMIN)
/admin/audit         → AuditLogPage (ADMIN)
```

**Protected Route wrapper:** reads role from Zustand authStore. If role mismatch → redirect
to appropriate home. If no token → redirect to /login.

### 3.4 Authentication Pages

#### RegisterPage
Full-width centered card layout. Fields:

| Field | Type | Validation |
|-------|------|------------|
| Full Name | text | required, min 3 chars |
| Email | email | required, valid email format |
| Student ID | text | required, alphanumeric |
| Department | select | required (dropdown of 6 departments) |
| Semester | select | required, 1–8 |
| Phone | tel | required, 10-digit India format |
| Password | password | required, min 8 chars, 1 uppercase + 1 digit + 1 special char |
| Confirm Password | password | must match password |

Validation: react-hook-form + Yup schema. Show inline red error message below each invalid
field. Disable submit button while submitting. On API 409 (email/studentId exists) show
server error at top of form. On success: show success toast "Account created. Please log in."
then redirect to `/login`. Never auto-login after register.

#### LoginPage
Centered card. Fields: Email, Password. Show/hide password toggle. On submit: call
`POST /auth/login`, store `{ token, userId, email, fullName, role, department }` in
localStorage AND Zustand authStore. Redirect based on role:
- ADMIN → `/admin`
- STUDENT → `/dashboard`

Show error toast on 401 "Invalid email or password."

### 3.5 Student Pages

#### StudentDashboardPage
- Welcome banner with student name, semester, credit count used / max
- 4 stat cards: Enrolled Courses, Waitlisted, Credits Used, Notifications Unread
- "My This Week" mini timetable (3-day preview)
- "Recently Added Courses" section (latest 4 published courses)
- Quick action buttons: "Browse Catalogue", "View Full Schedule"

#### CataloguePage
- Left sidebar filters: Department (checkboxes), Credits (1-5 radio), Status (Published only for students)
- Search bar at top: searches code + title
- Course grid (3 columns desktop, 2 tablet, 1 mobile)
- Each CourseCard shows: code, title, instructor, department badge, credits badge, seat bar
  (enrolled_count / max_seats as visual progress), "Enroll" / "Waitlist" / "Enrolled" button
- Pagination at bottom
- "Enroll" click: show ConflictCheckModal (call `/enrollments/check-conflict` first), if no
  conflict show confirmation, else show conflict detail. Confirm → call POST /enrollments.
- After enroll: update card button state, show success toast.

#### SchedulePage (Timetable)
- Weekly grid: columns Mon–Sat, rows 07:00–21:00 in 1-hour slots
- Enrolled courses render as colored blocks in the correct time cells. Each block:
  course code + room. Color assigned per course (consistent, not random per render).
- Below grid: list of enrolled courses with section details + "Drop" button
- Drop: confirmation modal "Are you sure you want to drop [course name]?" → call DELETE
- Responsive: on mobile show day tabs instead of all 6 columns

#### DegreeAuditPage
- Progress ring/bar showing total required credits vs completed + enrolled
- Three sections: Core Requirements, Electives, Lab
- Each requirement shows: course code, title, status (COMPLETED / IN-PROGRESS / NOT ENROLLED)
- "Enroll now" button on NOT ENROLLED rows that link directly to that course in catalogue

### 3.6 Admin Pages

#### AdminDashboardPage
- 6 stat cards: Total Students, Active Courses, Total Enrollments, Waitlisted, Draft Courses, Audit Events Today
- Bar chart (recharts): top 5 courses by enrollment
- Recent activity feed from audit_logs
- Quick links: Manage Courses, Manage Users, View Conflicts

#### Admin Courses Page
- Full-width table: Code | Title | Dept | Credits | Instructor | Status | Sections | Enrolled | Actions
- Status badge: DRAFT (amber), PUBLISHED (green), ARCHIVED (gray)
- Actions: Edit, Publish/Archive toggle, Delete (soft)
- "Add Course" button → CourseFormPage

#### CourseFormPage (Create/Edit)
All course fields + Section management:
- Section sub-form: add multiple sections with time/room/capacity
- Prerequisites multi-select dropdown (shows published courses)
- Status field visible to admin
- Full Bean-Validation backed, show field errors from API 400 response

#### Admin Users Page
- Table: Name | Email | Student ID | Dept | Semester | Credits Used | Status | Actions
- Filter by role, department, active status
- Click row → UserDetailPage
- Toggle active/inactive per user

#### UserDetailPage (Admin)
- User profile card (all fields)
- List of all their enrollments with section, time, status
- Admin can force-drop any enrollment from here

#### Conflicts Dashboard (`/admin/enrollments`)
- Table of all students with scheduling conflicts detected server-side
- Columns: Student | Course A | Course B | Overlap Time | Action
- "Resolve" → opens modal to manually drop one of the conflicting enrollments

#### AuditLogPage
- Paginated table: Timestamp | Admin | Action | Entity | Detail
- Filter by date range, action type

### 3.7 Shared Components

#### ConflictCheckModal
Triggered before any enrollment attempt. Calls `/enrollments/check-conflict`. Shows:
- Green checkmark if no conflicts
- Red warning with: "Time conflict with [COURSE_CODE] — [days] [time]–[time], Room [room]"
Shows separate warnings for: prerequisite not met, credit limit, seat full (shows waitlist
option instead).

#### WeeklyTimetable component
Props: `sections[]` (array of enrolled section objects). Renders a CSS-grid-based timetable.
Time slots = rows (07:00–21:00). Days = columns. Course blocks are absolutely positioned
within grid cells. Each course gets a stable color from a palette of 8 muted tones (not
random — deterministic based on courseId % 8).

#### NotificationBell
Top-right in navbar. Shows unread count badge. Click → dropdown list of latest 10 notifications.
Mark all read button. Poll `/notifications/unread-count` every 30 seconds.

### 3.8 Design System — Professional, No Emojis

```css
/* src/styles/variables.css */
:root {
  --color-primary:       #1a56db;   /* buttons, active states */
  --color-primary-dark:  #1340a8;
  --color-primary-light: #e8eeff;
  --color-secondary:     #374151;   /* headings */
  --color-text:          #111827;   /* body text */
  --color-text-muted:    #6b7280;   /* labels, hints */
  --color-bg:            #f9fafb;   /* page background */
  --color-surface:       #ffffff;   /* cards, panels */
  --color-border:        #e5e7eb;
  --color-success:       #047857;
  --color-success-bg:    #ecfdf5;
  --color-warning:       #b45309;
  --color-warning-bg:    #fffbeb;
  --color-danger:        #b91c1c;
  --color-danger-bg:     #fef2f2;
  --color-info:          #1d4ed8;
  --color-info-bg:       #eff6ff;
  --shadow-sm:           0 1px 3px rgba(0,0,0,0.08);
  --shadow-md:           0 4px 12px rgba(0,0,0,0.10);
  --radius-sm:           6px;
  --radius-md:           8px;
  --radius-lg:           12px;
  --font-sans:           'Inter', system-ui, sans-serif;
  --nav-height:          64px;
  --sidebar-width:       240px;
  --transition:          150ms ease;
}
```

**Layout rules:**
- Top navbar: height 64px, white bg, border-bottom, logo left, user menu + notification bell right
- Admin layout: fixed left sidebar 240px + main content area
- Student layout: top navbar only, no sidebar
- All content max-width 1200px, centered, padding 24px
- Mobile breakpoint: 768px — sidebar collapses to hamburger, grids go single-column

**Typography:**
- Page headings: 24px, weight 600, color `--color-secondary`
- Section headings: 18px, weight 600
- Body: 14px, weight 400, color `--color-text`
- Labels: 12px, weight 500, uppercase, letter-spacing 0.04em, color `--color-text-muted`

**Tables:**
- Header row: bg `#f3f4f6`, font 12px uppercase, weight 500, text-muted
- Body rows: 48px height, border-bottom `--color-border`, hover bg `#f9fafb`
- Pagination: prev/next buttons + page numbers, show current highlighted

**Buttons:**
- Primary: bg `--color-primary`, white text, radius 6px, padding 8px 16px, height 36px
- Secondary: border 1px `--color-border`, bg white, text `--color-secondary`
- Danger: bg `--color-danger`, white text
- Disabled: opacity 0.5, cursor not-allowed
- Loading state: spinner inside, text "Loading...", disabled

**Form inputs:**
- Height 40px, border 1px `--color-border`, radius 6px, padding 0 12px
- Focus: border-color `--color-primary`, box-shadow `0 0 0 3px rgba(26,86,219,0.15)`
- Error state: border-color `--color-danger`
- Error message: 12px, `--color-danger`, margin-top 4px

**Status badges:**
- PUBLISHED / ENROLLED: green bg + text
- DRAFT / WAITLISTED: amber bg + text
- ARCHIVED / DROPPED: gray bg + text
- All badges: 11px, weight 500, uppercase, letter-spacing 0.04em, padding 2px 8px, radius 4px

**No emojis anywhere. No illustrations. No stock image placeholders. Use simple
geometric shapes or solid-color placeholders where needed.**

### 3.9 Form Validation — Complete Rules

**Register:**
```js
const registerSchema = Yup.object({
  fullName:        Yup.string().min(3).max(120).required('Full name is required'),
  email:           Yup.string().email('Enter a valid email').required('Email is required'),
  studentId:       Yup.string().matches(/^[A-Z0-9]+$/i, 'Alphanumeric only').required(),
  department:      Yup.string().required('Select a department'),
  semester:        Yup.number().min(1).max(8).required('Select semester'),
  phone:           Yup.string().matches(/^[6-9]\d{9}$/, 'Enter a valid 10-digit mobile number').required(),
  password:        Yup.string()
    .min(8, 'Minimum 8 characters')
    .matches(/[A-Z]/, 'Must contain at least one uppercase letter')
    .matches(/\d/, 'Must contain at least one number')
    .matches(/[@$!%*?&#]/, 'Must contain at least one special character')
    .required('Password is required'),
  confirmPassword: Yup.string()
    .oneOf([Yup.ref('password')], 'Passwords do not match')
    .required('Please confirm your password'),
});
```

**Login:**
```js
const loginSchema = Yup.object({
  email:    Yup.string().email('Enter a valid email').required('Email is required'),
  password: Yup.string().required('Password is required'),
});
```

**Course Create/Edit (Admin):**
```js
const courseSchema = Yup.object({
  code:        Yup.string().matches(/^[A-Z]{2,4}\d{3}$/, 'Format: CS301').required(),
  title:       Yup.string().min(5).max(200).required(),
  description: Yup.string().min(20).max(2000).required(),
  credits:     Yup.number().min(1).max(6).required(),
  department:  Yup.string().required(),
  instructor:  Yup.string().min(3).required(),
});
```

---

## PART 4 — INTEGRATION CHECKLIST

Generate code such that every one of these flows works end-to-end without manual intervention:

- [ ] Student registers → redirect to `/login` → login → land on `/dashboard`
- [ ] Student browses `/catalogue` → filters by dept → clicks Enroll
  → conflict check runs → confirmation modal → enrolled → seat count decrements
- [ ] Student views `/schedule` → visual timetable renders with their enrolled sections
- [ ] Student drops a course → waitlisted student auto-promoted → both get notifications
- [ ] Admin logs in → sees `/admin` dashboard with real counts from DB
- [ ] Admin creates course → adds sections → publishes → appears in student catalogue
- [ ] Admin views any student's profile and all their enrollments
- [ ] Admin views conflicts dashboard → resolves a conflict by dropping one enrollment
- [ ] All API errors surface correctly in the frontend with field-level or top-level messages
- [ ] JWT expiry forces re-login gracefully
- [ ] Notification bell shows unread count and marks notifications read

---

## PART 5 — GENERATION INSTRUCTIONS FOR COPILOT

When generating code from this prompt:

1. **Generate backend first:** entities → repositories → DTOs → services → controllers → security config
2. **Then frontend:** axiosInstance → authStore → auth pages → layout → student pages → admin pages
3. **Every API call in React must use the shared axiosInstance** — no bare `fetch()` or direct `axios`
4. **Every service method in Spring Boot must have `@Transactional` where data is written**
5. **All controller methods must have `@Valid` on request bodies and return `ResponseEntity<>`**
6. **All React form fields must be registered with react-hook-form's `register()`** — no uncontrolled inputs
7. **Use React Router `<Navigate>` for redirects** — never `window.location.href` except in the axios interceptor
8. **Loading and error states are required on every page that fetches data** — show spinner while loading, show error message on failure
9. **No console.log statements in final code**
10. **All date/time rendering uses `Intl.DateTimeFormat`** — no raw ISO strings displayed to user

---

*End of GitHub Copilot Prompt — CourseFlow Full-Stack Application*
*Generated for FSAD-PS48 | React + Vite + Spring Boot 3.x + MySQL*

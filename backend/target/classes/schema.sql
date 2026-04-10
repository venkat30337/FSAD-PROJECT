CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role ENUM('STUDENT','ADMIN') NOT NULL DEFAULT 'STUDENT',
  student_id VARCHAR(30) UNIQUE,
  department VARCHAR(100),
  phone VARCHAR(20),
  semester INT,
  max_credits INT NOT NULL DEFAULT 24,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_role (role),
  INDEX idx_users_department (department),
  INDEX idx_users_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS courses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  credits INT NOT NULL,
  department VARCHAR(100) NOT NULL,
  instructor VARCHAR(120) NOT NULL,
  status ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_courses_created_by FOREIGN KEY (created_by) REFERENCES users(id),
  INDEX idx_courses_status (status),
  INDEX idx_courses_department (department),
  INDEX idx_courses_instructor (instructor),
  INDEX idx_courses_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sections (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id BIGINT NOT NULL,
  section_code VARCHAR(20) NOT NULL,
  room VARCHAR(60),
  days_of_week VARCHAR(50) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  max_seats INT NOT NULL DEFAULT 60,
  enrolled_count INT NOT NULL DEFAULT 0,
  academic_year VARCHAR(10) NOT NULL,
  semester_term ENUM('ODD','EVEN') NOT NULL,
  CONSTRAINT fk_sections_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
  UNIQUE KEY uq_section (course_id, section_code),
  INDEX idx_sections_course_id (course_id),
  INDEX idx_sections_academic (academic_year, semester_term)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prerequisites (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  course_id BIGINT NOT NULL,
  required_course_id BIGINT NOT NULL,
  CONSTRAINT fk_prereq_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
  CONSTRAINT fk_prereq_required_course FOREIGN KEY (required_course_id) REFERENCES courses(id) ON DELETE CASCADE,
  UNIQUE KEY uq_prereq (course_id, required_course_id),
  INDEX idx_prereq_course_id (course_id),
  INDEX idx_prereq_required_id (required_course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS enrollments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id BIGINT NOT NULL,
  section_id BIGINT NOT NULL,
  status ENUM('ENROLLED','WAITLISTED','DROPPED','COMPLETED') NOT NULL DEFAULT 'ENROLLED',
  waitlist_pos INT,
  enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  dropped_at DATETIME,
  CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES users(id),
  CONSTRAINT fk_enrollments_section FOREIGN KEY (section_id) REFERENCES sections(id),
  UNIQUE KEY uq_enrollment (student_id, section_id),
  INDEX idx_enrollments_student_id (student_id),
  INDEX idx_enrollments_section_id (section_id),
  INDEX idx_enrollments_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS degree_requirements (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  program VARCHAR(100) NOT NULL,
  course_id BIGINT NOT NULL,
  category ENUM('CORE','ELECTIVE','LAB') NOT NULL,
  CONSTRAINT fk_degree_requirements_course FOREIGN KEY (course_id) REFERENCES courses(id),
  INDEX idx_degree_program (program),
  INDEX idx_degree_category (category),
  INDEX idx_degree_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  message TEXT NOT NULL,
  type ENUM('ENROLLMENT','WAITLIST','DROP','ADMIN','SYSTEM') NOT NULL,
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_notifications_user_id (user_id),
  INDEX idx_notifications_unread (user_id, is_read),
  INDEX idx_notifications_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  admin_id BIGINT NOT NULL,
  action VARCHAR(100) NOT NULL,
  entity_type VARCHAR(60) NOT NULL,
  entity_id BIGINT,
  detail TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_admin FOREIGN KEY (admin_id) REFERENCES users(id),
  INDEX idx_audit_admin_id (admin_id),
  INDEX idx_audit_action (action),
  INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (id, full_name, email, password, role, student_id, department, phone, semester, max_credits, is_active)
VALUES
  (1, 'System Admin', 'admin@courseflow.edu', '$2a$10$y9wMddBiqHjWjO9QcNC.guhpvZd7VWxJw2SU72.68lppSfglubnsq', 'ADMIN', NULL, 'Administration', '9999999999', NULL, 24, TRUE),
  (2, 'Default Student', 'student@courseflow.edu', '$2a$10$iird8KvljvIAzXvPmiSoU.gAc2UPtmrogu1q/SAZoxriBbGOfoXTy', 'STUDENT', 'CS2024001', 'Computer Science', '9876543210', 3, 24, TRUE)
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), password = VALUES(password), updated_at = CURRENT_TIMESTAMP;

INSERT INTO courses (id, code, title, description, credits, department, instructor, status, created_by)
VALUES
  (1, 'CS301', 'Data Structures and Algorithms', 'Advanced data structures, complexity, and algorithmic design.', 4, 'Computer Science', 'Dr. Mehta', 'PUBLISHED', 1),
  (2, 'CS401', 'Machine Learning', 'Introduction to supervised and unsupervised machine learning techniques.', 4, 'Computer Science', 'Dr. Verma', 'PUBLISHED', 1),
  (3, 'EC201', 'Digital Electronics', 'Combinational and sequential circuits with practical design.', 3, 'Electronics', 'Dr. Rao', 'PUBLISHED', 1),
  (4, 'EC301', 'Signals and Systems', 'Continuous and discrete-time signal processing fundamentals.', 4, 'Electronics', 'Dr. Iyer', 'PUBLISHED', 1),
  (5, 'ME210', 'Thermodynamics', 'Laws of thermodynamics and engineering applications.', 3, 'Mechanical', 'Dr. Nair', 'PUBLISHED', 1),
  (6, 'ME320', 'CAD Systems', 'Computer-aided design workflows and engineering drawing.', 3, 'Mechanical', 'Dr. Kulkarni', 'PUBLISHED', 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), credits = VALUES(credits), updated_at = CURRENT_TIMESTAMP;

INSERT INTO sections (id, course_id, section_code, room, days_of_week, start_time, end_time, max_seats, enrolled_count, academic_year, semester_term)
VALUES
  (1, 1, 'A', 'LH-301', 'MON,WED,FRI', '09:00:00', '10:00:00', 60, 0, '2025-26', 'ODD'),
  (2, 1, 'B', 'LH-305', 'TUE,THU', '11:00:00', '12:30:00', 60, 0, '2025-26', 'ODD'),
  (3, 2, 'A', 'LH-401', 'MON,WED', '10:00:00', '11:30:00', 50, 0, '2025-26', 'ODD'),
  (4, 3, 'A', 'EC-201', 'TUE,THU', '09:30:00', '11:00:00', 55, 0, '2025-26', 'ODD'),
  (5, 4, 'A', 'EC-305', 'MON,WED', '12:00:00', '13:30:00', 55, 0, '2025-26', 'ODD'),
  (6, 4, 'B', 'EC-307', 'TUE,THU', '14:00:00', '15:30:00', 55, 0, '2025-26', 'ODD'),
  (7, 5, 'A', 'ME-112', 'MON,FRI', '15:00:00', '16:30:00', 45, 0, '2025-26', 'ODD'),
  (8, 6, 'A', 'ME-206', 'WED,FRI', '13:00:00', '14:30:00', 45, 0, '2025-26', 'ODD')
ON DUPLICATE KEY UPDATE room = VALUES(room), start_time = VALUES(start_time), end_time = VALUES(end_time);

INSERT INTO prerequisites (id, course_id, required_course_id)
VALUES
  (1, 2, 1),
  (2, 4, 3)
ON DUPLICATE KEY UPDATE required_course_id = VALUES(required_course_id);

INSERT INTO degree_requirements (id, program, course_id, category)
VALUES
  (1, 'Computer Science', 1, 'CORE'),
  (2, 'Computer Science', 2, 'ELECTIVE'),
  (3, 'Electronics', 3, 'CORE'),
  (4, 'Electronics', 4, 'CORE'),
  (5, 'Mechanical', 5, 'CORE'),
  (6, 'Mechanical', 6, 'LAB')
ON DUPLICATE KEY UPDATE category = VALUES(category);

INSERT INTO notifications (id, user_id, title, message, type, is_read)
VALUES
  (1, 2, 'Welcome to CourseFlow', 'Your student account is ready. You can begin course enrollment.', 'SYSTEM', FALSE)
ON DUPLICATE KEY UPDATE message = VALUES(message), is_read = VALUES(is_read);

INSERT INTO audit_logs (id, admin_id, action, entity_type, entity_id, detail)
VALUES
  (1, 1, 'SYSTEM_INIT', 'DATABASE', NULL, 'Initial seed data loaded for CourseFlow.')
ON DUPLICATE KEY UPDATE detail = VALUES(detail);

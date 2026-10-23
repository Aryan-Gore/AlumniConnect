-- =============================================================================
-- AlumniConnect — Database Schema (MySQL 8 / MariaDB compatible)
--
-- Design notes:
--  - `users` is the single auth table for all three login-capable roles
--    (STUDENT, ALUMNI, ADMIN). PUBLIC is not a stored role — it's simply the
--    absence of a session.
--  - `alumni_records` is intentionally NOT a subtype of `users`. An alumni
--    record can and usually does exist before any login (IMPORTED/INVITED),
--    per the SRS's "admin imports -> alumni claims" workflow. `user_id` is
--    nullable and only populated at claim time — this is what prevents the
--    duplicate-record problem the spec calls out explicitly.
--  - Every institution-controlled column lives directly on `alumni_records`
--    and is written only by admin-facing endpoints. Alumni-controlled
--    columns are written only by the /alumni/me self-service endpoint. The
--    database can't enforce that split by itself — see the Spring Boot DTOs,
--    which expose two different request shapes for the same row.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS alumniconnect
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE alumniconnect;

-- -----------------------------------------------------------------------------
-- institutes / departments — SVVV is a multi-institute university, not flat
-- departments (e.g. Shri Vaishnav Institute of Technology & Science contains
-- CSE/ECE/Mechanical/Civil/Electrical; Shri Vaishnav School of Management
-- contains MBA/BBA, etc). Replaces the free-text `department` VARCHAR that
-- both alumni_records and student_profiles used to carry directly.
-- -----------------------------------------------------------------------------
CREATE TABLE institutes (
  id    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name  VARCHAR(150) NOT NULL UNIQUE,
  code  VARCHAR(20)  NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE departments (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  institute_id  BIGINT UNSIGNED NOT NULL,
  name          VARCHAR(150) NOT NULL,
  code          VARCHAR(20),
  CONSTRAINT fk_departments_institute
    FOREIGN KEY (institute_id) REFERENCES institutes(id) ON DELETE CASCADE,
  UNIQUE KEY uq_department_institute_name (institute_id, name)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- users — base authentication for STUDENT / ALUMNI / ADMIN
-- -----------------------------------------------------------------------------
CREATE TABLE users (
  id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  email           VARCHAR(255)    NOT NULL UNIQUE,
  password_hash   VARCHAR(255)    NOT NULL,   -- Google-created accounts get an unusable random hash
  full_name       VARCHAR(150)    NOT NULL,
  role            ENUM('STUDENT','ALUMNI','ADMIN') NOT NULL,
  -- One photo per account regardless of role, so chat / feed / directory can
  -- all show an avatar without caring whether the user is a student or alumnus.
  -- Stored as a path relative to the API (e.g. /files/profiles/<uuid>.jpg).
  profile_photo_url VARCHAR(255),
  -- Google "sub" claim; set only for accounts created through Google sign-in.
  -- Matching on sub (not email) is what Google recommends for linking.
  google_sub      VARCHAR(64)     UNIQUE,
  is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- student_profiles — 1:1 with users where role = STUDENT
-- -----------------------------------------------------------------------------
CREATE TABLE student_profiles (
  id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id             BIGINT UNSIGNED NOT NULL UNIQUE,
  enrollment_number   VARCHAR(30)     NOT NULL UNIQUE,
  department_id       BIGINT UNSIGNED NOT NULL,
  batch               VARCHAR(20),
  skills              JSON,
  career_interests    TEXT,
  CONSTRAINT fk_student_profiles_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_student_profiles_department
    FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- alumni_records — the central "permanent record"; see design notes above
-- -----------------------------------------------------------------------------
CREATE TABLE alumni_records (
  id                    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id               BIGINT UNSIGNED UNIQUE,               -- NULL until claimed

  -- Institution-controlled (admin write only)
  full_name             VARCHAR(150)    NOT NULL,
  email                 VARCHAR(255)    NOT NULL,
  enrollment_number     VARCHAR(30)     NOT NULL UNIQUE,
  degree                VARCHAR(100),
  department_id         BIGINT UNSIGNED NOT NULL,
  batch                 VARCHAR(20),
  graduation_year       INT             NOT NULL,

  -- Alumni-controlled (self-service write only, once claimed)
  phone                 VARCHAR(20),
  current_location      VARCHAR(150),
  current_company       VARCHAR(150),
  job_title             VARCHAR(150),
  industry              VARCHAR(100),
  work_location         VARCHAR(150),
  experience_years      INT,
  linkedin_url          VARCHAR(255),
  github_url            VARCHAR(255),
  portfolio_url         VARCHAR(255),
  professional_summary  TEXT,
  -- (profile photo lives on users.profile_photo_url — one source of truth)
  is_verified           BOOLEAN         NOT NULL DEFAULT FALSE,  -- admin-set only

  -- Per-field contact privacy — LinkedIn/GitHub/portfolio stay always-visible
  -- once filled in (no toggle requested for those); email and phone are the
  -- two fields the spec calls out as needing Public/Alumni-only/Private.
  email_visibility      ENUM('PUBLIC','ALUMNI_ONLY','PRIVATE') NOT NULL DEFAULT 'ALUMNI_ONLY',
  phone_visibility      ENUM('PUBLIC','ALUMNI_ONLY','PRIVATE') NOT NULL DEFAULT 'PRIVATE',

  -- Alumni participation flags
  open_to_mentoring     BOOLEAN         NOT NULL DEFAULT FALSE,
  open_to_networking    BOOLEAN         NOT NULL DEFAULT FALSE,
  available_for_hiring  BOOLEAN         NOT NULL DEFAULT FALSE,
  interested_in_events  BOOLEAN         NOT NULL DEFAULT FALSE,

  directory_visibility  ENUM('PUBLIC','STUDENTS_AND_ALUMNI','ALUMNI_ONLY','PRIVATE')
                                        NOT NULL DEFAULT 'STUDENTS_AND_ALUMNI',

  -- Lifecycle
  status                ENUM('IMPORTED','INVITED','CLAIMED','ACTIVE','INACTIVE')
                                        NOT NULL DEFAULT 'IMPORTED',
  imported_at           TIMESTAMP       NULL,
  invited_at            TIMESTAMP       NULL,
  claimed_at            TIMESTAMP       NULL,
  created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_alumni_records_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
  CONSTRAINT fk_alumni_records_department
    FOREIGN KEY (department_id) REFERENCES departments(id),
  INDEX idx_alumni_status (status),
  INDEX idx_alumni_department (department_id),
  INDEX idx_alumni_batch (batch),
  INDEX idx_alumni_graduation_year (graduation_year),
  INDEX idx_alumni_industry (industry),
  INDEX idx_alumni_company (current_company),
  -- Supports the claim-lookup query (name + enrollment + email + grad year)
  INDEX idx_alumni_claim_lookup (enrollment_number, email, graduation_year)
) ENGINE=InnoDB;

-- Normalized skills — replaces a JSON array on alumni_records specifically so
-- "find alumni who know Java AND Spring Boot" is a real indexed join, not a
-- JSON-containment scan. student_profiles.skills stays JSON — it's a resume
-- list for the student's own use, not something the directory search needs
-- to query across.
CREATE TABLE skills (
  id    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name  VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE alumni_skills (
  alumni_id  BIGINT UNSIGNED NOT NULL,
  skill_id   BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (alumni_id, skill_id),
  CONSTRAINT fk_alumni_skills_alumni FOREIGN KEY (alumni_id) REFERENCES alumni_records(id) ON DELETE CASCADE,
  CONSTRAINT fk_alumni_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Additional degrees beyond the one this institution certifies (e.g. a
-- Master's completed elsewhere after graduating SVVV). Alumni-reported,
-- hence institution_name is free text rather than an FK to departments.
CREATE TABLE education_records (
  id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  alumni_id         BIGINT UNSIGNED NOT NULL,
  degree            VARCHAR(100)    NOT NULL,
  program           VARCHAR(150),
  institution_name  VARCHAR(200)    NOT NULL,
  start_year        INT,
  end_year          INT,
  cgpa              DECIMAL(4,2),
  CONSTRAINT fk_education_alumni FOREIGN KEY (alumni_id) REFERENCES alumni_records(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE projects (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  alumni_id     BIGINT UNSIGNED NOT NULL,
  name          VARCHAR(200)    NOT NULL,
  description   TEXT,
  technologies  JSON,
  project_url   VARCHAR(255),
  github_url    VARCHAR(255),
  start_date    DATE,
  end_date      DATE,
  CONSTRAINT fk_projects_alumni FOREIGN KEY (alumni_id) REFERENCES alumni_records(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Covers certifications, achievements, awards, and publications — the SRS's
-- examples span all four (AWS Certified Developer / Employee of the Year /
-- published research paper / hackathon winner), so one table with a `type`
-- column instead of four near-identical tables.
CREATE TABLE certifications (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  alumni_id      BIGINT UNSIGNED NOT NULL,
  type           ENUM('CERTIFICATION','ACHIEVEMENT','AWARD','PUBLICATION') NOT NULL DEFAULT 'CERTIFICATION',
  title          VARCHAR(200)    NOT NULL,
  issuer         VARCHAR(150),
  issue_date     DATE,
  credential_url VARCHAR(255),
  CONSTRAINT fk_certifications_alumni FOREIGN KEY (alumni_id) REFERENCES alumni_records(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE career_history_entries (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  alumni_id   BIGINT UNSIGNED NOT NULL,
  company     VARCHAR(150)    NOT NULL,
  job_title   VARCHAR(150)    NOT NULL,
  start_date  DATE            NOT NULL,
  end_date    DATE,
  description TEXT,  -- bullet-point responsibilities; frontend splits on newline
  CONSTRAINT fk_career_history_alumni
    FOREIGN KEY (alumni_id) REFERENCES alumni_records(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- CSV / Excel import — batches + per-row errors
-- -----------------------------------------------------------------------------
CREATE TABLE alumni_import_batches (
  id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  admin_id            BIGINT UNSIGNED NOT NULL,
  original_filename   VARCHAR(255)    NOT NULL,
  column_mapping      JSON,
  -- Holds the raw uploaded CSV between the upload and confirm steps of the
  -- wizard (see AlumniImportService). Cleared once status = CONFIRMED —
  -- this table is a staging area, not permanent storage for the file.
  staged_content      LONGTEXT,
  status              ENUM('UPLOADED','MAPPED','PREVIEWED','CONFIRMED','FAILED')
                                      NOT NULL DEFAULT 'UPLOADED',
  total_rows          INT UNSIGNED   DEFAULT 0,
  successful_rows     INT UNSIGNED   DEFAULT 0,
  failed_rows         INT UNSIGNED   DEFAULT 0,
  duplicate_rows      INT UNSIGNED   DEFAULT 0,
  created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confirmed_at        TIMESTAMP      NULL,
  CONSTRAINT fk_import_batches_admin
    FOREIGN KEY (admin_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE alumni_import_errors (
  id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  batch_id        BIGINT UNSIGNED NOT NULL,
  row_index       INT UNSIGNED    NOT NULL,
  error_message   VARCHAR(500)    NOT NULL,
  raw_row_data    JSON,
  CONSTRAINT fk_import_errors_batch
    FOREIGN KEY (batch_id) REFERENCES alumni_import_batches(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- Events
-- -----------------------------------------------------------------------------
CREATE TABLE events (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  title         VARCHAR(200)    NOT NULL,
  description   TEXT,
  event_date    DATETIME        NOT NULL,
  mode          ENUM('ONLINE','ON_CAMPUS') NOT NULL,
  location      VARCHAR(200),
  capacity      INT UNSIGNED    NOT NULL,
  status        ENUM('DRAFT','PUBLISHED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
  created_by    BIGINT UNSIGNED NOT NULL,
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_events_created_by FOREIGN KEY (created_by) REFERENCES users(id),
  INDEX idx_events_status_date (status, event_date)
) ENGINE=InnoDB;

CREATE TABLE event_registrations (
  id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  event_id        BIGINT UNSIGNED NOT NULL,
  user_id         BIGINT UNSIGNED NOT NULL,
  status          ENUM('REGISTERED','CANCELLED') NOT NULL DEFAULT 'REGISTERED',
  registered_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_registrations_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
  CONSTRAINT fk_registrations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uq_event_user (event_id, user_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- Mentorship
-- -----------------------------------------------------------------------------
CREATE TABLE mentorship_requests (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  student_id     BIGINT UNSIGNED NOT NULL,
  alumni_id      BIGINT UNSIGNED NOT NULL,
  message        TEXT,
  status         ENUM('PENDING','ACCEPTED','REJECTED','COMPLETED') NOT NULL DEFAULT 'PENDING',
  requested_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  responded_at   TIMESTAMP       NULL,
  CONSTRAINT fk_mentorship_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_mentorship_alumni FOREIGN KEY (alumni_id) REFERENCES alumni_records(id) ON DELETE CASCADE,
  INDEX idx_mentorship_status (status)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- Jobs
-- -----------------------------------------------------------------------------
CREATE TABLE job_postings (
  id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  -- Any signed-in user can post a listing (students pass on internship leads,
  -- alumni post openings, the office posts its own). Everyone's listings go
  -- through admin moderation except the admin's own.
  posted_by_user_id BIGINT UNSIGNED NOT NULL,   -- users.id
  title           VARCHAR(200)    NOT NULL,
  company         VARCHAR(150)    NOT NULL,
  location        VARCHAR(150),
  job_type        ENUM('FULL_TIME','INTERNSHIP','PART_TIME','CONTRACT') NOT NULL DEFAULT 'FULL_TIME',
  description     TEXT,
  status          ENUM('PENDING','APPROVED','REJECTED','REMOVED','EXPIRED') NOT NULL DEFAULT 'PENDING',
  posted_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at      DATE,
  moderated_by    BIGINT UNSIGNED,
  moderated_at    TIMESTAMP       NULL,
  CONSTRAINT fk_jobs_posted_by FOREIGN KEY (posted_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_jobs_moderated_by FOREIGN KEY (moderated_by) REFERENCES users(id),
  INDEX idx_jobs_status (status)
) ENGINE=InnoDB;

CREATE TABLE job_applications (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  job_id         BIGINT UNSIGNED NOT NULL,
  applicant_id   BIGINT UNSIGNED NOT NULL,
  status         ENUM('SUBMITTED','REVIEWED','SHORTLISTED','REJECTED') NOT NULL DEFAULT 'SUBMITTED',
  applied_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES job_postings(id) ON DELETE CASCADE,
  CONSTRAINT fk_applications_applicant FOREIGN KEY (applicant_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uq_job_applicant (job_id, applicant_id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- Announcements
-- -----------------------------------------------------------------------------
CREATE TABLE announcements (
  id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  title              VARCHAR(200)    NOT NULL,
  content            TEXT            NOT NULL,
  target_audience    ENUM('ALL','STUDENTS','ALUMNI','DEPARTMENT','BATCH') NOT NULL DEFAULT 'ALL',
  target_department_id BIGINT UNSIGNED,
  target_batch       VARCHAR(20),
  priority           ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
  publish_date       DATE            NOT NULL,
  status             ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'PUBLISHED',
  created_by         BIGINT UNSIGNED NOT NULL,
  created_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_announcements_created_by FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT fk_announcements_department FOREIGN KEY (target_department_id) REFERENCES departments(id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- Notifications
-- -----------------------------------------------------------------------------
CREATE TABLE notifications (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id      BIGINT UNSIGNED NOT NULL,
  category     ENUM('MENTORSHIP','EVENT','JOB','ANNOUNCEMENT','ACCOUNT','MESSAGE','POST') NOT NULL,
  message      VARCHAR(500)    NOT NULL,
  is_read      BOOLEAN         NOT NULL DEFAULT FALSE,
  created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_notifications_user_read (user_id, is_read)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- Audit log — append-only; no application role is ever granted UPDATE/DELETE
-- -----------------------------------------------------------------------------
CREATE TABLE audit_logs (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  admin_id     BIGINT UNSIGNED NOT NULL,
  action       ENUM('ADD_ALUMNI','UPDATE_ALUMNI','IMPORT_ALUMNI','EXPORT_ALUMNI',
                     'INVITE_ALUMNI','DEACTIVATE_ALUMNI','CREATE_EVENT','UPDATE_EVENT',
                     'APPROVE_JOB','REJECT_JOB','REMOVE_POST','VERIFY_ALUMNI',
                     'REMOVE_MESSAGE','DEACTIVATE_USER','REACTIVATE_USER') NOT NULL,
  details      VARCHAR(500),
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_audit_logs_admin FOREIGN KEY (admin_id) REFERENCES users(id),
  INDEX idx_audit_action (action)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- Chat — two kinds of conversation share one set of tables:
--   DIRECT      1:1 between two users. direct_key = "<lowerUserId>:<higherUserId>"
--               and its UNIQUE index make find-or-create race-safe: two users
--               opening a chat with each other at the same moment can never
--               end up with two separate threads.
--   DEPARTMENT  one group room per department (the "groups according to
--               departments" feature). department_id is UNIQUE, so there is
--               exactly one room per department; members are added when they
--               first open it, and only members of that department (or an
--               admin) are allowed in.
-- Unread counts use last_read_message_id (a monotonic cursor) rather than a
-- timestamp, so second-level TIMESTAMP rounding can never mark a message
-- read or unread by mistake.
-- -----------------------------------------------------------------------------
CREATE TABLE conversations (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  type           ENUM('DIRECT','DEPARTMENT') NOT NULL DEFAULT 'DIRECT',
  direct_key     VARCHAR(64)     UNIQUE,
  department_id  BIGINT UNSIGNED UNIQUE,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_conversations_department
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE conversation_participants (
  id                    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  conversation_id       BIGINT UNSIGNED NOT NULL,
  user_id               BIGINT UNSIGNED NOT NULL,
  last_read_message_id  BIGINT UNSIGNED NULL,
  CONSTRAINT fk_conv_participants_conversation
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
  CONSTRAINT fk_conv_participants_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uq_conversation_user (conversation_id, user_id)
) ENGINE=InnoDB;

CREATE TABLE messages (
  id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  conversation_id  BIGINT UNSIGNED NOT NULL,
  sender_id        BIGINT UNSIGNED NOT NULL,
  content          TEXT            NOT NULL,
  sent_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_messages_conversation
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
  CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
  INDEX idx_messages_conversation_id (conversation_id, id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- Social feed
-- -----------------------------------------------------------------------------
CREATE TABLE posts (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  author_id    BIGINT UNSIGNED NOT NULL,
  content      TEXT            NOT NULL,
  image_url    VARCHAR(255),
  -- PUBLIC = any signed-in user, ALUMNI_ONLY = alumni + admins, DEPARTMENT =
  -- people in the author's own department, CONNECTIONS = people the author
  -- has a direct-message conversation with.
  visibility   ENUM('PUBLIC','ALUMNI_ONLY','DEPARTMENT','CONNECTIONS') NOT NULL DEFAULT 'PUBLIC',
  created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_posts_created (created_at)
) ENGINE=InnoDB;

CREATE TABLE post_likes (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  post_id     BIGINT UNSIGNED NOT NULL,
  user_id     BIGINT UNSIGNED NOT NULL,
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uq_post_user_like (post_id, user_id)
) ENGINE=InnoDB;

CREATE TABLE post_comments (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  post_id     BIGINT UNSIGNED NOT NULL,
  author_id   BIGINT UNSIGNED NOT NULL,
  content     TEXT            NOT NULL,
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_post_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_comments_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- Seed data — real SVVV institutes/departments (see chat discussion; sourced
-- from svvv.edu.in pages surfaced by search, not a guaranteed-exhaustive
-- crawl — confirm against the current official site before this ships).
-- -----------------------------------------------------------------------------
INSERT INTO institutes (name, code) VALUES
  ('Shri Vaishnav Institute of Technology & Science', 'SVITS'),
  ('Shri Vaishnav School of Management', 'SVSM'),
  ('Shri Vaishnav Institute of Science', 'SVIS'),
  ('Shri Vaishnav Institute of Social Sciences, Humanities & Arts', 'SVISSHA'),
  ('Shri Vaishnav Institute of Architecture', 'SVIARCH'),
  ('Shri Vaishnav Institute of Planning', 'SVIP'),
  ('Shri Vaishnav Institute of Agriculture', 'SVIA'),
  ('Shri Vaishnav Institute of Education', 'SVIE');

INSERT INTO departments (institute_id, name, code) VALUES
  ((SELECT id FROM institutes WHERE code = 'SVITS'), 'Computer Science & Engineering', 'CSE'),
  ((SELECT id FROM institutes WHERE code = 'SVITS'), 'Electronics & Communication Engineering', 'ECE'),
  ((SELECT id FROM institutes WHERE code = 'SVITS'), 'Electrical Engineering', 'EE'),
  ((SELECT id FROM institutes WHERE code = 'SVITS'), 'Mechanical Engineering', 'ME'),
  ((SELECT id FROM institutes WHERE code = 'SVITS'), 'Civil Engineering', 'CE'),
  ((SELECT id FROM institutes WHERE code = 'SVITS'), 'Bachelor of Computer Applications', 'BCA'),
  ((SELECT id FROM institutes WHERE code = 'SVITS'), 'Master of Computer Applications', 'MCA'),
  ((SELECT id FROM institutes WHERE code = 'SVSM'),  'Master of Business Administration', 'MBA'),
  ((SELECT id FROM institutes WHERE code = 'SVSM'),  'Bachelor of Business Administration', 'BBA'),
  ((SELECT id FROM institutes WHERE code = 'SVSM'),  'Commerce', 'COM'),
  ((SELECT id FROM institutes WHERE code = 'SVIS'),  'Physics', 'PHY'),
  ((SELECT id FROM institutes WHERE code = 'SVIS'),  'Chemistry', 'CHEM'),
  ((SELECT id FROM institutes WHERE code = 'SVIS'),  'Biochemistry', 'BIOCHEM'),
  ((SELECT id FROM institutes WHERE code = 'SVISSHA'), 'English', 'ENG'),
  ((SELECT id FROM institutes WHERE code = 'SVISSHA'), 'Psychology', 'PSY'),
  ((SELECT id FROM institutes WHERE code = 'SVISSHA'), 'Economics', 'ECO'),
  ((SELECT id FROM institutes WHERE code = 'SVISSHA'), 'Political Science', 'POLSCI'),
  ((SELECT id FROM institutes WHERE code = 'SVISSHA'), 'Sociology', 'SOC'),
  ((SELECT id FROM institutes WHERE code = 'SVISSHA'), 'History', 'HIST'),
  ((SELECT id FROM institutes WHERE code = 'SVIARCH'), 'Architecture', 'ARCH'),
  ((SELECT id FROM institutes WHERE code = 'SVIP'),  'Urban Planning', 'PLAN'),
  ((SELECT id FROM institutes WHERE code = 'SVIA'),  'Agriculture', 'AGRI'),
  ((SELECT id FROM institutes WHERE code = 'SVIE'),  'Education', 'EDU');

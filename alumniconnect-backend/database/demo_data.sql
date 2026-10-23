-- =============================================================================
-- AlumniConnect — Demo / Seed Data
--
-- Run AFTER schema.sql (which already inserts institutes + departments).
-- All password_hash values below are BCrypt hashes of "Password@123"
-- (12 rounds) — safe to use for demo/testing only.
--
-- Personas at a glance:
--   Admin   : admin@svvv.edu.in           (Password@123)
--   Alumni  : rahul.sharma@gmail.com      (Password@123)  — ACTIVE, CSE
--             priya.verma@yahoo.com       (Password@123)  — ACTIVE, ECE
--             aniket.patil@outlook.com    (Password@123)  — ACTIVE, ME
--             neha.joshi@gmail.com        (Password@123)  — ACTIVE, MBA
--             vikram.singh@hotmail.com    (Password@123)  — ACTIVE, CSE
--             deepika.tiwari@gmail.com    (Password@123)  — CLAIMED (partial)
--             (8 more IMPORTED / INVITED — no user account yet)
--   Students: arjun.meena@svvv.edu.in     (Password@123)  — CSE 2026
--             kavya.patel@svvv.edu.in     (Password@123)  — ECE 2026
--             rohan.dubey@svvv.edu.in     (Password@123)  — CSE 2026
-- =============================================================================

USE alumniconnect;

-- =============================================================================
-- 1. USERS  (admin + active alumni + students)
--    All passwords are BCrypt of "Password@123"
-- =============================================================================
INSERT INTO users (id, email, password_hash, full_name, role, is_active) VALUES
  -- Admin
  (1,  'admin@svvv.edu.in',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Admin SVVV', 'ADMIN', TRUE),

  -- Alumni who have claimed their profiles (ACTIVE)
  (2,  'rahul.sharma@gmail.com',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Rahul Sharma', 'ALUMNI', TRUE),
  (3,  'priya.verma@yahoo.com',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Priya Verma', 'ALUMNI', TRUE),
  (4,  'aniket.patil@outlook.com',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Aniket Patil', 'ALUMNI', TRUE),
  (5,  'neha.joshi@gmail.com',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Neha Joshi', 'ALUMNI', TRUE),
  (6,  'vikram.singh@hotmail.com',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Vikram Singh', 'ALUMNI', TRUE),
  -- Alumni who claimed but hasn't fully filled profile
  (7,  'deepika.tiwari@gmail.com',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Deepika Tiwari', 'ALUMNI', TRUE),

  -- Students
  (8,  'arjun.meena@svvv.edu.in',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Arjun Meena', 'STUDENT', TRUE),
  (9,  'kavya.patel@svvv.edu.in',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Kavya Patel', 'STUDENT', TRUE),
  (10, 'rohan.dubey@svvv.edu.in',
       '$2a$12$LF4cFDC.ER8mwfMRNlflh.07KxEfJmX9IEsQKfQG.b0F7UqfFrWIq',
       'Rohan Dubey', 'STUDENT', TRUE);


-- =============================================================================
-- 2. STUDENT PROFILES
-- =============================================================================
INSERT INTO student_profiles (user_id, enrollment_number, department_id, batch, skills, career_interests) VALUES
  (8,  '0801CS211001',
       (SELECT id FROM departments WHERE code='CSE'),
       '2021-25',
       '["Java","Spring Boot","React","MySQL","Python"]',
       'Backend Development, Cloud Computing, DevOps'),
  (9,  '0801EC211042',
       (SELECT id FROM departments WHERE code='ECE'),
       '2021-25',
       '["Embedded C","Python","IoT","VLSI","MATLAB"]',
       'Embedded Systems, IoT Product Development'),
  (10, '0801CS211078',
       (SELECT id FROM departments WHERE code='CSE'),
       '2021-25',
       '["React","Node.js","MongoDB","TypeScript","Docker"]',
       'Full Stack Development, Startup Ecosystem');


-- =============================================================================
-- 3. ALUMNI RECORDS
--    IDs 1–5   → claimed/active alumni linked to user rows 2–6
--    IDs 6–7   → claimed (partial)  linked to user row 7
--    IDs 8–15  → imported/invited, no user account yet
-- =============================================================================
INSERT INTO alumni_records (
  id, user_id, full_name, email, enrollment_number, degree, department_id,
  batch, graduation_year, phone, current_location, current_company, job_title,
  industry, experience_years, linkedin_url, github_url, professional_summary,
  open_to_mentoring, open_to_networking, available_for_hiring, interested_in_events,
  is_verified, status, email_visibility, claimed_at
) VALUES

  -- ── 1 · Rahul Sharma · CSE · 2018 · Software Engineer @ TCS ──────────────
  (1,  2,
   'Rahul Sharma', 'rahul.sharma@gmail.com', '0801CS181023',
   'B.Tech', (SELECT id FROM departments WHERE code='CSE'),
   '2014-18', 2018,
   '+91-9876543210', 'Pune, Maharashtra', 'Tata Consultancy Services',
   'Senior Software Engineer', 'IT Services', 6,
   'https://linkedin.com/in/rahul-sharma-svvv',
   'https://github.com/rahulsharma-dev',
   'Full-stack developer with 6 years of experience in Java/Spring and React. I enjoy mentoring students entering the industry.',
   TRUE, TRUE, FALSE, TRUE,
   TRUE, 'ACTIVE', 'ALUMNI_ONLY',
   '2022-06-15 10:00:00'),

  -- ── 2 · Priya Verma · ECE · 2019 · VLSI @ Intel ──────────────────────────
  (2,  3,
   'Priya Verma', 'priya.verma@yahoo.com', '0801EC181057',
   'B.Tech', (SELECT id FROM departments WHERE code='ECE'),
   '2015-19', 2019,
   '+91-9845012345', 'Bengaluru, Karnataka', 'Intel India',
   'VLSI Design Engineer', 'Semiconductor', 5,
   'https://linkedin.com/in/priya-verma-ece',
   NULL,
   'VLSI and embedded systems engineer working on Intel''s next-gen chip design teams. Passionate about hardware-software co-design.',
   TRUE, TRUE, FALSE, TRUE,
   TRUE, 'ACTIVE', 'ALUMNI_ONLY',
   '2022-09-20 11:30:00'),

  -- ── 3 · Aniket Patil · ME · 2020 · Product Design @ Mahindra ─────────────
  (3,  4,
   'Aniket Patil', 'aniket.patil@outlook.com', '0801ME181009',
   'B.Tech', (SELECT id FROM departments WHERE code='ME'),
   '2016-20', 2020,
   '+91-9123456789', 'Pune, Maharashtra', 'Mahindra & Mahindra',
   'Product Design Engineer', 'Automotive', 4,
   'https://linkedin.com/in/aniket-patil-me',
   NULL,
   'Mechanical engineer focused on EV powertrain components and lightweight structural design at Mahindra''s R&D center.',
   FALSE, TRUE, FALSE, TRUE,
   FALSE, 'ACTIVE', 'ALUMNI_ONLY',
   '2023-01-10 09:00:00'),

  -- ── 4 · Neha Joshi · MBA · 2019 · Product Manager @ Flipkart ─────────────
  (4,  5,
   'Neha Joshi', 'neha.joshi@gmail.com', '0802MB191005',
   'MBA', (SELECT id FROM departments WHERE code='MBA'),
   '2017-19', 2019,
   '+91-9988776655', 'Bengaluru, Karnataka', 'Flipkart',
   'Senior Product Manager', 'E-Commerce', 5,
   'https://linkedin.com/in/neha-joshi-pm',
   NULL,
   'Product leader driving growth features for Flipkart''s seller ecosystem. Background in operations and supply chain from MBA. Open to coffee chats with business and CS students.',
   TRUE, TRUE, FALSE, TRUE,
   TRUE, 'ACTIVE', 'ALUMNI_ONLY',
   '2022-03-07 14:00:00'),

  -- ── 5 · Vikram Singh · CSE · 2017 · Co-founder @ Stealth Startup ─────────
  (5,  6,
   'Vikram Singh', 'vikram.singh@hotmail.com', '0801CS171034',
   'B.Tech', (SELECT id FROM departments WHERE code='CSE'),
   '2013-17', 2017,
   '+91-9456781234', 'Mumbai, Maharashtra', 'StealthAI Labs',
   'Co-founder & CTO', 'SaaS / AI', 7,
   'https://linkedin.com/in/vikram-singh-cto',
   'https://github.com/vikramsingh-dev',
   'Built and scaled two SaaS products from 0 to 10k users. Currently building an AI-powered contract review tool. Happy to mentor on startups and system design.',
   TRUE, TRUE, TRUE, TRUE,
   TRUE, 'ACTIVE', 'PUBLIC',
   '2021-11-22 16:45:00'),

  -- ── 6 · Deepika Tiwari · CSE · 2021 · CLAIMED but sparse ────────────────
  (6,  7,
   'Deepika Tiwari', 'deepika.tiwari@gmail.com', '0801CS211099',
   'B.Tech', (SELECT id FROM departments WHERE code='CSE'),
   '2017-21', 2021,
   NULL, 'Indore, Madhya Pradesh', NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'CLAIMED', 'PRIVATE',
   '2024-02-01 08:00:00'),

  -- ── 7–15 · Imported/Invited records (no user_id) ─────────────────────────
  (7,  NULL,
   'Suresh Gupta', 'suresh.gupta@rediffmail.com', '0801CS161045',
   'B.Tech', (SELECT id FROM departments WHERE code='CSE'),
   '2012-16', 2016,
   NULL, NULL, NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'INVITED', 'ALUMNI_ONLY',
   NULL),

  (8,  NULL,
   'Anjali Mishra', 'anjali.mishra@gmail.com', '0802MB181012',
   'MBA', (SELECT id FROM departments WHERE code='MBA'),
   '2016-18', 2018,
   NULL, NULL, NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'IMPORTED', 'ALUMNI_ONLY',
   NULL),

  (9,  NULL,
   'Karan Mehta', 'karan.mehta@yahoo.co.in', '0801EC171028',
   'B.Tech', (SELECT id FROM departments WHERE code='ECE'),
   '2013-17', 2017,
   NULL, NULL, NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'IMPORTED', 'ALUMNI_ONLY',
   NULL),

  (10, NULL,
   'Pooja Yadav', 'pooja.yadav@gmail.com', '0801CS191067',
   'B.Tech', (SELECT id FROM departments WHERE code='CSE'),
   '2015-19', 2019,
   NULL, NULL, NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'IMPORTED', 'ALUMNI_ONLY',
   NULL),

  (11, NULL,
   'Harish Tomar', 'harish.tomar@outlook.com', '0801ME161018',
   'B.Tech', (SELECT id FROM departments WHERE code='ME'),
   '2012-16', 2016,
   NULL, NULL, NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'INVITED', 'ALUMNI_ONLY',
   NULL),

  (12, NULL,
   'Swati Rathore', 'swati.rathore@gmail.com', '0801CS201011',
   'B.Tech', (SELECT id FROM departments WHERE code='CSE'),
   '2016-20', 2020,
   NULL, NULL, NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'IMPORTED', 'ALUMNI_ONLY',
   NULL),

  (13, NULL,
   'Manish Chouhan', 'manish.chouhan@gmail.com', '0801CS151034',
   'B.Tech', (SELECT id FROM departments WHERE code='CSE'),
   '2011-15', 2015,
   NULL, NULL, NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'IMPORTED', 'ALUMNI_ONLY',
   NULL),

  (14, NULL,
   'Ritu Patel', 'ritu.patel@yahoo.com', '0801BCA181006',
   'BCA', (SELECT id FROM departments WHERE code='BCA'),
   '2015-18', 2018,
   NULL, NULL, NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'IMPORTED', 'ALUMNI_ONLY',
   NULL),

  (15, NULL,
   'Lokesh Sharma', 'lokesh.sharma@gmail.com', '0801ECE141023',
   'B.Tech', (SELECT id FROM departments WHERE code='ECE'),
   '2010-14', 2014,
   NULL, NULL, NULL, NULL, NULL, 0,
   NULL, NULL, NULL,
   FALSE, FALSE, FALSE, FALSE,
   FALSE, 'INVITED', 'ALUMNI_ONLY',
   NULL);


-- =============================================================================
-- 4. SKILLS (normalized) + ALUMNI_SKILLS
-- =============================================================================
INSERT INTO skills (id, name) VALUES
  (1, 'Java'), (2, 'Spring Boot'), (3, 'React'), (4, 'MySQL'),
  (5, 'Python'), (6, 'VLSI Design'), (7, 'Embedded C'), (8, 'IoT'),
  (9, 'System Design'), (10, 'Product Management'), (11, 'SaaS'),
  (12, 'Machine Learning'), (13, 'Node.js'), (14, 'Docker'),
  (15, 'AWS'), (16, 'Mechanical Design'), (17, 'AutoCAD'), (18, 'CAD/CAM'),
  (19, 'Supply Chain'), (20, 'Data Analysis');

-- Rahul Sharma: Java, Spring Boot, React, MySQL, System Design, AWS
INSERT INTO alumni_skills (alumni_id, skill_id) VALUES
  (1, 1), (1, 2), (1, 3), (1, 4), (1, 9), (1, 15);

-- Priya Verma: VLSI Design, Embedded C, Python, IoT
INSERT INTO alumni_skills (alumni_id, skill_id) VALUES
  (2, 6), (2, 7), (2, 5), (2, 8);

-- Aniket Patil: Mechanical Design, AutoCAD, CAD/CAM, Python
INSERT INTO alumni_skills (alumni_id, skill_id) VALUES
  (3, 16), (3, 17), (3, 18), (3, 5);

-- Neha Joshi: Product Management, Data Analysis, Supply Chain
INSERT INTO alumni_skills (alumni_id, skill_id) VALUES
  (4, 10), (4, 20), (4, 19);

-- Vikram Singh: Java, Spring Boot, SaaS, Machine Learning, Docker, AWS, System Design
INSERT INTO alumni_skills (alumni_id, skill_id) VALUES
  (5, 1), (5, 2), (5, 11), (5, 12), (5, 14), (5, 15), (5, 9);


-- =============================================================================
-- 5. CAREER HISTORY
-- =============================================================================
INSERT INTO career_history_entries (alumni_id, company, job_title, start_date, end_date, description) VALUES
  -- Rahul Sharma
  (1, 'Infosys', 'Systems Engineer', '2018-07-01', '2020-06-30',
   'Java backend development for banking clients\nDesigned REST APIs consumed by 3 downstream systems'),
  (1, 'Tata Consultancy Services', 'Senior Software Engineer', '2020-07-01', NULL,
   'Tech lead on a React + Spring Boot insurance portal\nMentored 4 junior engineers in code review practices'),

  -- Priya Verma
  (2, 'Qualcomm India', 'Associate VLSI Engineer', '2019-08-01', '2021-07-31',
   'RTL design for 5G baseband modules\nFPGA prototyping and timing closure'),
  (2, 'Intel India', 'VLSI Design Engineer', '2021-08-01', NULL,
   'Physical design for Alder Lake mobile platform\nDRC/LVS sign-off coordination across 3 international sites'),

  -- Aniket Patil
  (3, 'TATA Motors', 'Graduate Engineer Trainee', '2020-07-01', '2021-06-30',
   'Chassis structural analysis for passenger cars\nCoordinated with supply chain for component validation'),
  (3, 'Mahindra & Mahindra', 'Product Design Engineer', '2021-07-01', NULL,
   'EV powertrain packaging and weight optimization\nDesigned 3D models in CATIA reducing part count by 12%'),

  -- Neha Joshi
  (4, 'Accenture', 'Business Analyst', '2019-06-01', '2021-03-31',
   'Requirements gathering and process mapping for BFSI clients\nDelivered three SAP-integrated projects on time'),
  (4, 'Flipkart', 'Product Manager', '2021-04-01', '2022-12-31',
   'Owned the seller onboarding funnel — grew monthly GMV by 18%'),
  (4, 'Flipkart', 'Senior Product Manager', '2023-01-01', NULL,
   'Leading seller growth squad across 5 product lines'),

  -- Vikram Singh
  (5, 'Persistent Systems', 'Software Engineer', '2017-07-01', '2018-12-31',
   'Microservices architecture for healthcare client\nImplemented OAuth2 SSO across 6 internal tools'),
  (5, 'Razorpay', 'Senior Software Engineer', '2019-01-01', '2021-05-31',
   'Core payments infrastructure team; reduced API latency by 40%\nBuilt internal event-streaming platform'),
  (5, 'StealthAI Labs', 'Co-founder & CTO', '2021-06-01', NULL,
   'Building an AI-powered legal contract review SaaS\nGrew to 3500 paying users in 18 months');


-- =============================================================================
-- 6. EDUCATION RECORDS (post-SVVV degrees)
-- =============================================================================
INSERT INTO education_records (alumni_id, degree, program, institution_name, start_year, end_year, cgpa) VALUES
  (1, 'B.Tech', 'Computer Science & Engineering', 'Shree Vaishnav Vidyapeeth Vishwavidyalay', 2014, 2018, 8.4),
  (2, 'B.Tech', 'Electronics & Communication Engineering', 'Shree Vaishnav Vidyapeeth Vishwavidyalay', 2015, 2019, 8.7),
  (3, 'B.Tech', 'Mechanical Engineering', 'Shree Vaishnav Vidyapeeth Vishwavidyalay', 2016, 2020, 7.9),
  (4, 'MBA', 'Business Administration', 'Shree Vaishnav Vidyapeeth Vishwavidyalay', 2017, 2019, 8.2),
  (5, 'B.Tech', 'Computer Science & Engineering', 'Shree Vaishnav Vidyapeeth Vishwavidyalay', 2013, 2017, 8.9);


-- =============================================================================
-- 7. CERTIFICATIONS & ACHIEVEMENTS
-- =============================================================================
INSERT INTO certifications (alumni_id, type, title, issuer, issue_date, credential_url) VALUES
  (1, 'CERTIFICATION', 'AWS Certified Developer – Associate', 'Amazon Web Services', '2021-04-15',
   'https://aws.amazon.com/verification'),
  (1, 'CERTIFICATION', 'Oracle Certified Professional Java SE 11', 'Oracle', '2020-09-10', NULL),
  (1, 'ACHIEVEMENT', 'Best Employee Q3 2023', 'Tata Consultancy Services', '2023-10-01', NULL),

  (2, 'CERTIFICATION', 'Cadence Allegro PCB Designer', 'Cadence Design Systems', '2022-03-20', NULL),
  (2, 'PUBLICATION', 'Low-Power RTL Design Techniques for 5G SoCs',
   'IEEE International Conference on VLSI Design', '2021-01-15',
   'https://ieeexplore.ieee.org/document/example'),

  (4, 'CERTIFICATION', 'Google Analytics Certified', 'Google', '2022-08-01',
   'https://skillshop.credential.net/example'),
  (4, 'ACHIEVEMENT', 'Product Leadership Award 2023', 'Flipkart', '2023-12-15', NULL),

  (5, 'ACHIEVEMENT', 'Top 10 — Startup India Innovate Challenge 2022',
   'DPIIT, Government of India', '2022-11-01', NULL),
  (5, 'CERTIFICATION', 'AWS Solutions Architect – Professional', 'Amazon Web Services', '2020-06-20', NULL);


-- =============================================================================
-- 8. PROJECTS
-- =============================================================================
INSERT INTO projects (alumni_id, name, description, technologies, project_url, github_url, start_date, end_date) VALUES
  (1, 'Insurance Claims Portal',
   'Full-stack portal for motor insurance claims processing at TCS — handles 15k claims/day.',
   '["Java 17","Spring Boot 3","React 18","MySQL","Redis","AWS S3"]',
   NULL, NULL, '2021-01-01', '2022-06-30'),

  (5, 'ContractAI',
   'AI-powered legal contract review SaaS — uses LLMs to flag risky clauses and suggest redlines.',
   '["Python","FastAPI","OpenAI GPT-4","React","PostgreSQL","Docker","Kubernetes"]',
   'https://contractai.io', 'https://github.com/vikramsingh-dev/contractai',
   '2021-06-01', NULL);


-- =============================================================================
-- 9. EVENTS
-- =============================================================================
INSERT INTO events (id, title, description, event_date, mode, location, capacity, status, created_by) VALUES
  -- Upcoming
  (1, 'Alumni Conclave 2025',
   'Annual alumni gathering with panel discussions, networking sessions, and a felicitation ceremony for distinguished alumni. All batches welcome.',
   '2025-01-25 10:00:00', 'ON_CAMPUS',
   'Main Auditorium, SVVV Campus, Indore',
   500, 'PUBLISHED', 1),

  (2, 'Tech Talk: Careers in VLSI & Semiconductor',
   'Priya Verma (Intel) and Karan Mehta (alumni, Qualcomm) discuss career paths in chip design — ideal for ECE students.',
   '2024-12-10 15:00:00', 'ONLINE',
   'Zoom (link shared after registration)',
   200, 'PUBLISHED', 1),

  (3, 'Start-up Masterclass: From Idea to Series A',
   'Vikram Singh (Co-founder, StealthAI Labs) walks through the 0-to-fundraise journey — product, team, pitch, mistakes.',
   '2025-02-14 17:30:00', 'ONLINE',
   'Google Meet (details after registration)',
   300, 'PUBLISHED', 1),

  -- Past
  (4, 'Resume & Interview Bootcamp — CSE Batch 2025',
   'Two-hour hands-on session with alumni reviewers. Students got live feedback on their CVs and mock technical interviews.',
   '2024-09-20 14:00:00', 'ON_CAMPUS',
   'Seminar Hall B, SVITS Building',
   80, 'PUBLISHED', 1),

  -- Draft
  (5, 'Women in Tech Panel — IWD 2025',
   'International Women''s Day panel featuring SVVV alumnae from TCS, Intel, Flipkart and academia.',
   '2025-03-08 11:00:00', 'ON_CAMPUS',
   'Conference Hall, SVVV',
   150, 'DRAFT', 1);


-- =============================================================================
-- 10. EVENT REGISTRATIONS
-- =============================================================================
INSERT INTO event_registrations (event_id, user_id, status) VALUES
  -- Alumni Conclave: all active alumni + students registered
  (1, 2, 'REGISTERED'), (1, 3, 'REGISTERED'), (1, 4, 'REGISTERED'),
  (1, 5, 'REGISTERED'), (1, 6, 'REGISTERED'),
  (1, 8, 'REGISTERED'), (1, 9, 'REGISTERED'), (1, 10, 'REGISTERED'),

  -- Tech Talk VLSI: ECE student + relevant alumni
  (2, 9, 'REGISTERED'), (2, 3, 'REGISTERED'),

  -- Startup Masterclass: students + a couple of alumni
  (3, 8, 'REGISTERED'), (3, 10, 'REGISTERED'), (3, 5, 'REGISTERED'),

  -- Resume Bootcamp (past): all three students
  (4, 8, 'REGISTERED'), (4, 9, 'REGISTERED'), (4, 10, 'REGISTERED');


-- =============================================================================
-- 11. MENTORSHIP REQUESTS
-- =============================================================================
INSERT INTO mentorship_requests (id, student_id, alumni_id, message, status, requested_at, responded_at) VALUES
  -- Active mentorship: Arjun → Rahul (accepted)
  (1, 8, 1,
   'Hi Rahul bhai, I am a final-year CSE student preparing for software engineering interviews at product companies. Would love to be mentored on DSA and system design by you.',
   'ACCEPTED', '2024-10-01 09:00:00', '2024-10-03 11:00:00'),

  -- Active mentorship: Arjun → Vikram (accepted)
  (2, 8, 5,
   'Sir, I am working on a startup idea and would really value your perspective on go-to-market strategy and product prioritization.',
   'ACCEPTED', '2024-10-05 14:00:00', '2024-10-07 10:30:00'),

  -- Pending: Kavya → Priya
  (3, 9, 2,
   'Hi Priya didi, I am in ECE final year and very interested in VLSI/semiconductor careers. Would be grateful for your guidance on the hiring process at Intel.',
   'PENDING', '2024-11-01 16:00:00', NULL),

  -- Pending: Rohan → Neha
  (4, 10, 4,
   'Hi Neha ma''am, I have been following your product management journey. I would love to understand how to transition from engineering to PM after college.',
   'PENDING', '2024-11-05 12:00:00', NULL),

  -- Rejected (old): Rohan → Rahul
  (5, 10, 1,
   'Hi, would like mentorship on backend development.',
   'REJECTED', '2024-08-15 10:00:00', '2024-08-18 08:00:00'),

  -- Completed: Arjun → Neha (earlier engagement)
  (6, 8, 4,
   'Wanted help understanding product management career paths.',
   'COMPLETED', '2024-06-01 10:00:00', '2024-06-05 09:00:00');


-- =============================================================================
-- 12. JOB POSTINGS
-- =============================================================================
INSERT INTO job_postings (
  id, posted_by_user_id, title, company, location, job_type, description,
  status, posted_at, expires_at, moderated_by, moderated_at
) VALUES
  -- Approved jobs (live)
  (1, 2, 'Software Engineer – Java Spring Boot',
   'Tata Consultancy Services', 'Pune / Hyderabad (Hybrid)',
   'FULL_TIME',
   'TCS is hiring entry-level software engineers for our Banking & Financial Services practice.\n\nResponsibilities:\n- Design and develop REST APIs using Java 17 and Spring Boot 3\n- Write unit and integration tests (JUnit 5, Testcontainers)\n- Collaborate with business analysts and offshore QA teams\n\nRequirements:\n- B.Tech/BCA in CS/IT with 60% aggregate\n- Strong fundamentals in Java, OOP, and SQL\n- Good communication skills\n\nApply via the portal or reach out to me directly.',
   'APPROVED', '2024-10-15 10:00:00', '2025-01-15', 1, '2024-10-15 10:30:00'),

  (2, 6, 'Early-Stage SaaS Engineer (Full Stack)',
   'StealthAI Labs', 'Bengaluru / Remote',
   'FULL_TIME',
   'We are a seed-funded LegalTech startup looking for engineers who want to build things from scratch.\n\nStack: React, Node.js, Python (FastAPI), PostgreSQL, Redis, AWS.\n\nWhat you will do:\n- Own features end-to-end from design to deployment\n- Work directly with the founders\n- Get real equity, not a token amount\n\nIdeal fit: 0–2 years experience or a final-year student ready to join in Jan 2025.',
   'APPROVED', '2024-11-01 15:00:00', '2025-02-28', 1, '2024-11-01 16:00:00'),

  (3, 5, 'Product Management Intern (6 months)',
   'Flipkart', 'Bengaluru',
   'INTERNSHIP',
   'Join Flipkart''s Seller Growth squad as a PM intern.\n\nYou will:\n- Write PRDs and user stories for A/B experiments\n- Analyse funnel metrics with SQL and Looker\n- Present weekly to the team''s product director\n\nRequirements: MBA students or final-year Engineering students with strong analytical skills. Prior internship a plus.',
   'APPROVED', '2024-11-10 09:00:00', '2025-01-31', 1, '2024-11-10 10:00:00'),

  -- Pending moderation
  (4, 3, 'Hardware Verification Intern',
   'Qualcomm India', 'Hyderabad',
   'INTERNSHIP',
   'Qualcomm is looking for motivated ECE students for a 6-month hardware verification internship. Knowledge of SystemVerilog and UVM preferred.',
   'PENDING', '2024-11-12 14:00:00', '2025-03-31', NULL, NULL),

  -- Rejected
  (5, 9, 'Data Science Intern',
   'Analytics Co.', 'Remote',
   'INTERNSHIP',
   'Looking for interns for data analysis work.',
   'REJECTED', '2024-10-20 12:00:00', '2024-12-31', 1, '2024-10-21 09:00:00');


-- =============================================================================
-- 13. JOB APPLICATIONS
-- =============================================================================
INSERT INTO job_applications (job_id, applicant_id, status, applied_at) VALUES
  (1, 8, 'SUBMITTED', '2024-10-20 12:00:00'),
  (1, 10, 'REVIEWED', '2024-10-22 14:00:00'),
  (2, 8, 'SUBMITTED', '2024-11-05 10:00:00'),
  (2, 10, 'SUBMITTED', '2024-11-06 11:00:00'),
  (3, 8, 'SHORTLISTED', '2024-11-12 09:00:00'),
  (3, 10, 'SUBMITTED', '2024-11-13 10:30:00');


-- =============================================================================
-- 14. ANNOUNCEMENTS
-- =============================================================================
INSERT INTO announcements (id, title, content, target_audience, priority, publish_date, status, created_by) VALUES
  (1, 'AlumniConnect Platform Launch',
   'We are pleased to announce the official launch of AlumniConnect — SVVV''s centralized alumni engagement platform.\n\nAll alumni can now claim their profiles, connect with current students, browse job opportunities, and register for events at one place.\n\nVisit alumni.svvv.edu.in to get started. Alumni who have not yet claimed their profile will receive an invitation email shortly.',
   'ALL', 'HIGH', '2024-10-01', 'PUBLISHED', 1),

  (2, 'Alumni Conclave 2025 — Registrations Open',
   'The Annual Alumni Conclave will be held on 25 January 2025 at SVVV''s Main Auditorium.\n\nHighlights:\n- Keynote by distinguished alumni\n- Department networking sessions\n- Awards for Exceptional Alumni\n- Cultural programme\n\nAll alumni and current students are invited. Register through the Events section before 10 January 2025.',
   'ALL', 'HIGH', '2024-11-01', 'PUBLISHED', 1),

  (3, 'Placement Drive — TCS and Infosys (December 2024)',
   'TCS and Infosys will be conducting campus placement drives for B.Tech and MCA final-year students in December 2024.\n\nEligibility: 60% aggregate throughout academics, no active backlogs.\n\nRegistrations close on 20 November 2024 through the Training & Placement portal.',
   'STUDENTS', 'HIGH', '2024-11-08', 'PUBLISHED', 1),

  (4, 'Call for Mentors — Student Mentorship Programme 2025',
   'Dear Alumni,\n\nThe SVVV Training & Placement Cell is looking for alumni volunteers for the 2025 Student Mentorship Programme.\n\nAs a mentor you commit to:\n- 1–2 hours per month (video calls or campus visits)\n- Reviewing a student''s resume and mock interview\n\nInterested? Mark yourself "Open to Mentoring" on your AlumniConnect profile, or write to placement@svvv.edu.in.',
   'ALUMNI', 'MEDIUM', '2024-11-15', 'PUBLISHED', 1),

  (5, 'Semester End Examinations Schedule — November 2024',
   'The Semester End Examinations for all programmes will commence from 25 November 2024.\n\nDetailed timetables have been uploaded to the ERP portal. Students are advised to download hall tickets by 20 November 2024.',
   'STUDENTS', 'MEDIUM', '2024-11-10', 'PUBLISHED', 1);


-- =============================================================================
-- 15. CONVERSATIONS & MESSAGES (chat demo)
-- =============================================================================
-- Direct conversation: Arjun ↔ Rahul (mentorship follow-up)
INSERT INTO conversations (id, type, direct_key) VALUES
  (1, 'DIRECT', '2:8');

INSERT INTO conversation_participants (conversation_id, user_id) VALUES
  (1, 2), (1, 8);

INSERT INTO messages (conversation_id, sender_id, content, sent_at) VALUES
  (1, 2, 'Hi Arjun! Good to connect. What topics do you want to start with for DSA prep?', '2024-10-03 11:30:00'),
  (1, 8, 'Hello Rahul bhai! I am currently doing LeetCode medium problems on trees and graphs. Would love to cover system design basics too.', '2024-10-03 12:00:00'),
  (1, 2, 'Perfect. I suggest completing Striver''s SDE sheet first — all the important patterns are there. For system design, start with Alex Xu''s System Design Interview Vol. 1. Let''s plan a call this weekend?', '2024-10-03 12:15:00'),
  (1, 8, 'Sounds great! Saturday 5pm works for me.', '2024-10-03 12:20:00'),

-- Direct conversation: Arjun ↔ Vikram
(2, 'DIRECT', '6:8');

INSERT INTO conversation_participants (conversation_id, user_id) VALUES
  (2, 6), (2, 8);

INSERT INTO messages (conversation_id, sender_id, content, sent_at) VALUES
  (2, 6, 'Hey Arjun, saw your mentorship request. Happy to help on the startup side. What is your idea?', '2024-10-07 10:45:00'),
  (2, 8, 'Thank you sir! I want to build a SaaS for college societies to manage events and finances. Not sure how to validate the idea.', '2024-10-07 11:00:00'),
  (2, 6, 'Good space. Step 1 — talk to 20 society heads before writing a single line of code. Does the pain exist? Can they pay? DM me your problem statement doc and I will give feedback.', '2024-10-07 11:10:00');

-- Department group chat: CSE
INSERT INTO conversations (id, type, direct_key, department_id)
  VALUES (3, 'DEPARTMENT', NULL, (SELECT id FROM departments WHERE code='CSE'));

INSERT INTO conversation_participants (conversation_id, user_id) VALUES
  (3, 2), (3, 6), (3, 7), (3, 8), (3, 10);

INSERT INTO messages (conversation_id, sender_id, content, sent_at) VALUES
  (3, 1, 'Welcome to the CSE Department Group! Alumni and students from CSE can connect, share opportunities, and collaborate here.', '2024-10-01 09:00:00'),
  (3, 2, 'Great to be here. Happy to answer questions about software engineering careers and TCS campus hiring.', '2024-10-01 09:15:00'),
  (3, 6, 'Same here — feel free to ask about startups, system design, or how to crack product-company interviews.', '2024-10-01 09:30:00'),
  (3, 8, 'This is awesome! Really looking forward to learning from the seniors.', '2024-10-01 10:00:00');


-- =============================================================================
-- 16. POSTS (social feed)
-- =============================================================================
INSERT INTO posts (id, author_id, content, visibility, created_at) VALUES
  (1, 2,
   'Thrilled to share that I just cleared the AWS Certified Solutions Architect – Professional exam on first attempt! 🎉\n\nIf any SVVV juniors are preparing for AWS certifications, feel free to reach out — happy to share my study plan and resources.\n\n#AWS #CloudComputing #SVVV',
   'PUBLIC', '2024-10-05 18:00:00'),

  (2, 6,
   'Milestone alert: ContractAI crossed 3,500 paying customers this week — 18 months after we launched the beta! 🚀\n\nStarting from a dorm room to a team of 11 people, this journey has been insane. For anyone thinking of starting up — start small, talk to customers obsessively, and do not wait for the "perfect" product.\n\nAlways happy to mentor SVVV students who want to take the startup path.',
   'PUBLIC', '2024-10-18 20:00:00'),

  (3, 5,
   'If you are a CSE/IT final-year student targeting product companies in 2025, this is the non-obvious prep advice I wish I had:\n\n1. System design > LeetCode in terms of offer-differentiation at mid-size companies.\n2. Build one real project you can talk about for 20 minutes without notes.\n3. Mock interviews matter more than solo practice — find a partner.\n\nFeel free to connect. Happy to do a mock system design call with interested students from SVVV.',
   'PUBLIC', '2024-11-01 12:00:00'),

  (4, 3,
   'Just joined a 3-day CATIA V5 advanced modelling workshop at Mahindra''s Chakan plant. The gap between what we learn in college and what industry actually uses for complex assemblies is significant — but the fundamentals from SVVV ME lab definitely helped. Keep practicing your CAD skills, juniors!',
   'PUBLIC', '2024-10-25 19:30:00'),

  (5, 4,
   'Flipkart Seller Growth PM internship applications are open! (See the Jobs section)\n\nWhat we look for beyond the JD: structured problem-solving, curiosity about why metrics move, and the ability to explain a complex idea simply.\n\nDrop me a message if you want a candid take on whether PM is the right path for you.',
   'PUBLIC', '2024-11-10 16:00:00'),

  (6, 8,
   'Just had my first mentorship call with Rahul bhai. In 45 minutes he mapped out my entire 3-month interview prep plan — structured, prioritized, and actually doable. This platform is genuinely game-changing for students like us. 🙏',
   'PUBLIC', '2024-10-04 17:00:00');


-- =============================================================================
-- 17. POST LIKES & COMMENTS
-- =============================================================================
INSERT INTO post_likes (post_id, user_id, created_at) VALUES
  (1, 8, '2024-10-05 18:30:00'),
  (1, 9, '2024-10-05 19:00:00'),
  (1, 10, '2024-10-05 19:15:00'),
  (1, 5, '2024-10-06 08:00:00'),
  (2, 8, '2024-10-18 21:00:00'),
  (2, 9, '2024-10-18 21:30:00'),
  (2, 10, '2024-10-19 07:00:00'),
  (3, 8, '2024-11-01 13:00:00'),
  (3, 10, '2024-11-01 14:00:00'),
  (5, 8, '2024-11-10 17:00:00'),
  (5, 10, '2024-11-10 17:30:00'),
  (6, 2, '2024-10-04 18:00:00'),
  (6, 5, '2024-10-04 18:30:00');

INSERT INTO post_comments (post_id, author_id, content, created_at) VALUES
  (1, 8,
   'Congratulations Rahul bhai! 🙌 Could you share which practice resources you used for the SAA-C03 exam?',
   '2024-10-05 18:45:00'),
  (1, 2,
   'Sure Arjun! I used Stephane Maarek''s Udemy course + practice exams by Jon Bonso. Give yourself 6–8 weeks if you are working full time.',
   '2024-10-05 19:00:00'),
  (2, 8,
   'This is so inspiring sir! What was the hardest part of the first 6 months?',
   '2024-10-18 21:15:00'),
  (2, 6,
   'Honestly — figuring out pricing. We undercharged for too long. Lesson: charge earlier than feels comfortable.',
   '2024-10-18 21:45:00'),
  (3, 10,
   'The mock interviews point hit home. Any advice on finding a practice partner outside your college circle?',
   '2024-11-01 15:00:00'),
  (3, 5,
   'Post here! The CSE group has students prepping for the same companies — pair up. Also try Pramp for structured online mocks.',
   '2024-11-01 15:30:00');


-- =============================================================================
-- 18. NOTIFICATIONS
-- =============================================================================
INSERT INTO notifications (user_id, category, message, is_read, created_at) VALUES
  -- Arjun
  (8, 'MENTORSHIP', 'Rahul Sharma accepted your mentorship request.', TRUE,  '2024-10-03 11:00:00'),
  (8, 'MENTORSHIP', 'Vikram Singh accepted your mentorship request.', TRUE,  '2024-10-07 10:30:00'),
  (8, 'EVENT',      'You are registered for Alumni Conclave 2025.',    TRUE,  '2024-11-01 09:00:00'),
  (8, 'JOB',        'Your application for "Software Engineer – Java Spring Boot" at TCS has been received.', FALSE, '2024-10-20 12:05:00'),
  (8, 'POST',       'Rahul Sharma commented on your post.', FALSE, '2024-10-04 18:00:00'),

  -- Kavya
  (9, 'EVENT',      'You are registered for the Tech Talk: Careers in VLSI & Semiconductor.', TRUE, '2024-11-02 10:00:00'),
  (9, 'ANNOUNCEMENT','New announcement: Placement Drive — TCS and Infosys (December 2024).', FALSE, '2024-11-08 09:00:00'),

  -- Rohan
  (10, 'MENTORSHIP', 'Your mentorship request to Neha Joshi is pending review.', FALSE, '2024-11-05 12:05:00'),
  (10, 'JOB',        'Your application for "Product Management Intern" at Flipkart has been shortlisted!', FALSE, '2024-11-14 10:00:00'),

  -- Rahul (alumni — pending mentorship from students)
  (2, 'MENTORSHIP', 'Arjun Meena sent you a mentorship request.', TRUE, '2024-10-01 09:05:00'),

  -- Priya (alumni — pending mentorship)
  (3, 'MENTORSHIP', 'Kavya Patel sent you a mentorship request.', FALSE, '2024-11-01 16:05:00'),

  -- Neha (alumni — pending mentorship)
  (5, 'MENTORSHIP', 'Rohan Dubey sent you a mentorship request.', FALSE, '2024-11-05 12:05:00');


-- =============================================================================
-- 19. AUDIT LOGS
-- =============================================================================
INSERT INTO audit_logs (admin_id, action, details, created_at) VALUES
  (1, 'IMPORT_ALUMNI',
   'Batch import: 10 records from svvv_alumni_2015_2021.csv — 9 successful, 1 duplicate',
   '2024-09-30 14:00:00'),
  (1, 'INVITE_ALUMNI',
   'Invitation sent to alumni_id=7 (Suresh Gupta)',
   '2024-10-02 09:30:00'),
  (1, 'INVITE_ALUMNI',
   'Invitation sent to alumni_id=11 (Harish Tomar)',
   '2024-10-02 09:31:00'),
  (1, 'INVITE_ALUMNI',
   'Invitation sent to alumni_id=15 (Lokesh Sharma)',
   '2024-10-02 09:32:00'),
  (1, 'CREATE_EVENT',
   'Event "Alumni Conclave 2025" (id=1) created and published',
   '2024-10-05 11:00:00'),
  (1, 'CREATE_EVENT',
   'Event "Tech Talk: Careers in VLSI & Semiconductor" (id=2) created and published',
   '2024-10-06 09:00:00'),
  (1, 'APPROVE_JOB',
   'Job posting id=1 approved — "Software Engineer – Java Spring Boot" by Rahul Sharma',
   '2024-10-15 10:30:00'),
  (1, 'APPROVE_JOB',
   'Job posting id=2 approved — "Early-Stage SaaS Engineer" by Vikram Singh',
   '2024-11-01 16:00:00'),
  (1, 'APPROVE_JOB',
   'Job posting id=3 approved — "PM Intern" by Neha Joshi',
   '2024-11-10 10:00:00'),
  (1, 'REJECT_JOB',
   'Job posting id=5 rejected — insufficient company details',
   '2024-10-21 09:00:00'),
  (1, 'VERIFY_ALUMNI',
   'alumni_id=1 (Rahul Sharma) marked as verified',
   '2024-10-10 10:00:00'),
  (1, 'VERIFY_ALUMNI',
   'alumni_id=2 (Priya Verma) marked as verified',
   '2024-10-10 10:05:00'),
  (1, 'VERIFY_ALUMNI',
   'alumni_id=4 (Neha Joshi) marked as verified',
   '2024-10-10 10:10:00'),
  (1, 'VERIFY_ALUMNI',
   'alumni_id=5 (Vikram Singh) marked as verified',
   '2024-10-10 10:12:00');


-- =============================================================================
-- 20. IMPORT BATCH (example of a completed import)
-- =============================================================================
INSERT INTO alumni_import_batches (
  id, admin_id, original_filename, column_mapping,
  staged_content, status,
  total_rows, successful_rows, failed_rows, duplicate_rows,
  created_at, confirmed_at
) VALUES (
  1, 1, 'svvv_alumni_2015_2021.csv',
  '{"0":"full_name","1":"email","2":"enrollment_number","3":"degree","4":"department","5":"batch","6":"graduation_year"}',
  NULL,                   -- cleared after confirmation
  'CONFIRMED',
  10, 9, 0, 1,
  '2024-09-30 13:45:00', '2024-09-30 14:00:00'
);

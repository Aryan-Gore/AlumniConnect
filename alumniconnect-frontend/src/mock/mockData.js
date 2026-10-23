// Placeholder data only. Every field here mirrors the shape the real API
// modules in src/api/ will return — once Spring Boot is live, each screen
// swaps its `mock*` import for the matching `*Api` call with no markup
// changes needed.

export const mockAlumni = [
  {
    id: 'al_1001',
    fullName: 'Ritika Anand',
    enrollmentNumber: 'CS2019014',
    department: 'Computer Science',
    degree: 'B.Tech',
    batch: '2019–2023',
    graduationYear: 2023,
    currentCompany: 'Razorpay',
    jobTitle: 'Backend Engineer II',
    industry: 'Fintech',
    location: 'Bengaluru, IN',
    skills: ['Java', 'Spring Boot', 'Kafka'],
    status: 'ACTIVE',
    openToMentoring: true,
  },
  {
    id: 'al_1002',
    fullName: 'Karan Mehta',
    enrollmentNumber: 'CS2018041',
    department: 'Computer Science',
    degree: 'B.Tech',
    batch: '2018–2022',
    graduationYear: 2022,
    currentCompany: 'Deloitte',
    jobTitle: 'Senior Consultant',
    industry: 'Consulting',
    location: 'Mumbai, IN',
    skills: ['Data Analytics', 'Power BI', 'SQL'],
    status: 'ACTIVE',
    openToMentoring: true,
  },
  {
    id: 'al_1003',
    fullName: 'Simran Kaur',
    enrollmentNumber: 'EC2020009',
    department: 'Electronics & Comm.',
    degree: 'B.Tech',
    batch: '2020–2024',
    graduationYear: 2024,
    currentCompany: 'Qualcomm',
    jobTitle: 'RF Design Engineer',
    industry: 'Semiconductors',
    location: 'Hyderabad, IN',
    skills: ['VLSI', 'RF Design', 'MATLAB'],
    status: 'ACTIVE',
    openToMentoring: false,
  },
  {
    id: 'al_1004',
    fullName: 'Aditya Rao',
    enrollmentNumber: 'CS2017022',
    department: 'Computer Science',
    degree: 'B.Tech',
    batch: '2017–2021',
    graduationYear: 2021,
    currentCompany: 'Google',
    jobTitle: 'Software Engineer III',
    industry: 'Technology',
    location: 'Singapore',
    skills: ['Go', 'Distributed Systems', 'Kubernetes'],
    status: 'ACTIVE',
    openToMentoring: true,
  },
  {
    id: 'al_1005',
    fullName: 'Neha Joshi',
    enrollmentNumber: 'ME2019017',
    department: 'Mechanical',
    degree: 'B.Tech',
    batch: '2019–2023',
    graduationYear: 2023,
    currentCompany: 'Tata Motors',
    jobTitle: 'Design Engineer',
    industry: 'Automotive',
    location: 'Pune, IN',
    skills: ['CATIA', 'GD&T', 'Product Design'],
    status: 'CLAIMED',
    openToMentoring: false,
  },
  {
    id: 'al_1006',
    fullName: 'Vivaan Shah',
    enrollmentNumber: 'CS2016005',
    department: 'Computer Science',
    degree: 'B.Tech',
    batch: '2016–2020',
    graduationYear: 2020,
    currentCompany: 'Independent',
    jobTitle: 'Founder',
    industry: 'Technology',
    location: 'Jabalpur, IN',
    skills: ['Product Strategy', 'Fundraising', 'React'],
    status: 'ACTIVE',
    openToMentoring: true,
  },
]

export const mockStudentDashboard = {
  featuredAlumni: mockAlumni.filter((a) => a.openToMentoring).slice(0, 3),
  upcomingEvents: [
    { id: 'ev_1', title: 'Annual Alumni Meet 2026', date: '2026-10-18', mode: 'On campus' },
    { id: 'ev_2', title: 'Careers in Product Management', date: '2026-09-12', mode: 'Online' },
  ],
  availableJobs: [
    { id: 'job_1', title: 'SDE Intern', company: 'Razorpay', postedBy: 'Ritika Anand' },
    { id: 'job_2', title: 'Analyst — Data', company: 'Deloitte', postedBy: 'Karan Mehta' },
  ],
  mentorshipStatus: { active: 1, pending: 1 },
  notifications: [
    { id: 'n_1', text: 'Your mentorship request to Aditya Rao is pending.', read: false, timestamp: '2026-08-25T10:00:00Z' },
    { id: 'n_2', text: 'New event: Annual Alumni Meet 2026.', read: false, timestamp: '2026-08-24T09:00:00Z' },
    { id: 'n_3', text: 'Registration confirmed for Careers in Product Management.', read: true, timestamp: '2026-08-20T09:00:00Z' },
  ],
}

export const mockAdminSummary = {
  totalAlumni: 2480,
  activeAlumni: 1962,
  claimedProfiles: 2110,
  unclaimedProfiles: 370,
  upcomingEvents: 3,
  activeMentorships: 214,
  publishedJobs: 41,
  pendingJobApprovals: 6,
  byDepartment: [
    { label: 'Computer Science', value: 940 },
    { label: 'Electronics & Comm.', value: 512 },
    { label: 'Mechanical', value: 486 },
    { label: 'Civil', value: 322 },
    { label: 'Electrical', value: 220 },
  ],
  byGraduationYear: [
    { label: '2021', value: 410 },
    { label: '2022', value: 452 },
    { label: '2023', value: 498 },
    { label: '2024', value: 470 },
    { label: '2025', value: 405 },
    { label: '2026', value: 245 },
  ],
}

// ---------------------------------------------------------------------------
// Mentorship
// ---------------------------------------------------------------------------
export const mockMentorshipSent = [
  {
    id: 'mr_1',
    alumniName: 'Aditya Rao',
    alumniTitle: 'Software Engineer III',
    alumniCompany: 'Google',
    message: "I'm exploring distributed systems roles and would love guidance on how to prepare.",
    status: 'PENDING',
    requestedOn: '2026-08-22',
  },
  {
    id: 'mr_2',
    alumniName: 'Ritika Anand',
    alumniTitle: 'Backend Engineer II',
    alumniCompany: 'Razorpay',
    message: 'Would appreciate advice on breaking into fintech backend roles.',
    status: 'ACCEPTED',
    requestedOn: '2026-08-10',
  },
  {
    id: 'mr_3',
    alumniName: 'Vivaan Shah',
    alumniTitle: 'Founder',
    alumniCompany: 'Independent',
    message: 'Curious what it takes to start a company right after graduating.',
    status: 'COMPLETED',
    requestedOn: '2026-06-02',
  },
]

export const mockMentorshipReceived = [
  {
    id: 'mr_101',
    studentName: 'Priyansh Tiwari',
    department: 'Computer Science',
    batch: '2022–2026',
    message: 'Could you share how you prepared for backend interviews at Razorpay?',
    status: 'PENDING',
    requestedOn: '2026-08-24',
  },
  {
    id: 'mr_102',
    studentName: 'Ishita Verma',
    department: 'Computer Science',
    batch: '2023–2027',
    message: "I'm building a Kafka-based project and would value 20 minutes of your time.",
    status: 'PENDING',
    requestedOn: '2026-08-20',
  },
  {
    id: 'mr_103',
    studentName: 'Devansh Patil',
    department: 'Computer Science',
    batch: '2021–2025',
    message: 'Would love mentorship as I prepare for full-time SDE applications.',
    status: 'ACCEPTED',
    requestedOn: '2026-07-30',
  },
]

// ---------------------------------------------------------------------------
// Events
// ---------------------------------------------------------------------------
export const mockEvents = [
  {
    id: 'ev_1',
    title: 'Annual Alumni Meet 2026',
    description: 'The flagship yearly reunion — campus tour, department meetups, and dinner.',
    date: '2026-10-18',
    mode: 'On campus',
    location: 'Main Auditorium',
    capacity: 400,
    registeredCount: 268,
    isRegistered: true,
  },
  {
    id: 'ev_2',
    title: 'Careers in Product Management',
    description: 'A panel of alumni product managers on breaking into PM roles from engineering.',
    date: '2026-09-12',
    mode: 'Online',
    location: 'Zoom',
    capacity: 200,
    registeredCount: 142,
    isRegistered: true,
  },
  {
    id: 'ev_3',
    title: 'Intro to System Design Workshop',
    description: 'Hands-on workshop covering scalability fundamentals, led by alumni engineers.',
    date: '2026-09-28',
    mode: 'Online',
    location: 'Zoom',
    capacity: 150,
    registeredCount: 98,
    isRegistered: false,
  },
  {
    id: 'ev_4',
    title: 'Fintech Networking Evening',
    description: 'Informal networking with alumni working across banking, payments, and lending.',
    date: '2026-11-05',
    mode: 'On campus',
    location: 'Placement Cell Lawn',
    capacity: 120,
    registeredCount: 41,
    isRegistered: false,
  },
]

// ---------------------------------------------------------------------------
// Jobs
// ---------------------------------------------------------------------------
export const mockJobs = [
  {
    id: 'job_1',
    title: 'SDE Intern',
    company: 'Razorpay',
    location: 'Bengaluru, IN',
    type: 'Internship',
    postedBy: 'Ritika Anand',
    postedOn: '2026-08-18',
    status: 'APPROVED',
    description: 'Work on payment reconciliation systems handling high-throughput transactions.',
    hasApplied: true,
  },
  {
    id: 'job_2',
    title: 'Analyst — Data',
    company: 'Deloitte',
    location: 'Mumbai, IN',
    type: 'Full-time',
    postedBy: 'Karan Mehta',
    postedOn: '2026-08-15',
    status: 'APPROVED',
    description: 'Support client engagements with dashboards and statistical analysis.',
    hasApplied: false,
  },
  {
    id: 'job_3',
    title: 'New Grad SWE',
    company: 'Google',
    location: 'Singapore',
    type: 'Full-time',
    postedBy: 'Aditya Rao',
    postedOn: '2026-08-20',
    status: 'APPROVED',
    description: 'Distributed systems team, focused on infrastructure reliability.',
    hasApplied: false,
  },
  {
    id: 'job_4',
    title: 'Founding Engineer',
    company: 'Independent (Stealth)',
    location: 'Remote',
    type: 'Full-time',
    postedBy: 'Vivaan Shah',
    postedOn: '2026-08-25',
    status: 'PENDING',
    description: 'Early engineer role at a pre-seed startup — full-stack, high ownership.',
    hasApplied: false,
  },
]

// ---------------------------------------------------------------------------
// Announcements
// ---------------------------------------------------------------------------
export const mockAnnouncements = [
  {
    id: 'an_1',
    title: 'Annual Alumni Meet — registrations open',
    content: 'Registrations for the 2026 Annual Alumni Meet are now open. Early registration closes September 30th.',
    targetAudience: 'All users',
    priority: 'HIGH',
    publishDate: '2026-08-20',
    status: 'PUBLISHED',
  },
  {
    id: 'an_2',
    title: 'CSE Department batch reunion planning',
    content: 'CSE alumni from 2018–2022 batches — we are coordinating a department-specific meetup alongside the main event.',
    targetAudience: 'Alumni · CSE',
    priority: 'MEDIUM',
    publishDate: '2026-08-15',
    status: 'PUBLISHED',
  },
  {
    id: 'an_3',
    title: 'Placement cell: resume review drive',
    content: 'Final-year students can book a 1:1 resume review slot with alumni volunteers next week.',
    targetAudience: 'Students',
    priority: 'MEDIUM',
    publishDate: '2026-08-12',
    status: 'PUBLISHED',
  },
  {
    id: 'an_4',
    title: 'Platform maintenance window',
    content: 'AlumniConnect will be briefly unavailable on Sunday 2 AM–4 AM IST for scheduled maintenance.',
    targetAudience: 'All users',
    priority: 'LOW',
    publishDate: '2026-08-09',
    status: 'PUBLISHED',
  },
]

// ---------------------------------------------------------------------------
// Notifications (full history — dashboard shows a trimmed preview of this)
// ---------------------------------------------------------------------------
export const mockNotifications = [
  { id: 'n_1', text: 'Your mentorship request to Aditya Rao is pending.', category: 'Mentorship', read: false, timestamp: '2026-08-25T10:00:00Z' },
  { id: 'n_2', text: 'New event: Annual Alumni Meet 2026.', category: 'Event', read: false, timestamp: '2026-08-24T09:00:00Z' },
  { id: 'n_3', text: 'Devansh Patil accepted your mentorship request.', category: 'Mentorship', read: false, timestamp: '2026-08-23T14:20:00Z' },
  { id: 'n_4', text: 'Registration confirmed for Careers in Product Management.', category: 'Event', read: true, timestamp: '2026-08-20T09:00:00Z' },
  { id: 'n_5', text: 'New announcement: Placement cell resume review drive.', category: 'Announcement', read: true, timestamp: '2026-08-12T08:30:00Z' },
  { id: 'n_6', text: 'Your job application to Razorpay was received.', category: 'Jobs', read: true, timestamp: '2026-08-11T16:45:00Z' },
  { id: 'n_7', text: 'Your alumni profile was successfully claimed.', category: 'Account', read: true, timestamp: '2026-07-02T11:00:00Z' },
]

// ---------------------------------------------------------------------------
// Deeper analytics (Admin > Analytics, beyond the dashboard overview)
// ---------------------------------------------------------------------------
export const mockAnalytics = {
  byIndustry: [
    { label: 'Technology', value: 812 },
    { label: 'Fintech', value: 344 },
    { label: 'Consulting', value: 298 },
    { label: 'Automotive', value: 220 },
    { label: 'Semiconductors', value: 176 },
    { label: 'Other', value: 630 },
  ],
  byLocation: [
    { label: 'Bengaluru', value: 588 },
    { label: 'Mumbai', value: 412 },
    { label: 'Pune', value: 305 },
    { label: 'Hyderabad', value: 274 },
    { label: 'Delhi NCR', value: 240 },
    { label: 'Abroad', value: 316 },
  ],
  monthlyActiveUsers: [
    { label: 'Mar', value: 640 },
    { label: 'Apr', value: 705 },
    { label: 'May', value: 690 },
    { label: 'Jun', value: 812 },
    { label: 'Jul', value: 890 },
    { label: 'Aug', value: 965 },
  ],
}

// ---------------------------------------------------------------------------
// Audit log (Admin > Audit Log)
// ---------------------------------------------------------------------------
export const mockAuditLog = [
  { id: 'log_1', admin: 'admin@svvv.edu.in', action: 'IMPORT_ALUMNI', details: '520 records · 510 succeeded, 10 failed', timestamp: '2026-08-25T11:20:00Z' },
  { id: 'log_2', admin: 'admin@svvv.edu.in', action: 'INVITE_ALUMNI', details: 'Neha Joshi (ME2019017)', timestamp: '2026-08-24T15:05:00Z' },
  { id: 'log_3', admin: 'placement@svvv.edu.in', action: 'APPROVE_JOB', details: 'SDE Intern — Razorpay', timestamp: '2026-08-24T10:40:00Z' },
  { id: 'log_4', admin: 'admin@svvv.edu.in', action: 'CREATE_EVENT', details: 'Fintech Networking Evening', timestamp: '2026-08-22T09:15:00Z' },
  { id: 'log_5', admin: 'admin@svvv.edu.in', action: 'DEACTIVATE_ALUMNI', details: 'Sample record — duplicate cleanup', timestamp: '2026-08-20T13:50:00Z' },
  { id: 'log_6', admin: 'placement@svvv.edu.in', action: 'REJECT_JOB', details: 'Unverified listing — missing company details', timestamp: '2026-08-19T17:30:00Z' },
  { id: 'log_7', admin: 'admin@svvv.edu.in', action: 'UPDATE_ALUMNI', details: 'Karan Mehta — corrected department', timestamp: '2026-08-18T12:00:00Z' },
  { id: 'log_8', admin: 'admin@svvv.edu.in', action: 'EXPORT_ALUMNI', details: 'Filtered export — Computer Science, 2023 batch', timestamp: '2026-08-14T09:45:00Z' },
]

export const mockCurrentUserProfile = {
  alumni: {
    fullName: 'Ritika Anand',
    email: 'ritika.a@example.com',
    phone: '+91 98765 43210',
    location: 'Bengaluru, IN',
    enrollmentNumber: 'CS2019014',
    degree: 'B.Tech',
    department: 'Computer Science',
    batch: '2019–2023',
    graduationYear: 2023,
    currentCompany: 'Razorpay',
    jobTitle: 'Backend Engineer II',
    industry: 'Fintech',
    skills: ['Java', 'Spring Boot', 'Kafka'],
    linkedin: 'linkedin.com/in/ritika-anand',
    github: 'github.com/ritikaanand',
    summary: 'Backend engineer focused on payments infrastructure and reliability.',
  },
  student: {
    fullName: 'Priyansh Tiwari',
    email: 'priyansh.t@svvv.edu.in',
    department: 'Computer Science',
    batch: '2022–2026',
    enrollmentNumber: 'CS2022031',
    skills: ['React', 'Node.js', 'PostgreSQL'],
    careerInterests: 'Backend engineering, distributed systems',
  },
}

// Central vocabulary for the app. Keeping these as named constants (instead
// of raw strings scattered through components) means the role model and the
// status lifecycles from the SRS stay enforced in one place.

export const ROLES = Object.freeze({
  PUBLIC: 'PUBLIC',
  STUDENT: 'STUDENT',
  ALUMNI: 'ALUMNI',
  ADMIN: 'ADMIN',
})

export const ALUMNI_RECORD_STATUS = Object.freeze({
  IMPORTED: 'IMPORTED',
  INVITED: 'INVITED',
  CLAIMED: 'CLAIMED',
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
})

export const MENTORSHIP_STATUS = Object.freeze({
  PENDING: 'PENDING',
  ACCEPTED: 'ACCEPTED',
  REJECTED: 'REJECTED',
  COMPLETED: 'COMPLETED',
})

export const JOB_STATUS = Object.freeze({
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
  REMOVED: 'REMOVED',
  EXPIRED: 'EXPIRED',
})

export const CONVERSATION_TYPE = Object.freeze({
  DIRECT: 'DIRECT',
  DEPARTMENT: 'DEPARTMENT',
})

// Maps every status value used across the app to a badge tone, so a
// <StatusBadge value="ACTIVE" /> always renders consistently no matter
// which module it came from.
export const STATUS_TONE = Object.freeze({
  IMPORTED: 'info',
  INVITED: 'warning',
  CLAIMED: 'info',
  ACTIVE: 'success',
  INACTIVE: 'muted',
  PENDING: 'warning',
  ACCEPTED: 'success',
  APPROVED: 'success',
  REJECTED: 'danger',
  REMOVED: 'danger',
  EXPIRED: 'muted',
  COMPLETED: 'info',
  DRAFT: 'muted',
  PUBLISHED: 'success',
  CANCELLED: 'danger',
  ARCHIVED: 'muted',
  SUBMITTED: 'info',
  REVIEWED: 'info',
  SHORTLISTED: 'success',
  READY: 'success',
  DUPLICATE: 'warning',
  ERROR: 'danger',
})

// Route patterns (what <Route path> receives). Use `paths` below to build real links.
export const ROUTES = Object.freeze({
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  CLAIM_PROFILE: '/claim-profile',
  DASHBOARD: '/dashboard',
  FEED: '/feed',
  MESSAGES: '/messages',
  MESSAGE_THREAD: '/messages/:conversationId',
  GROUPS: '/groups',
  GROUP_DETAIL: '/groups/:departmentId',
  DIRECTORY: '/directory',
  DIRECTORY_DETAIL: '/directory/:alumniId',
  MENTORSHIP: '/mentorship',
  EVENTS: '/events',
  JOBS: '/jobs',
  ANNOUNCEMENTS: '/announcements',
  NOTIFICATIONS: '/notifications',
  PROFILE: '/profile',
  ADMIN_ALUMNI: '/admin/alumni',
  ADMIN_IMPORT: '/admin/alumni/import',
  ADMIN_EVENTS: '/admin/events',
  ADMIN_JOBS: '/admin/jobs',
  ADMIN_POSTS: '/admin/posts',
  ADMIN_USERS: '/admin/users',
  ADMIN_ANALYTICS: '/admin/analytics',
  ADMIN_AUDIT_LOG: '/admin/audit-log',
})

// Builders for concrete links, so nobody string-concatenates paths by hand.
export const paths = Object.freeze({
  alumniProfile: (alumniId) => `/directory/${alumniId}`,
  thread: (conversationId) => `/messages/${conversationId}`,
  group: (departmentId) => `/groups/${departmentId}`,
  // Opens Messages and starts (or resumes) a direct chat with that user.
  messageUser: (userId) => `/messages?with=${userId}`,
  userPosts: (userId) => `/feed?author=${userId}`,
})

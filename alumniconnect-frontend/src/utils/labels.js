// Human-readable labels for the enum values the API sends, plus the option lists
// the forms use, so every screen words them the same way.

export const ROLE_LABELS = Object.freeze({
  STUDENT: 'Student',
  ALUMNI: 'Alumnus',
  ADMIN: 'Alumni office',
})

export const JOB_TYPE_LABELS = Object.freeze({
  FULL_TIME: 'Full-time',
  INTERNSHIP: 'Internship',
  PART_TIME: 'Part-time',
  CONTRACT: 'Contract',
})

export const EVENT_MODE_LABELS = Object.freeze({
  ONLINE: 'Online',
  ON_CAMPUS: 'On campus',
})

export const POST_VISIBILITY_LABELS = Object.freeze({
  PUBLIC: 'Everyone',
  ALUMNI_ONLY: 'Alumni only',
  DEPARTMENT: 'My department',
  CONNECTIONS: 'My connections',
})

export const POST_VISIBILITY_HINTS = Object.freeze({
  PUBLIC: 'Anyone signed in can see this post.',
  ALUMNI_ONLY: 'Only alumni (and the alumni office) can see this post.',
  DEPARTMENT: 'Only people in your department can see this post.',
  CONNECTIONS: 'Only people you have exchanged messages with can see this post.',
})

export const DIRECTORY_VISIBILITY_LABELS = Object.freeze({
  PUBLIC: 'Public — any signed-in user',
  STUDENTS_AND_ALUMNI: 'Students and alumni',
  ALUMNI_ONLY: 'Alumni only',
  PRIVATE: 'Private — not listed in the directory',
})

export const CONTACT_VISIBILITY_LABELS = Object.freeze({
  PUBLIC: 'Visible to everyone signed in',
  ALUMNI_ONLY: 'Visible to alumni only',
  PRIVATE: 'Only me',
})

export const CERTIFICATION_TYPE_LABELS = Object.freeze({
  CERTIFICATION: 'Certification',
  ACHIEVEMENT: 'Achievement',
  AWARD: 'Award',
  PUBLICATION: 'Publication',
})

export const TARGET_AUDIENCE_LABELS = Object.freeze({
  ALL: 'Everyone',
  STUDENTS: 'Students',
  ALUMNI: 'Alumni',
  DEPARTMENT: 'A department',
  BATCH: 'A batch',
})

export const PRIORITY_LABELS = Object.freeze({
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
})

export const NOTIFICATION_CATEGORY_LABELS = Object.freeze({
  MENTORSHIP: 'Mentorship',
  EVENT: 'Events',
  JOB: 'Jobs',
  ANNOUNCEMENT: 'Announcements',
  ACCOUNT: 'Account',
  MESSAGE: 'Messages',
  POST: 'Posts',
})

/** [{ value, label }] from a labels map, for <option> lists. */
export function toOptions(labels) {
  return Object.entries(labels).map(([value, label]) => ({ value, label }))
}

export function labelOf(labels, value, fallback = '—') {
  return labels[value] ?? value ?? fallback
}

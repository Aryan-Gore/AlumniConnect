import { axiosClient, cleanParams } from './axiosClient'

const data = (res) => res.data

// Everything here is ROLE_ADMIN only. Spring Boot is responsible for invoking
// the Python/Pandas analytics job and persisting the results, so the frontend
// only ever talks to one backend — see the note in axiosClient.js.
export const adminApi = {
  // { totalAlumni, activeAlumni, claimedProfiles, unclaimedProfiles, upcomingEvents, activeMentorships,
  //   publishedJobs, pendingJobApprovals, byDepartment: [{label, value}], byGraduationYear: [{label, value}] }
  dashboardSummary: () => axiosClient.get('/admin/dashboard/summary').then(data),

  // Each resolves to [{ label, value }]
  alumniByDepartment: () => axiosClient.get('/admin/analytics/alumni-by-department').then(data),
  alumniByInstitute: () => axiosClient.get('/admin/analytics/alumni-by-institute').then(data),
  alumniByBatch: () => axiosClient.get('/admin/analytics/alumni-by-batch').then(data),
  alumniByIndustry: () => axiosClient.get('/admin/analytics/alumni-by-industry').then(data),
  alumniByLocation: () => axiosClient.get('/admin/analytics/alumni-by-location').then(data),

  // [{ id, adminEmail, action, details, timestamp }], newest first. params: { action }
  auditLog: (params) => axiosClient.get('/admin/audit-log', { params: cleanParams(params) }).then(data),

  // ---- Feed moderation ----
  // params: { query, page, size } -> PageResponse<Post> (every post, whatever its visibility)
  posts: (params) => axiosClient.get('/admin/posts', { params: cleanParams(params) }).then(data),
  removePost: (postId) => axiosClient.delete(`/admin/posts/${postId}`).then(() => undefined),
  removeComment: (postId, commentId) =>
    axiosClient.delete(`/admin/posts/${postId}/comments/${commentId}`).then(() => undefined),

  // ---- Chat moderation (department rooms only) ----
  removeChatMessage: (messageId) => axiosClient.delete(`/admin/chat/messages/${messageId}`).then(() => undefined),

  // ---- User management ----
  // params: { query, role, active, page, size } -> PageResponse<{ id, fullName, email, role, active, profilePhotoUrl, department, alumniId, createdAt }>
  users: (params) => axiosClient.get('/admin/users', { params: cleanParams(params) }).then(data),
  setUserActive: (userId, isActive) => axiosClient.patch(`/admin/users/${userId}/status`, { isActive }).then(data),
}

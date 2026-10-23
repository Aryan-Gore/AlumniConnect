import { axiosClient, cleanParams } from './axiosClient'

const data = (res) => res.data

// One group per department, organised Institute -> Department.
// Contract: docs/API_CONTRACT.md §10 in the backend project.
export const groupsApi = {
  // [{ institute: { id, name, code }, groups: [{ departmentId, name, code, memberCount, studentCount, alumniCount, member, ... }] }]
  list: () => axiosClient.get('/groups').then(data),
  // GroupSummary + { instituteName, conversationId } (conversationId only when the viewer is a member/admin)
  get: (departmentId) => axiosClient.get(`/groups/${departmentId}`).then(data),
  // params: { query, page, size } -> PageResponse<UserSummary>
  members: (departmentId, params) =>
    axiosClient.get(`/groups/${departmentId}/members`, { params: cleanParams(params) }).then(data),
}

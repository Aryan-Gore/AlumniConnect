import { axiosClient } from './axiosClient'

export const mentorshipApi = {
  // Student side
  sendRequest: (alumniId, message) =>
    axiosClient.post('/mentorship/requests', { alumniId, message }).then((res) => res.data),
  myRequests: () => axiosClient.get('/mentorship/requests/sent').then((res) => res.data),

  // Alumni side
  receivedRequests: (params) => axiosClient.get('/mentorship/requests/received', { params }).then((res) => res.data),
  respondToRequest: (requestId, decision) =>
    axiosClient.patch(`/mentorship/requests/${requestId}`, { decision }).then((res) => res.data),

  markCompleted: (requestId) =>
    axiosClient.patch(`/mentorship/requests/${requestId}/complete`).then((res) => res.data),
}

import { axiosClient } from './axiosClient'

export const authApi = {
  login: (credentials) => axiosClient.post('/auth/login', credentials).then((res) => res.data),

  registerStudent: (payload) => axiosClient.post('/auth/register/student', payload).then((res) => res.data),

  // Step 1 of the claim flow — checks the supplied identity details against
  // an existing IMPORTED/INVITED alumni record without creating anything.
  findClaimableRecord: (payload) =>
    axiosClient.post('/auth/claim/lookup', payload).then((res) => res.data),

  // Step 2 — sets a password on the matched record and links it to a new
  // user account. Never creates a second alumni row for the same person.
  claimProfile: (payload) => axiosClient.post('/auth/claim/confirm', payload).then((res) => res.data),

  // { googleEnabled, googleClientId } — whether to show "Continue with Google".
  config: () => axiosClient.get('/auth/config').then((res) => res.data),

  // Step 1 of Google sign-in. Resolves to { status: 'AUTHENTICATED', auth } for a
  // known Google account, or { status: 'REGISTRATION_REQUIRED', registrationToken,
  // email, fullName, pictureUrl } for a new student who still has to tell us their
  // enrollment number and department.
  googleLogin: (credential) => axiosClient.post('/auth/google', { credential }).then((res) => res.data),

  // Step 2: { registrationToken, enrollmentNumber, departmentId, batch? } -> { token, user }
  googleRegister: (payload) => axiosClient.post('/auth/google/register', payload).then((res) => res.data),

  getCurrentUser: () => axiosClient.get('/auth/me').then((res) => res.data),

  logout: () => axiosClient.post('/auth/logout').then((res) => res.data),
}

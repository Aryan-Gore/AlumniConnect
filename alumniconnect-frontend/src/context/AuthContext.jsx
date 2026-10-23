import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import { authApi } from '@/api/authApi'
import { getStoredToken, registerUnauthorizedHandler, setStoredToken } from '@/api/axiosClient'
import { ROUTES } from '@/utils/constants'

export const AuthContext = createContext(null)

// Shape of `user` once authenticated (the backend's UserResponse):
// { id, fullName, email, role: 'STUDENT' | 'ALUMNI' | 'ADMIN',
//   profilePhotoUrl, department: { id, name, code, instituteId, instituteName, instituteCode } | null,
//   alumniId: number | null, verified: boolean }
//
// `id` is the USER id (what chat and the feed use); `alumniId` is the
// alumni-record id (what /directory/:alumniId uses) and only exists for alumni.

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isInitializing, setIsInitializing] = useState(true)
  const [authError, setAuthError] = useState(null)

  const clearSession = useCallback(() => {
    setStoredToken(null)
    setUser(null)
  }, [])

  // On first load, if a token is already in this tab's session storage,
  // resolve who it belongs to instead of forcing a re-login on every refresh.
  useEffect(() => {
    const token = getStoredToken()
    if (!token) {
      setIsInitializing(false)
      return
    }
    authApi
      .getCurrentUser()
      .then((currentUser) => setUser(currentUser))
      .catch(() => clearSession())
      .finally(() => setIsInitializing(false))
  }, [clearSession])

  // Any 401 from the API (expired/invalid token) drops the session cleanly.
  useEffect(() => {
    registerUnauthorizedHandler(() => {
      clearSession()
      if (window.location.pathname !== ROUTES.LOGIN) {
        window.location.assign(ROUTES.LOGIN)
      }
    })
  }, [clearSession])

  // Runs an auth call that resolves to { token, user }, stores the session, and
  // records a readable error message when it fails.
  const authenticate = useCallback(async (request, fallbackMessage) => {
    setAuthError(null)
    try {
      const { token, user: authenticatedUser } = await request()
      setStoredToken(token)
      setUser(authenticatedUser)
      return authenticatedUser
    } catch (error) {
      setAuthError(error.response?.data?.message ?? fallbackMessage)
      throw error
    }
  }, [])

  const login = useCallback(
    (credentials) => authenticate(() => authApi.login(credentials), 'Could not sign in with those credentials.'),
    [authenticate]
  )

  const registerStudent = useCallback(
    (payload) => authenticate(() => authApi.registerStudent(payload), 'Could not complete registration.'),
    [authenticate]
  )

  const claimProfile = useCallback(
    (payload) => authenticate(() => authApi.claimProfile(payload), 'Could not verify that alumni record.'),
    [authenticate]
  )

  // Step 2 of Google sign-up (enrollment number + department for a brand-new student).
  const googleRegister = useCallback(
    (payload) => authenticate(() => authApi.googleRegister(payload), 'Could not finish creating your account.'),
    [authenticate]
  )

  // Step 1 of Google sign-in. If the Google account is already known the user is
  // signed in and { status: 'AUTHENTICATED', user } comes back; for a new person
  // { status: 'REGISTRATION_REQUIRED', registrationToken, email, fullName, pictureUrl }
  // comes back and the caller shows the "finish sign-up" form.
  const googleLogin = useCallback(async (credential) => {
    setAuthError(null)
    try {
      const result = await authApi.googleLogin(credential)
      if (result.status === 'AUTHENTICATED') {
        setStoredToken(result.auth.token)
        setUser(result.auth.user)
        return { status: 'AUTHENTICATED', user: result.auth.user }
      }
      return result
    } catch (error) {
      setAuthError(error.response?.data?.message ?? 'Google sign-in failed. Please try again.')
      throw error
    }
  }, [])

  const logout = useCallback(() => {
    authApi.logout().catch(() => {
      // Session is cleared locally regardless of whether the API call succeeds.
    })
    clearSession()
  }, [clearSession])

  // Re-read the signed-in user from the server (after a photo upload, a department change, …).
  const refreshUser = useCallback(async () => {
    const currentUser = await authApi.getCurrentUser()
    setUser(currentUser)
    return currentUser
  }, [])

  // Merge a few known-good fields into the cached user without a round trip.
  const updateUser = useCallback((patch) => {
    setUser((current) => (current ? { ...current, ...patch } : current))
  }, [])

  const value = useMemo(
    () => ({
      user,
      role: user?.role ?? null,
      isAuthenticated: Boolean(user),
      isInitializing,
      authError,
      login,
      registerStudent,
      claimProfile,
      googleLogin,
      googleRegister,
      logout,
      refreshUser,
      updateUser,
      setUser,
    }),
    [
      user,
      isInitializing,
      authError,
      login,
      registerStudent,
      claimProfile,
      googleLogin,
      googleRegister,
      logout,
      refreshUser,
      updateUser,
    ]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

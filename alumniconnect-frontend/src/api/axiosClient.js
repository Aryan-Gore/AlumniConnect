import axios from 'axios'

// Every request from the frontend goes through the Spring Boot API — the
// Python/FastAPI analytics service is never called from the browser. Spring
// Boot orchestrates Python internally (for CSV cleaning, stats, etc.) and
// exposes the results as ordinary REST endpoints, so this is the one place
// the app ever needs a base URL or an auth header.
export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api').replace(/\/+$/, '')

export const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  // Spring binds repeated keys (skills=Java&skills=Spring) to a List; axios'
  // default would send skills[]=Java&skills[]=Spring, which Spring ignores.
  paramsSerializer: { indexes: null },
})

const TOKEN_KEY = 'alumniconnect.token'

export function getStoredToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function setStoredToken(token) {
  if (token) sessionStorage.setItem(TOKEN_KEY, token)
  else sessionStorage.removeItem(TOKEN_KEY)
}

axiosClient.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Centralised 401 handling: any expired/invalid session drops the user back
// to login exactly once, instead of every feature module reimplementing it.
let onUnauthorized = null
export function registerUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // A 401 from the login/registration calls themselves just means "wrong
    // credentials" — only a 401 on an already-signed-in session means "expired".
    const hadSession = Boolean(getStoredToken())
    if (error.response?.status === 401 && hadSession) {
      setStoredToken(null)
      onUnauthorized?.()
    }
    return Promise.reject(error)
  }
)

/** POST a single file as multipart/form-data and return the response body. */
export function postFile(url, file, field = 'file') {
  const formData = new FormData()
  formData.append(field, file)
  return axiosClient
    .post(url, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((res) => res.data)
}

/** Drops undefined, null and empty-string entries so they are not sent as `key=`. */
export function cleanParams(params = {}) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => {
      if (value === undefined || value === null || value === '') return false
      if (Array.isArray(value) && value.length === 0) return false
      return true
    })
  )
}

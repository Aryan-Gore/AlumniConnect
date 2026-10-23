import { API_BASE_URL } from '@/api/axiosClient'

/**
 * Stored image URLs are relative to the API ("/files/profiles/x.jpg").
 * Turn one into something an <img> can load; absolute URLs (e.g. a Google
 * profile picture) pass through untouched.
 */
export function assetUrl(path) {
  if (!path) return null
  if (/^https?:\/\//i.test(path)) return path
  return `${API_BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
}

/** The WebSocket endpoint that belongs to the configured API (http→ws, https→wss). */
export function chatSocketUrl() {
  return `${API_BASE_URL.replace(/^http/i, 'ws')}/ws`
}

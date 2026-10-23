/**
 * One place that turns whatever axios threw into a sentence a person can read.
 * The backend always answers with { status, message, fieldErrors? }.
 */
export function getErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const data = error?.response?.data

  if (data?.fieldErrors && typeof data.fieldErrors === 'object') {
    const first = Object.values(data.fieldErrors)[0]
    if (first) return String(first)
  }
  if (data?.message) return String(data.message)

  if (error?.code === 'ERR_NETWORK' || (error?.request && !error?.response)) {
    return "Can't reach the server. Check your connection and that the backend is running."
  }
  if (error?.response?.status === 413) return 'That file is too large.'
  return fallback
}

/** { fieldName: message } from a 400 validation response, or {}. */
export function getFieldErrors(error) {
  const fieldErrors = error?.response?.data?.fieldErrors
  return fieldErrors && typeof fieldErrors === 'object' ? fieldErrors : {}
}

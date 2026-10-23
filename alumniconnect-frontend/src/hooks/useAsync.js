import { useCallback, useEffect, useRef, useState } from 'react'

/**
 * Runs `asyncFn` when the component mounts and again whenever `deps` change,
 * and exposes { data, error, isLoading, reload, setData }.
 *
 * - A response that arrives after a newer request started (or after unmount)
 *   is ignored, so fast filter changes can never show stale results.
 * - `reload()` re-runs the latest `asyncFn` (e.g. after a mutation).
 * - `setData(valueOrUpdater)` lets a page patch the loaded data locally
 *   (optimistic updates) without another round trip.
 */
export function useAsync(asyncFn, deps = []) {
  const [state, setState] = useState({ data: null, error: null, isLoading: true })
  const requestId = useRef(0)
  const fnRef = useRef(asyncFn)

  useEffect(() => {
    fnRef.current = asyncFn
  })

  const run = useCallback(() => {
    requestId.current += 1
    const id = requestId.current
    setState((prev) => ({ ...prev, isLoading: true, error: null }))
    return fnRef
      .current()
      .then((data) => {
        if (id === requestId.current) setState({ data, error: null, isLoading: false })
        return data
      })
      .catch((error) => {
        if (id === requestId.current) setState((prev) => ({ ...prev, error, isLoading: false }))
        return undefined
      })
  }, [])

  useEffect(() => {
    run()
    return () => {
      requestId.current += 1
    }
    // `deps` is the caller's dependency list, spread on purpose.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [run, ...deps])

  const setData = useCallback((valueOrUpdater) => {
    setState((prev) => ({
      ...prev,
      data: typeof valueOrUpdater === 'function' ? valueOrUpdater(prev.data) : valueOrUpdater,
    }))
  }, [])

  return { ...state, reload: run, setData }
}

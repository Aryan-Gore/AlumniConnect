import { useEffect } from 'react'

const SUFFIX = 'AlumniConnect'

/** Sets the browser tab title for the current screen: usePageTitle('Feed') → "Feed · AlumniConnect". */
export function usePageTitle(title) {
  useEffect(() => {
    document.title = title ? `${title} · ${SUFFIX}` : SUFFIX
  }, [title])
}

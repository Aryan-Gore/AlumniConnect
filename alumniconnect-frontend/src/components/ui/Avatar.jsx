import { useState } from 'react'
import { cn } from '@/utils/cn'
import { assetUrl } from '@/utils/assetUrl'
import { initials } from '@/utils/formatters'

const SIZES = {
  xs: 'size-6 text-[10px]',
  sm: 'size-8 text-xs',
  md: 'size-10 text-sm',
  lg: 'size-14 text-base',
  xl: 'size-24 text-2xl',
  '2xl': 'size-32 text-4xl',
}

/**
 * A person's photo, or their initials when they have none (or it fails to load).
 * `src` is the stored relative URL straight from the API (e.g. profilePhotoUrl).
 */
export function Avatar({ src, name = '', size = 'md', className }) {
  const url = assetUrl(src)
  const [failedUrl, setFailedUrl] = useState(null)
  const showImage = Boolean(url) && failedUrl !== url

  return (
    <span
      className={cn(
        'relative inline-flex shrink-0 select-none items-center justify-center overflow-hidden rounded-full',
        'bg-accent-soft font-mono font-medium text-accent',
        SIZES[size] ?? SIZES.md,
        className
      )}
    >
      {showImage ? (
        <img
          src={url}
          alt={name}
          loading="lazy"
          className="size-full object-cover"
          onError={() => setFailedUrl(url)}
        />
      ) : (
        <span aria-hidden={!name ? 'true' : undefined}>{initials(name) || '?'}</span>
      )}
    </span>
  )
}

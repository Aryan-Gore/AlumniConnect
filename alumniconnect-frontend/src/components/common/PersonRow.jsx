import { Link } from 'react-router-dom'
import { Avatar } from '@/components/ui/Avatar'
import { VerifiedBadge } from '@/components/ui/VerifiedBadge'
import { cn } from '@/utils/cn'
import { paths } from '@/utils/constants'

/**
 * "Who is this person" row used everywhere a name appears next to content
 * (feed authors, chat headers, group members, job posters, applicants).
 *
 *  - `user` is the backend's UserSummaryResponse:
 *    { id, fullName, role, profilePhotoUrl, headline, department, alumniId, verified }
 *  - The name links to the alumni profile when the person is an alumnus.
 *  - `subtitle` replaces the headline (e.g. "2 hours ago · Public").
 *  - `actions` is rendered on the right (buttons, menus).
 */
export function PersonRow({ user, subtitle, actions, size = 'md', linkToProfile = true, className }) {
  if (!user) return null

  const profileLink = linkToProfile && user.alumniId ? paths.alumniProfile(user.alumniId) : null
  const name = (
    <span className="truncate font-medium text-text-primary">{user.fullName}</span>
  )

  return (
    <div className={cn('flex items-center gap-3', className)}>
      <Avatar src={user.profilePhotoUrl} name={user.fullName} size={size} />
      <div className="min-w-0 flex-1">
        <div className="flex min-w-0 items-center gap-1.5 text-sm">
          {profileLink ? (
            <Link to={profileLink} className="min-w-0 truncate hover:text-accent hover:underline">
              {name}
            </Link>
          ) : (
            name
          )}
          {user.verified && <VerifiedBadge size="size-3.5" />}
        </div>
        {(subtitle ?? user.headline) && (
          <p className="truncate text-xs text-text-secondary">{subtitle ?? user.headline}</p>
        )}
      </div>
      {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
    </div>
  )
}

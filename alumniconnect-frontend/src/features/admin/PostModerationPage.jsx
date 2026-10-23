import { ComingSoon } from '@/components/common/ComingSoon'

// PLACEHOLDER — replaced by the feed-moderation screen (same export name).
export function PostModerationPage() {
  return (
    <ComingSoon
      title="Feed moderation"
      description="Review and remove posts and comments."
      plannedFeatures={['Search posts', 'Remove a post or comment', 'Everything is audit-logged']}
    />
  )
}

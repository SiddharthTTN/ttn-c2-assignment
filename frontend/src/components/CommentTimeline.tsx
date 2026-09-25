import type { Comment } from '../api/types'
import { formatDateTime } from '../utils/helpers'

export function CommentTimeline({ comments }: { comments: Comment[] }) {
  if (comments.length === 0) {
    return <p className="loading-status">No comments yet.</p>
  }

  return (
    <ol className="comment-timeline" aria-label="Ticket comments">
      {comments.map((comment) => (
        <li key={comment.id} className="comment-item">
          <p className="comment-item__meta">{formatDateTime(comment.createdAt)}</p>
          <p>{comment.body}</p>
        </li>
      ))}
    </ol>
  )
}

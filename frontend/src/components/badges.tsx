import type { TicketPriority, TicketStatus } from '../api/types'
import { PRIORITY_LABELS, STATUS_LABELS } from '../api/types'

export function StatusBadge({ status }: { status: TicketStatus }) {
  return (
    <span className={`badge badge--status-${status.toLowerCase()}`}>
      {STATUS_LABELS[status]}
    </span>
  )
}

export function PriorityBadge({ priority }: { priority: TicketPriority }) {
  return (
    <span className={`badge badge--priority-${priority.toLowerCase()}`}>
      {PRIORITY_LABELS[priority]}
    </span>
  )
}

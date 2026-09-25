import type { TicketStatus } from '../api/types'
import { STATUS_LABELS } from '../api/types'
import { getAllowedNextStatuses } from '../utils/helpers'

interface StatusActionsProps {
  currentStatus: TicketStatus
  disabled?: boolean
  onTransition: (status: TicketStatus) => void
}

export function StatusActions({
  currentStatus,
  disabled = false,
  onTransition,
}: StatusActionsProps) {
  const nextStatuses = getAllowedNextStatuses(currentStatus)

  if (nextStatuses.length === 0) {
    return <p className="loading-status">No further status actions available.</p>
  }

  return (
    <div className="status-actions" aria-label="Status actions">
      {nextStatuses.map((status) => (
        <button
          key={status}
          type="button"
          className={
            status === 'CANCELLED' ? 'btn btn--destructive' : 'btn btn--secondary'
          }
          disabled={disabled}
          onClick={() => onTransition(status)}
        >
          Mark {STATUS_LABELS[status]}
        </button>
      ))}
    </div>
  )
}

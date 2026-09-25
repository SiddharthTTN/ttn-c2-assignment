import { TICKET_STATUSES, STATUS_LABELS, type TicketStatus } from '../api/types'
import { FieldError } from './FieldError'

interface TicketFilterBarProps {
  search: string
  status: TicketStatus | ''
  onSearchChange: (value: string) => void
  onStatusChange: (value: TicketStatus | '') => void
  resultCount?: number
}

export function TicketFilterBar({
  search,
  status,
  onSearchChange,
  onStatusChange,
  resultCount,
}: TicketFilterBarProps) {
  return (
    <div className="toolbar" role="search">
      <div className="field toolbar__field">
        <label htmlFor="ticket-search">Search tickets</label>
        <input
          id="ticket-search"
          name="q"
          type="search"
          value={search}
          placeholder="Search title or description"
          onChange={(event) => onSearchChange(event.target.value)}
        />
      </div>
      <div className="field toolbar__field">
        <label htmlFor="ticket-status">Status</label>
        <select
          id="ticket-status"
          name="status"
          value={status}
          onChange={(event) =>
            onStatusChange(event.target.value as TicketStatus | '')
          }
        >
          <option value="">All statuses</option>
          {TICKET_STATUSES.map((option) => (
            <option key={option} value={option}>
              {STATUS_LABELS[option]}
            </option>
          ))}
        </select>
        <FieldError id="ticket-status-error" />
      </div>
      {typeof resultCount === 'number' ? (
        <p className="toolbar__meta" aria-live="polite">
          {resultCount} result{resultCount === 1 ? '' : 's'}
        </p>
      ) : null}
    </div>
  )
}

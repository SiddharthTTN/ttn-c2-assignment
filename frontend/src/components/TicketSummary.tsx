import type { Ticket } from '../api/types'
import { PriorityBadge, StatusBadge } from './badges'
import { formatDateTime } from '../utils/helpers'

export function TicketSummary({ ticket }: { ticket: Ticket }) {
  return (
    <dl className="meta-list">
      <div>
        <dt>Ticket ID</dt>
        <dd>{ticket.id}</dd>
      </div>
      <div>
        <dt>Status</dt>
        <dd>
          <StatusBadge status={ticket.status} />
        </dd>
      </div>
      <div>
        <dt>Priority</dt>
        <dd>
          <PriorityBadge priority={ticket.priority} />
        </dd>
      </div>
      <div>
        <dt>Created</dt>
        <dd>{formatDateTime(ticket.createdAt)}</dd>
      </div>
      <div>
        <dt>Updated</dt>
        <dd>{formatDateTime(ticket.updatedAt)}</dd>
      </div>
    </dl>
  )
}

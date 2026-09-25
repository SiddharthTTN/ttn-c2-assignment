import { Link } from 'react-router-dom'
import type { Ticket } from '../api/types'
import { PriorityBadge, StatusBadge } from './badges'

interface TicketTableProps {
  tickets: Ticket[]
}

export function TicketTable({ tickets }: TicketTableProps) {
  return (
    <div className="table-scroll" aria-label="Ticket results">
      <table className="data-table">
        <caption>Support tickets</caption>
        <thead>
          <tr>
            <th scope="col">ID</th>
            <th scope="col">Title</th>
            <th scope="col">Status</th>
            <th scope="col">Priority</th>
            <th scope="col">Assignee</th>
          </tr>
        </thead>
        <tbody>
          {tickets.map((ticket) => (
            <tr key={ticket.id}>
              <td>
                <Link to={`/tickets/${ticket.id}`}>{ticket.id}</Link>
              </td>
              <td>
                <Link to={`/tickets/${ticket.id}`}>{ticket.title}</Link>
              </td>
              <td>
                <StatusBadge status={ticket.status} />
              </td>
              <td>
                <PriorityBadge priority={ticket.priority} />
              </td>
              <td>{ticket.assignee ?? 'Unassigned'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

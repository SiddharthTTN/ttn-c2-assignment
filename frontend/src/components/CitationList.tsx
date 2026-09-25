import { Link } from 'react-router-dom'

export function CitationList({ ticketIds }: { ticketIds: string[] }) {
  if (ticketIds.length === 0) {
    return null
  }

  return (
    <div>
      <h3 className="h3">Cited tickets</h3>
      <ul className="citation-list">
        {ticketIds.map((ticketId) => (
          <li key={ticketId}>
            <Link className="citation-link" to={`/tickets/${ticketId}`}>
              {ticketId}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}

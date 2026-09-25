import { useDeferredValue, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { listTickets } from '../api/tickets'
import { ticketKeys } from '../api/queryKeys'
import type { TicketStatus } from '../api/types'
import { ApiError } from '../api/client'
import { EmptyState } from '../components/EmptyState'
import { ErrorAlert } from '../components/ErrorAlert'
import { LoadingSkeleton } from '../components/LoadingSkeleton'
import { PageHeader } from '../components/PageHeader'
import { TicketFilterBar } from '../components/TicketFilterBar'
import { TicketTable } from '../components/TicketTable'

export function TicketListPage() {
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState<TicketStatus | ''>('')
  const deferredSearch = useDeferredValue(search.trim())

  const query = useQuery({
    queryKey: ticketKeys.list(deferredSearch, status),
    queryFn: () => listTickets({ q: deferredSearch, status }),
  })

  const isFiltered = deferredSearch.length > 0 || status !== ''
  const items = query.data?.items ?? []

  const errorMessage = useMemo(() => {
    if (!query.error) {
      return null
    }
    if (query.error instanceof ApiError) {
      return query.error.body.error
    }
    return 'Unable to load tickets.'
  }, [query.error])

  return (
    <>
      <PageHeader
        title="Tickets"
        actions={
          <Link to="/tickets/new" className="btn btn--primary">
            Create ticket
          </Link>
        }
      />
      <TicketFilterBar
        search={search}
        status={status}
        onSearchChange={setSearch}
        onStatusChange={setStatus}
        resultCount={query.isSuccess ? items.length : undefined}
      />
      {query.isLoading ? <LoadingSkeleton /> : null}
      {query.isError ? (
        <ErrorAlert
          title="Could not load tickets"
          message={errorMessage ?? 'Unable to load tickets.'}
          details={
            query.error instanceof ApiError ? query.error.body.details : undefined
          }
        />
      ) : null}
      {query.isSuccess && items.length === 0 && !isFiltered ? (
        <EmptyState
          title="No tickets yet"
          description="Create your first support ticket to get started."
          action={
            <Link to="/tickets/new" className="btn btn--primary">
              Create ticket
            </Link>
          }
        />
      ) : null}
      {query.isSuccess && items.length === 0 && isFiltered ? (
        <EmptyState
          title="No tickets match your search or filter"
          description="Try a different keyword or clear the status filter."
        />
      ) : null}
      {query.isSuccess && items.length > 0 ? <TicketTable tickets={items} /> : null}
    </>
  )
}

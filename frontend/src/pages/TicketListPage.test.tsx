import { describe, expect, it, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TicketListPage } from '../pages/TicketListPage'
import { mockJsonResponse, renderWithProviders } from '../test/test-utils'

const sampleTicket = {
  id: 'TKT-1001',
  title: 'Payment failed at checkout',
  description: 'Card authorization timed out.',
  status: 'RESOLVED',
  priority: 'HIGH',
  assignee: 'Asha',
  category: 'Payments',
  resolutionNotes: 'Retried through secondary gateway.',
  createdAt: '2026-09-24T10:00:00Z',
  updatedAt: '2026-09-25T07:30:00Z',
  comments: [],
}

describe('TicketListPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('renders unfiltered empty state', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockJsonResponse({ items: [] }),
    )

    renderWithProviders(<TicketListPage />, { initialEntries: ['/tickets'] })

    expect(await screen.findByText('No tickets yet')).toBeInTheDocument()
  })

  it('renders filtered no-results state', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(() =>
      Promise.resolve(mockJsonResponse({ items: [] })),
    )

    const user = userEvent.setup()
    renderWithProviders(<TicketListPage />, { initialEntries: ['/tickets'] })

    await user.type(screen.getByLabelText('Search tickets'), 'missing')

    expect(
      await screen.findByText('No tickets match your search or filter'),
    ).toBeInTheDocument()
  })

  it('shows API error banner when list request fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockJsonResponse(
        { error: 'Validation failed', details: ['status is invalid'] },
        { status: 400 },
      ),
    )

    renderWithProviders(<TicketListPage />, { initialEntries: ['/tickets'] })

    expect(await screen.findByText('Validation failed')).toBeInTheDocument()
  })

  it('renders ticket table rows', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockJsonResponse({ items: [sampleTicket] }),
    )

    renderWithProviders(<TicketListPage />, { initialEntries: ['/tickets'] })

    expect(await screen.findByRole('link', { name: 'TKT-1001' })).toBeInTheDocument()
    expect(screen.getByText('Payment failed at checkout')).toBeInTheDocument()
    expect(screen.getByText('Asha')).toBeInTheDocument()
  })
})

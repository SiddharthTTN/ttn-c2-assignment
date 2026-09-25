import { describe, expect, it, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AskPage } from './AskPage'
import { mockJsonResponse, renderWithProviders } from '../test/test-utils'

describe('AskPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('renders no-match state when API returns no relevant tickets', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockJsonResponse({
        answer: 'no relevant tickets found',
        ticketIds: [],
        noRelevantTickets: true,
      }),
    )

    const user = userEvent.setup()
    renderWithProviders(<AskPage />)

    await user.type(
      screen.getByLabelText('Your question'),
      'What caused the outage?',
    )
    await user.click(screen.getByRole('button', { name: 'Ask' }))

    expect(
      await screen.findByText('No relevant ticket evidence'),
    ).toBeInTheDocument()
  })

  it('renders grounded answer with citation links', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockJsonResponse({
        answer: 'Payment failures were caused by gateway timeouts [TKT-1001].',
        ticketIds: ['TKT-1001'],
        noRelevantTickets: false,
      }),
    )

    const user = userEvent.setup()
    renderWithProviders(<AskPage />)

    await user.type(
      screen.getByLabelText('Your question'),
      'What caused previous payment failures?',
    )
    await user.click(screen.getByRole('button', { name: 'Ask' }))

    expect(await screen.findByRole('heading', { name: 'Answer' })).toBeInTheDocument()
    expect(
      screen.getByText(
        'Payment failures were caused by gateway timeouts [TKT-1001].',
      ),
    ).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'TKT-1001' })).toHaveAttribute(
      'href',
      '/tickets/TKT-1001',
    )
  })

  it('shows service error state for AI failures', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockJsonResponse(
        {
          error: 'AI service unavailable',
          details: ['Try again later.'],
        },
        { status: 503 },
      ),
    )

    const user = userEvent.setup()
    renderWithProviders(<AskPage />)

    await user.type(screen.getByLabelText('Your question'), 'Any open tickets?')
    await user.click(screen.getByRole('button', { name: 'Ask' }))

    expect(await screen.findByText('Ask request failed')).toBeInTheDocument()
    expect(screen.getByText('AI service unavailable')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Retry ask' })).toBeInTheDocument()
  })
})

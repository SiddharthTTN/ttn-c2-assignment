import { describe, expect, it, vi, beforeEach } from 'vitest'
import { fireEvent, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AskPage } from './AskPage'
import { ASK_QUESTION_MAX_LENGTH } from './AskPage'
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
    renderWithProviders(<AskPage />, { initialEntries: ['/ask'] })

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
    renderWithProviders(<AskPage />, { initialEntries: ['/ask'] })

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
    renderWithProviders(<AskPage />, { initialEntries: ['/ask'] })

    await user.type(screen.getByLabelText('Your question'), 'Any open tickets?')
    await user.click(screen.getByRole('button', { name: 'Ask' }))

    expect(await screen.findByText('Ask request failed')).toBeInTheDocument()
    expect(screen.getByText('AI service unavailable')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Retry ask' })).toBeInTheDocument()
  })

  it('shows client-side required validation inline', async () => {
    const fetchMock = vi.fn()
    vi.spyOn(globalThis, 'fetch').mockImplementation(fetchMock)

    const user = userEvent.setup()
    renderWithProviders(<AskPage />, { initialEntries: ['/ask'] })

    await user.click(screen.getByRole('button', { name: 'Ask' }))

    expect(await screen.findByText('Question is required.')).toBeInTheDocument()
    expect(screen.queryByText('Ask request failed')).not.toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('shows inline API validation on 400 without a global alert', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockJsonResponse(
        {
          error: 'Validation failed',
          details: ['question must not exceed 2000 characters'],
        },
        { status: 400 },
      ),
    )

    const user = userEvent.setup()
    renderWithProviders(<AskPage />, { initialEntries: ['/ask'] })

    await user.type(screen.getByLabelText('Your question'), 'Valid question')
    await user.click(screen.getByRole('button', { name: 'Ask' }))

    expect(
      await screen.findByText('question must not exceed 2000 characters'),
    ).toBeInTheDocument()
    expect(screen.queryByRole('alert', { name: /Ask request failed/i })).not.toBeInTheDocument()
    expect(screen.queryByText('Validation failed')).not.toBeInTheDocument()
  })

  it('clears a prior answer when submitting a new question', async () => {
    let askCalls = 0
    vi.spyOn(globalThis, 'fetch').mockImplementation(() => {
      askCalls += 1
      if (askCalls === 1) {
        return Promise.resolve(
          mockJsonResponse({
            answer: 'First answer [TKT-1001].',
            ticketIds: ['TKT-1001'],
            noRelevantTickets: false,
          }),
        )
      }
      return Promise.resolve(
        mockJsonResponse({
          answer: 'no relevant tickets found',
          ticketIds: [],
          noRelevantTickets: true,
        }),
      )
    })

    const user = userEvent.setup()
    renderWithProviders(<AskPage />, { initialEntries: ['/ask'] })

    const question = screen.getByLabelText('Your question')
    await user.type(question, 'First question')
    await user.click(screen.getByRole('button', { name: 'Ask' }))
    expect(await screen.findByText('First answer [TKT-1001].')).toBeInTheDocument()

    fireEvent.change(question, { target: { value: 'Second question' } })
    await user.click(screen.getByRole('button', { name: 'Ask' }))

    expect(screen.queryByText('First answer [TKT-1001].')).not.toBeInTheDocument()
    expect(
      await screen.findByText('No relevant ticket evidence'),
    ).toBeInTheDocument()
  })

  it('validates question length client-side before calling the API', async () => {
    const fetchMock = vi.fn()
    vi.spyOn(globalThis, 'fetch').mockImplementation(fetchMock)

    const user = userEvent.setup()
    renderWithProviders(<AskPage />, { initialEntries: ['/ask'] })

    const tooLong = `${'a'.repeat(ASK_QUESTION_MAX_LENGTH)}b`
    fireEvent.change(screen.getByLabelText('Your question'), {
      target: { value: tooLong },
    })
    await user.click(screen.getByRole('button', { name: 'Ask' }))

    expect(
      await screen.findByText(
        `Question must be at most ${ASK_QUESTION_MAX_LENGTH} characters.`,
      ),
    ).toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalled()
  })
})

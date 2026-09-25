import { describe, expect, it, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { CreateTicketPage } from './CreateTicketPage'
import { mockJsonResponse, renderWithProviders } from '../test/test-utils'

describe('CreateTicketPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('shows inline validation errors for required fields', async () => {
    vi.spyOn(globalThis, 'fetch')
    const user = userEvent.setup()

    renderWithProviders(<CreateTicketPage />, {
      routerProps: { initialEntries: ['/tickets/new'] },
    })

    await user.click(screen.getByRole('button', { name: 'Save ticket' }))

    expect(screen.getByText('Title is required.')).toBeInTheDocument()
    expect(screen.getByText('Description is required.')).toBeInTheDocument()
    expect(globalThis.fetch).not.toHaveBeenCalled()
  })

  it('shows server validation errors from API response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      mockJsonResponse(
        {
          error: 'Validation failed',
          details: ['title must not be blank'],
        },
        { status: 400 },
      ),
    )

    const user = userEvent.setup()
    renderWithProviders(<CreateTicketPage />, {
      routerProps: { initialEntries: ['/tickets/new'] },
    })

    await user.type(screen.getByLabelText('Title'), 'Test ticket')
    await user.type(screen.getByLabelText('Description'), 'Valid description')
    await user.click(screen.getByRole('button', { name: 'Save ticket' }))

    expect(await screen.findByText('Could not create ticket')).toBeInTheDocument()
    expect(document.getElementById('title-error')).toHaveTextContent(
      'title must not be blank',
    )
  })
})

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import {
  createTicketApiMock,
  mockJsonResponse,
  renderWithAppRouter,
  sampleTicket,
} from '../test/test-utils'

describe('App router flows', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('loads and renders ticket detail from the router', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    expect(
      await screen.findByRole('heading', {
        name: sampleTicket.title,
      }),
    ).toBeInTheDocument()
    expect(screen.getByLabelText('Description')).toHaveValue(
      sampleTicket.description,
    )
  })

  it('saves edited ticket fields through the detail route', async () => {
    const fetchMock = vi
      .fn()
      .mockImplementation(createTicketApiMock())
    vi.spyOn(globalThis, 'fetch').mockImplementation(fetchMock)

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    const titleInput = await screen.findByLabelText('Title')
    await user.clear(titleInput)
    await user.type(titleInput, 'Updated checkout failure')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith(
        expect.stringContaining('/api/tickets/TKT-1001'),
        expect.objectContaining({ method: 'PATCH' }),
      )
    })

    expect(await screen.findByDisplayValue('Updated checkout failure')).toBeInTheDocument()
  })

  it('performs an allowed status transition on ticket detail', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    await screen.findByRole('button', { name: 'Mark In progress' })
    await user.click(screen.getByRole('button', { name: 'Mark In progress' }))

    expect(await screen.findByText('In progress')).toBeInTheDocument()
  })

  it('submits a comment and shows it in the timeline', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    await user.type(
      await screen.findByLabelText('Add comment'),
      'Gateway timeout matched provider maintenance.',
    )
    await user.click(screen.getByRole('button', { name: 'Post comment' }))

    expect(
      await screen.findByText('Gateway timeout matched provider maintenance.'),
    ).toBeInTheDocument()
  })

  it('navigates to the created ticket after save on create route', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/tickets/new'] })

    await user.type(await screen.findByLabelText('Title'), 'New support ticket')
    await user.type(
      screen.getByLabelText('Description'),
      'Customer cannot complete checkout.',
    )
    await user.click(screen.getByRole('button', { name: 'Save ticket' }))

    expect(
      await screen.findByRole('heading', { name: 'New support ticket' }),
    ).toBeInTheDocument()
    expect(screen.getByText('TKT-1002')).toBeInTheDocument()
  })

  it('navigates from ask citations to ticket detail', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/ask'] })

    await user.type(
      screen.getByLabelText('Your question'),
      'What caused previous payment failures?',
    )
    await user.click(screen.getByRole('button', { name: 'Ask' }))

    const citation = await screen.findByRole('link', { name: 'TKT-1001' })
    await user.click(citation)

    expect(
      await screen.findByRole('heading', {
        name: sampleTicket.title,
      }),
    ).toBeInTheDocument()
  })
})

describe('App router loading state', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('shows ticket detail loading status before data resolves', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(
      () =>
        new Promise((resolve) => {
          setTimeout(
            () => resolve(mockJsonResponse(sampleTicket)),
            50,
          )
        }),
    )

    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    expect(screen.getByText('Loading tickets…')).toBeInTheDocument()
    expect(
      await screen.findByRole('heading', { name: sampleTicket.title }),
    ).toBeInTheDocument()
  })
})

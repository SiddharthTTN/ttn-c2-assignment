import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fireEvent, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import {
  createTicketApiMock,
  mockJsonResponse,
  renderWithAppRouter,
  sampleTicket,
  secondSampleTicket,
} from '../test/test-utils'
import { COMMENT_BODY_MAX_LENGTH } from '../pages/TicketDetailPage'

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

  it('resets detail form state when navigating between cached ticket routes', async () => {
    const fetchMock = vi
      .fn()
      .mockImplementation(
        createTicketApiMock([sampleTicket, secondSampleTicket]),
      )
    vi.spyOn(globalThis, 'fetch').mockImplementation(fetchMock)

    const user = userEvent.setup()
    const { router } = renderWithAppRouter({
      initialEntries: ['/tickets/TKT-1002', '/tickets/TKT-1001'],
      initialIndex: 1,
    })

    await screen.findByRole('heading', { name: sampleTicket.title })
    await router.navigate('/tickets/TKT-1002')

    expect(
      await screen.findByRole('heading', { name: secondSampleTicket.title }),
    ).toBeInTheDocument()
    expect(screen.getByLabelText('Title')).toHaveValue(secondSampleTicket.title)
    expect(screen.getByLabelText('Description')).toHaveValue(
      secondSampleTicket.description,
    )

    await user.clear(screen.getByLabelText('Title'))
    await user.type(screen.getByLabelText('Title'), 'Spinner fix deployed')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith(
        expect.stringContaining('/api/tickets/TKT-1002'),
        expect.objectContaining({ method: 'PATCH' }),
      )
    })
    expect(
      fetchMock.mock.calls.some(
        ([url, init]) =>
          String(url).includes('/api/tickets/TKT-1001') &&
          init?.method === 'PATCH',
      ),
    ).toBe(false)
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

    expect(screen.getByText('Loading ticket…')).toBeInTheDocument()
    expect(
      await screen.findByRole('heading', { name: sampleTicket.title }),
    ).toBeInTheDocument()
  })
})

describe('App router unsaved changes', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('blocks navigation and keeps editing when the user stays', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    const titleInput = await screen.findByLabelText('Title')
    await user.type(titleInput, ' extra')
    await user.click(screen.getByRole('link', { name: 'Back to list' }))

    const dialog = await screen.findByRole('dialog')
    expect(dialog).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Keep editing' }))

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    })
    expect(screen.getByRole('heading', { name: sampleTicket.title })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Back to list' })).toHaveFocus()
    expect(titleInput).toHaveValue(`${sampleTicket.title} extra`)
  })

  it('discards edits and proceeds when the user confirms leaving', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    const titleInput = await screen.findByLabelText('Title')
    await user.type(titleInput, ' unsaved')
    await user.click(screen.getByRole('link', { name: 'Back to list' }))

    await user.click(
      await screen.findByRole('button', { name: 'Discard changes' }),
    )

    expect(
      await screen.findByRole('heading', { name: 'Tickets' }),
    ).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('closes the blocker dialog on Escape and restores focus', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    await user.type(await screen.findByLabelText('Title'), ' draft')
    const cancelButton = screen.getByRole('button', { name: 'Cancel' })
    await user.click(cancelButton)

    await screen.findByRole('dialog')
    await user.keyboard('{Escape}')

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    })
    expect(cancelButton).toHaveFocus()
  })

  it('traps focus within the unsaved changes dialog', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    await user.type(await screen.findByLabelText('Title'), ' edit')
    await user.click(screen.getByRole('link', { name: 'Back to list' }))

    const keepEditing = await screen.findByRole('button', {
      name: 'Keep editing',
    })
    const discardChanges = screen.getByRole('button', {
      name: 'Discard changes',
    })
    expect(keepEditing).toHaveFocus()

    await user.tab()
    expect(discardChanges).toHaveFocus()

    await user.tab()
    expect(keepEditing).toHaveFocus()

    await user.tab({ shift: true })
    expect(discardChanges).toHaveFocus()
  })
})

describe('App router comment validation', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('enforces the comment body max length in the UI', async () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(createTicketApiMock())

    const user = userEvent.setup()
    renderWithAppRouter({ initialEntries: ['/tickets/TKT-1001'] })

    const commentField = await screen.findByLabelText('Add comment')
    expect(commentField).toHaveAttribute(
      'maxLength',
      String(COMMENT_BODY_MAX_LENGTH),
    )

    const tooLong = `${'a'.repeat(COMMENT_BODY_MAX_LENGTH)}b`
    fireEvent.change(commentField, { target: { value: tooLong } })
    await user.click(screen.getByRole('button', { name: 'Post comment' }))

    expect(
      await screen.findByText(
        `Comment must be at most ${COMMENT_BODY_MAX_LENGTH} characters.`,
      ),
    ).toBeInTheDocument()
  })
})

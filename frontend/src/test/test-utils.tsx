import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, type RenderOptions } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import type { ReactElement, ReactNode } from 'react'
import { createAppMemoryRouter } from '../router'
import type { Ticket } from '../api/types'

interface AppRouterOptions {
  initialEntries?: string[]
  initialIndex?: number
  queryClient?: QueryClient
}

export function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  })
}

export function renderWithAppRouter(
  {
    initialEntries = ['/tickets'],
    initialIndex,
    queryClient = createTestQueryClient(),
  }: AppRouterOptions = {},
  options?: Omit<RenderOptions, 'wrapper'>,
) {
  const router = createAppMemoryRouter(initialEntries, initialIndex)

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    )
  }

  return {
    queryClient,
    router,
    ...render(<RouterProvider router={router} />, {
      wrapper: Wrapper,
      ...options,
    }),
  }
}

export function renderWithProviders(
  ui: ReactElement,
  {
    initialEntries = ['/'],
    initialIndex,
    queryClient = createTestQueryClient(),
  }: AppRouterOptions = {},
  options?: Omit<RenderOptions, 'wrapper'>,
) {
  const router = createMemoryRouter([{ path: '*', element: ui }], {
    initialEntries,
    initialIndex,
  })

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    )
  }

  return {
    queryClient,
    router,
    ...render(<RouterProvider router={router} />, {
      wrapper: Wrapper,
      ...options,
    }),
  }
}

export function mockJsonResponse(body: unknown, init?: ResponseInit) {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
    ...init,
  })
}

export const sampleTicket = {
  id: 'TKT-1001',
  title: 'Payment failed at checkout',
  description: 'Card authorization timed out.',
  status: 'OPEN' as const,
  priority: 'HIGH' as const,
  assignee: 'Asha',
  category: 'Payments',
  resolutionNotes: null,
  createdAt: '2026-09-24T10:00:00Z',
  updatedAt: '2026-09-25T07:30:00Z',
  comments: [] as Array<{
    id: number
    ticketId: string
    body: string
    createdAt: string
  }>,
}

export const secondSampleTicket = {
  id: 'TKT-1002',
  title: 'Login page spinner',
  description: 'Users see an infinite spinner after SSO.',
  status: 'IN_PROGRESS' as const,
  priority: 'MEDIUM' as const,
  assignee: 'Ravi',
  category: 'Identity',
  resolutionNotes: null,
  createdAt: '2026-09-23T14:00:00Z',
  updatedAt: '2026-09-25T06:00:00Z',
  comments: [],
}

function nextAvailableTicketId(ticketStates: Map<string, Ticket>) {
  let sequence = 1002
  while (ticketStates.has(`TKT-${sequence}`)) {
    sequence += 1
  }
  return `TKT-${sequence}`
}

export function createTicketApiMock(
  initialTicketOrTickets: Ticket | Ticket[] = sampleTicket,
) {
  const initialTickets = Array.isArray(initialTicketOrTickets)
    ? initialTicketOrTickets
    : [initialTicketOrTickets]
  const ticketStates = new Map(
    initialTickets.map((ticket) => [ticket.id, structuredClone(ticket)]),
  )

  const getTicketIdFromPath = (path: string) => {
    const match = path.match(/\/api\/tickets\/(TKT-\d+)$/)
    return match?.[1]
  }

  return (input: RequestInfo | URL, init?: RequestInit) => {
    const url =
      typeof input === 'string'
        ? input
        : input instanceof URL
          ? input.href
          : input.url
    const method = (init?.method ?? 'GET').toUpperCase()
    const path = url.replace(/^https?:\/\/[^/]+/, '')

    if (method === 'GET' && /\/api\/tickets\/TKT-\d+$/.test(path)) {
      const ticketId = getTicketIdFromPath(path)
      const ticket = ticketId ? ticketStates.get(ticketId) : undefined
      if (!ticket) {
        return Promise.resolve(
          mockJsonResponse({ error: 'Not found' }, { status: 404 }),
        )
      }
      return Promise.resolve(mockJsonResponse(ticket))
    }
    if (method === 'GET' && path.startsWith('/api/tickets')) {
      return Promise.resolve(
        mockJsonResponse({ items: Array.from(ticketStates.values()) }),
      )
    }
    if (method === 'POST' && path === '/api/tickets') {
      const body = init?.body ? JSON.parse(String(init.body)) : {}
      const newId = nextAvailableTicketId(ticketStates)
      const created = {
        ...sampleTicket,
        id: newId,
        ...body,
        status: 'OPEN' as const,
        comments: [],
        createdAt: '2026-09-25T08:00:00Z',
        updatedAt: '2026-09-25T08:00:00Z',
      }
      ticketStates.set(newId, created)
      return Promise.resolve(mockJsonResponse(created, { status: 201 }))
    }
    if (method === 'PATCH' && /\/api\/tickets\/TKT-\d+$/.test(path)) {
      const ticketId = getTicketIdFromPath(path)
      const ticketState = ticketId ? ticketStates.get(ticketId) : undefined
      if (!ticketState) {
        return Promise.resolve(
          mockJsonResponse({ error: 'Not found' }, { status: 404 }),
        )
      }
      const body = init?.body ? JSON.parse(String(init.body)) : {}
      const updated = {
        ...ticketState,
        ...body,
        updatedAt: '2026-09-25T08:10:00Z',
      }
      ticketStates.set(ticketId!, updated)
      return Promise.resolve(mockJsonResponse(updated))
    }
    if (method === 'POST' && path.endsWith('/status')) {
      const ticketId = path.match(/\/api\/tickets\/(TKT-\d+)\/status$/)?.[1]
      const ticketState = ticketId ? ticketStates.get(ticketId) : undefined
      if (!ticketState) {
        return Promise.resolve(
          mockJsonResponse({ error: 'Not found' }, { status: 404 }),
        )
      }
      const body = init?.body ? JSON.parse(String(init.body)) : {}
      const updated = {
        ...ticketState,
        status: body.status,
        updatedAt: '2026-09-25T08:11:00Z',
      }
      ticketStates.set(ticketId!, updated)
      return Promise.resolve(mockJsonResponse(updated))
    }
    if (method === 'POST' && path.endsWith('/comments')) {
      const ticketId = path.match(
        /\/api\/tickets\/(TKT-\d+)\/comments$/,
      )?.[1]
      const ticketState = ticketId ? ticketStates.get(ticketId) : undefined
      if (!ticketState) {
        return Promise.resolve(
          mockJsonResponse({ error: 'Not found' }, { status: 404 }),
        )
      }
      const body = init?.body ? JSON.parse(String(init.body)) : {}
      const comment = {
        id: 42,
        ticketId: ticketState.id,
        body: body.body,
        createdAt: '2026-09-25T08:12:00Z',
      }
      const updated = {
        ...ticketState,
        comments: [...ticketState.comments, comment],
        updatedAt: '2026-09-25T08:12:00Z',
      }
      ticketStates.set(ticketId!, updated)
      return Promise.resolve(mockJsonResponse(comment, { status: 201 }))
    }
    if (method === 'POST' && path === '/api/ai/ask') {
      return Promise.resolve(
        mockJsonResponse({
          answer: 'Prior payment failures involved timeouts [TKT-1001].',
          ticketIds: ['TKT-1001'],
          noRelevantTickets: false,
        }),
      )
    }

    return Promise.resolve(
      mockJsonResponse(
        { error: 'Unhandled test request', details: [path, method] },
        { status: 500 },
      ),
    )
  }
}

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, type RenderOptions } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import type { ReactElement, ReactNode } from 'react'
import { createAppMemoryRouter } from '../router'

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

export function createTicketApiMock(initialTicket = sampleTicket) {
  let ticketState = structuredClone(initialTicket)

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
      return Promise.resolve(mockJsonResponse(ticketState))
    }
    if (method === 'GET' && path.startsWith('/api/tickets')) {
      return Promise.resolve(mockJsonResponse({ items: [ticketState] }))
    }
    if (method === 'POST' && path === '/api/tickets') {
      const body = init?.body ? JSON.parse(String(init.body)) : {}
      ticketState = {
        ...sampleTicket,
        id: 'TKT-1002',
        ...body,
        status: 'OPEN',
        comments: [],
        createdAt: '2026-09-25T08:00:00Z',
        updatedAt: '2026-09-25T08:00:00Z',
      }
      return Promise.resolve(mockJsonResponse(ticketState, { status: 201 }))
    }
    if (method === 'PATCH' && /\/api\/tickets\/TKT-\d+$/.test(path)) {
      const body = init?.body ? JSON.parse(String(init.body)) : {}
      ticketState = {
        ...ticketState,
        ...body,
        updatedAt: '2026-09-25T08:10:00Z',
      }
      return Promise.resolve(mockJsonResponse(ticketState))
    }
    if (method === 'POST' && path.endsWith('/status')) {
      const body = init?.body ? JSON.parse(String(init.body)) : {}
      ticketState = {
        ...ticketState,
        status: body.status,
        updatedAt: '2026-09-25T08:11:00Z',
      }
      return Promise.resolve(mockJsonResponse(ticketState))
    }
    if (method === 'POST' && path.endsWith('/comments')) {
      const body = init?.body ? JSON.parse(String(init.body)) : {}
      const comment = {
        id: 42,
        ticketId: ticketState.id,
        body: body.body,
        createdAt: '2026-09-25T08:12:00Z',
      }
      ticketState = {
        ...ticketState,
        comments: [...ticketState.comments, comment],
        updatedAt: '2026-09-25T08:12:00Z',
      }
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

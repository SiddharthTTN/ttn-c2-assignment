import { apiRequest } from './client'
import type {
  AskResponse,
  Comment,
  CreateTicketRequest,
  PatchTicketRequest,
  Ticket,
  TicketListResponse,
  TicketStatus,
} from './types'

export interface ListTicketsParams {
  q?: string
  status?: TicketStatus | ''
}

export async function listTickets(
  params: ListTicketsParams = {},
): Promise<TicketListResponse> {
  const search = new URLSearchParams()
  const q = params.q?.trim()
  if (q) {
    search.set('q', q)
  }
  if (params.status) {
    search.set('status', params.status)
  }
  const query = search.toString()
  return apiRequest<TicketListResponse>(
    query ? `/tickets?${query}` : '/tickets',
  )
}

export async function getTicket(id: string): Promise<Ticket> {
  return apiRequest<Ticket>(`/tickets/${encodeURIComponent(id)}`)
}

export async function createTicket(
  payload: CreateTicketRequest,
): Promise<Ticket> {
  return apiRequest<Ticket>('/tickets', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function patchTicket(
  id: string,
  payload: PatchTicketRequest,
): Promise<Ticket> {
  return apiRequest<Ticket>(`/tickets/${encodeURIComponent(id)}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export async function changeTicketStatus(
  id: string,
  status: TicketStatus,
): Promise<Ticket> {
  return apiRequest<Ticket>(`/tickets/${encodeURIComponent(id)}/status`, {
    method: 'POST',
    body: JSON.stringify({ status }),
  })
}

export async function addComment(
  id: string,
  body: string,
): Promise<Comment> {
  return apiRequest<Comment>(`/tickets/${encodeURIComponent(id)}/comments`, {
    method: 'POST',
    body: JSON.stringify({ body }),
  })
}

export async function askQuestion(question: string): Promise<AskResponse> {
  return apiRequest<AskResponse>('/ai/ask', {
    method: 'POST',
    body: JSON.stringify({ question }),
  })
}

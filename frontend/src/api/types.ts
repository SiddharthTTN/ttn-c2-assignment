export type TicketStatus =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CLOSED'
  | 'CANCELLED'

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface Comment {
  id: number
  ticketId: string
  body: string
  createdAt: string
}

export interface Ticket {
  id: string
  title: string
  description: string
  status: TicketStatus
  priority: TicketPriority
  assignee: string | null
  category: string | null
  resolutionNotes: string | null
  createdAt: string
  updatedAt: string
  comments: Comment[]
}

export interface ApiErrorBody {
  error: string
  details: string[]
}

export interface TicketListResponse {
  items: Ticket[]
}

export interface AskResponse {
  answer: string
  ticketIds: string[]
  noRelevantTickets: boolean
}

export interface CreateTicketRequest {
  title: string
  description: string
  priority: TicketPriority
  assignee?: string | null
  category?: string | null
  resolutionNotes?: string | null
}

export interface PatchTicketRequest {
  title?: string
  description?: string
  priority?: TicketPriority
  assignee?: string | null
  category?: string | null
  resolutionNotes?: string | null
}

export const TICKET_STATUSES: TicketStatus[] = [
  'OPEN',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
  'CANCELLED',
]

export const TICKET_PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH']

export const STATUS_LABELS: Record<TicketStatus, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In progress',
  RESOLVED: 'Resolved',
  CLOSED: 'Closed',
  CANCELLED: 'Cancelled',
}

export const PRIORITY_LABELS: Record<TicketPriority, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
}

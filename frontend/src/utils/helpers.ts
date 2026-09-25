import type { TicketStatus } from '../api/types'

const ALLOWED_NEXT: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ['IN_PROGRESS', 'CANCELLED'],
  IN_PROGRESS: ['RESOLVED', 'CANCELLED'],
  RESOLVED: ['CLOSED'],
  CLOSED: [],
  CANCELLED: [],
}

export function getAllowedNextStatuses(status: TicketStatus): TicketStatus[] {
  return ALLOWED_NEXT[status]
}

export function formatDateTime(iso: string): string {
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(iso))
}

export function mapDetailsToFields(
  details: string[],
): Partial<Record<string, string>> {
  const fieldErrors: Partial<Record<string, string>> = {}
  for (const detail of details) {
    const lower = detail.toLowerCase()
    if (lower.includes('title')) {
      fieldErrors.title = detail
    } else if (lower.includes('description')) {
      fieldErrors.description = detail
    } else if (lower.includes('priority')) {
      fieldErrors.priority = detail
    } else if (lower.includes('assignee')) {
      fieldErrors.assignee = detail
    } else if (lower.includes('category')) {
      fieldErrors.category = detail
    } else if (lower.includes('resolution')) {
      fieldErrors.resolutionNotes = detail
    } else if (lower.includes('body') || lower.includes('comment')) {
      fieldErrors.body = detail
    } else if (lower.includes('question')) {
      fieldErrors.question = detail
    }
  }
  return fieldErrors
}

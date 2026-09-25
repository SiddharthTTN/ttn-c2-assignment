import type { FormEvent } from 'react'
import {
  TICKET_PRIORITIES,
  PRIORITY_LABELS,
  type TicketPriority,
} from '../api/types'
import { FieldError } from './FieldError'

export interface TicketFormValues {
  title: string
  description: string
  priority: TicketPriority
  assignee: string
  category: string
  resolutionNotes: string
}

interface TicketFormProps {
  values: TicketFormValues
  errors: Partial<Record<keyof TicketFormValues, string>>
  disabled?: boolean
  submitLabel: string
  onChange: (field: keyof TicketFormValues, value: string) => void
  onSubmit: () => void
  onCancel: () => void
}

export function TicketForm({
  values,
  errors,
  disabled = false,
  submitLabel,
  onChange,
  onSubmit,
  onCancel,
}: TicketFormProps) {
  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    onSubmit()
  }

  return (
    <form className="form-panel" onSubmit={handleSubmit} noValidate>
      <div className="field">
        <label htmlFor="title">Title</label>
        <input
          id="title"
          name="title"
          value={values.title}
          aria-invalid={Boolean(errors.title)}
          aria-describedby={errors.title ? 'title-error' : undefined}
          disabled={disabled}
          onChange={(event) => onChange('title', event.target.value)}
        />
        <FieldError id="title-error" message={errors.title} />
      </div>
      <div className="field">
        <label htmlFor="description">Description</label>
        <textarea
          id="description"
          name="description"
          value={values.description}
          aria-invalid={Boolean(errors.description)}
          aria-describedby={errors.description ? 'description-error' : undefined}
          disabled={disabled}
          onChange={(event) => onChange('description', event.target.value)}
        />
        <FieldError id="description-error" message={errors.description} />
      </div>
      <div className="field">
        <label htmlFor="priority">Priority</label>
        <select
          id="priority"
          name="priority"
          value={values.priority}
          aria-invalid={Boolean(errors.priority)}
          aria-describedby={errors.priority ? 'priority-error' : undefined}
          disabled={disabled}
          onChange={(event) =>
            onChange('priority', event.target.value as TicketPriority)
          }
        >
          {TICKET_PRIORITIES.map((priority) => (
            <option key={priority} value={priority}>
              {PRIORITY_LABELS[priority]}
            </option>
          ))}
        </select>
        <FieldError id="priority-error" message={errors.priority} />
      </div>
      <div className="field">
        <label htmlFor="assignee">Assignee</label>
        <input
          id="assignee"
          name="assignee"
          value={values.assignee}
          aria-invalid={Boolean(errors.assignee)}
          aria-describedby={errors.assignee ? 'assignee-error' : undefined}
          disabled={disabled}
          onChange={(event) => onChange('assignee', event.target.value)}
        />
        <FieldError id="assignee-error" message={errors.assignee} />
      </div>
      <div className="field">
        <label htmlFor="category">Category</label>
        <input
          id="category"
          name="category"
          value={values.category}
          aria-invalid={Boolean(errors.category)}
          aria-describedby={errors.category ? 'category-error' : undefined}
          disabled={disabled}
          onChange={(event) => onChange('category', event.target.value)}
        />
        <FieldError id="category-error" message={errors.category} />
      </div>
      <div className="field">
        <label htmlFor="resolutionNotes">Resolution notes</label>
        <textarea
          id="resolutionNotes"
          name="resolutionNotes"
          value={values.resolutionNotes}
          aria-invalid={Boolean(errors.resolutionNotes)}
          aria-describedby={
            errors.resolutionNotes ? 'resolutionNotes-error' : undefined
          }
          disabled={disabled}
          onChange={(event) => onChange('resolutionNotes', event.target.value)}
        />
        <FieldError
          id="resolutionNotes-error"
          message={errors.resolutionNotes}
        />
      </div>
      <div className="page-header__actions">
        <button type="submit" className="btn btn--primary" disabled={disabled}>
          {submitLabel}
        </button>
        <button
          type="button"
          className="btn btn--secondary"
          disabled={disabled}
          onClick={onCancel}
        >
          Cancel
        </button>
      </div>
    </form>
  )
}

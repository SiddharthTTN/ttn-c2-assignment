import type { FormEvent } from 'react'
import { FieldError } from './FieldError'

interface CommentFormProps {
  value: string
  error?: string
  maxLength?: number
  disabled?: boolean
  onChange: (value: string) => void
  onSubmit: () => void
}

export function CommentForm({
  value,
  error,
  maxLength,
  disabled = false,
  onChange,
  onSubmit,
}: CommentFormProps) {
  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    onSubmit()
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="comment-body">Add comment</label>
        <textarea
          id="comment-body"
          name="body"
          value={value}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? 'comment-body-error' : undefined}
          disabled={disabled}
          maxLength={maxLength}
          onChange={(event) => onChange(event.target.value)}
        />
        <FieldError id="comment-body-error" message={error} />
      </div>
      <button type="submit" className="btn btn--primary" disabled={disabled}>
        Post comment
      </button>
    </form>
  )
}

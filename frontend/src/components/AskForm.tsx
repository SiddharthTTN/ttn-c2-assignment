import type { FormEvent } from 'react'
import { FieldError } from './FieldError'
import { EXAMPLE_QUESTIONS } from '../constants/askExamples'

interface AskFormProps {
  question: string
  error?: string
  disabled?: boolean
  onQuestionChange: (value: string) => void
  onSubmit: () => void
}

export function AskForm({
  question,
  error,
  disabled = false,
  onQuestionChange,
  onSubmit,
}: AskFormProps) {
  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    onSubmit()
  }

  return (
    <div className="ask-layout">
      <p id="ask-examples-label" className="loading-status">
        Example questions
      </p>
      <div className="example-chips" aria-labelledby="ask-examples-label">
        {EXAMPLE_QUESTIONS.map((example) => (
          <button
            key={example}
            type="button"
            className="chip"
            disabled={disabled}
            onClick={() => onQuestionChange(example)}
          >
            {example}
          </button>
        ))}
      </div>
      <form onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="ask-question">Your question</label>
          <textarea
            id="ask-question"
            name="question"
            value={question}
            aria-invalid={Boolean(error)}
            aria-describedby={error ? 'ask-question-error' : undefined}
            disabled={disabled}
            onChange={(event) => onQuestionChange(event.target.value)}
          />
          <FieldError id="ask-question-error" message={error} />
        </div>
        <button type="submit" className="btn btn--primary" disabled={disabled}>
          Ask
        </button>
      </form>
    </div>
  )
}

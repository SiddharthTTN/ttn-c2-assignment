import { useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { askQuestion } from '../api/tickets'
import { ApiError } from '../api/client'
import { AnswerCard } from '../components/AnswerCard'
import { AskForm } from '../components/AskForm'
import { CitationList } from '../components/CitationList'
import { ErrorAlert } from '../components/ErrorAlert'
import { NoMatchState } from '../components/NoMatchState'
import { PageHeader } from '../components/PageHeader'
import { mapDetailsToFields } from '../utils/helpers'
import type { AskResponse } from '../api/types'

export function AskPage() {
  const [question, setQuestion] = useState('')
  const [fieldError, setFieldError] = useState<string | undefined>()
  const [serviceError, setServiceError] = useState<string | null>(null)
  const [result, setResult] = useState<AskResponse | null>(null)
  const answerHeadingRef = useRef<HTMLHeadingElement>(null)

  const mutation = useMutation({
    mutationFn: askQuestion,
    onSuccess: (response) => {
      setResult(response)
      setServiceError(null)
      setFieldError(undefined)
      requestAnimationFrame(() => {
        answerHeadingRef.current?.focus()
      })
    },
    onError: (error) => {
      setResult(null)
      if (error instanceof ApiError) {
        if (error.status === 400) {
          const mapped = mapDetailsToFields(error.body.details)
          setFieldError(mapped.question ?? error.body.details[0])
        }
        setServiceError(error.body.error)
        return
      }
      setServiceError('Unable to ask question.')
    },
  })

  const handleSubmit = () => {
    const trimmed = question.trim()
    if (!trimmed) {
      setFieldError('Question is required.')
      return
    }
    mutation.mutate(trimmed)
  }

  return (
    <>
      <PageHeader title="Ask tickets" />
      <AskForm
        question={question}
        error={fieldError}
        disabled={mutation.isPending}
        onQuestionChange={(value) => {
          setQuestion(value)
          setFieldError(undefined)
          setServiceError(null)
        }}
        onSubmit={handleSubmit}
      />
      {mutation.isPending ? (
        <p className="loading-status" aria-live="polite">
          Searching ticket history and generating an answer…
        </p>
      ) : null}
      {serviceError ? (
        <ErrorAlert
          title="Ask request failed"
          message={serviceError}
          details={
            mutation.error instanceof ApiError
              ? mutation.error.body.details
              : undefined
          }
        />
      ) : null}
      {serviceError && mutation.error instanceof ApiError && mutation.error.status === 503 ? (
        <button
          type="button"
          className="btn btn--secondary"
          disabled={mutation.isPending}
          onClick={handleSubmit}
        >
          Retry ask
        </button>
      ) : null}
      {result?.noRelevantTickets ? <NoMatchState /> : null}
      {result && !result.noRelevantTickets ? (
        <>
          <AnswerCard ref={answerHeadingRef} answer={result.answer} />
          <CitationList ticketIds={result.ticketIds} />
        </>
      ) : null}
    </>
  )
}

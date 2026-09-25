import { useCallback, useMemo, useState } from 'react'
import { Link, useBlocker, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  addComment,
  changeTicketStatus,
  getTicket,
  patchTicket,
} from '../api/tickets'
import { ticketKeys } from '../api/queryKeys'
import type { Ticket, TicketStatus } from '../api/types'
import { ApiError } from '../api/client'
import { CommentForm } from '../components/CommentForm'
import { CommentTimeline } from '../components/CommentTimeline'
import { ConfirmNavigationDialog } from '../components/ConfirmNavigationDialog'
import { ErrorAlert } from '../components/ErrorAlert'
import { LoadingSkeleton } from '../components/LoadingSkeleton'
import { PageHeader } from '../components/PageHeader'
import { StatusActions } from '../components/StatusActions'
import { TicketSummary } from '../components/TicketSummary'
import {
  TicketForm,
  type TicketFormValues,
} from '../components/TicketForm'
import { mapDetailsToFields } from '../utils/helpers'

export const COMMENT_BODY_MAX_LENGTH = 10000

function toFormValues(ticket: {
  title: string
  description: string
  priority: TicketFormValues['priority']
  assignee: string | null
  category: string | null
  resolutionNotes: string | null
}): TicketFormValues {
  return {
    title: ticket.title,
    description: ticket.description,
    priority: ticket.priority,
    assignee: ticket.assignee ?? '',
    category: ticket.category ?? '',
    resolutionNotes: ticket.resolutionNotes ?? '',
  }
}

function valuesEqual(a: TicketFormValues, b: TicketFormValues): boolean {
  return (
    a.title === b.title &&
    a.description === b.description &&
    a.priority === b.priority &&
    a.assignee === b.assignee &&
    a.category === b.category &&
    a.resolutionNotes === b.resolutionNotes
  )
}

export function TicketDetailPage() {
  const { id = '' } = useParams()

  const ticketQuery = useQuery({
    queryKey: ticketKeys.detail(id),
    queryFn: () => getTicket(id),
    enabled: Boolean(id),
  })

  const loadErrorMessage = useMemo(() => {
    if (!ticketQuery.error) {
      return null
    }
    if (ticketQuery.error instanceof ApiError) {
      return ticketQuery.error.body.error
    }
    return 'Unable to load ticket.'
  }, [ticketQuery.error])

  if (ticketQuery.isLoading) {
    return (
      <>
        <PageHeader title="Ticket details" />
        <LoadingSkeleton rows={3} statusText="Loading ticket…" />
      </>
    )
  }

  if (ticketQuery.isError) {
    const isNotFound =
      ticketQuery.error instanceof ApiError && ticketQuery.error.status === 404
    return (
      <>
        <PageHeader title="Ticket not found" />
        <ErrorAlert
          title={isNotFound ? 'Ticket not found' : 'Could not load ticket'}
          message={loadErrorMessage ?? 'Unable to load ticket.'}
          details={
            ticketQuery.error instanceof ApiError
              ? ticketQuery.error.body.details
              : undefined
          }
        />
        <Link to="/tickets" className="btn btn--primary">
          Back to tickets
        </Link>
      </>
    )
  }

  return (
    <TicketDetailContent key={ticketQuery.data!.id} ticket={ticketQuery.data!} />
  )
}

function TicketDetailContent({ ticket }: { ticket: Ticket }) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [savedValues, setSavedValues] = useState(() => toFormValues(ticket))
  const [values, setValues] = useState(() => toFormValues(ticket))
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof TicketFormValues, string>>
  >({})
  const [saveError, setSaveError] = useState<string | null>(null)
  const [statusError, setStatusError] = useState<string | null>(null)
  const [commentValue, setCommentValue] = useState('')
  const [commentError, setCommentError] = useState<string | undefined>()
  const [commentActionError, setCommentActionError] = useState<string | null>(
    null,
  )

  const isDirty = !valuesEqual(values, savedValues)
  const blocker = useBlocker(isDirty)
  const keepEditing = useCallback(() => blocker.reset?.(), [blocker.reset])
  const discardChanges = useCallback(
    () => blocker.proceed?.(),
    [blocker.proceed],
  )

  const saveMutation = useMutation({
    mutationFn: () =>
      patchTicket(ticket.id, {
        title: values.title.trim(),
        description: values.description.trim(),
        priority: values.priority,
        assignee: values.assignee.trim() || null,
        category: values.category.trim() || null,
        resolutionNotes: values.resolutionNotes.trim() || null,
      }),
    onSuccess: async (updated) => {
      const next = toFormValues(updated)
      setSavedValues(next)
      setValues(next)
      setFieldErrors({})
      setSaveError(null)
      await queryClient.invalidateQueries({
        queryKey: ticketKeys.detail(ticket.id),
      })
      await queryClient.invalidateQueries({ queryKey: ticketKeys.lists() })
    },
    onError: (error) => {
      if (error instanceof ApiError) {
        setFieldErrors(mapDetailsToFields(error.body.details))
        setSaveError(error.body.error)
        return
      }
      setSaveError('Unable to save ticket changes.')
    },
  })

  const statusMutation = useMutation({
    mutationFn: (status: TicketStatus) => changeTicketStatus(ticket.id, status),
    onSuccess: async () => {
      setStatusError(null)
      await queryClient.invalidateQueries({
        queryKey: ticketKeys.detail(ticket.id),
      })
      await queryClient.invalidateQueries({ queryKey: ticketKeys.lists() })
    },
    onError: (error) => {
      if (error instanceof ApiError) {
        setStatusError(error.body.error)
        return
      }
      setStatusError('Unable to update ticket status.')
    },
  })

  const commentMutation = useMutation({
    mutationFn: () => addComment(ticket.id, commentValue.trim()),
    onSuccess: async () => {
      setCommentValue('')
      setCommentError(undefined)
      setCommentActionError(null)
      await queryClient.invalidateQueries({
        queryKey: ticketKeys.detail(ticket.id),
      })
    },
    onError: (error) => {
      if (error instanceof ApiError) {
        const mapped = mapDetailsToFields(error.body.details)
        setCommentError(mapped.body)
        setCommentActionError(error.body.error)
        return
      }
      setCommentActionError('Unable to add comment.')
    },
  })

  const handleSave = () => {
    if (!values.title.trim()) {
      setFieldErrors({ title: 'Title is required.' })
      return
    }
    if (!values.description.trim()) {
      setFieldErrors({ description: 'Description is required.' })
      return
    }
    saveMutation.mutate()
  }

  const handleCommentSubmit = () => {
    const trimmed = commentValue.trim()
    if (!trimmed) {
      setCommentError('Comment is required.')
      return
    }
    if (trimmed.length > COMMENT_BODY_MAX_LENGTH) {
      setCommentError(
        `Comment must be at most ${COMMENT_BODY_MAX_LENGTH} characters.`,
      )
      return
    }
    commentMutation.mutate()
  }

  return (
    <>
      <PageHeader
        title={ticket.title}
        actions={
          <Link to="/tickets" className="btn btn--secondary">
            Back to list
          </Link>
        }
      />
      <div className="detail-layout">
        <div>
          {saveError ? (
            <ErrorAlert
              title="Could not save changes"
              message={saveError}
              details={
                saveMutation.error instanceof ApiError
                  ? saveMutation.error.body.details
                  : undefined
              }
            />
          ) : null}
          <TicketForm
            values={values}
            errors={fieldErrors}
            disabled={saveMutation.isPending || statusMutation.isPending}
            submitLabel={saveMutation.isPending ? 'Saving…' : 'Save changes'}
            onChange={(field, value) => {
              setValues((current) => ({ ...current, [field]: value }))
              setFieldErrors((current) => ({ ...current, [field]: undefined }))
              setSaveError(null)
            }}
            onSubmit={handleSave}
            onCancel={() => navigate('/tickets')}
          />
          <section className="card" aria-labelledby="comments-heading">
            <h2 id="comments-heading" className="h2">
              Comments
            </h2>
            <CommentTimeline comments={ticket.comments} />
            {commentActionError ? (
              <ErrorAlert
                title="Could not post comment"
                message={commentActionError}
                details={
                  commentMutation.error instanceof ApiError
                    ? commentMutation.error.body.details
                    : undefined
                }
              />
            ) : null}
            <CommentForm
              value={commentValue}
              error={commentError}
              maxLength={COMMENT_BODY_MAX_LENGTH}
              disabled={commentMutation.isPending}
              onChange={(value) => {
                setCommentValue(value)
                setCommentError(undefined)
                setCommentActionError(null)
              }}
              onSubmit={handleCommentSubmit}
            />
          </section>
        </div>
        <aside className="card">
          <h2 className="h2">Summary</h2>
          <TicketSummary ticket={ticket} />
          <h3 className="h3">Status actions</h3>
          {statusError ? (
            <ErrorAlert title="Status change failed" message={statusError} />
          ) : null}
          <StatusActions
            currentStatus={ticket.status}
            disabled={statusMutation.isPending || saveMutation.isPending}
            onTransition={(status) => statusMutation.mutate(status)}
          />
        </aside>
      </div>
      <ConfirmNavigationDialog
        open={blocker.state === 'blocked'}
        onStay={keepEditing}
        onLeave={discardChanges}
      />
    </>
  )
}

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createTicket } from '../api/tickets'
import { ticketKeys } from '../api/queryKeys'
import { ApiError } from '../api/client'
import { ErrorAlert } from '../components/ErrorAlert'
import { PageHeader } from '../components/PageHeader'
import {
  TicketForm,
  type TicketFormValues,
} from '../components/TicketForm'
import { mapDetailsToFields } from '../utils/helpers'

const emptyValues: TicketFormValues = {
  title: '',
  description: '',
  priority: 'MEDIUM',
  assignee: '',
  category: '',
  resolutionNotes: '',
}

function validate(values: TicketFormValues): Partial<Record<keyof TicketFormValues, string>> {
  const errors: Partial<Record<keyof TicketFormValues, string>> = {}
  if (!values.title.trim()) {
    errors.title = 'Title is required.'
  }
  if (!values.description.trim()) {
    errors.description = 'Description is required.'
  }
  return errors
}

export function CreateTicketPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [values, setValues] = useState<TicketFormValues>(emptyValues)
  const [clientErrors, setClientErrors] = useState<
    Partial<Record<keyof TicketFormValues, string>>
  >({})
  const [actionError, setActionError] = useState<string | null>(null)

  const mutation = useMutation({
    mutationFn: createTicket,
    onSuccess: async (ticket) => {
      await queryClient.invalidateQueries({ queryKey: ticketKeys.all })
      navigate(`/tickets/${ticket.id}`)
    },
    onError: (error) => {
      if (error instanceof ApiError) {
        setClientErrors(mapDetailsToFields(error.body.details))
        setActionError(error.body.error)
        return
      }
      setActionError('Unable to create ticket.')
    },
  })

  const handleChange = (field: keyof TicketFormValues, value: string) => {
    setValues((current) => ({ ...current, [field]: value }))
    setClientErrors((current) => ({ ...current, [field]: undefined }))
    setActionError(null)
  }

  const handleSubmit = () => {
    const nextErrors = validate(values)
    setClientErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) {
      return
    }
    mutation.mutate({
      title: values.title.trim(),
      description: values.description.trim(),
      priority: values.priority,
      assignee: values.assignee.trim() || null,
      category: values.category.trim() || null,
      resolutionNotes: values.resolutionNotes.trim() || null,
    })
  }

  return (
    <>
      <PageHeader title="Create ticket" />
      {actionError ? (
        <ErrorAlert
          title="Could not create ticket"
          message={actionError}
          details={
            mutation.error instanceof ApiError
              ? mutation.error.body.details
              : undefined
          }
        />
      ) : null}
      <TicketForm
        values={values}
        errors={clientErrors}
        disabled={mutation.isPending}
        submitLabel={mutation.isPending ? 'Saving…' : 'Save ticket'}
        onChange={handleChange}
        onSubmit={handleSubmit}
        onCancel={() => navigate('/tickets')}
      />
    </>
  )
}

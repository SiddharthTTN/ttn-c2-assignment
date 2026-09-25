import type { ReactNode } from 'react'

interface EmptyStateProps {
  title: string
  description: string
  action?: ReactNode
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="empty-state">
      <h2 className="empty-state__title h2">{title}</h2>
      <p className="empty-state__body">{description}</p>
      {action}
    </div>
  )
}

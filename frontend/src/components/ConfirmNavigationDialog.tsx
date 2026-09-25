import { useEffect, useRef } from 'react'

interface ConfirmNavigationDialogProps {
  open: boolean
  onStay: () => void
  onLeave: () => void
}

function getFocusableElements(container: HTMLElement): HTMLElement[] {
  return Array.from(
    container.querySelectorAll<HTMLElement>(
      'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
    ),
  )
}

export function ConfirmNavigationDialog({
  open,
  onStay,
  onLeave,
}: ConfirmNavigationDialogProps) {
  const stayButtonRef = useRef<HTMLButtonElement>(null)
  const dialogRef = useRef<HTMLDivElement>(null)
  const previouslyFocusedRef = useRef<HTMLElement | null>(null)

  useEffect(() => {
    if (!open) {
      return
    }

    previouslyFocusedRef.current = document.activeElement as HTMLElement | null
    stayButtonRef.current?.focus()

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        event.preventDefault()
        onStay()
        return
      }

      if (event.key !== 'Tab' || !dialogRef.current) {
        return
      }

      const focusable = getFocusableElements(dialogRef.current)
      if (focusable.length === 0) {
        return
      }

      const first = focusable[0]
      const last = focusable[focusable.length - 1]
      const active = document.activeElement

      if (event.shiftKey && active === first) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && active === last) {
        event.preventDefault()
        first.focus()
      }
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      previouslyFocusedRef.current?.focus()
    }
  }, [open, onStay])

  if (!open) {
    return null
  }

  return (
    <div className="dialog-backdrop" role="presentation" onClick={onStay}>
      <div
        ref={dialogRef}
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="unsaved-dialog-title"
        onClick={(event) => event.stopPropagation()}
      >
        <h2 id="unsaved-dialog-title" className="h2">
          Discard unsaved changes?
        </h2>
        <p>You have unsaved edits on this ticket. Leave without saving?</p>
        <div className="dialog__actions">
          <button
            ref={stayButtonRef}
            type="button"
            className="btn btn--primary"
            onClick={onStay}
          >
            Keep editing
          </button>
          <button type="button" className="btn btn--destructive" onClick={onLeave}>
            Discard changes
          </button>
        </div>
      </div>
    </div>
  )
}

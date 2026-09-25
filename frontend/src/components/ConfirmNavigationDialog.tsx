import { useEffect, useRef } from 'react'

interface ConfirmNavigationDialogProps {
  open: boolean
  onStay: () => void
  onLeave: () => void
}

export function ConfirmNavigationDialog({
  open,
  onStay,
  onLeave,
}: ConfirmNavigationDialogProps) {
  const stayButtonRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (open) {
      stayButtonRef.current?.focus()
    }
  }, [open])

  if (!open) {
    return null
  }

  return (
    <div className="dialog-backdrop" role="presentation" onClick={onStay}>
      <div
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

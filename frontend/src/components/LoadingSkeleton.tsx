export function LoadingSkeleton({
  rows = 5,
  statusText = 'Loading tickets…',
}: {
  rows?: number
  statusText?: string
}) {
  return (
    <div aria-busy="true" aria-live="polite">
      <p className="loading-status">{statusText}</p>
      {Array.from({ length: rows }, (_, index) => (
        <div key={index} className="skeleton skeleton-table-row" />
      ))}
    </div>
  )
}

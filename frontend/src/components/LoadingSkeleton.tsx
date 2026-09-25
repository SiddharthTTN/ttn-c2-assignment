export function LoadingSkeleton({ rows = 5 }: { rows?: number }) {
  return (
    <div aria-busy="true" aria-live="polite">
      <p className="loading-status">Loading tickets…</p>
      {Array.from({ length: rows }, (_, index) => (
        <div key={index} className="skeleton skeleton-table-row" />
      ))}
    </div>
  )
}

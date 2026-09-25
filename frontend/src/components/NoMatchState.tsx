export function NoMatchState() {
  return (
    <section className="no-match" role="status" aria-labelledby="no-match-heading">
      <h2 id="no-match-heading" className="h2">
        No relevant ticket evidence
      </h2>
      <p>
        No relevant tickets were found for this question. Try rephrasing or
        creating a ticket with more detail.
      </p>
    </section>
  )
}

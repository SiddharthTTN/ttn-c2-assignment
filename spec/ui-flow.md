# UI flows

The desktop-first React application has:

1. Ticket list: search input, status filter, result table, empty state, create action.
2. Create ticket: labeled title, description, priority, assignee, category, and resolution-note fields with inline errors.
3. Ticket details: immutable id/timestamps, editable fields, chronological comments, and only currently valid status actions.
4. Ask tickets: question form, loading state, grounded answer, linked ticket-id citations, no-match state, and service error state.

Navigation uses a persistent header with Tickets and Ask destinations. Form controls have visible labels, keyboard focus indicators, associated error text, and submit locking while requests are active. At widths below 1024 px the layout may stack without claiming mobile-native support.

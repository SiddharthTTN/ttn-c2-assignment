import { Navigate, type RouteObject } from 'react-router-dom'
import { AppShell } from './components/AppShell'
import { AskPage } from './pages/AskPage'
import { CreateTicketPage } from './pages/CreateTicketPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { TicketDetailPage } from './pages/TicketDetailPage'
import { TicketListPage } from './pages/TicketListPage'

export const appRoutes: RouteObject[] = [
  {
    path: '/',
    element: <AppShell />,
    children: [
      { index: true, element: <Navigate to="/tickets" replace /> },
      { path: 'tickets', element: <TicketListPage /> },
      { path: 'tickets/new', element: <CreateTicketPage /> },
      { path: 'tickets/:id', element: <TicketDetailPage /> },
      { path: 'ask', element: <AskPage /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]

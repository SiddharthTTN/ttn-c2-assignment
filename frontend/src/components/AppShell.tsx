import { Outlet } from 'react-router-dom'
import { TopNav } from './TopNav'

export function AppShell() {
  return (
    <div className="app-shell">
      <TopNav />
      <main className="main-content" id="main-content">
        <Outlet />
      </main>
    </div>
  )
}

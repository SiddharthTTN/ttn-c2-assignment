import { NavLink } from 'react-router-dom'
import { navLinkClassName } from './navLinkClassName'

export function TopNav() {
  return (
    <header className="top-nav">
      <div className="top-nav__inner">
        <NavLink to="/tickets" className="top-nav__brand">
          Support Console
        </NavLink>
        <nav aria-label="Primary">
          <ul className="top-nav__links">
            <li>
              <NavLink to="/tickets" className={navLinkClassName} end>
                Tickets
              </NavLink>
            </li>
            <li>
              <NavLink to="/ask" className={navLinkClassName}>
                Ask
              </NavLink>
            </li>
          </ul>
        </nav>
      </div>
    </header>
  )
}

import { Link } from 'react-router-dom'
import { PageHeader } from '../components/PageHeader'

export function NotFoundPage() {
  return (
    <>
      <PageHeader title="Page not found" />
      <p>The page you requested does not exist.</p>
      <Link to="/tickets" className="btn btn--primary">
        Go to tickets
      </Link>
    </>
  )
}

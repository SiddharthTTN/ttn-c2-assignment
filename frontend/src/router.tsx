import {
  createBrowserRouter,
  createMemoryRouter,
  type MemoryRouterProps,
} from 'react-router-dom'
import { appRoutes } from './routes'

export function createAppBrowserRouter() {
  return createBrowserRouter(appRoutes)
}

export function createAppMemoryRouter(
  initialEntries: MemoryRouterProps['initialEntries'] = ['/tickets'],
  initialIndex?: number,
) {
  return createMemoryRouter(appRoutes, {
    initialEntries,
    initialIndex,
  })
}

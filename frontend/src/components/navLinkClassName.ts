export function navLinkClassName({ isActive }: { isActive: boolean }) {
  return isActive ? 'top-nav__link top-nav__link--active' : 'top-nav__link'
}

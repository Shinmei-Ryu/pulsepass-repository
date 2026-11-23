import { Navigate, Outlet } from 'react-router'
import { tieneRol, useAuth } from '@/funcionalidades/auth/hooks'
import type { Rol } from '@/funcionalidades/auth/tipos'
import { RUTAS } from '@/lib/rutas'

// Siempre va dentro de RutaProtegida: aquí la sesión ya está autenticada
export default function RutaPorRol({ roles }: { roles: Rol[] }) {
  const { sesion } = useAuth()
  if (!tieneRol(sesion, ...roles)) return <Navigate to={RUTAS.sinPermisos} replace />
  return <Outlet />
}

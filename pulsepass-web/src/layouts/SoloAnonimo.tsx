import { Navigate, Outlet, useLocation } from 'react-router'
import Cargando from '@/componentes/Cargando'
import { useAuth } from '@/funcionalidades/auth/hooks'
import { destinoSeguro } from '@/lib/rutas'

// Login y registro no tienen sentido con sesión: se devuelve a la persona a donde iba
export default function SoloAnonimo() {
    const { sesion } = useAuth()
    const location = useLocation()

    if (sesion.estado === 'cargando') return <Cargando etiqueta="Verificando sesión…" />
    if (sesion.estado === 'autenticado') return <Navigate to={destinoSeguro(location.state)} replace />
    return <Outlet />
}

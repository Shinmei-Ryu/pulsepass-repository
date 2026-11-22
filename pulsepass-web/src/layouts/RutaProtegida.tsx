import { Navigate, Outlet, useLocation } from 'react-router'
import Cargando from '@/componentes/Cargando'
import { useAuth } from '@/funcionalidades/auth/hooks'
import { RUTAS } from '@/lib/rutas'

// Es experiencia de usuario, no seguridad: quien decide de verdad es el backend (FE-004)
export default function RutaProtegida() {
    const { sesion, motivoCierre } = useAuth()
    const location = useLocation()

    // Mientras se confirma el token guardado no se redirige: evitaría un salto a /login al recargar
    if (sesion.estado === 'cargando') return <Cargando etiqueta="Verificando sesión…" />

    if (sesion.estado === 'anonimo') {
        const desde = location.pathname + location.search
        // Si la sesión se cerró por vencimiento, el login lo explica (PRD 12.2: state.motivo)
        const state = motivoCierre === 'expirada' ? { desde, motivo: motivoCierre } : { desde }
        return <Navigate to={RUTAS.login} replace state={state} />
    }

    return <Outlet />
}

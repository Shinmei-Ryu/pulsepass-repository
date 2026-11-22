import { createContext, useContext } from 'react'
import type { MotivoCierre, Rol, Sesion } from './tipos'

export interface AuthContextValor {
    sesion: Sesion
    motivoCierre: MotivoCierre | null
    iniciarSesion: (identifier: string, password: string) => Promise<void>
    cerrarSesion: (motivo?: MotivoCierre | null) => void
    olvidarMotivo: () => void
}

// El contexto vive aquí, junto a sus hooks; el proveedor (AuthContext.tsx) solo lo llena.
// Van en archivos separados porque un archivo que exporta un componente no debe exportar nada más (react-refresh).
export const AuthContext = createContext<AuthContextValor | null>(null)

export function useAuth(): AuthContextValor {
    const valor = useContext(AuthContext)
    if (!valor) throw new Error('useAuth debe usarse dentro de <AuthProvider>')
    return valor
}

export function tieneRol(sesion: Sesion, ...roles: Rol[]): boolean {
    return sesion.estado === 'autenticado' && sesion.usuario.roles.some((rol) => roles.includes(rol))
}

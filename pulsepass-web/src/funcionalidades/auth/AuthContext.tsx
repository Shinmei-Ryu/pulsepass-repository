import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { conectarSesion } from '@/lib/http'
import { AuthContext, type AuthContextValor } from './hooks'
import { iniciarSesionApi, obtenerYo } from './api'
import { SesionGuardadaSchema, type MotivoCierre, type Sesion, type SesionGuardada } from './tipos'

const CLAVE = 'pulsepass.sesion'

function leerGuardada(): SesionGuardada | null {
    try {
        const crudo = sessionStorage.getItem(CLAVE)
        if (!crudo) return null
        const json: unknown = JSON.parse(crudo)
        const guardada = SesionGuardadaSchema.safeParse(json)
        if (guardada.success && Date.now() < guardada.data.expiraEn) return guardada.data
        sessionStorage.removeItem(CLAVE) // vencida o malformada
    } catch {
        // JSON roto o almacenamiento bloqueado: se trata como "sin sesión"
    }
    return null
}

function guardar(guardada: SesionGuardada | null) {
    try {
        if (guardada) sessionStorage.setItem(CLAVE, JSON.stringify(guardada))
        else sessionStorage.removeItem(CLAVE)
    } catch {
        // sin almacenamiento, la sesión vive solo en memoria
    }
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const queryClient = useQueryClient()
    const [guardada, setGuardada] = useState<SesionGuardada | null>(leerGuardada)
    const [sesion, setSesion] = useState<Sesion>(() => (guardada ? { estado: 'cargando' } : { estado: 'anonimo' }))
    const [motivoCierre, setMotivoCierre] = useState<MotivoCierre | null>(null)

    // http.ts lee el token desde aquí: un ref siempre tiene el valor más reciente
    const tokenRef = useRef<SesionGuardada | null>(guardada)

    const establecer = useCallback((nueva: SesionGuardada | null) => {
        tokenRef.current = nueva
        setGuardada(nueva)
        guardar(nueva)
    }, [])

    const cerrarSesion = useCallback(
        (motivo: MotivoCierre | null = 'manual') => {
            establecer(null)
            queryClient.clear() // ningún dato del usuario anterior queda en caché
            setSesion({ estado: 'anonimo' })
            setMotivoCierre(motivo)
        },
        [establecer, queryClient],
    )

    // 1. Conecta el cliente HTTP con la sesión
    useEffect(() => {
        conectarSesion({
            obtenerToken: () => {
                const actual = tokenRef.current
                // Un token vencido no se envía: la API responde 401 incluso en rutas públicas
                return actual && Date.now() < actual.expiraEn ? actual.token : null
            },
            alRecibir401: () => cerrarSesion('expirada'),
        })
    }, [cerrarSesion])

    // 2. Al cargar la página: si había token vigente, se confirma con /auth/me
    useEffect(() => {
        if (!tokenRef.current) return
        // StrictMode monta dos veces en desarrollo: la respuesta del primer montaje se descarta
        let vigente = true
        obtenerYo()
            .then((usuario) => {
                // Si entre tanto otra petición recibió un 401, la sesión ya se cerró: no se revive
                if (vigente && tokenRef.current) setSesion({ estado: 'autenticado', usuario })
            })
            .catch(() => {
                // Un 401 ya cerró la sesión desde http.ts; aquí se cubren red y 5xx
                if (vigente && tokenRef.current) cerrarSesion(null)
            })
        return () => {
            vigente = false
        }
    }, [cerrarSesion])

    // 3. Cierre programado justo cuando vence el token
    useEffect(() => {
        if (!guardada) return
        const restante = Math.max(guardada.expiraEn - Date.now(), 0)
        const reloj = setTimeout(() => cerrarSesion('expirada'), restante)
        return () => clearTimeout(reloj)
    }, [guardada, cerrarSesion])

    const iniciarSesion = useCallback(
        async (identifier: string, password: string) => {
            const respuesta = await iniciarSesionApi({ identifier, password })
            establecer({ token: respuesta.accessToken, expiraEn: Date.now() + respuesta.expiresIn * 1000 })
            try {
                // Los roles salen de /auth/me, nunca de decodificar el JWT
                const usuario = await obtenerYo()
                setMotivoCierre(null)
                setSesion({ estado: 'autenticado', usuario })
            } catch (error) {
                cerrarSesion(null)
                throw error
            }
        },
        [establecer, cerrarSesion],
    )

    const olvidarMotivo = useCallback(() => setMotivoCierre(null), [])

    const valor = useMemo<AuthContextValor>(
        () => ({ sesion, motivoCierre, iniciarSesion, cerrarSesion, olvidarMotivo }),
        [sesion, motivoCierre, iniciarSesion, cerrarSesion, olvidarMotivo],
    )

    return <AuthContext value={valor}>{children}</AuthContext>
}

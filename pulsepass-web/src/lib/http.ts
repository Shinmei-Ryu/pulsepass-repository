import { z } from 'zod'

// Forma única de todos los errores del backend
const ErrorResponseSchema = z.object({
    timestamp: z.string(),
    status: z.number(),
    error: z.string(),
    message: z.string(),
    details: z.record(z.string(), z.string()),
})

export class ApiError extends Error {
    status: number
    details: Record<string, string>

    constructor(status: number, message: string, details: Record<string, string> = {}) {
        super(message)
        this.name = 'ApiError'
        this.status = status
        this.details = details
    }
}

// La sesión vive en AuthProvider; http solo conoce estas dos funciones
interface EnlaceSesion {
    obtenerToken: () => string | null
    alRecibir401: () => void
}

let enlace: EnlaceSesion = { obtenerToken: () => null, alRecibir401: () => {} }

export function conectarSesion(nuevo: EnlaceSesion) {
    enlace = nuevo
}

interface Opciones {
    method?: 'GET' | 'POST' | 'PATCH'
    body?: unknown
}

const BASE: string = import.meta.env.VITE_API_BASE_URL ?? '/api'

export async function http<T>(ruta: string, esquema: z.ZodType<T>, opciones: Opciones = {}): Promise<T> {
    const token = enlace.obtenerToken()
    const headers = new Headers({ Accept: 'application/json' })
    if (opciones.body !== undefined) headers.set('Content-Type', 'application/json')
    if (token) headers.set('Authorization', `Bearer ${token}`)

    let res: Response
    try {
        // URL absoluta: en el navegador no cambia nada, y en las pruebas evita depender de cómo se resuelven las relativas
        res = await fetch(new URL(BASE + ruta, window.location.origin), {
            method: opciones.method ?? 'GET',
            headers,
            body: opciones.body === undefined ? undefined : JSON.stringify(opciones.body),
        })
    } catch {
        // fetch solo rechaza por fallos de red (servidor apagado, sin conexión)
        throw new ApiError(0, 'No pudimos conectar con el servidor.')
    }

    const cuerpo: unknown = await res.json().catch(() => null)

    if (!res.ok) {
        // Solo hay "sesión expirada" si de verdad se envió un token (el 401 del login no cuenta)
        if (res.status === 401 && token) enlace.alRecibir401()
        const error = ErrorResponseSchema.safeParse(cuerpo)
        throw error.success
            ? new ApiError(res.status, error.data.message, error.data.details)
            : new ApiError(res.status, 'Ocurrió un error inesperado. Inténtalo más tarde.')
    }

    const datos = esquema.safeParse(cuerpo)
    if (!datos.success) throw new ApiError(500, 'El servidor respondió con un formato inesperado.')
    return datos.data
}

// Texto seguro para mostrar a la persona según el tipo de error
export function mensajeDeError(error: unknown): string {
    if (!(error instanceof ApiError)) return 'Ocurrió un error inesperado. Inténtalo más tarde.'
    if (error.status === 0) return error.message
    if (error.status === 401) return 'Tu sesión expiró. Inicia sesión de nuevo.'
    if (error.status === 403) return 'No tienes permisos para realizar esta acción.'
    if (error.status >= 500) return 'Ocurrió un error inesperado. Inténtalo más tarde.'
    return error.message
}

export function esEstado(error: unknown, ...estados: number[]): boolean {
    return error instanceof ApiError && estados.includes(error.status)
}

import { z } from 'zod'

// La API no lista todos los eventos (GAP-01): se recuerdan solo los CÓDIGOS creados en esta pestaña.
// Los datos de cada evento se siguen pidiendo con useEvento: nada de copiar estado del servidor.
const CLAVE = 'pulsepass.eventosCreados'
const CodigosSchema = z.array(z.string())

export function leerEventosCreados(): string[] {
    try {
        const json: unknown = JSON.parse(sessionStorage.getItem(CLAVE) ?? '[]')
        const codigos = CodigosSchema.safeParse(json)
        return codigos.success ? codigos.data : []
    } catch {
        return []
    }
}

export function recordarEventoCreado(eventCode: string) {
    try {
        const codigos = leerEventosCreados().filter((codigo) => codigo !== eventCode)
        sessionStorage.setItem(CLAVE, JSON.stringify([eventCode, ...codigos]))
    } catch {
        // sin almacenamiento, la lista simplemente no se recuerda
    }
}

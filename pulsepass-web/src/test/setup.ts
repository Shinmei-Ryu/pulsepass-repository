import '@testing-library/jest-dom/vitest'
import { configure } from '@testing-library/react'
import { server } from './mocks/server'

// Las páginas se cargan con lazy: la primera importación puede tardar más que el segundo por defecto
configure({ asyncUtilTimeout: 5000 })

// "error": una petición sin handler hace fallar la prueba en vez de salir a la red
// Peticiones que llegaron a MSW sin respuesta simulada. Se revisan al final de cada prueba.
const sinRespuesta: string[] = []

beforeAll(() => {
    server.listen()
    server.events.on('request:unhandled', ({ request }) => {
        sinRespuesta.push(`${request.method} ${request.url}`)
    })
})

// QT-FE-009: reloj fijo. Solo se simula Date; setTimeout sigue siendo real para que user-event y waitFor funcionen
beforeEach(() => {
    vi.useFakeTimers({ toFake: ['Date'] })
    vi.setSystemTime(new Date('2026-11-16T15:00:00'))
})

afterEach(() => {
    vi.useRealTimers()
    server.resetHandlers()
    sessionStorage.clear()
    localStorage.clear()
    // Si una prueba pidió una ruta sin respuesta simulada, falla y dice cuál
    const pendientes = sinRespuesta.splice(0)
    if (pendientes.length > 0) {
        throw new Error(`Peticiones sin respuesta simulada: ${pendientes.join(', ')}`)
    }
})

afterAll(() => server.close())

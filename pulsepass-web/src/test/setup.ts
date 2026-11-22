import '@testing-library/jest-dom/vitest'
import { configure } from '@testing-library/react'
import { server } from './mocks/server'

// Las páginas se cargan con lazy: la primera importación puede tardar más que el segundo por defecto
configure({ asyncUtilTimeout: 5000 })

// "error": una petición sin handler hace fallar la prueba en vez de salir a la red
beforeAll(() => server.listen({ onUnhandledFrame: 'error' }))

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
})

afterAll(() => server.close())


import { http as red, HttpResponse } from 'msw'
import { z } from 'zod'
import { errorApi } from '@/test/mocks/handlers'
import { server } from '@/test/mocks/server'
import { ApiError, conectarSesion, http } from './http'

const Esquema = z.object({ ok: z.boolean() })

afterEach(() => conectarSesion({ obtenerToken: () => null, alRecibir401: () => {} }))

describe('lib/http', () => {
    it('TEST-FE-LIB-001: añade Authorization Bearer cuando hay sesión', async () => {
        let cabecera: string | null = null
        server.use(
            red.get('*/api/prueba', ({ request }) => {
                cabecera = request.headers.get('Authorization')
                return HttpResponse.json({ ok: true })
            }),
        )
        conectarSesion({ obtenerToken: () => 'abc', alRecibir401: () => {} })

        await http('/prueba', Esquema)

        expect(cabecera).toBe('Bearer abc')
    })

    it('TEST-FE-LIB-002: un ErrorResponse válido se convierte en ApiError con status y details', async () => {
        server.use(red.get('*/api/prueba', () => errorApi(400, 'Request validation failed', { name: 'Event name is required' })))

        const error = await http('/prueba', Esquema).catch((e: unknown) => e)

        expect(error).toBeInstanceOf(ApiError)
        expect(error).toMatchObject({
            status: 400,
            message: 'Request validation failed',
            details: { name: 'Event name is required' },
        })
    })

    it('TEST-FE-LIB-003: un cuerpo de error que no cumple el contrato da un ApiError genérico', async () => {
        server.use(red.get('*/api/prueba', () => new HttpResponse('<html>Bad Gateway</html>', { status: 502 })))

        const error = await http('/prueba', Esquema).catch((e: unknown) => e)

        expect(error).toBeInstanceOf(ApiError)
        expect(error).toMatchObject({ status: 502, details: {} })
        expect((error as ApiError).message).not.toContain('html')
    })

    it('TEST-FE-LIB-004: un 401 con token dispara el cierre de sesión; sin token, no', async () => {
        const alRecibir401 = vi.fn()
        server.use(red.get('*/api/prueba', () => errorApi(401, 'Invalid or expired token')))

        conectarSesion({ obtenerToken: () => null, alRecibir401 })
        await http('/prueba', Esquema).catch(() => {})
        expect(alRecibir401).not.toHaveBeenCalled()

        conectarSesion({ obtenerToken: () => 'abc', alRecibir401 })
        await http('/prueba', Esquema).catch(() => {})
        expect(alRecibir401).toHaveBeenCalledTimes(1)
    })
})

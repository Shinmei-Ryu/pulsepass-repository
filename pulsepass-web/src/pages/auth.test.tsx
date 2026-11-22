import { screen, waitFor } from '@testing-library/react'
import { http as red, HttpResponse } from 'msw'
import { errorApi } from '@/test/mocks/handlers'
import { server } from '@/test/mocks/server'
import { esperarSesion, renderizar } from '@/test/utils'

type Usuario = ReturnType<typeof renderizar>['usuario']

async function llenarRegistro(usuario: Usuario, password: string) {
    await usuario.type(await screen.findByLabelText('Nombre de usuario'), 'andrea')
    await usuario.type(screen.getByLabelText('Correo electrónico'), 'andrea@pulsepass.test')
    await usuario.type(screen.getByLabelText('Nombres'), 'Andrea')
    await usuario.type(screen.getByLabelText('Apellidos'), 'Ruiz')
    await usuario.type(screen.getByLabelText('Fecha de nacimiento'), '1998-04-12')
    await usuario.type(screen.getByLabelText('Contraseña'), password)
    await usuario.click(screen.getByRole('button', { name: 'Crear cuenta' }))
}

describe('Autenticación y guardas', () => {
    it('TEST-FE-AUTH-001: login válido deja sesión autenticada y vuelve a la ruta de origen', async () => {
        const { usuario, router } = renderizar('/mis-tickets')

        await usuario.type(await screen.findByLabelText('Usuario o correo'), 'andrea')
        await usuario.type(screen.getByLabelText('Contraseña'), 'UnaFraseSeguraLarga2026!')
        await usuario.click(screen.getByRole('button', { name: 'Ingresar' }))

        // Vuelve a la ruta que pidió antes de que la guarda lo enviara a login
        await waitFor(() => expect(router.state.location.pathname).toBe('/mis-tickets'))
        expect(sessionStorage.getItem('pulsepass.sesion')).toContain('token-USER')
    })

    it('TEST-FE-AUTH-002: login 401 muestra "Credenciales inválidas"', async () => {
        server.use(red.post('*/api/auth/login', () => errorApi(401, 'Invalid credentials')))
        const { usuario } = renderizar('/login')

        await usuario.type(await screen.findByLabelText('Usuario o correo'), 'andrea')
        await usuario.type(screen.getByLabelText('Contraseña'), 'incorrecta')
        await usuario.click(screen.getByRole('button', { name: 'Ingresar' }))

        expect(await screen.findByText('Credenciales inválidas.')).toBeInTheDocument()
    })

    it('TEST-FE-AUTH-003: login sin campos muestra errores y no llama a la API', async () => {
        let llamadas = 0
        server.use(
            red.post('*/api/auth/login', () => {
                llamadas++
                return errorApi(401, 'Invalid credentials')
            }),
        )
        const { usuario } = renderizar('/login')

        await usuario.click(await screen.findByRole('button', { name: 'Ingresar' }))

        expect(await screen.findByText('Escribe tu usuario o correo')).toBeInTheDocument()
        expect(screen.getByText('Escribe tu contraseña')).toBeInTheDocument()
        expect(llamadas).toBe(0)
    })

    it('TEST-FE-AUTH-004: registro con contraseña corta muestra error y no llama a la API', async () => {
        let llamadas = 0
        server.use(
            red.post('*/api/users', () => {
                llamadas++
                return errorApi(400, 'Request validation failed')
            }),
        )
        const { usuario } = renderizar('/registro')

        await llenarRegistro(usuario, 'corta')

        expect(await screen.findByText('Mínimo 12 caracteres')).toBeInTheDocument()
        expect(llamadas).toBe(0)
    })

    it('TEST-FE-AUTH-005: registro 201 redirige a /login con aviso', async () => {
        const { usuario, router } = renderizar('/registro')

        await llenarRegistro(usuario, 'UnaFraseSeguraLarga2026!')

        expect(await screen.findByText('Cuenta creada, inicia sesión.')).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/login')
    })

    it('TEST-FE-AUTH-006: registro 409 muestra un mensaje genérico, no el del backend', async () => {
        server.use(red.post('*/api/users', () => errorApi(409, 'Email already exists: andrea@pulsepass.test')))
        const { usuario } = renderizar('/registro')

        await llenarRegistro(usuario, 'UnaFraseSeguraLarga2026!')

        expect(await screen.findByText('El usuario o correo ya está registrado.')).toBeInTheDocument()
        expect(screen.queryByText(/already exists/)).not.toBeInTheDocument()
    })

    it('TEST-FE-AUTH-007: RutaProtegida envía al anónimo a /login con state.desde', async () => {
        const { router } = renderizar('/cuenta')

        expect(await screen.findByRole('heading', { name: 'Inicia sesión' })).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/login')
        expect(router.state.location.state).toEqual({ desde: '/cuenta' })
    })

    it('TEST-FE-AUTH-008: RutaPorRol envía a un USER que entra a /admin a /403', async () => {
        const { router } = renderizar('/admin/eventos', { rol: 'USER' })

        expect(await screen.findByText('No tienes permisos para acceder a esta sección.')).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/403')
    })

    it('TEST-FE-AUTH-009: RutaPorRol deja pasar a un ADMIN', async () => {
        const { router } = renderizar('/admin/eventos', { rol: 'ADMIN' })
        await esperarSesion()

        expect(router.state.location.pathname).toBe('/admin/eventos')
        expect(screen.queryByText(/No tienes permisos/)).not.toBeInTheDocument()
    })

    it('TEST-FE-AUTH-010: cerrar sesión borra el token y la caché del usuario', async () => {
        const { usuario, queryClient } = renderizar('/cuenta', { rol: 'USER' })
        expect(await screen.findByText('Andrea Ruiz')).toBeInTheDocument()
        expect(queryClient.getQueryData(['usuario', 'andrea@pulsepass.test'])).toBeDefined()

        await usuario.click(screen.getByRole('button', { name: 'Cerrar sesión' }))

        expect(await screen.findByRole('link', { name: 'Ingresar' })).toBeInTheDocument()
        expect(sessionStorage.getItem('pulsepass.sesion')).toBeNull()
        expect(queryClient.getQueryData(['usuario', 'andrea@pulsepass.test'])).toBeUndefined()
    })

    it('TEST-FE-AUTH-011: un token vencido al restaurar la sesión deja al visitante como anónimo', async () => {
        let llamadasMe = 0
        server.use(
            red.get('*/api/auth/me', () => {
                llamadasMe++
                return HttpResponse.json({})
            }),
        )
        sessionStorage.setItem('pulsepass.sesion', JSON.stringify({ token: 'token-USER', expiraEn: Date.now() - 1000 }))

        renderizar('/')

        expect(await screen.findByRole('link', { name: 'Ingresar' })).toBeInTheDocument()
        await waitFor(() => expect(sessionStorage.getItem('pulsepass.sesion')).toBeNull())
        expect(llamadasMe).toBe(0)
    })
})

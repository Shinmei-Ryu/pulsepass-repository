import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import type { ReactNode } from 'react'
import { createMemoryRouter } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import type { Rol } from '@/funcionalidades/auth/tipos'
import { renderizar } from '@/test/utils'
import ErrorPagina from './ErrorPagina'

function Explota(): ReactNode {
    throw new Error('SECRETO: detalle interno que nadie debe ver')
}

describe('Transversales', () => {
    it('TEST-FE-GEN-001: una ruta inexistente muestra el 404', async () => {
        renderizar('/esta/ruta/no/existe')

        expect(await screen.findByText('No encontramos esta página.')).toBeInTheDocument()
    })

    it('TEST-FE-GEN-002: errorElement muestra un mensaje seguro, sin el error interno', async () => {
        // React escribe el error en consola: se silencia para no ensuciar la salida de la prueba
        const consola = vi.spyOn(console, 'error').mockImplementation(() => {})
        const router = createMemoryRouter([{ path: '/', element: <Explota />, errorElement: <ErrorPagina /> }])

        render(
            <QueryClientProvider client={new QueryClient()}>
                <RouterProvider router={router} />
            </QueryClientProvider>,
        )

        expect(await screen.findByRole('heading', { name: '¡Ups! Algo salió mal' })).toBeInTheDocument()
        expect(screen.queryByText(/SECRETO/)).not.toBeInTheDocument()
        consola.mockRestore()
    })

    const casos: { rol: Rol | undefined; visibles: string[]; ocultos: string[] }[] = [
        { rol: undefined, visibles: ['Eventos', 'Ingresar', 'Registrarse'], ocultos: ['Mis tickets', 'Validar ingreso', 'Tickets pagados', 'Administrar eventos'] },
        { rol: 'USER', visibles: ['Eventos', 'Mis tickets'], ocultos: ['Ingresar', 'Validar ingreso', 'Tickets pagados', 'Administrar eventos'] },
        { rol: 'STAFF', visibles: ['Eventos', 'Validar ingreso', 'Tickets pagados'], ocultos: ['Ingresar', 'Mis tickets', 'Administrar eventos'] },
        { rol: 'ADMIN', visibles: ['Eventos', 'Validar ingreso', 'Tickets pagados', 'Administrar eventos'], ocultos: ['Ingresar', 'Mis tickets'] },
    ]

    it.each(casos)('TEST-FE-GEN-003: la navegación muestra los enlaces del rol $rol', async ({ rol, visibles, ocultos }: (typeof casos)[number]) => {
        renderizar('/sedes', { rol })
        // Se espera a que la sesión esté resuelta: con rol aparece el menú de usuario; sin rol, "Ingresar"
        if (rol) await screen.findByRole('button', { name: /Menú de usuario/ })
        else await screen.findByRole('link', { name: 'Ingresar' })

        const navegacion = screen.getByRole('banner')
        for (const texto of visibles) expect(navegacionTiene(navegacion, texto)).toBe(true)
        for (const texto of ocultos) expect(navegacionTiene(navegacion, texto)).toBe(false)
    })

    it('TEST-FE-GEN-004: el menú móvil se abre y se cierra con el teclado', async () => {
        const { usuario } = renderizar('/sedes')
        const boton = await screen.findByRole('button', { name: 'Abrir menú' })

        boton.focus()
        await usuario.keyboard('{Enter}')
        expect(screen.getByRole('navigation', { name: 'Menú móvil' })).toBeInTheDocument()
        expect(screen.getByRole('button', { name: 'Cerrar menú' })).toHaveAttribute('aria-expanded', 'true')

        await usuario.keyboard('{Escape}')
        expect(screen.queryByRole('navigation', { name: 'Menú móvil' })).not.toBeInTheDocument()
        expect(screen.getByRole('button', { name: 'Abrir menú' })).toHaveAttribute('aria-expanded', 'false')
    })
})

function navegacionTiene(navegacion: HTMLElement, texto: string): boolean {
    return Array.from(navegacion.querySelectorAll('a')).some((enlace) => enlace.textContent === texto)
}

import { act, screen, waitFor } from '@testing-library/react'
import { http as red, HttpResponse } from 'msw'
import { server } from '@/test/mocks/server'
import { renderizar } from '@/test/utils'

describe('Catálogo, artistas y sedes', () => {
    it('TEST-FE-EVT-001: la cartelera lista los eventos publicados', async () => {
        renderizar('/eventos')

        expect(await screen.findByRole('link', { name: 'Caribbean Music Fest 2026' })).toBeInTheDocument()
        expect(screen.getByText('1 evento')).toBeInTheDocument()
    })

    it('TEST-FE-EVT-002: una cartelera vacía muestra el estado vacío', async () => {
        server.use(red.get('*/api/events/published', () => HttpResponse.json([])))
        renderizar('/eventos')

        expect(await screen.findByText('Aún no hay eventos publicados')).toBeInTheDocument()
    })

    it('TEST-FE-EVT-003: los filtros de texto y categoría actualizan la URL', async () => {
        const { usuario, router } = renderizar('/eventos')

        await usuario.type(await screen.findByLabelText('Buscar por nombre'), 'jazz')
        await waitFor(() => expect(router.state.location.search).toContain('q=jazz'))

        await usuario.selectOptions(screen.getByLabelText('Categoría'), 'MUSIC')
        await waitFor(() => expect(router.state.location.search).toContain('categoria=MUSIC'))
    })

    it('TEST-FE-EVT-004: el detalle muestra evento, artistas y sede', async () => {
        renderizar('/eventos/CMF-2026')

        expect(await screen.findByRole('heading', { name: 'Caribbean Music Fest 2026' })).toBeInTheDocument()
        expect(await screen.findByRole('link', { name: 'Solar Beat' })).toBeInTheDocument()
        expect(await screen.findByText(/Santa Marta · Capacidad 3/)).toBeInTheDocument()
    })

    it('TEST-FE-EVT-005: un 404 muestra "Evento no disponible"', async () => {
        renderizar('/eventos/NO-EXISTE')

        expect(await screen.findByRole('heading', { name: 'Evento no disponible' })).toBeInTheDocument()
    })

    it('TEST-FE-ART-001: lista los artistas activos', async () => {
        renderizar('/artistas')

        expect(await screen.findByRole('link', { name: /Solar Beat/ })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: /Neon Waves/ })).toBeInTheDocument()
    })

    it('TEST-FE-ART-002: el detalle de artista muestra sus eventos', async () => {
        renderizar('/artistas/1')

        expect(await screen.findByRole('heading', { name: 'Solar Beat' })).toBeInTheDocument()
        expect(await screen.findByRole('link', { name: 'Caribbean Music Fest 2026' })).toBeInTheDocument()
    })

    it('TEST-FE-ART-003: un id no numérico muestra 404 sin llamar a la API', async () => {
        let llamadas = 0
        server.use(
            red.get('*/api/artists/:id', () => {
                llamadas++
                return HttpResponse.json({})
            }),
        )
        renderizar('/artistas/abc')

        expect(await screen.findByRole('heading', { name: 'Artista no disponible' })).toBeInTheDocument()
        expect(llamadas).toBe(0)
    })

    it('TEST-FE-VEN-001: lista de sedes, detalle y 404 de sede', async () => {
        const { router } = renderizar('/sedes')
        expect(await screen.findByRole('link', { name: /Marina Convention Center/ })).toBeInTheDocument()

        // act(): la navegación no viene de un clic, así que se espera explícitamente a que React dibuje
        await act(() => router.navigate('/sedes/VEN-SMR-01'))
        expect(await screen.findByText('VEN-SMR-01')).toBeInTheDocument()
        expect(screen.getByText('3 personas')).toBeInTheDocument()

        await act(() => router.navigate('/sedes/NO-EXISTE'))
        expect(await screen.findByRole('heading', { name: 'Sede no disponible' })).toBeInTheDocument()
    })
})
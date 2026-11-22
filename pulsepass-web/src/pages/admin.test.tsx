import { screen, within } from '@testing-library/react'
import { http as red, HttpResponse } from 'msw'
import { eventoDetalle } from '@/test/mocks/datos'
import { errorApi } from '@/test/mocks/handlers'
import { server } from '@/test/mocks/server'
import { renderizar } from '@/test/utils'

type Usuario = ReturnType<typeof renderizar>['usuario']

async function llenarEvento(usuario: Usuario) {
  await usuario.type(await screen.findByLabelText('Código del evento'), 'NEW-2027')
  await usuario.type(screen.getByLabelText('Nombre'), 'Evento Nuevo')
  await usuario.type(screen.getByLabelText('Fecha y hora'), '2027-01-20T20:00')
  // Las sedes llegan de la API: se espera a que la opción exista antes de elegirla
  await screen.findByRole('option', { name: /Marina Convention Center/ })
  await usuario.selectOptions(screen.getByLabelText('Sede'), 'VEN-SMR-01')
  await usuario.click(screen.getByRole('button', { name: 'Crear evento' }))
}

describe('Administración ADMIN', () => {
  it('TEST-FE-ADM-001: crear evento con campos vacíos muestra errores y no llama a la API', async () => {
    let llamadas = 0
    server.use(
      red.post('*/api/events', () => {
        llamadas++
        return errorApi(400, 'Request validation failed')
      }),
    )
    const { usuario } = renderizar('/admin/eventos/nuevo', { rol: 'ADMIN' })

    await usuario.click(await screen.findByRole('button', { name: 'Crear evento' }))

    expect(await screen.findByText('El código es obligatorio')).toBeInTheDocument()
    expect(screen.getByText('El nombre es obligatorio')).toBeInTheDocument()
    expect(llamadas).toBe(0)
  })

  it('TEST-FE-ADM-002: un 201 navega a la gestión del evento', async () => {
    const { usuario, router } = renderizar('/admin/eventos/nuevo', { rol: 'ADMIN' })

    await llenarEvento(usuario)

    expect(await screen.findByRole('heading', { name: 'Evento Nuevo' })).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/admin/eventos/NEW-2027')
  })

  it('TEST-FE-ADM-003: un 400 con details pone cada error en su campo', async () => {
    server.use(
      red.post('*/api/events', () =>
        errorApi(400, 'Request validation failed', { name: 'Event name is required' }),
      ),
    )
    const { usuario } = renderizar('/admin/eventos/nuevo', { rol: 'ADMIN' })

    await llenarEvento(usuario)

    expect(await screen.findByText('Event name is required')).toBeInTheDocument()
    expect(screen.getByLabelText('Nombre')).toHaveAttribute('aria-invalid', 'true')
  })

  it('TEST-FE-ADM-004: publicar un borrador muestra el badge PUBLISHED', async () => {
    let publicado = false
    server.use(
      red.get('*/api/events/:eventCode', () =>
        HttpResponse.json({ ...eventoDetalle, status: publicado ? 'PUBLISHED' : 'DRAFT' }),
      ),
      red.patch('*/api/events/:eventCode/publish', () => {
        publicado = true
        return HttpResponse.json({ ...eventoDetalle, status: 'PUBLISHED' })
      }),
    )
    const { usuario } = renderizar('/admin/eventos/CMF-2026', { rol: 'ADMIN' })
    expect(await screen.findByText('DRAFT')).toBeInTheDocument()

    await usuario.click(screen.getByRole('button', { name: 'Publicar evento' }))
    await usuario.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Publicar' }))

    expect(await screen.findByText('PUBLISHED')).toBeInTheDocument()
  })

  it('TEST-FE-ADM-005: un 409 al publicar se muestra en el modal', async () => {
    server.use(
      red.get('*/api/events/:eventCode', () => HttpResponse.json({ ...eventoDetalle, status: 'DRAFT' })),
      red.patch('*/api/events/:eventCode/publish', () =>
        errorApi(409, 'Cannot publish an event with a past date.'),
      ),
    )
    const { usuario } = renderizar('/admin/eventos/CMF-2026', { rol: 'ADMIN' })

    await usuario.click(await screen.findByRole('button', { name: 'Publicar evento' }))
    const modal = screen.getByRole('dialog')
    await usuario.click(within(modal).getByRole('button', { name: 'Publicar' }))

    expect(await within(modal).findByText('Cannot publish an event with a past date.')).toBeInTheDocument()
  })

  it('TEST-FE-ADM-006: un duplicado muestra el 409; una asociación válida añade el artista a la lista', async () => {
    let asociados = ['Solar Beat']
    server.use(
      red.get('*/api/events/:eventCode', () =>
        HttpResponse.json({ ...eventoDetalle, status: 'DRAFT', artists: asociados }),
      ),
      red.post('*/api/events/:eventCode/artists/:artistId', () =>
        errorApi(409, 'Artist is already associated with event.'),
      ),
    )
    const { usuario } = renderizar('/admin/eventos/CMF-2026', { rol: 'ADMIN' })

    await screen.findByRole('option', { name: 'Neon Waves' })
    await usuario.selectOptions(screen.getByLabelText('Agregar artista'), 'Neon Waves')
    await usuario.click(screen.getByRole('button', { name: 'Asociar' }))
    expect(await screen.findByText('Artist is already associated with event.')).toBeInTheDocument()

    // Ahora el backend acepta: la invalidación vuelve a pedir el evento y la lista crece
    server.use(
      red.post('*/api/events/:eventCode/artists/:artistId', () => {
        asociados = ['Solar Beat', 'Neon Waves']
        return HttpResponse.json({ ...eventoDetalle, status: 'DRAFT', artists: asociados })
      }),
    )
    await usuario.click(screen.getByRole('button', { name: 'Asociar' }))

    const lista = screen.getByRole('list', { name: 'Artistas asociados' })
    expect(await within(lista).findByText('Neon Waves')).toBeInTheDocument()
  })
})

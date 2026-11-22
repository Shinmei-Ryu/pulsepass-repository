import { screen, waitFor, within } from '@testing-library/react'
import { http as red, HttpResponse } from 'msw'
import { eventoDetalle, perfil, ticketPagado } from '@/test/mocks/datos'
import { errorApi } from '@/test/mocks/handlers'
import { server } from '@/test/mocks/server'
import { esperarSesion, renderizar } from '@/test/utils'

// Pruebas más allá de las 50 mínimas del PRD §21. Cubren los criterios QT-FE-003, 005, 008 y los
// comportamientos de las secciones 9.2, 9.8, 9.19, 10.3 y 12.2. El prefijo X las distingue de las del PRD.
describe('Mapeo de errores (QT-FE-005)', () => {
  it('TEST-FE-X-001: un 500 muestra el mensaje genérico y nunca el del servidor', async () => {
    server.use(
      red.get('*/api/events/published', () => errorApi(500, 'NullPointerException en EventServiceImpl')),
    )
    renderizar('/eventos')

    expect(await screen.findByText('Ocurrió un error inesperado. Inténtalo más tarde.')).toBeInTheDocument()
    expect(screen.queryByText(/NullPointerException/)).not.toBeInTheDocument()
  })

  it('TEST-FE-X-002: una caída de red muestra el aviso de conexión con reintento', async () => {
    server.use(red.get('*/api/events/published', () => HttpResponse.error()))
    renderizar('/eventos')

    expect(await screen.findByText('No pudimos conectar con el servidor.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
  })

  it('TEST-FE-X-003: un 403 de la API al cargar una página lleva a /403', async () => {
    server.use(red.get('*/api/tickets/by-user', () => errorApi(403, 'Access denied')))
    const { router } = renderizar('/mis-tickets', { rol: 'USER' })

    expect(await screen.findByText('No tienes permisos para acceder a esta sección.')).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/403')
  })

  it('TEST-FE-X-004: un 401 en una página pública cierra la sesión y lleva a login con el aviso', async () => {
    server.use(red.get('*/api/events/published', () => errorApi(401, 'Authentication is required')))
    const { router } = renderizar('/eventos', { rol: 'USER' })

    expect(await screen.findByText('Tu sesión expiró. Inicia sesión de nuevo.')).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/login')
    expect(router.state.location.state).toEqual({ desde: '/eventos', motivo: 'expirada' })
    expect(sessionStorage.getItem('pulsepass.sesion')).toBeNull()
  })
})

describe('Formato inválido en formularios (QT-FE-003)', () => {
  it('TEST-FE-X-005: un correo mal formado en el registro muestra el error y no llama a la API', async () => {
    let llamadas = 0
    server.use(
      red.post('*/api/users', () => {
        llamadas++
        return errorApi(400, 'Request validation failed')
      }),
    )
    const { usuario } = renderizar('/registro')

    await usuario.type(await screen.findByLabelText('Nombre de usuario'), 'andrea')
    await usuario.type(screen.getByLabelText('Correo electrónico'), 'esto-no-es-un-correo')
    await usuario.type(screen.getByLabelText('Nombres'), 'Andrea')
    await usuario.type(screen.getByLabelText('Apellidos'), 'Ruiz')
    await usuario.type(screen.getByLabelText('Fecha de nacimiento'), '1998-04-12')
    await usuario.type(screen.getByLabelText('Contraseña'), 'UnaFraseSeguraLarga2026!')
    await usuario.click(screen.getByRole('button', { name: 'Crear cuenta' }))

    expect(await screen.findByText('Escribe un correo válido')).toBeInTheDocument()
    expect(llamadas).toBe(0)
  })

  it('TEST-FE-X-006: un código de evento con caracteres no permitidos no llama a la API', async () => {
    let llamadas = 0
    server.use(
      red.post('*/api/events', () => {
        llamadas++
        return errorApi(400, 'Request validation failed')
      }),
    )
    const { usuario } = renderizar('/admin/eventos/nuevo', { rol: 'ADMIN' })

    await usuario.type(await screen.findByLabelText('Código del evento'), 'CMF 2026!')
    await usuario.type(screen.getByLabelText('Nombre'), 'Evento Nuevo')
    await usuario.click(screen.getByRole('button', { name: 'Crear evento' }))

    expect(await screen.findByText('Solo letras, números, punto, guion y guion bajo')).toBeInTheDocument()
    expect(llamadas).toBe(0)
  })
})

describe('Cobertura de la operación 10 (PRD 10.3)', () => {
  it('TEST-FE-X-007: ?artista= resuelve el artista con by-stage-name y enlaza a su página', async () => {
    renderizar('/eventos?artista=Solar%20Beat')

    expect(await screen.findByRole('link', { name: 'Solar Beat' })).toHaveAttribute('href', '/artistas/1')
  })

  it('TEST-FE-X-008: un artista inexistente en la URL muestra el aviso', async () => {
    server.use(red.get('*/api/artists/by-stage-name', () => errorApi(404, 'Artist not found: Nadie')))
    renderizar('/eventos?artista=Nadie')

    expect(await screen.findByText('No encontramos un artista con ese nombre.')).toBeInTheDocument()
  })
})

describe('Invalidación de caché tras mutaciones (QT-FE-008)', () => {
  it('TEST-FE-X-009: comprar invalida mis tickets, el evento y la cartelera', async () => {
    const { usuario, queryClient } = renderizar('/eventos/CMF-2026', { rol: 'USER' })
    const invalidar = vi.spyOn(queryClient, 'invalidateQueries')
    await esperarSesion()

    await usuario.click(await screen.findByRole('button', { name: 'Comprar entrada' }))
    await usuario.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Confirmar' }))
    await screen.findByRole('heading', { name: '¡Compra realizada con éxito!' })

    expect(invalidar).toHaveBeenCalledWith({ queryKey: ['tickets', 'usuario', 'andrea@pulsepass.test'] })
    expect(invalidar).toHaveBeenCalledWith({ queryKey: ['evento', 'CMF-2026'] })
    expect(invalidar).toHaveBeenCalledWith({ queryKey: ['eventos', 'publicados'] })
  })

  it('TEST-FE-X-010: marcar como usado invalida el ticket y los pagados del evento', async () => {
    const { usuario, queryClient } = renderizar('/staff/validacion', { rol: 'STAFF' })
    const invalidar = vi.spyOn(queryClient, 'invalidateQueries')

    await usuario.type(await screen.findByLabelText('Código de ticket'), ticketPagado.ticketCode)
    await usuario.click(screen.getByRole('button', { name: 'Buscar' }))
    await usuario.click(await screen.findByRole('button', { name: 'Marcar como usado' }))
    await screen.findByText('Ingreso registrado.')

    expect(invalidar).toHaveBeenCalledWith({ queryKey: ['ticket', ticketPagado.ticketCode] })
    expect(invalidar).toHaveBeenCalledWith({ queryKey: ['tickets', 'pagados', 'CMF-2026'] })
  })

  it('TEST-FE-X-011: asociar un artista invalida el evento y los eventos de ese artista', async () => {
    server.use(
      red.get('*/api/events/:eventCode', () => HttpResponse.json({ ...eventoDetalle, status: 'DRAFT' })),
    )
    const { usuario, queryClient } = renderizar('/admin/eventos/CMF-2026', { rol: 'ADMIN' })
    const invalidar = vi.spyOn(queryClient, 'invalidateQueries')

    await screen.findByRole('option', { name: 'Neon Waves' })
    await usuario.selectOptions(screen.getByLabelText('Agregar artista'), 'Neon Waves')
    await usuario.click(screen.getByRole('button', { name: 'Asociar' }))

    await waitFor(() => expect(invalidar).toHaveBeenCalledWith({ queryKey: ['evento', 'CMF-2026'] }))
    expect(invalidar).toHaveBeenCalledWith({ queryKey: ['eventos', 'artista', 'Neon Waves'] })
  })
})

describe('Detalles de las vistas (PRD 9.2, 9.8 y 9.17)', () => {
  it('TEST-FE-X-012: el menú de usuario se abre y se cierra con el teclado', async () => {
    const { usuario } = renderizar('/sedes', { rol: 'USER' })
    const boton = await screen.findByRole('button', { name: /Menú de usuario/ })

    boton.focus()
    await usuario.keyboard('{Enter}')
    expect(screen.getByRole('link', { name: 'Mi cuenta' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Salir' })).toBeInTheDocument()

    await usuario.keyboard('{Escape}')
    expect(screen.queryByRole('link', { name: 'Mi cuenta' })).not.toBeInTheDocument()
  })

  it('TEST-FE-X-013: el botón de ver contraseña alterna el tipo del campo', async () => {
    const { usuario } = renderizar('/login')
    const campo = await screen.findByLabelText('Contraseña')
    expect(campo).toHaveAttribute('type', 'password')

    await usuario.click(screen.getByRole('button', { name: 'Mostrar contraseña' }))
    expect(campo).toHaveAttribute('type', 'text')

    await usuario.click(screen.getByRole('button', { name: 'Ocultar contraseña' }))
    expect(campo).toHaveAttribute('type', 'password')
  })

  it('TEST-FE-X-014: tras crear un evento, la gestión muestra el aviso de éxito', async () => {
    const { usuario } = renderizar('/admin/eventos/nuevo', { rol: 'ADMIN' })

    await usuario.type(await screen.findByLabelText('Código del evento'), 'NEW-2027')
    await usuario.type(screen.getByLabelText('Nombre'), 'Evento Nuevo')
    await usuario.type(screen.getByLabelText('Fecha y hora'), '2027-01-20T20:00')
    await screen.findByRole('option', { name: /Marina Convention Center/ })
    await usuario.selectOptions(screen.getByLabelText('Sede'), 'VEN-SMR-01')
    await usuario.click(screen.getByRole('button', { name: 'Crear evento' }))

    expect(await screen.findByText(/Evento creado\./)).toBeInTheDocument()
  })

  it('TEST-FE-X-015: sin fecha de nacimiento, un evento con edad mínima no deja comprar', async () => {
    server.use(red.get('*/api/users/by-email', () => HttpResponse.json({ ...perfil, birthDate: null })))
    renderizar('/eventos/CMF-2026', { rol: 'USER' })

    expect(await screen.findByText(/no tiene fecha de nacimiento/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Comprar entrada' })).toBeDisabled()
  })

  it('TEST-FE-X-016: "Mi cuenta" muestra la fecha de nacimiento sin correrla un día', async () => {
    renderizar('/cuenta', { rol: 'USER' })

    expect(await screen.findByText('Nacimiento')).toBeInTheDocument()
    expect(screen.getByText(/12.*1998/)).toBeInTheDocument()
  })
})

describe('Sesión vencida (PRD 12.2)', () => {
  it('TEST-FE-X-017: un 401 en una página protegida lleva a login con el aviso', async () => {
    server.use(red.get('*/api/tickets/by-user', () => errorApi(401, 'Authentication is required')))
    const { router } = renderizar('/mis-tickets', { rol: 'USER' })

    expect(await screen.findByText('Tu sesión expiró. Inicia sesión de nuevo.')).toBeInTheDocument()
    expect(router.state.location.pathname).toBe('/login')
    expect(router.state.location.state).toEqual({ desde: '/mis-tickets', motivo: 'expirada' })
  })
})

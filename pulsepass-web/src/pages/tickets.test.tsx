import { act, screen, waitFor, within } from '@testing-library/react'
import { http as red, HttpResponse } from 'msw'
import { eventoDetalle, ticketCancelado, ticketPagado, ticketUsado } from '@/test/mocks/datos'
import { errorApi } from '@/test/mocks/handlers'
import { server } from '@/test/mocks/server'
import { esperarSesion, renderizar } from '@/test/utils'

describe('Tickets del usuario', () => {
  it('TEST-FE-EVT-006: un evento SOLD_OUT deshabilita la compra', async () => {
    server.use(
      red.get('*/api/events/:eventCode', () => HttpResponse.json({ ...eventoDetalle, status: 'SOLD_OUT' })),
    )
    renderizar('/eventos/CMF-2026', { rol: 'USER' })

    expect(await screen.findByRole('button', { name: 'Comprar entrada' })).toBeDisabled()
    expect(screen.getByText('Entradas agotadas.')).toBeInTheDocument()
  })

  it('TEST-FE-TKT-001: una compra válida navega a la confirmación', async () => {
    const { usuario } = renderizar('/eventos/CMF-2026', { rol: 'USER' })
    await esperarSesion()

    await usuario.click(await screen.findByRole('button', { name: 'Comprar entrada' }))
    await usuario.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Confirmar' }))

    expect(await screen.findByRole('heading', { name: '¡Compra realizada con éxito!' })).toBeInTheDocument()
  })

  it('TEST-FE-TKT-002: comprar sin sesión redirige a login recordando el evento', async () => {
    const { usuario, router } = renderizar('/eventos/CMF-2026')

    await usuario.click(await screen.findByRole('button', { name: 'Inicia sesión para comprar' }))

    expect(await screen.findByRole('heading', { name: 'Inicia sesión' })).toBeInTheDocument()
    expect(router.state.location.state).toEqual({ desde: '/eventos/CMF-2026' })
  })

  it('TEST-FE-TKT-003: un 409 muestra el mensaje del backend dentro del modal', async () => {
    server.use(
      red.post('*/api/tickets', () => errorApi(409, 'User does not meet minimum age for event: CMF-2026')),
    )
    const { usuario } = renderizar('/eventos/CMF-2026', { rol: 'USER' })
    await esperarSesion()

    await usuario.click(await screen.findByRole('button', { name: 'Comprar entrada' }))
    const modal = screen.getByRole('dialog')
    await usuario.click(within(modal).getByRole('button', { name: 'Confirmar' }))

    expect(await within(modal).findByText(/User does not meet minimum age/)).toBeInTheDocument()
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

  it('TEST-FE-TKT-004: la compra envía el correo de la sesión', async () => {
    let cuerpo: unknown = null
    server.use(
      red.post('*/api/tickets', async ({ request }) => {
        cuerpo = await request.json()
        return HttpResponse.json(ticketPagado, { status: 201 })
      }),
    )
    const { usuario } = renderizar('/eventos/CMF-2026', { rol: 'USER' })
    await esperarSesion()

    await usuario.click(await screen.findByRole('radio', { name: 'VIP' }))
    await usuario.click(screen.getByRole('button', { name: 'Comprar entrada' }))
    await usuario.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Confirmar' }))

    await waitFor(() =>
      expect(cuerpo).toEqual({ userEmail: 'andrea@pulsepass.test', eventCode: 'CMF-2026', type: 'VIP' }),
    )
  })

  it('TEST-FE-TKT-005: mis tickets lista y filtra por estado', async () => {
    const { usuario } = renderizar('/mis-tickets', { rol: 'USER' })
    expect(await screen.findByText(ticketPagado.ticketCode)).toBeInTheDocument()
    expect(screen.getByText(ticketUsado.ticketCode)).toBeInTheDocument()

    await usuario.click(screen.getByRole('button', { name: 'Usados' }))

    expect(screen.queryByText(ticketPagado.ticketCode)).not.toBeInTheDocument()
    expect(screen.getByText(ticketUsado.ticketCode)).toBeInTheDocument()
  })

  it('TEST-FE-TKT-006: sin tickets se muestra el estado vacío', async () => {
    server.use(red.get('*/api/tickets/by-user', () => HttpResponse.json([])))
    renderizar('/mis-tickets', { rol: 'USER' })

    expect(await screen.findByText('Aún no tienes tickets')).toBeInTheDocument()
  })

  it('TEST-FE-TKT-007: cancelar un ticket PAID actualiza la lista', async () => {
    let cancelado = false
    server.use(
      red.get('*/api/tickets/by-user', () =>
        HttpResponse.json([{ ...ticketPagado, status: cancelado ? 'CANCELLED' : 'PAID' }]),
      ),
      red.patch('*/api/tickets/:ticketCode/cancel', () => {
        cancelado = true
        return HttpResponse.json({ ...ticketPagado, status: 'CANCELLED' })
      }),
    )
    const { usuario } = renderizar('/mis-tickets', { rol: 'USER' })

    await usuario.click(await screen.findByRole('button', { name: 'Cancelar' }))
    await usuario.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Cancelar ticket' }))

    expect(await screen.findByText('Ticket cancelado.')).toBeInTheDocument()
    // La invalidación vuelve a pedir la lista: el botón desaparece porque ya no está PAID
    await waitFor(() => expect(screen.queryByRole('button', { name: 'Cancelar' })).not.toBeInTheDocument())
  })

  it('TEST-FE-TKT-008: un 409 al cancelar muestra el mensaje y mantiene el modal', async () => {
    server.use(
      red.patch('*/api/tickets/:ticketCode/cancel', () =>
        errorApi(409, 'Only PAID tickets can be cancelled.'),
      ),
    )
    const { usuario } = renderizar('/mis-tickets', { rol: 'USER' })

    await usuario.click(await screen.findByRole('button', { name: 'Cancelar' }))
    const modal = screen.getByRole('dialog')
    await usuario.click(within(modal).getByRole('button', { name: 'Cancelar ticket' }))

    expect(await within(modal).findByText('Only PAID tickets can be cancelled.')).toBeInTheDocument()
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })

  it('TEST-FE-TKT-009: un ticket ajeno (403) o inexistente (404) muestra "Ticket no disponible"', async () => {
    server.use(red.get('*/api/tickets/AJENO', () => errorApi(403, 'Access denied')))
    const { router } = renderizar('/mis-tickets/AJENO', { rol: 'USER' })
    expect(await screen.findByRole('heading', { name: 'Ticket no disponible' })).toBeInTheDocument()

    // act(): espera a que React dibuje la página nueva. Sin él, findBy encuentra el título del ticket
    // anterior justo antes de que desaparezca, y la aserción falla de forma intermitente
    await act(() => router.navigate('/mis-tickets/NO-EXISTE'))
    expect(await screen.findByRole('heading', { name: 'Ticket no disponible' })).toBeInTheDocument()
  })

  it('TEST-FE-TKT-010: el botón cancelar solo aparece en tickets PAID', async () => {
    renderizar('/mis-tickets', { rol: 'USER' })
    expect(await screen.findByText(ticketCancelado.ticketCode)).toBeInTheDocument()

    // Hay tres tickets (PAID, USED, CANCELLED) y un solo botón
    expect(screen.getAllByRole('button', { name: 'Cancelar' })).toHaveLength(1)
  })
})

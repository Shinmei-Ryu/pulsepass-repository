import { screen, within } from '@testing-library/react'
import { http as red } from 'msw'
import { ticketPagado } from '@/test/mocks/datos'
import { errorApi } from '@/test/mocks/handlers'
import { server } from '@/test/mocks/server'
import { renderizar } from '@/test/utils'

type Usuario = ReturnType<typeof renderizar>['usuario']

async function buscarTicket(usuario: Usuario, codigo: string) {
    await usuario.type(await screen.findByLabelText('Código de ticket'), codigo)
    await usuario.click(screen.getByRole('button', { name: 'Buscar' }))
}

describe('Operación STAFF', () => {
    it('TEST-FE-STF-001: buscar un ticket y marcarlo como usado', async () => {
        const { usuario } = renderizar('/staff/validacion', { rol: 'STAFF' })

        await buscarTicket(usuario, ticketPagado.ticketCode)
        await usuario.click(await screen.findByRole('button', { name: 'Marcar como usado' }))

        expect(await screen.findByText('Ingreso registrado.')).toBeInTheDocument()
    })

    it('TEST-FE-STF-002: marcar por segunda vez muestra el 409', async () => {
        server.use(red.patch('*/api/tickets/:ticketCode/use', () => errorApi(409, 'Ticket has already been used.')))
        const { usuario } = renderizar('/staff/validacion', { rol: 'STAFF' })

        await buscarTicket(usuario, ticketPagado.ticketCode)
        await usuario.click(await screen.findByRole('button', { name: 'Marcar como usado' }))

        expect(await screen.findByText('Ticket has already been used.')).toBeInTheDocument()
    })

    it('TEST-FE-STF-003: un ticket inexistente muestra el 404', async () => {
        const { usuario } = renderizar('/staff/validacion', { rol: 'STAFF' })

        await buscarTicket(usuario, 'NO-EXISTE')

        expect(await screen.findByText('Ticket no encontrado.')).toBeInTheDocument()
    })

    it('TEST-FE-STF-004: la tabla de tickets pagados muestra sus filas', async () => {
        renderizar('/staff/eventos/CMF-2026/tickets', { rol: 'STAFF' })

        // La misma información existe como tabla (escritorio) y como tarjetas (móvil): se consulta la tabla
        const tabla = await screen.findByRole('table')
        expect(within(tabla).getByText(ticketPagado.ticketCode)).toBeInTheDocument()
        expect(within(tabla).getByRole('button', { name: `Usar ticket ${ticketPagado.ticketCode}` })).toBeInTheDocument()
    })
})

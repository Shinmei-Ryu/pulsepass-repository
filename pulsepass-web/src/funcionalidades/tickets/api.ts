import { z } from 'zod'
import { http } from '@/lib/http'
import { TicketResponseSchema, type TicketType } from './tipos'

const e = encodeURIComponent

export interface CompraTicket {
    userEmail: string
    eventCode: string
    type: TicketType
}

export const comprarTicket = (compra: CompraTicket) =>
    http('/tickets', TicketResponseSchema, { method: 'POST', body: compra })

export const obtenerTicket = (ticketCode: string) =>
    http(`/tickets/${e(ticketCode)}`, TicketResponseSchema)

export const obtenerTicketsDeUsuario = (email: string) =>
    http(`/tickets/by-user?email=${e(email)}`, z.array(TicketResponseSchema))

export const obtenerTicketsPagados = (eventCode: string) =>
    http(`/events/${e(eventCode)}/tickets/paid`, z.array(TicketResponseSchema))

export const cancelarTicket = (ticketCode: string) =>
    http(`/tickets/${e(ticketCode)}/cancel`, TicketResponseSchema, { method: 'PATCH' })

export const usarTicket = (ticketCode: string) =>
    http(`/tickets/${e(ticketCode)}/use`, TicketResponseSchema, { method: 'PATCH' })

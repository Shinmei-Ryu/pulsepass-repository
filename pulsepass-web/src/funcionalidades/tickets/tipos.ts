import { z } from 'zod'
import { FechaHoraLocalSchema } from '@/lib/esquemas'

export const TIPOS_TICKET = ['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'] as const
export const TicketTypeSchema = z.enum(TIPOS_TICKET)
export const TicketStatusSchema = z.enum(['RESERVED', 'PAID', 'CANCELLED', 'USED'])

export type TicketType = z.infer<typeof TicketTypeSchema>
export type TicketStatus = z.infer<typeof TicketStatusSchema>

export const TicketResponseSchema = z.object({
    id: z.number(),
    ticketCode: z.string(),
    type: TicketTypeSchema,
    // Llega como número JSON, a veces con cuatro decimales (60000.0000)
    price: z.number(),
    status: TicketStatusSchema,
    purchaseDate: FechaHoraLocalSchema,
    userEmail: z.string(),
    eventCode: z.string(),
    eventName: z.string(),
})

export type TicketResponse = z.infer<typeof TicketResponseSchema>

// Lo que viaja en la URL debe ser seguro: sin "/" ni espacios
export const CodigoTicketSchema = z
    .string()
    .trim()
    .min(1, 'Escribe el código del ticket')
    .regex(/^[A-Za-z0-9._-]+$/, 'El código solo tiene letras, números y guiones')

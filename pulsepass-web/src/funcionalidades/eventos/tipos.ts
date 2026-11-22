import { z } from 'zod'
import { FechaHoraLocalSchema } from '@/lib/esquemas'

export const CATEGORIAS = ['MUSIC', 'SPORTS', 'TECHNOLOGY', 'EDUCATION', 'CULTURE', 'ENTERTAINMENT'] as const
export const EventCategorySchema = z.enum(CATEGORIAS)
export const EventStatusSchema = z.enum(['DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED', 'FINISHED'])

export type EventCategory = z.infer<typeof EventCategorySchema>
export type EventStatus = z.infer<typeof EventStatusSchema>

export const EventSummaryResponseSchema = z.object({
    id: z.number(),
    eventCode: z.string(),
    name: z.string(),
    category: EventCategorySchema,
    status: EventStatusSchema,
    eventDate: FechaHoraLocalSchema,
    venueName: z.string(),
})

export const EventResponseSchema = EventSummaryResponseSchema.extend({
    description: z.string().nullable(),
    minimumAge: z.number().int(),
    venueCode: z.string(),
    // La API entrega solo los nombres artísticos, no objetos
    artists: z.array(z.string()),
})

export type EventSummaryResponse = z.infer<typeof EventSummaryResponseSchema>
export type EventResponse = z.infer<typeof EventResponseSchema>

// Formulario de creación. Los máximos son los de las columnas de la base.
export const CrearEventoSchema = z.object({
    eventCode: z
        .string()
        .min(1, 'El código es obligatorio')
        .max(100, 'Máximo 100 caracteres')
        .regex(/^[A-Za-z0-9._-]+$/, 'Solo letras, números, punto, guion y guion bajo'),
    name: z.string().min(1, 'El nombre es obligatorio').max(150, 'Máximo 150 caracteres'),
    description: z.string().max(500, 'Máximo 500 caracteres'),
    category: EventCategorySchema,
    minimumAge: z.number({ error: 'Indica la edad mínima' }).int('Debe ser un número entero').min(0, 'No puede ser negativa'),
    // Valor de <input type="datetime-local">: YYYY-MM-DDTHH:mm
    eventDate: z.string().min(1, 'La fecha es obligatoria'),
    venueCode: z.string().min(1, 'Selecciona una sede'),
})

export type CrearEventoForm = z.infer<typeof CrearEventoSchema>

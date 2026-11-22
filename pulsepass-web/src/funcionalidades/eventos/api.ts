import { z } from 'zod'
import { http } from '@/lib/http'
import { EventResponseSchema, EventSummaryResponseSchema, type CrearEventoForm } from './tipos'

const e = encodeURIComponent

export const obtenerEventosPublicados = () =>
    http('/events/published', z.array(EventSummaryResponseSchema))

export const obtenerEvento = (eventCode: string) =>
    http(`/events/${e(eventCode)}`, EventResponseSchema)

export const obtenerEventosPorArtista = (stageName: string) =>
    http(`/events/by-artist?stageName=${e(stageName)}`, z.array(EventSummaryResponseSchema))

export const crearEvento = (form: CrearEventoForm) =>
    http('/events', EventResponseSchema, {
        method: 'POST',
        body: {
            ...form,
            description: form.description.trim() === '' ? null : form.description,
            // datetime-local entrega "2026-12-12T20:00"; el backend espera hora local sin zona.
            // Nunca toISOString(): convertiría a UTC.
            eventDate: form.eventDate.length === 16 ? `${form.eventDate}:00` : form.eventDate,
        },
    })

export const publicarEvento = (eventCode: string) =>
    http(`/events/${e(eventCode)}/publish`, EventResponseSchema, { method: 'PATCH' })

export const asociarArtista = (eventCode: string, artistId: number) =>
    http(`/events/${e(eventCode)}/artists/${artistId}`, EventResponseSchema, { method: 'POST' })

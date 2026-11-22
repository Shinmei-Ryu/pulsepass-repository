import { z } from 'zod'
import { http } from '@/lib/http'
import { VenueResponseSchema } from './tipos'

export const obtenerSedesActivas = () =>
    http('/venues/active', z.array(VenueResponseSchema))

export const obtenerSede = (code: string) =>
    http(`/venues/${encodeURIComponent(code)}`, VenueResponseSchema)

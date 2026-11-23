import { z } from 'zod'
import { http } from '@/lib/http'
import { ArtistResponseSchema } from './tipos'

export const obtenerArtistasActivos = () => http('/artists/active', z.array(ArtistResponseSchema))

export const obtenerArtista = (id: number) => http(`/artists/${id}`, ArtistResponseSchema)

export const obtenerArtistaPorNombre = (stageName: string) =>
  http(`/artists/by-stage-name?stageName=${encodeURIComponent(stageName)}`, ArtistResponseSchema)

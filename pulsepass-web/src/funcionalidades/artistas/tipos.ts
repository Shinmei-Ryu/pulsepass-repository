import { z } from 'zod'

export const ArtistResponseSchema = z.object({
  id: z.number(),
  stageName: z.string(),
  country: z.string(),
  genre: z.string(),
  active: z.boolean(),
})

export type ArtistResponse = z.infer<typeof ArtistResponseSchema>

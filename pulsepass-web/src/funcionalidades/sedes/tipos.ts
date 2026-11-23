import { z } from 'zod'

export const VenueResponseSchema = z.object({
  id: z.number(),
  code: z.string(),
  name: z.string(),
  city: z.string(),
  address: z.string(),
  capacity: z.number().int(),
  active: z.boolean(),
})

export type VenueResponse = z.infer<typeof VenueResponseSchema>

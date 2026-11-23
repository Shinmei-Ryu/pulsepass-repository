import { z } from 'zod'
import { hoyISO } from '@/lib/formato'

export const UserResponseSchema = z.object({
  id: z.number(),
  username: z.string(),
  email: z.string(),
  active: z.boolean(),
  firstName: z.string().nullable(),
  lastName: z.string().nullable(),
  city: z.string().nullable(),
  // nullish: null si la cuenta no tiene fecha; ausente si el backend aún no entrega el campo
  birthDate: z
    .string()
    .regex(/^\d{4}-\d{2}-\d{2}$/)
    .nullish(),
})

export type UserResponse = z.infer<typeof UserResponseSchema>

// bcrypt solo usa los primeros 72 bytes: se mide en bytes UTF-8, no en caracteres
const bytes = (texto: string) => new TextEncoder().encode(texto).length

export const RegistroSchema = z.object({
  username: z
    .string()
    .min(1, 'El usuario es obligatorio')
    .max(100, 'Máximo 100 caracteres')
    .regex(/^[^@\s]+$/, 'Sin espacios ni @'),
  email: z.email('Escribe un correo válido').max(200, 'Máximo 200 caracteres'),
  firstName: z.string().min(1, 'El nombre es obligatorio').max(150, 'Máximo 150 caracteres'),
  lastName: z.string().min(1, 'El apellido es obligatorio').max(150, 'Máximo 150 caracteres'),
  // Se comparan cadenas YYYY-MM-DD: new Date('1998-04-12') se interpretaría en UTC
  birthDate: z
    .string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'La fecha de nacimiento es obligatoria')
    .refine((fecha) => fecha <= hoyISO(), 'La fecha no puede ser futura'),
  password: z
    .string()
    .min(12, 'Mínimo 12 caracteres')
    .refine((p) => bytes(p) <= 72, 'Máximo 72 bytes'),
})

export type RegistroForm = z.infer<typeof RegistroSchema>

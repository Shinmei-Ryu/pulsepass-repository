import { z } from 'zod'

export const RolSchema = z.enum(['USER', 'STAFF', 'ADMIN'])
export type Rol = z.infer<typeof RolSchema>

export const AuthResponseSchema = z.object({
    accessToken: z.string().min(1),
    tokenType: z.literal('Bearer'),
    expiresIn: z.number().int().positive(),
})

export const UsuarioSesionSchema = z.object({
    id: z.number(),
    username: z.string(),
    email: z.string(),
    roles: z.array(RolSchema),
})
export type UsuarioSesion = z.infer<typeof UsuarioSesionSchema>

// Lo único que se guarda en sessionStorage. Se valida al leerlo: es un dato externo.
export const SesionGuardadaSchema = z.object({
    token: z.string().min(1),
    expiraEn: z.number(),
})
export type SesionGuardada = z.infer<typeof SesionGuardadaSchema>

// Unión discriminada: no existe "autenticado sin usuario" (FE-006)
export type Sesion =
    | { estado: 'cargando' }
    | { estado: 'anonimo' }
    | { estado: 'autenticado'; usuario: UsuarioSesion }

export type MotivoCierre = 'manual' | 'expirada'

export const LoginSchema = z.object({
    identifier: z.string().min(1, 'Escribe tu usuario o correo'),
    password: z.string().min(1, 'Escribe tu contraseña'),
})
export type LoginForm = z.infer<typeof LoginSchema>

import { http } from '@/lib/http'
import { UserResponseSchema, type RegistroForm } from './tipos'

export const registrarUsuario = (form: RegistroForm) =>
    http('/users', UserResponseSchema, { method: 'POST', body: form })

export const obtenerUsuarioPorEmail = (email: string) =>
    http(`/users/by-email?email=${encodeURIComponent(email)}`, UserResponseSchema)

export const obtenerUsuarioPorUsername = (username: string) =>
    http(`/users/by-username?username=${encodeURIComponent(username)}`, UserResponseSchema)

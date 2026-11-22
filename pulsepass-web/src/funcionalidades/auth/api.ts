import { http } from '@/lib/http'
import { AuthResponseSchema, UsuarioSesionSchema, type LoginForm } from './tipos'

export const iniciarSesionApi = (credenciales: LoginForm) =>
    http('/auth/login', AuthResponseSchema, { method: 'POST', body: credenciales })

export const obtenerYo = () => http('/auth/me', UsuarioSesionSchema)
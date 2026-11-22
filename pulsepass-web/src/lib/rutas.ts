import { z } from 'zod'

const c = encodeURIComponent

// Única fuente de rutas de la aplicación (FE-003)
export const RUTAS = {
  inicio: '/',
  eventos: '/eventos',
  evento: (eventCode: string) => `/eventos/${c(eventCode)}`,
  artistas: '/artistas',
  artista: (id: number) => `/artistas/${id}`,
  sedes: '/sedes',
  sede: (code: string) => `/sedes/${c(code)}`,
  login: '/login',
  registro: '/registro',
  cuenta: '/cuenta',
  misTickets: '/mis-tickets',
  ticket: (ticketCode: string) => `/mis-tickets/${c(ticketCode)}`,
  confirmacion: (ticketCode: string) => `/compra/confirmacion/${c(ticketCode)}`,
  staffValidacion: '/staff/validacion',
  // Misma página, con el foco en el formulario que pide el código del evento
  staffTicketsPagados: '/staff/validacion#tickets-pagados',
  ticketsPagados: (eventCode: string) => `/staff/eventos/${c(eventCode)}/tickets`,
  adminEventos: '/admin/eventos',
  adminNuevoEvento: '/admin/eventos/nuevo',
  adminEvento: (eventCode: string) => `/admin/eventos/${c(eventCode)}`,
  sinPermisos: '/403',
} as const

const EstadoNavegacionSchema = z.object({
  desde: z.string().optional(),
  registrado: z.boolean().optional(),
  creado: z.boolean().optional(),
  motivo: z.literal('expirada').optional(),
})

// location.state es "any": se valida antes de usarlo
export function leerEstadoNavegacion(state: unknown) {
  const r = EstadoNavegacionSchema.safeParse(state)
  return r.success ? r.data : {}
}

// Solo se aceptan rutas internas: empieza por "/" y no por "//" (evita redirecciones a otro sitio)
export function destinoSeguro(state: unknown): string {
  const { desde } = leerEstadoNavegacion(state)
  return desde && desde.startsWith('/') && !desde.startsWith('//') ? desde : RUTAS.inicio
}

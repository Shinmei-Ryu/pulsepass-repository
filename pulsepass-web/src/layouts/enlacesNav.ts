import clsx from 'clsx'
import { tieneRol } from '@/funcionalidades/auth/hooks'
import type { Sesion } from '@/funcionalidades/auth/tipos'
import { RUTAS } from '@/lib/rutas'

export interface Enlace {
  a: string
  texto: string
  end?: boolean
  // Un enlace con ancla comparte ruta con otro: no se marca como activo
  sinActivo?: boolean
}

// Tabla 8.4 del PRD: qué ve cada rol
export function enlacesPara(sesion: Sesion): Enlace[] {
  const enlaces: Enlace[] = [
    { a: RUTAS.eventos, texto: 'Eventos' },
    { a: RUTAS.artistas, texto: 'Artistas' },
    { a: RUTAS.sedes, texto: 'Sedes' },
  ]
  if (tieneRol(sesion, 'USER')) enlaces.push({ a: RUTAS.misTickets, texto: 'Mis tickets' })
  if (tieneRol(sesion, 'STAFF', 'ADMIN')) {
    enlaces.push({ a: RUTAS.staffValidacion, texto: 'Validar ingreso' })
    // La tabla de pagados necesita un código de evento: el enlace lleva al formulario que lo pide
    enlaces.push({ a: RUTAS.staffTicketsPagados, texto: 'Tickets pagados', sinActivo: true })
  }
  if (tieneRol(sesion, 'ADMIN')) enlaces.push({ a: RUTAS.adminEventos, texto: 'Administrar eventos' })
  return enlaces
}

export const claseEnlaceNav = ({ isActive }: { isActive: boolean }) =>
  clsx(
    'inline-flex min-h-11 items-center rounded-lg px-3 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600',
    isActive
      ? 'bg-marca-50 text-marca-700 dark:bg-slate-800 dark:text-marca-500'
      : 'text-slate-600 hover:text-slate-900 dark:text-slate-300 dark:hover:text-white',
  )

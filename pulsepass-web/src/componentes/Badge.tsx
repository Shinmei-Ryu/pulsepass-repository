import type { EventStatus } from '@/funcionalidades/eventos/tipos'
import type { TicketStatus } from '@/funcionalidades/tickets/tipos'

const base = 'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold tracking-wide'

// Colores de la tabla 9.1 del PRD (más su variante oscura).
// Un Record obliga a cubrir todos los estados: si el backend añade uno, TypeScript avisa aquí
const porEvento: Record<EventStatus, string> = {
  DRAFT: 'bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-200',
  PUBLISHED: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900 dark:text-emerald-200',
  SOLD_OUT: 'bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-200',
  CANCELLED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
  FINISHED: 'bg-slate-300 text-slate-800 dark:bg-slate-600 dark:text-slate-200',
}

const porTicket: Record<TicketStatus, string> = {
  RESERVED: 'bg-sky-100 text-sky-800 dark:bg-sky-900 dark:text-sky-200',
  PAID: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900 dark:text-emerald-200',
  CANCELLED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
  USED: 'bg-slate-300 text-slate-800 dark:bg-slate-600 dark:text-slate-200',
}

export function BadgeEvento({ estado }: { estado: EventStatus }) {
  return <span className={`${base} ${porEvento[estado]}`}>{estado}</span>
}

export function BadgeTicket({ estado }: { estado: TicketStatus }) {
  return <span className={`${base} ${porTicket[estado]}`}>{estado}</span>
}

export function BadgeActivo({ activo }: { activo: boolean }) {
  return (
    <span className={`${base} ${activo ? porTicket.PAID : porTicket.USED}`}>
      {activo ? 'Activo' : 'Inactivo'}
    </span>
  )
}

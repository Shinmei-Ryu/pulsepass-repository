import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router'
import clsx from 'clsx'
import Boton from '@/componentes/Boton'
import Tarjeta from '@/componentes/Tarjeta'
import { claseSubtitulo, claseTextoSuave } from '@/componentes/estilos'
import { tieneRol, useAuth } from '@/funcionalidades/auth/hooks'
import type { EventResponse, EventStatus } from '@/funcionalidades/eventos/tipos'
import { useUsuarioPorEmail } from '@/funcionalidades/usuarios/hooks'
import { RUTAS } from '@/lib/rutas'
import { useComprarTicket } from '../hooks'
import { TicketTypeSchema, TIPOS_TICKET, type TicketType } from '../tipos'
import ModalConfirmarCompra from './ModalConfirmarCompra'

// Por qué no se puede comprar en cada estado. Es presentación: quien decide es el backend.
const MOTIVO: Record<EventStatus, string | null> = {
  PUBLISHED: null,
  DRAFT: 'Este evento aún no está publicado.',
  SOLD_OUT: 'Entradas agotadas.',
  CANCELLED: 'Este evento fue cancelado.',
  FINISHED: 'Este evento ya finalizó.',
}

const SIN_FECHA = 'Tu cuenta no tiene fecha de nacimiento y este evento tiene edad mínima.'

export default function PanelCompra({ evento }: { evento: EventResponse }) {
  const { sesion } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [tipo, setTipo] = useState<TicketType>('GENERAL')
  const [confirmando, setConfirmando] = useState(false)
  const compra = useComprarTicket()
  // Perfil propio, solo para USER: dice si la cuenta tiene fecha de nacimiento
  const esUser = tieneRol(sesion, 'USER')
  const perfil = useUsuarioPorEmail(
    sesion.estado === 'autenticado' && esUser ? sesion.usuario.email : undefined,
  )

  // STAFF y ADMIN no compran: la API les respondería 403
  if (sesion.estado === 'autenticado' && !tieneRol(sesion, 'USER')) return null

  // Se comprueba que el dato EXISTE; la edad no se calcula aquí (PRD §24.1): eso lo decide el backend.
  // null = la cuenta no tiene fecha; undefined = el backend aún no entrega el campo, y no se bloquea.
  const sinFecha = evento.minimumAge > 0 && perfil.data?.birthDate === null
  const motivo = MOTIVO[evento.status] ?? (sinFecha ? SIN_FECHA : null)

  const cerrar = () => {
    setConfirmando(false)
    compra.reset()
  }

  const confirmar = () => {
    // Solo viajan evento y tipo; el correo lo pone el hook desde la sesión
    compra.mutate(
      { eventCode: evento.eventCode, type: tipo },
      { onSuccess: (ticket) => navigate(RUTAS.confirmacion(ticket.ticketCode)) },
    )
  }

  return (
    <Tarjeta como="aside" className="h-fit space-y-4 p-5">
      <h2 className={claseSubtitulo}>Entradas</h2>

      <fieldset disabled={motivo !== null} className="space-y-2">
        <legend className={`${claseTextoSuave} mb-2`}>Tipo de entrada</legend>
        {TIPOS_TICKET.map((opcion) => (
          <label
            key={opcion}
            className={clsx(
              'flex min-h-11 cursor-pointer items-center gap-3 rounded-lg border px-3 text-sm font-medium',
              tipo === opcion
                ? 'border-marca-600 bg-marca-50 dark:bg-slate-900'
                : 'border-slate-300 dark:border-slate-600',
            )}
          >
            <input
              type="radio"
              name="tipo"
              value={opcion}
              checked={tipo === opcion}
              onChange={(e) => {
                const valido = TicketTypeSchema.safeParse(e.target.value)
                if (valido.success) setTipo(valido.data)
              }}
              className="accent-marca-600"
            />
            {opcion}
          </label>
        ))}
      </fieldset>

      {/* En móvil el botón queda fijo abajo (DES-07); desde md vuelve a su lugar */}
      <div className="fixed inset-x-0 bottom-0 z-30 border-t border-slate-200 bg-white p-3 md:static md:border-0 md:bg-transparent md:p-0 dark:border-slate-700 dark:bg-slate-900 dark:md:bg-transparent">
        {sesion.estado === 'anonimo' ? (
          <Boton
            className="w-full"
            onClick={() => navigate(RUTAS.login, { state: { desde: location.pathname } })}
          >
            Inicia sesión para comprar
          </Boton>
        ) : (
          <Boton
            className="w-full"
            // Mientras se confirma la sesión guardada aún no se sabe quién compra
            disabled={motivo !== null || sesion.estado === 'cargando'}
            onClick={() => setConfirmando(true)}
          >
            Comprar entrada
          </Boton>
        )}
      </div>

      {motivo ? (
        <p className="text-sm font-medium text-amber-800 dark:text-amber-300">{motivo}</p>
      ) : (
        <p className="text-xs text-slate-500 dark:text-slate-400">
          El precio lo define el sistema al confirmar.
        </p>
      )}

      {confirmando && sesion.estado === 'autenticado' && (
        <ModalConfirmarCompra
          nombreEvento={evento.name}
          tipo={tipo}
          email={sesion.usuario.email}
          cargando={compra.isPending}
          error={compra.error}
          onCerrar={cerrar}
          onConfirmar={confirmar}
        />
      )}
    </Tarjeta>
  )
}

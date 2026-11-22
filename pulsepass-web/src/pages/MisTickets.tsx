import { useState } from 'react'
import { Link } from 'react-router'
import Aviso from '@/componentes/Aviso'
import Cargando from '@/componentes/Cargando'
import EstadoVacio from '@/componentes/EstadoVacio'
import { claseBoton, claseTitulo } from '@/componentes/estilos'
import FiltroEstadoTicket, {
  type FiltroTicket,
} from '@/funcionalidades/tickets/componentes/FiltroEstadoTicket'
import ModalConfirmarCancelacion from '@/funcionalidades/tickets/componentes/ModalConfirmarCancelacion'
import TarjetaTicket from '@/funcionalidades/tickets/componentes/TarjetaTicket'
import { useMisTickets } from '@/funcionalidades/tickets/hooks'
import type { TicketResponse } from '@/funcionalidades/tickets/tipos'
import ErrorDeCarga from '@/layouts/ErrorDeCarga'
import { RUTAS } from '@/lib/rutas'

export default function MisTickets() {
  const tickets = useMisTickets()
  const [filtro, setFiltro] = useState<FiltroTicket>('TODOS')
  const [porCancelar, setPorCancelar] = useState<TicketResponse | null>(null)
  const [cancelado, setCancelado] = useState(false)

  const visibles = (tickets.data ?? []).filter((ticket) => filtro === 'TODOS' || ticket.status === filtro)

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <h1 className={claseTitulo}>Mis tickets</h1>
      <FiltroEstadoTicket valor={filtro} onCambiar={setFiltro} />

      {cancelado && (
        <Aviso tipo="exito" onCerrar={() => setCancelado(false)}>
          Ticket cancelado.
        </Aviso>
      )}

      {tickets.isPending && <Cargando />}
      {tickets.isError && <ErrorDeCarga error={tickets.error} onReintentar={tickets.refetch} />}
      {tickets.data && visibles.length === 0 && (
        <EstadoVacio
          mensaje={tickets.data.length === 0 ? 'Aún no tienes tickets' : 'No tienes tickets en este estado'}
          accion={
            <Link to={RUTAS.eventos} className={claseBoton('secundario')}>
              Ver cartelera
            </Link>
          }
        />
      )}

      <ul className="space-y-3">
        {visibles.map((ticket) => (
          <TarjetaTicket key={ticket.ticketCode} ticket={ticket} onCancelar={setPorCancelar} />
        ))}
      </ul>

      {porCancelar && (
        <ModalConfirmarCancelacion
          ticket={porCancelar}
          onCerrar={() => setPorCancelar(null)}
          onCancelado={() => {
            setPorCancelar(null)
            setCancelado(true)
          }}
        />
      )}
    </div>
  )
}

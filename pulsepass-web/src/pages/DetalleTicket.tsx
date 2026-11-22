import { useState } from 'react'
import { Link, useParams } from 'react-router'
import Aviso from '@/componentes/Aviso'
import { BadgeTicket } from '@/componentes/Badge'
import Boton from '@/componentes/Boton'
import Dato from '@/componentes/Dato'
import Cargando from '@/componentes/Cargando'
import EstadoError from '@/componentes/EstadoError'
import { IconoFlechaIzquierda } from '@/componentes/Iconos'
import PrecioCOP from '@/componentes/PrecioCOP'
import { claseEnlace, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import ModalConfirmarCancelacion from '@/funcionalidades/tickets/componentes/ModalConfirmarCancelacion'
import { useTicket } from '@/funcionalidades/tickets/hooks'
import { formatearFechaHora } from '@/lib/formato'
import { esEstado, mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'
import NoEncontrada from './NoEncontrada'
import Tarjeta from '@/componentes/Tarjeta'

export default function DetalleTicket() {
  const { ticketCode } = useParams<{ ticketCode: string }>()
  const ticket = useTicket(ticketCode)
  const [cancelando, setCancelando] = useState(false)
  const [copiado, setCopiado] = useState(false)

  if (ticket.isPending) return <Cargando />
  if (ticket.isError) {
    // 403 (ajeno) y 404 (no existe) se ven igual: la interfaz no revela cuál de los dos ocurrió
    return esEstado(ticket.error, 403, 404) ? (
        <NoEncontrada titulo="Ticket no disponible" mensaje="El ticket no existe o no pertenece a tu cuenta." />
    ) : (
        <EstadoError mensaje={mensajeDeError(ticket.error)} onReintentar={ticket.refetch} />
    )
  }

  const datos = ticket.data
  if (!datos) return null

  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(datos.ticketCode)
      setCopiado(true)
    } catch {
      // Sin permiso de portapapeles el código sigue visible para copiarlo a mano
    }
  }

  return (
      <div className="mx-auto max-w-3xl space-y-4">
        <Link to={RUTAS.misTickets} className={`${claseEnlace} inline-flex min-h-11 items-center gap-1 text-sm`}>
          <IconoFlechaIzquierda className="size-4" />
          Mis tickets
        </Link>

        <Tarjeta como="article" className="space-y-3 p-5">
          <div className="flex items-start justify-between gap-2">
            <h1 className={claseTitulo}>{datos.eventName}</h1>
            <BadgeTicket estado={datos.status} />
          </div>
          <dl>
            <Dato etiqueta="Código">
              <span className="font-mono">{datos.ticketCode}</span>
            </Dato>
            <Dato etiqueta="Tipo">{datos.type}</Dato>
            <Dato etiqueta="Precio">
              <PrecioCOP valor={datos.price} />
            </Dato>
            <Dato etiqueta="Comprado el">{formatearFechaHora(datos.purchaseDate)}</Dato>
            {/* La API entrega el correo del titular, no su nombre (D5) */}
            <Dato etiqueta="Titular">{datos.userEmail}</Dato>
          </dl>
          <p className={claseTextoSuave}>Presenta este código en el ingreso.</p>
          {copiado && (
              <Aviso tipo="exito" onCerrar={() => setCopiado(false)}>
                Código copiado.
              </Aviso>
          )}
        </Tarjeta>

        <div className="flex justify-end gap-2">
          <Boton variante="secundario" onClick={copiar}>
            Copiar código
          </Boton>
          {datos.status === 'PAID' && (
              <Boton variante="peligro" onClick={() => setCancelando(true)}>
                Cancelar ticket
              </Boton>
          )}
        </div>

        {cancelando && (
            <ModalConfirmarCancelacion ticket={datos} onCerrar={() => setCancelando(false)} onCancelado={() => setCancelando(false)} />
        )}
      </div>
  )
}

import { Link, useParams } from 'react-router'
import { BadgeTicket } from '@/componentes/Badge'
import Cargando from '@/componentes/Cargando'
import EstadoError from '@/componentes/EstadoError'
import { IconoCheck } from '@/componentes/Iconos'
import PrecioCOP from '@/componentes/PrecioCOP'
import { claseBoton, claseTextoSuave } from '@/componentes/estilos'
import { useTicket } from '@/funcionalidades/tickets/hooks'
import { esEstado, mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'
import NoEncontrada from './NoEncontrada'
import Tarjeta from '@/componentes/Tarjeta'

export default function ConfirmacionCompra() {
  const { ticketCode } = useParams<{ ticketCode: string }>()
  // Los datos salen de la API y no de location.state: así la página sobrevive a recargar
  const ticket = useTicket(ticketCode)

  if (ticket.isPending) return <Cargando />
  if (ticket.isError) {
    return esEstado(ticket.error, 403, 404) ? (
      <NoEncontrada titulo="Ticket no disponible" mensaje="El ticket no existe o no pertenece a tu cuenta." />
    ) : (
      <EstadoError mensaje={mensajeDeError(ticket.error)} onReintentar={ticket.refetch} />
    )
  }

  const datos = ticket.data
  if (!datos) return null

  return (
    <Tarjeta como="section" className="mx-auto flex max-w-md flex-col items-center gap-3 p-8 text-center">
      <span className="flex size-12 items-center justify-center rounded-full bg-green-100 text-green-700">
        <IconoCheck className="size-6" />
      </span>
      <h1 className="font-titulo text-xl font-bold">¡Compra realizada con éxito!</h1>
      <p className="text-sm font-semibold">{datos.eventName}</p>
      <p className={claseTextoSuave}>
        Ticket <span className="font-mono break-all">{datos.ticketCode}</span> · {datos.type} ·{' '}
        <PrecioCOP valor={datos.price} />
      </p>
      <BadgeTicket estado={datos.status} />
      <div className="mt-2 flex flex-wrap justify-center gap-2">
        <Link to={RUTAS.misTickets} className={claseBoton('primario')}>
          Ver mis tickets
        </Link>
        <Link to={RUTAS.eventos} className={claseBoton('secundario')}>
          Seguir explorando
        </Link>
      </div>
    </Tarjeta>
  )
}

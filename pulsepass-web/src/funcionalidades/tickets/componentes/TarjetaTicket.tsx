import { Link } from 'react-router'
import { BadgeTicket } from '@/componentes/Badge'
import Boton from '@/componentes/Boton'
import PrecioCOP from '@/componentes/PrecioCOP'
import Tarjeta from '@/componentes/Tarjeta'
import { claseBoton, claseTextoSuave } from '@/componentes/estilos'
import { formatearFecha } from '@/lib/formato'
import { RUTAS } from '@/lib/rutas'
import type { TicketResponse } from '../tipos'

interface TarjetaTicketProps {
    ticket: TicketResponse
    onCancelar: (ticket: TicketResponse) => void
}

export default function TarjetaTicket({ ticket, onCancelar }: TarjetaTicketProps) {
    return (
        <Tarjeta como="li" className="space-y-3 p-4">
            <div className="flex items-start justify-between gap-2">
                <div>
                    <h2 className="text-sm font-semibold">{ticket.eventName}</h2>
                    <p className={claseTextoSuave}>
                        <span className="font-mono break-all">{ticket.ticketCode}</span> · {ticket.type} ·{' '}
                        <PrecioCOP valor={ticket.price} /> · {formatearFecha(ticket.purchaseDate)}
                    </p>
                </div>
                <BadgeTicket estado={ticket.status} />
            </div>
            <div className="flex justify-end gap-2">
                <Link to={RUTAS.ticket(ticket.ticketCode)} className={claseBoton('secundario')}>
                    Ver detalle
                </Link>
                {/* Solo se ofrece cancelar lo que el backend podría aceptar; la decisión final es suya */}
                {ticket.status === 'PAID' && (
                    <Boton variante="peligro" onClick={() => onCancelar(ticket)}>
                        Cancelar
                    </Boton>
                )}
            </div>
        </Tarjeta>
    )
}

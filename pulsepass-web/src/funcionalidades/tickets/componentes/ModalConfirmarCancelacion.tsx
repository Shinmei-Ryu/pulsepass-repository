import Aviso from '@/componentes/Aviso'
import Boton from '@/componentes/Boton'
import Modal from '@/componentes/Modal'
import { mensajeDeError } from '@/lib/http'
import { useCancelarTicket } from '../hooks'
import type { TicketResponse } from '../tipos'

interface ModalConfirmarCancelacionProps {
    ticket: TicketResponse
    onCerrar: () => void
    onCancelado: () => void
}

export default function ModalConfirmarCancelacion({ ticket, onCerrar, onCancelado }: ModalConfirmarCancelacionProps) {
    const cancelacion = useCancelarTicket()

    return (
        <Modal titulo="Cancelar ticket" onCerrar={onCerrar}>
            <p className="text-sm text-slate-700 dark:text-slate-300">
                ¿Quieres cancelar el ticket <span className="font-mono font-semibold break-all">{ticket.ticketCode}</span> ({ticket.type})
                de {ticket.eventName}?
            </p>
            {/* Si la regla de negocio lo impide (409), el motivo se ve aquí y el modal no se cierra */}
            {cancelacion.isError && (
                <div className="mt-3">
                    <Aviso tipo="error">{mensajeDeError(cancelacion.error)}</Aviso>
                </div>
            )}
            <div className="mt-4 flex justify-end gap-2">
                <Boton variante="secundario" onClick={onCerrar}>
                    Volver
                </Boton>
                <Boton
                    variante="peligro"
                    cargando={cancelacion.isPending}
                    onClick={() => cancelacion.mutate(ticket.ticketCode, { onSuccess: onCancelado })}
                >
                    Cancelar ticket
                </Boton>
            </div>
        </Modal>
    )
}

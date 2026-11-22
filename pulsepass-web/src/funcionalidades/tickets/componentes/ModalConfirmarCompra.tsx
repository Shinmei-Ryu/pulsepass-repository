import Aviso from '@/componentes/Aviso'
import Boton from '@/componentes/Boton'
import Dato from '@/componentes/Dato'
import Modal from '@/componentes/Modal'
import { mensajeDeError } from '@/lib/http'
import type { TicketType } from '../tipos'

interface ModalConfirmarCompraProps {
    nombreEvento: string
    tipo: TicketType
    email: string
    // La compra la hace PanelCompra; el modal solo muestra su estado
    cargando: boolean
    error: Error | null
    onCerrar: () => void
    onConfirmar: () => void
}

export default function ModalConfirmarCompra({ nombreEvento, tipo, email, cargando, error, onCerrar, onConfirmar }: ModalConfirmarCompraProps) {
    return (
        <Modal titulo="Confirmar compra" onCerrar={onCerrar}>
            <dl>
                <Dato etiqueta="Evento">{nombreEvento}</Dato>
                <Dato etiqueta="Tipo">{tipo}</Dato>
                <Dato etiqueta="Cuenta">{email}</Dato>
            </dl>
            {/* El 409 trae la regla de negocio que falló: se muestra como texto y el modal sigue abierto */}
            {error && (
                <div className="mt-3">
                    <Aviso tipo="error">No se pudo completar la compra: {mensajeDeError(error)}</Aviso>
                </div>
            )}
            <div className="mt-4 flex justify-end gap-2">
                <Boton variante="secundario" onClick={onCerrar}>
                    Cancelar
                </Boton>
                <Boton cargando={cargando} onClick={onConfirmar}>
                    Confirmar
                </Boton>
            </div>
        </Modal>
    )
}

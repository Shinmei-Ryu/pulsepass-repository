import { claseTextoSuave } from '@/componentes/estilos'
import Tarjeta from '@/componentes/Tarjeta'
import type { EventResponse } from '@/funcionalidades/eventos/tipos'

// Versión temporal para que DetalleEvento compile: la compra real llega en la etapa 8
export default function PanelCompra({ evento }: { evento: EventResponse }) {
    return (
        <Tarjeta como="aside" className="h-fit p-5">
            <p className={claseTextoSuave}>La compra de entradas para {evento.name} se habilita en la etapa 8.</p>
        </Tarjeta>
    )
}

import { useState } from 'react'
import Aviso from '@/componentes/Aviso'
import { BadgeEvento, BadgeTicket } from '@/componentes/Badge'
import Boton from '@/componentes/Boton'
import Campo from '@/componentes/Campo'
import { Cargando, EstadoError, EstadoVacio } from '@/componentes/Estados'
import FiltroChips from '@/componentes/FiltroChips'
import Modal from '@/componentes/Modal'
import PrecioCOP from '@/componentes/PrecioCOP'
import Tabla from '@/componentes/Tabla'
import { claseSubtitulo, claseTitulo } from '@/componentes/estilos'

const FILAS = [
    { codigo: 'TCK-1', tipo: 'VIP', precio: 240000 },
    { codigo: 'TCK-2', tipo: 'GENERAL', precio: 120000 },
]
const CHIPS = [
    { valor: 'a', texto: 'Todos' },
    { valor: 'b', texto: 'Pagados' },
] as const

// Galería temporal para revisar los componentes contra docs/diseno/Sistema.png. Se borra en la etapa 5.
export default function App() {
    const [modal, setModal] = useState(false)
    const [chip, setChip] = useState<'a' | 'b'>('a')

    return (
        <main className="mx-auto max-w-3xl space-y-6 p-6">
            <h1 className={claseTitulo}>Sistema visual</h1>

            <h2 className={claseSubtitulo}>Botones</h2>
            <div className="flex flex-wrap gap-2">
                <Boton>Primario</Boton>
                <Boton variante="secundario">Secundario</Boton>
                <Boton variante="peligro">Peligro</Boton>
                <Boton variante="fantasma">Fantasma</Boton>
                <Boton cargando>No se ve</Boton>
                <Boton onClick={() => setModal(true)}>Abrir modal</Boton>
            </div>

            <h2 className={claseSubtitulo}>Badges</h2>
            <div className="flex flex-wrap gap-2">
                <BadgeEvento estado="DRAFT" />
                <BadgeEvento estado="PUBLISHED" />
                <BadgeEvento estado="SOLD_OUT" />
                <BadgeEvento estado="CANCELLED" />
                <BadgeTicket estado="PAID" />
                <BadgeTicket estado="USED" />
            </div>

            <h2 className={claseSubtitulo}>Campos</h2>
            <div className="grid gap-4 sm:grid-cols-2">
                <Campo etiqueta="Campo normal" ayuda="Texto de ayuda" />
                <Campo etiqueta="Campo con error" error="Este campo es obligatorio" />
            </div>

            <h2 className={claseSubtitulo}>Avisos y estados</h2>
            <Aviso tipo="exito">Cuenta creada, inicia sesión.</Aviso>
            <Aviso tipo="error">El ticket no puede cancelarse en su estado actual.</Aviso>
            <Aviso tipo="advertencia">El ticket ya fue utilizado.</Aviso>
            <Cargando />
            <EstadoVacio mensaje="Aún no hay eventos publicados" />
            <EstadoError mensaje="No pudimos conectar con el servidor." onReintentar={() => {}} />

            <h2 className={claseSubtitulo}>Chips, precio y tabla</h2>
            <FiltroChips etiqueta="Ejemplo" opciones={CHIPS} valor={chip} onCambiar={setChip} />
            <p>
                <PrecioCOP valor={250000} />
            </p>
            <Tabla
                titulo="Ejemplo"
                filas={FILAS}
                claveDe={(fila) => fila.codigo}
                columnas={[
                    { encabezado: 'Código', celda: (fila) => fila.codigo },
                    { encabezado: 'Tipo', celda: (fila) => fila.tipo },
                    { encabezado: 'Precio', celda: (fila) => <PrecioCOP valor={fila.precio} /> },
                ]}
            />

            {modal && (
                <Modal titulo="Modal de ejemplo" onCerrar={() => setModal(false)}>
                    <p className="text-sm">Prueba Tab, Shift+Tab y Esc.</p>
                    <div className="mt-4 flex justify-end">
                        <Boton onClick={() => setModal(false)}>Entendido</Boton>
                    </div>
                </Modal>
            )}
        </main>
    )
}

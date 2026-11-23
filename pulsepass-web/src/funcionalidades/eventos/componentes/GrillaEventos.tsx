import type { EventSummaryResponse } from '../tipos'
import TarjetaEvento from './TarjetaEvento'

// Cuadrícula de tarjetas de evento: una columna en el teléfono, dos en tableta, tres en escritorio (PRD 9.3 y 9.4)
export default function GrillaEventos({ eventos }: { eventos: EventSummaryResponse[] }) {
  return (
    <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {eventos.map((evento) => (
        <li key={evento.eventCode}>
          <TarjetaEvento evento={evento} />
        </li>
      ))}
    </ul>
  )
}

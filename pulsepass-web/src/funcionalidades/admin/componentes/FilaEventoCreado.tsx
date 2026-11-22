import { Link } from 'react-router'
import { BadgeEvento } from '@/componentes/Badge'
import { claseBoton } from '@/componentes/estilos'
import Tarjeta from '@/componentes/Tarjeta'
import { useEvento } from '@/funcionalidades/eventos/hooks'
import { RUTAS } from '@/lib/rutas'

// Una fila de "Creados en esta sesión": solo conoce el código y pide el resto a la API,
// para mostrar siempre el nombre y el estado actuales (GAP-01)
export default function FilaEventoCreado({ eventCode }: { eventCode: string }) {
  const evento = useEvento(eventCode)
  return (
    <Tarjeta como="li" className="flex items-center justify-between gap-3 p-4">
      <p className="text-sm">
        <span className="font-mono font-semibold">{eventCode}</span>
        {evento.data && <> · {evento.data.name}</>}
      </p>
      <span className="flex items-center gap-2">
        {evento.data && <BadgeEvento estado={evento.data.status} />}
        <Link to={RUTAS.adminEvento(eventCode)} className={claseBoton('secundario')}>
          Gestionar
        </Link>
      </span>
    </Tarjeta>
  )
}

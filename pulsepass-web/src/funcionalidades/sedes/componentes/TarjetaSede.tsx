import { Link } from 'react-router'
import { IconoEdificio } from '@/componentes/Iconos'
import { claseTarjeta, claseTextoSuave } from '@/componentes/estilos'
import { RUTAS } from '@/lib/rutas'
import type { VenueResponse } from '../tipos'

interface TarjetaSedeProps {
  codigo: string
  nombre: string
  // Llega después del evento (consulta dependiente): mientras tanto solo se muestra el nombre
  sede?: VenueResponse
}

export default function TarjetaSede({ codigo, nombre, sede }: TarjetaSedeProps) {
  return (
    <Link
      to={RUTAS.sede(codigo)}
      className={`${claseTarjeta} focus-visible:outline-marca-600 mt-2 flex items-center gap-3 p-3 focus-visible:outline-2`}
    >
      <IconoEdificio className="size-5 text-slate-500" />
      <span>
        <span className="block text-sm font-medium">{nombre}</span>
        {sede && (
          <span className={claseTextoSuave}>
            {sede.city} · Capacidad {sede.capacity}
          </span>
        )}
      </span>
    </Link>
  )
}

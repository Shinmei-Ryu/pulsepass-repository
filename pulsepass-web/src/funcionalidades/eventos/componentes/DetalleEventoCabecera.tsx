import { BadgeEvento } from '@/componentes/Badge'
import { IconoCalendario, IconoUsuario } from '@/componentes/Iconos'
import { claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import { formatearFechaHora } from '@/lib/formato'
import { CATEGORIA } from '../categorias'
import type { EventResponse } from '../tipos'

// Parte superior del detalle: franja de color de la categoría, estado, nombre, fecha, edad y descripción
export default function DetalleEventoCabecera({ evento }: { evento: EventResponse }) {
  const { fondo, texto, Icono } = CATEGORIA[evento.category]

  return (
    <>
      <div className={`flex h-40 items-center justify-center ${fondo} ${texto}`}>
        <Icono className="size-14" />
      </div>
      <header className="space-y-4 px-5 pt-5">
        <div className="flex items-center gap-2">
          <span className={`text-xs font-semibold tracking-wide ${texto}`}>{evento.category}</span>
          <BadgeEvento estado={evento.status} />
        </div>
        <h1 className={claseTitulo}>{evento.name}</h1>
        <p className={`${claseTextoSuave} flex flex-wrap gap-x-4 gap-y-1`}>
          <span className="flex items-center gap-1.5">
            <IconoCalendario className="size-4" />
            <time dateTime={evento.eventDate}>{formatearFechaHora(evento.eventDate)}</time>
          </span>
          <span className="flex items-center gap-1.5">
            <IconoUsuario className="size-4" />
            Edad mínima: {evento.minimumAge}
          </span>
        </p>
        {evento.description && (
          <p className="text-sm text-slate-700 dark:text-slate-300">{evento.description}</p>
        )}
      </header>
    </>
  )
}

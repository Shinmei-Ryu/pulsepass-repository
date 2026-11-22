import { Link } from 'react-router'
import { BadgeEvento } from '@/componentes/Badge'
import { IconoCalendario, IconoUbicacion } from '@/componentes/Iconos'
import { formatearFecha } from '@/lib/formato'
import { RUTAS } from '@/lib/rutas'
import { CATEGORIA } from '../categorias'
import type { EventSummaryResponse } from '../tipos'
import Tarjeta from '@/componentes/Tarjeta'

export default function TarjetaEvento({ evento }: { evento: EventSummaryResponse }) {
    const { fondo, texto, Icono } = CATEGORIA[evento.category]

    return (
        <Tarjeta como="article" className="overflow-hidden transition-shadow hover:shadow-md">
            <div className={`flex h-28 items-center justify-center ${fondo} ${texto}`}>
                <Icono className="size-10" />
            </div>
            <div className="space-y-1 p-4">
                <p className={`text-xs font-semibold tracking-wide ${texto}`}>{evento.category}</p>
                <h3 className="font-titulo text-base font-semibold text-slate-900 dark:text-white">
                    <Link
                        to={RUTAS.evento(evento.eventCode)}
                        className="rounded hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600"
                    >
                        {evento.name}
                    </Link>
                </h3>
                <p className="flex items-center gap-1.5 text-sm text-slate-600 dark:text-slate-400">
                    <IconoCalendario className="size-4" />
                    <time dateTime={evento.eventDate}>{formatearFecha(evento.eventDate)}</time>
                </p>
                {/* La API de listados no trae la ciudad: se muestra el nombre de la sede (D1) */}
                <p className="flex items-center gap-1.5 text-sm text-slate-600 dark:text-slate-400">
                    <IconoUbicacion className="size-4" />
                    {evento.venueName}
                </p>
                <div className="pt-1">
                    <BadgeEvento estado={evento.status} />
                </div>
            </div>
        </Tarjeta>
    )
}

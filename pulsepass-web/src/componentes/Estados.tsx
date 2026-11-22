import type { ReactNode } from 'react'
import Boton from './Boton'
import { IconoAlerta, IconoBandeja } from './Iconos'
import Tarjeta from './Tarjeta'

// Los tres estados que toda pantalla con datos debe definir además del de éxito (FE-014)

export function Cargando({ etiqueta = 'Cargando…' }: { etiqueta?: string }) {
    return (
        <div role="status" aria-busy="true" className="animate-pulse space-y-3 py-4">
            <span className="sr-only">{etiqueta}</span>
            <div className="h-32 rounded-xl bg-slate-200 dark:bg-slate-700" />
            <div className="h-4 w-2/3 rounded bg-slate-200 dark:bg-slate-700" />
            <div className="h-4 w-1/2 rounded bg-slate-200 dark:bg-slate-700" />
        </div>
    )
}

export function EstadoVacio({ mensaje, accion }: { mensaje: string; accion?: ReactNode }) {
    return (
        <Tarjeta className="flex flex-col items-center gap-3 p-8 text-center">
            <IconoBandeja className="size-8 text-slate-400" />
            <p className="text-sm font-medium text-slate-700 dark:text-slate-200">{mensaje}</p>
            {accion}
        </Tarjeta>
    )
}

export function EstadoError({ mensaje, onReintentar }: { mensaje: string; onReintentar?: () => void }) {
    return (
        <Tarjeta className="flex flex-col items-center gap-3 p-8 text-center">
            <div role="alert" className="flex items-center gap-2 text-sm text-red-700 dark:text-red-400">
                <IconoAlerta className="size-4 shrink-0" />
                {mensaje}
            </div>
            {onReintentar && <Boton onClick={onReintentar}>Reintentar</Boton>}
        </Tarjeta>
    )
}

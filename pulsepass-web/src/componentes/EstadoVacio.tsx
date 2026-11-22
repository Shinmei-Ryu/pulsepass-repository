import type { ReactNode } from 'react'
import { IconoBandeja } from './Iconos'
import Tarjeta from './Tarjeta'

// Uno de los tres estados que toda pantalla con datos debe definir además del de éxito (FE-014)
export default function EstadoVacio({ mensaje, accion }: { mensaje: string; accion?: ReactNode }) {
    return (
        <Tarjeta className="flex flex-col items-center gap-3 p-8 text-center">
            <IconoBandeja className="size-8 text-slate-400" />
            <p className="text-sm font-medium text-slate-700 dark:text-slate-200">{mensaje}</p>
            {accion}
        </Tarjeta>
    )
}

import { useEffect, type ReactNode } from 'react'
import clsx from 'clsx'
import { IconoAlerta, IconoCheck } from './Iconos'

type Tipo = 'exito' | 'error' | 'info' | 'advertencia'

const estilos: Record<Tipo, string> = {
  exito:
    'border-green-200 bg-green-100 text-green-800 dark:border-green-800 dark:bg-green-950 dark:text-green-200',
  error: 'border-red-200 bg-red-100 text-red-800 dark:border-red-800 dark:bg-red-950 dark:text-red-200',
  info: 'border-marca-500/30 bg-marca-50 text-marca-700 dark:bg-slate-800 dark:text-marca-500',
  advertencia:
    'border-amber-200 bg-amber-100 text-amber-800 dark:border-amber-800 dark:bg-amber-950 dark:text-amber-200',
}

interface AvisoProps {
  tipo: Tipo
  children: ReactNode
  // Si se entrega, los avisos de éxito se cierran solos a los 4 segundos
  onCerrar?: () => void
}

export default function Aviso({ tipo, children, onCerrar }: AvisoProps) {
  useEffect(() => {
    if (tipo !== 'exito' || !onCerrar) return
    const reloj = setTimeout(onCerrar, 4000)
    return () => clearTimeout(reloj)
  }, [tipo, onCerrar])

  return (
    <div
      // "alert" interrumpe al lector de pantalla; "status" espera su turno
      role={tipo === 'error' || tipo === 'advertencia' ? 'alert' : 'status'}
      className={clsx('flex items-start gap-2 rounded-lg border px-3 py-2 text-sm', estilos[tipo])}
    >
      {tipo === 'exito' ? (
        <IconoCheck className="mt-0.5 size-4 shrink-0" />
      ) : (
        <IconoAlerta className="mt-0.5 size-4 shrink-0" />
      )}
      <div>{children}</div>
    </div>
  )
}

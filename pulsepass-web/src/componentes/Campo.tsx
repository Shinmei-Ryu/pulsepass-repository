import { useId, type ComponentProps } from 'react'
import clsx from 'clsx'
import { claseControl } from './estilos'

export interface CampoProps extends ComponentProps<'input'> {
  etiqueta: string
  error?: string
  ayuda?: string
}

// En React 19 "ref" es una prop más: {...register('x')} de React Hook Form funciona sin forwardRef
export default function Campo({ etiqueta, error, ayuda, className, ...resto }: CampoProps) {
  const id = useId()
  const idError = `${id}-error`
  const idAyuda = `${id}-ayuda`

  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1 block text-sm font-medium text-slate-700 dark:text-slate-200">
        {etiqueta}
      </label>
      <input
        id={id}
        aria-invalid={error ? true : undefined}
        aria-describedby={clsx(error && idError, ayuda && idAyuda) || undefined}
        className={clsx(claseControl, error ? 'border-red-500' : 'border-slate-300 dark:border-slate-600')}
        {...resto}
      />
      {ayuda && !error && (
        <p id={idAyuda} className="mt-1 text-xs text-slate-500 dark:text-slate-400">
          {ayuda}
        </p>
      )}
      {error && (
        <p id={idError} className="mt-1 text-sm text-red-700 dark:text-red-400">
          {error}
        </p>
      )}
    </div>
  )
}

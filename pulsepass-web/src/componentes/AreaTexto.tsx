import { useId, type ComponentProps } from 'react'
import clsx from 'clsx'
import { claseControl } from './estilos'

interface AreaTextoProps extends ComponentProps<'textarea'> {
    etiqueta: string
    error?: string
    ayuda?: string
}

export default function AreaTexto({ etiqueta, error, ayuda, className, ...resto }: AreaTextoProps) {
    const id = useId()
    const idError = `${id}-error`

    return (
        <div className={className}>
            <label htmlFor={id} className="mb-1 block text-sm font-medium text-slate-700 dark:text-slate-200">
                {etiqueta}
            </label>
            <textarea
                id={id}
                rows={3}
                aria-invalid={error ? true : undefined}
                aria-describedby={error ? idError : undefined}
                className={clsx(claseControl, error ? 'border-red-500' : 'border-slate-300 dark:border-slate-600')}
                {...resto}
            />
            {ayuda && <p className="mt-1 text-right text-xs text-slate-500 dark:text-slate-400">{ayuda}</p>}
            {error && (
                <p id={idError} className="mt-1 text-sm text-red-700 dark:text-red-400">
                    {error}
                </p>
            )}
        </div>
    )
}
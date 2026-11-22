import { useId, type ComponentProps } from 'react'
import clsx from 'clsx'
import { claseControl } from './estilos'

interface SelectorProps extends ComponentProps<'select'> {
    etiqueta: string
    error?: string
}

export default function Selector({ etiqueta, error, className, children, ...resto }: SelectorProps) {
    const id = useId()
    const idError = `${id}-error`

    return (
        <div className={className}>
            <label htmlFor={id} className="mb-1 block text-sm font-medium text-slate-700 dark:text-slate-200">
                {etiqueta}
            </label>
            <select
                id={id}
                aria-invalid={error ? true : undefined}
                aria-describedby={error ? idError : undefined}
                className={clsx(claseControl, error ? 'border-red-500' : 'border-slate-300 dark:border-slate-600')}
                {...resto}
            >
                {children}
            </select>
            {error && (
                <p id={idError} className="mt-1 text-sm text-red-700 dark:text-red-400">
                    {error}
                </p>
            )}
        </div>
    )
}

import clsx from 'clsx'

interface FiltroChipsProps<T extends string> {
    etiqueta: string
    opciones: readonly { valor: T; texto: string }[]
    valor: T
    onCambiar: (valor: T) => void
}

export default function FiltroChips<T extends string>({ etiqueta, opciones, valor, onCambiar }: FiltroChipsProps<T>) {
    return (
        <div role="group" aria-label={etiqueta} className="flex flex-wrap gap-2">
            {opciones.map((opcion) => {
                const activo = opcion.valor === valor
                return (
                    <button
                        key={opcion.valor}
                        type="button"
                        aria-pressed={activo}
                        onClick={() => onCambiar(opcion.valor)}
                        className={clsx(
                            'min-h-11 rounded-full px-4 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600',
                            activo
                                ? 'bg-marca-600 text-white'
                                : 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-200',
                        )}
                    >
                        {opcion.texto}
                    </button>
                )
            })}
        </div>
    )
}

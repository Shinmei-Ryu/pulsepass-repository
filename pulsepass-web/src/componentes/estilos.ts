import clsx from 'clsx'

// Clases compartidas. Siempre cadenas completas: Tailwind no detecta nombres armados con variables.
export type VarianteBoton = 'primario' | 'secundario' | 'peligro' | 'fantasma'

const base =
  'inline-flex min-h-11 items-center justify-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors ' +
  'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600 ' +
  'disabled:cursor-not-allowed disabled:opacity-50'

const variantes: Record<VarianteBoton, string> = {
  primario: 'bg-marca-600 text-white hover:bg-marca-700',
  secundario:
    'border border-slate-300 bg-white text-slate-800 hover:bg-slate-50 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700',
  peligro: 'bg-red-600 text-white hover:bg-red-700',
  fantasma: 'text-marca-600 hover:bg-marca-50 dark:text-marca-500 dark:hover:bg-slate-800',
}

// Sirve para <button> y también para <Link> con apariencia de botón
export function claseBoton(variante: VarianteBoton = 'primario', extra?: string) {
  return clsx(base, variantes[variante], extra)
}

export const claseTarjeta =
  'rounded-xl border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800'

export const claseControl =
  'block min-h-11 w-full rounded-lg border bg-white px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 ' +
  'focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-marca-600 ' +
  'dark:bg-slate-900 dark:text-slate-100'

export const claseEnlace =
  'rounded text-marca-600 hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600 dark:text-marca-500'

export const claseTitulo = 'font-titulo text-2xl font-bold text-slate-900 sm:text-3xl dark:text-white'
export const claseSubtitulo = 'font-titulo text-lg font-semibold text-slate-900 dark:text-white'
export const claseTextoSuave = 'text-sm text-slate-600 dark:text-slate-400'

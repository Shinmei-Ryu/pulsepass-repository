import type { ReactNode } from 'react'

// Fila "etiqueta — valor" de las fichas de detalle (sede, ticket, compra)
export default function Dato({ etiqueta, children }: { etiqueta: string; children: ReactNode }) {
  return (
    <div className="flex items-center justify-between gap-4 border-b border-slate-100 py-2 text-sm last:border-0 dark:border-slate-700">
      <dt className="text-slate-500 dark:text-slate-400">{etiqueta}</dt>
      <dd className="text-right font-medium break-all text-slate-900 dark:text-slate-100">{children}</dd>
    </div>
  )
}

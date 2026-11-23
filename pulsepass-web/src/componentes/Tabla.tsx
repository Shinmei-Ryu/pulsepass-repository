import type { ReactNode } from 'react'
import Tarjeta from './Tarjeta'

export interface Columna<T> {
  encabezado: string
  celda: (fila: T) => ReactNode
}

interface TablaProps<T> {
  titulo: string
  columnas: Columna<T>[]
  filas: T[]
  claveDe: (fila: T) => string
}

// Genérica: sirve para cualquier tipo de fila sin perder el tipado de cada celda.
// En pantallas pequeñas la misma información se muestra como tarjetas.
export default function Tabla<T>({ titulo, columnas, filas, claveDe }: TablaProps<T>) {
  return (
    <>
      <Tarjeta className="hidden overflow-hidden md:block">
        <table className="w-full text-left text-sm">
          <caption className="sr-only">{titulo}</caption>
          <thead className="bg-slate-50 text-xs text-slate-600 dark:bg-slate-900 dark:text-slate-400">
            <tr>
              {columnas.map((columna) => (
                <th key={columna.encabezado} scope="col" className="px-4 py-3 font-medium">
                  {columna.encabezado}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200 dark:divide-slate-700">
            {filas.map((fila) => (
              <tr key={claveDe(fila)}>
                {columnas.map((columna) => (
                  <td key={columna.encabezado} className="px-4 py-3 text-slate-800 dark:text-slate-200">
                    {columna.celda(fila)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </Tarjeta>

      <ul className="space-y-3 md:hidden">
        {filas.map((fila) => (
          <Tarjeta como="li" key={claveDe(fila)} className="space-y-2 p-4">
            {columnas.map((columna) => (
              <div key={columna.encabezado} className="flex items-center justify-between gap-4 text-sm">
                <span className="text-slate-500 dark:text-slate-400">{columna.encabezado}</span>
                <span className="text-right text-slate-800 dark:text-slate-200">{columna.celda(fila)}</span>
              </div>
            ))}
          </Tarjeta>
        ))}
      </ul>
    </>
  )
}

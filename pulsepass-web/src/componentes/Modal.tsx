import { useEffect, useId, useRef, type KeyboardEvent, type ReactNode } from 'react'
import { IconoCerrar } from './Iconos'

interface ModalProps {
  titulo: string
  onCerrar: () => void
  children: ReactNode
}

const ENFOCABLES =
  'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled])'

// El componente padre decide cuándo existe: {abierto && <Modal …>}
export default function Modal({ titulo, onCerrar, children }: ModalProps) {
  const idTitulo = useId()
  const panel = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    // Recuerda quién tenía el foco para devolvérselo al cerrar
    const anterior = document.activeElement
    panel.current?.querySelector<HTMLElement>(ENFOCABLES)?.focus()
    return () => {
      if (anterior instanceof HTMLElement) anterior.focus()
    }
  }, [])

  const alTeclear = (evento: KeyboardEvent<HTMLDivElement>) => {
    if (evento.key === 'Escape') {
      onCerrar()
      return
    }
    if (evento.key !== 'Tab' || !panel.current) return

    // Trampa de foco: Tab no puede salir del modal
    const elementos = panel.current.querySelectorAll<HTMLElement>(ENFOCABLES)
    const primero = elementos[0]
    const ultimo = elementos[elementos.length - 1]
    if (!primero || !ultimo) return

    if (evento.shiftKey && document.activeElement === primero) {
      evento.preventDefault()
      ultimo.focus()
    } else if (!evento.shiftKey && document.activeElement === ultimo) {
      evento.preventDefault()
      primero.focus()
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4"
      onKeyDown={alTeclear}
    >
      <div
        ref={panel}
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        className="w-full max-w-md rounded-xl bg-white p-5 shadow-xl dark:bg-slate-800"
      >
        <div className="mb-4 flex items-center justify-between gap-4">
          <h2 id={idTitulo} className="font-titulo text-lg font-semibold text-slate-900 dark:text-white">
            {titulo}
          </h2>
          <button
            type="button"
            onClick={onCerrar}
            aria-label="Cerrar"
            className="focus-visible:outline-marca-600 flex size-11 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 focus-visible:outline-2 dark:hover:bg-slate-700"
          >
            <IconoCerrar className="size-5" />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}

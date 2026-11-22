import { useEffect, useRef, useState, type FocusEvent, type KeyboardEvent } from 'react'
import { Link } from 'react-router'
import { IconoChevronAbajo } from '@/componentes/Iconos'
import { iniciales } from '@/lib/formato'
import { RUTAS } from '@/lib/rutas'

interface MenuUsuarioProps {
  username: string
  // "Mis tickets" solo existe para el rol USER (tabla 8.4 del PRD)
  conTickets: boolean
  onSalir: () => void
}

const claseItem =
  'flex min-h-11 w-full items-center rounded-lg px-3 text-left text-sm font-medium text-slate-700 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-marca-600 dark:text-slate-200 dark:hover:bg-slate-800'

// Menú de usuario del escritorio (DES-03): avatar con iniciales y lista desplegable
export default function MenuUsuario({ username, conTickets, onSalir }: MenuUsuarioProps) {
  const [abierto, setAbierto] = useState(false)
  const cerrar = () => setAbierto(false)

  const contenedor = useRef<HTMLDivElement | null>(null)

  // Clic o toque fuera del menú: se cierra
  useEffect(() => {
    if (!abierto) return
    const alPulsarFuera = (evento: PointerEvent) => {
      if (evento.target instanceof Node && !contenedor.current?.contains(evento.target)) setAbierto(false)
    }
    document.addEventListener('pointerdown', alPulsarFuera)
    return () => document.removeEventListener('pointerdown', alPulsarFuera)
  }, [abierto])

  // Teclado: al salir con Tab más allá del último elemento, se cierra.
  // relatedTarget es null en un clic de Safari (no enfoca botones): ese caso lo cubre el efecto de arriba.
  const alPerderFoco = (evento: FocusEvent<HTMLDivElement>) => {
    const destino = evento.relatedTarget
    if (destino instanceof Node && !evento.currentTarget.contains(destino)) cerrar()
  }

  const alTeclear = (evento: KeyboardEvent<HTMLDivElement>) => {
    if (evento.key === 'Escape') cerrar()
  }

  return (
    <div ref={contenedor} className="relative" onBlur={alPerderFoco} onKeyDown={alTeclear}>
      <button
        type="button"
        aria-expanded={abierto}
        aria-controls="menu-usuario"
        aria-label={`Menú de usuario: ${username}`}
        onClick={() => setAbierto((actual) => !actual)}
        className="focus-visible:outline-marca-600 flex min-h-11 items-center gap-2 rounded-lg px-2 text-sm font-medium text-slate-700 hover:bg-slate-100 focus-visible:outline-2 dark:text-slate-200 dark:hover:bg-slate-800"
      >
        <span
          aria-hidden="true"
          className="bg-marca-600 flex size-8 items-center justify-center rounded-full text-xs font-semibold text-white"
        >
          {iniciales(username)}
        </span>
        {username}
        <IconoChevronAbajo className="size-4" />
      </button>

      {abierto && (
        <div
          id="menu-usuario"
          className="absolute right-0 z-50 mt-1 w-48 rounded-lg border border-slate-200 bg-white p-1 shadow-lg dark:border-slate-700 dark:bg-slate-900"
        >
          <Link to={RUTAS.cuenta} className={claseItem} onClick={cerrar}>
            Mi cuenta
          </Link>
          {conTickets && (
            <Link to={RUTAS.misTickets} className={claseItem} onClick={cerrar}>
              Mis tickets
            </Link>
          )}
          <button type="button" className={claseItem} onClick={onSalir}>
            Salir
          </button>
        </div>
      )}
    </div>
  )
}

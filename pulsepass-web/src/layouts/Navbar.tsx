import { useState, type KeyboardEvent } from 'react'
import { Link, NavLink, useNavigate } from 'react-router'
import clsx from 'clsx'
import { IconoCerrar, IconoLuna, IconoMenu, IconoMusica, IconoSol } from '@/componentes/Iconos'
import { claseBoton } from '@/componentes/estilos'
import { tieneRol, useAuth } from '@/funcionalidades/auth/hooks'
import { RUTAS } from '@/lib/rutas'
import { useTema } from '@/lib/useTema'
import MenuUsuario from './MenuUsuario'
import { claseEnlaceNav, enlacesPara, type Enlace } from './enlacesNav'

export default function Navbar() {
  const { sesion, cerrarSesion } = useAuth()
  const navigate = useNavigate()
  const { tema, alternar } = useTema()
  const [menuAbierto, setMenuAbierto] = useState(false)

  const enlaces = enlacesPara(sesion)
  const cerrarMenu = () => setMenuAbierto(false)

  const salir = () => {
    cerrarMenu()
    cerrarSesion('manual')
    navigate(RUTAS.inicio)
  }

  const alTeclear = (evento: KeyboardEvent<HTMLElement>) => {
    if (evento.key === 'Escape') cerrarMenu()
  }

  const claseDe = (enlace: Enlace) => (estado: { isActive: boolean }) =>
    claseEnlaceNav({ isActive: estado.isActive && !enlace.sinActivo })

  const anonimo = (
    <>
      <Link to={RUTAS.login} className={claseBoton('fantasma')} onClick={cerrarMenu}>
        Ingresar
      </Link>
      <Link to={RUTAS.registro} className={claseBoton('primario')} onClick={cerrarMenu}>
        Registrarse
      </Link>
    </>
  )

  // Escritorio: menú desplegable con avatar (DES-03). Móvil: las mismas acciones, a la vista.
  const accionesEscritorio =
    sesion.estado === 'autenticado' ? (
      <MenuUsuario username={sesion.usuario.username} conTickets={tieneRol(sesion, 'USER')} onSalir={salir} />
    ) : sesion.estado === 'anonimo' ? (
      anonimo
    ) : null

  const accionesMovil =
    sesion.estado === 'autenticado' ? (
      <>
        <NavLink to={RUTAS.cuenta} className={claseEnlaceNav} onClick={cerrarMenu}>
          Mi cuenta ({sesion.usuario.username})
        </NavLink>
        <button type="button" onClick={salir} className={claseBoton('secundario')}>
          Salir
        </button>
      </>
    ) : sesion.estado === 'anonimo' ? (
      anonimo
    ) : null

  return (
    <header
      className="sticky top-0 z-40 border-b border-slate-200 bg-white dark:border-slate-700 dark:bg-slate-900"
      onKeyDown={alTeclear}
    >
      <div className="mx-auto flex max-w-6xl items-center gap-4 px-4 py-2">
        <Link
          to={RUTAS.inicio}
          className="font-titulo focus-visible:outline-marca-600 flex min-h-11 items-center gap-2 rounded-lg text-lg font-bold text-slate-900 focus-visible:outline-2 dark:text-white"
        >
          <span className="bg-marca-600 flex size-8 items-center justify-center rounded-lg text-white">
            <IconoMusica className="size-4" />
          </span>
          PulsePass
        </Link>

        <nav aria-label="Principal" className="hidden flex-1 items-center gap-1 md:flex">
          {enlaces.map((enlace) => (
            <NavLink key={enlace.a} to={enlace.a} end={enlace.end} className={claseDe(enlace)}>
              {enlace.texto}
            </NavLink>
          ))}
        </nav>

        <div className="ml-auto flex items-center gap-2">
          <button
            type="button"
            onClick={alternar}
            aria-label={tema === 'oscuro' ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'}
            className="focus-visible:outline-marca-600 flex size-11 items-center justify-center rounded-lg text-slate-600 hover:bg-slate-100 focus-visible:outline-2 dark:text-slate-300 dark:hover:bg-slate-800"
          >
            {tema === 'oscuro' ? <IconoSol className="size-5" /> : <IconoLuna className="size-5" />}
          </button>

          <div className="hidden items-center gap-2 md:flex">{accionesEscritorio}</div>

          <button
            type="button"
            onClick={() => setMenuAbierto((abierto) => !abierto)}
            aria-expanded={menuAbierto}
            aria-controls="menu-movil"
            aria-label={menuAbierto ? 'Cerrar menú' : 'Abrir menú'}
            className="focus-visible:outline-marca-600 flex size-11 items-center justify-center rounded-lg text-slate-700 hover:bg-slate-100 focus-visible:outline-2 md:hidden dark:text-slate-200 dark:hover:bg-slate-800"
          >
            {menuAbierto ? <IconoCerrar className="size-6" /> : <IconoMenu className="size-6" />}
          </button>
        </div>
      </div>

      {menuAbierto && (
        <nav
          id="menu-movil"
          aria-label="Menú móvil"
          className="space-y-1 border-t border-slate-200 px-4 py-3 md:hidden dark:border-slate-700"
        >
          {enlaces.map((enlace) => (
            <NavLink
              key={enlace.a}
              to={enlace.a}
              end={enlace.end}
              onClick={cerrarMenu}
              className={(estado: { isActive: boolean }) => clsx(claseDe(enlace)(estado), 'flex w-full')}
            >
              {enlace.texto}
            </NavLink>
          ))}
          <div className="flex flex-wrap gap-2 border-t border-slate-200 pt-3 dark:border-slate-700">
            {accionesMovil}
          </div>
        </nav>
      )}
    </header>
  )
}

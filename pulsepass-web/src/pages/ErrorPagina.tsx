import { isRouteErrorResponse, Link, useRouteError } from 'react-router'
import { claseBoton, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import { RUTAS } from '@/lib/rutas'

// errorElement de la ruta raíz: atrapa errores de renderizado y de carga de rutas.
// Nunca muestra error.message ni la traza: pueden contener detalles internos.
export default function ErrorPagina() {
  const error = useRouteError()
  const noEncontrada = isRouteErrorResponse(error) && error.status === 404

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-50 p-6 text-center dark:bg-slate-950">
      <h1 className={claseTitulo}>{noEncontrada ? 'No encontramos esta página' : '¡Ups! Algo salió mal'}</h1>
      <p className={claseTextoSuave}>Ocurrió un error inesperado. Inténtalo más tarde.</p>
      <div className="flex gap-2">
        <button type="button" className={claseBoton('primario')} onClick={() => window.location.reload()}>
          Reintentar
        </button>
        <Link to={RUTAS.inicio} className={claseBoton('secundario')}>
          Volver al inicio
        </Link>
      </div>
    </main>
  )
}

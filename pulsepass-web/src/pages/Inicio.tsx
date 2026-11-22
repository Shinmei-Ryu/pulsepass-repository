import { Link } from 'react-router'
import Cargando from '@/componentes/Cargando'
import EstadoError from '@/componentes/EstadoError'
import EstadoVacio from '@/componentes/EstadoVacio'
import { claseBoton, claseEnlace, claseSubtitulo } from '@/componentes/estilos'
import AvatarArtista from '@/funcionalidades/artistas/componentes/AvatarArtista'
import { useArtistasActivos } from '@/funcionalidades/artistas/hooks'
import { useAuth } from '@/funcionalidades/auth/hooks'
import GrillaEventos from '@/funcionalidades/eventos/componentes/GrillaEventos'
import Hero from '@/funcionalidades/eventos/componentes/Hero'
import { useEventosPublicados } from '@/funcionalidades/eventos/hooks'
import { mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'

export default function Inicio() {
  const { sesion } = useAuth()
  const eventos = useEventosPublicados()
  const artistas = useArtistasActivos()

  return (
    <div className="space-y-10">
      <Hero mostrarRegistro={sesion.estado === 'anonimo'} />

      <section aria-labelledby="proximos">
        <div className="mb-4 flex items-center justify-between">
          <h2 id="proximos" className={claseSubtitulo}>
            Próximos eventos
          </h2>
          <Link to={RUTAS.eventos} className={claseEnlace}>
            Ver todos
          </Link>
        </div>
        {eventos.isPending && <Cargando />}
        {eventos.isError && (
          <EstadoError mensaje={mensajeDeError(eventos.error)} onReintentar={eventos.refetch} />
        )}
        {eventos.data && eventos.data.length === 0 && (
          <EstadoVacio
            mensaje="Aún no hay eventos publicados"
            accion={
              <Link to={RUTAS.artistas} className={claseBoton('secundario')}>
                Ver artistas
              </Link>
            }
          />
        )}
        {eventos.data && eventos.data.length > 0 && <GrillaEventos eventos={eventos.data.slice(0, 3)} />}
      </section>

      <section aria-labelledby="destacados">
        <div className="mb-4 flex items-center justify-between">
          <h2 id="destacados" className={claseSubtitulo}>
            Artistas destacados
          </h2>
          <Link to={RUTAS.artistas} className={claseEnlace}>
            Ver todos
          </Link>
        </div>
        {artistas.isPending && <Cargando />}
        {artistas.isError && (
          <EstadoError mensaje={mensajeDeError(artistas.error)} onReintentar={artistas.refetch} />
        )}
        {artistas.data && (
          <ul className="flex flex-wrap gap-4">
            {artistas.data.slice(0, 6).map((artista) => (
              <li key={artista.id}>
                <Link
                  to={RUTAS.artista(artista.id)}
                  className={`${claseEnlace} flex min-h-11 items-center gap-2 text-sm font-medium`}
                >
                  <AvatarArtista nombre={artista.stageName} tamano="sm" />
                  {artista.stageName}
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}

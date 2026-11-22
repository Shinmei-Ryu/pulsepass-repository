import { Link } from 'react-router'
import Cargando from '@/componentes/Cargando'
import EstadoError from '@/componentes/EstadoError'
import EstadoVacio from '@/componentes/EstadoVacio'
import { claseTarjeta, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import AvatarArtista from '@/funcionalidades/artistas/componentes/AvatarArtista'
import { useArtistasActivos } from '@/funcionalidades/artistas/hooks'
import { mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'

export default function ListaArtistas() {
  const artistas = useArtistasActivos()

  return (
      <div className="space-y-6">
        <h1 className={claseTitulo}>Artistas</h1>
        {artistas.isPending && <Cargando />}
        {artistas.isError && <EstadoError mensaje={mensajeDeError(artistas.error)} onReintentar={artistas.refetch} />}
        {artistas.data && artistas.data.length === 0 && <EstadoVacio mensaje="Aún no hay artistas activos" />}
        {artistas.data && artistas.data.length > 0 && (
            <ul className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
              {artistas.data.map((artista) => (
                  <li key={artista.id}>
                    <Link
                        to={RUTAS.artista(artista.id)}
                        className={`${claseTarjeta} flex flex-col items-center gap-2 p-5 text-center hover:shadow-md focus-visible:outline-2 focus-visible:outline-marca-600`}
                    >
                      <AvatarArtista nombre={artista.stageName} />
                      <span className="text-sm font-semibold">{artista.stageName}</span>
                      <span className={claseTextoSuave}>{artista.genre}</span>
                    </Link>
                  </li>
              ))}
            </ul>
        )}
      </div>
  )
}

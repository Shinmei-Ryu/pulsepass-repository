import { Link, useParams } from 'react-router'
import { BadgeActivo } from '@/componentes/Badge'
import Cargando from '@/componentes/Cargando'
import EstadoError from '@/componentes/EstadoError'
import EstadoVacio from '@/componentes/EstadoVacio'
import { IconoFlechaIzquierda } from '@/componentes/Iconos'
import { claseEnlace, claseSubtitulo, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import AvatarArtista from '@/funcionalidades/artistas/componentes/AvatarArtista'
import { useArtista } from '@/funcionalidades/artistas/hooks'
import GrillaEventos from '@/funcionalidades/eventos/componentes/GrillaEventos'
import { useEventosPorArtista } from '@/funcionalidades/eventos/hooks'
import { esEstado, mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'
import NoEncontrada from './NoEncontrada'
import Tarjeta from '@/componentes/Tarjeta'

export default function DetalleArtista() {
  const params = useParams<{ id: string }>()
  // useParams siempre entrega texto: se convierte y se valida antes de llamar a la API
  const id = Number(params.id)
  const idValido = Number.isInteger(id) && id > 0

  const artista = useArtista(idValido ? id : undefined)
  const eventos = useEventosPorArtista(artista.data?.stageName)

  if (!idValido) return <NoEncontrada titulo="Artista no disponible" mensaje="El identificador del artista no es válido." />
  if (artista.isPending) return <Cargando />
  if (artista.isError) {
    return esEstado(artista.error, 404) ? (
        <NoEncontrada titulo="Artista no disponible" mensaje="No encontramos este artista." />
    ) : (
        <EstadoError mensaje={mensajeDeError(artista.error)} onReintentar={artista.refetch} />
    )
  }

  const datos = artista.data
  if (!datos) return null

  return (
      <div className="space-y-6">
        <Link to={RUTAS.artistas} className={`${claseEnlace} inline-flex min-h-11 items-center gap-1 text-sm`}>
          <IconoFlechaIzquierda className="size-4" />
          Artistas
        </Link>

        <Tarjeta como="header" className="flex items-center gap-4 p-5">
          <AvatarArtista nombre={datos.stageName} tamano="lg" />
          <div className="space-y-1">
            <h1 className={claseTitulo}>{datos.stageName}</h1>
            <p className={claseTextoSuave}>
              {datos.genre} · {datos.country}
            </p>
            <BadgeActivo activo={datos.active} />
          </div>
        </Tarjeta>

        <section aria-labelledby="eventos-artista" className="space-y-4">
          <h2 id="eventos-artista" className={claseSubtitulo}>
            Próximos eventos de {datos.stageName}
          </h2>
          {eventos.isPending && <Cargando />}
          {eventos.isError && <EstadoError mensaje={mensajeDeError(eventos.error)} onReintentar={eventos.refetch} />}
          {eventos.data && eventos.data.length === 0 && <EstadoVacio mensaje="Este artista aún no tiene eventos" />}
          {eventos.data && eventos.data.length > 0 && (
              <GrillaEventos eventos={eventos.data} />
          )}
        </section>
      </div>
  )
}

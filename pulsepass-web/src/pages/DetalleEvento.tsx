import { Link, useParams } from 'react-router'
import Cargando from '@/componentes/Cargando'
import EstadoError from '@/componentes/EstadoError'
import { IconoFlechaIzquierda } from '@/componentes/Iconos'
import Tarjeta from '@/componentes/Tarjeta'
import { claseEnlace, claseSubtitulo } from '@/componentes/estilos'
import DetalleEventoCabecera from '@/funcionalidades/eventos/componentes/DetalleEventoCabecera'
import ListaArtistasEvento from '@/funcionalidades/eventos/componentes/ListaArtistasEvento'
import { useEvento } from '@/funcionalidades/eventos/hooks'
import TarjetaSede from '@/funcionalidades/sedes/componentes/TarjetaSede'
import { useSede } from '@/funcionalidades/sedes/hooks'
import PanelCompra from '@/funcionalidades/tickets/componentes/PanelCompra'
import { esEstado, mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'
import NoEncontrada from './NoEncontrada'

export default function DetalleEvento() {
  const { eventCode } = useParams<{ eventCode: string }>()
  const evento = useEvento(eventCode)
  // Consulta dependiente: solo se dispara cuando ya se conoce el código de la sede (D2)
  const sede = useSede(evento.data?.venueCode)

  if (evento.isPending) return <Cargando />
  if (evento.isError) {
    // GAP-02: un borrador responde 404 (o 403) a anónimos y USER; se trata igual que un código inexistente
    return esEstado(evento.error, 404, 403) ? (
        <NoEncontrada titulo="Evento no disponible" mensaje="El evento no existe o todavía no está publicado." />
    ) : (
        <EstadoError mensaje={mensajeDeError(evento.error)} onReintentar={evento.refetch} />
    )
  }

  const datos = evento.data
  if (!datos) return null

  return (
      <div className="space-y-4 pb-24 md:pb-0">
        <Link to={RUTAS.eventos} className={`${claseEnlace} inline-flex min-h-11 items-center gap-1 text-sm`}>
          <IconoFlechaIzquierda className="size-4" />
          Volver a la cartelera
        </Link>

        <div className="grid gap-6 lg:grid-cols-3">
          <Tarjeta como="article" className="overflow-hidden lg:col-span-2">
            <DetalleEventoCabecera evento={datos} />
            <div className="space-y-4 p-5">
              <ListaArtistasEvento artistas={datos.artists} />
              <section aria-labelledby="sede-evento">
                <h2 id="sede-evento" className={claseSubtitulo}>
                  Sede
                </h2>
                <TarjetaSede codigo={datos.venueCode} nombre={datos.venueName} sede={sede.data} />
              </section>
            </div>
          </Tarjeta>

          <PanelCompra evento={datos} />
        </div>
      </div>
  )
}

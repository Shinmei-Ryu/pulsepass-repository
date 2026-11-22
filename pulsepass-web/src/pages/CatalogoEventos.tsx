import { useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import Aviso from '@/componentes/Aviso'
import Boton from '@/componentes/Boton'
import Cargando from '@/componentes/Cargando'
import EstadoError from '@/componentes/EstadoError'
import EstadoVacio from '@/componentes/EstadoVacio'
import { claseEnlace, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import { useArtistaPorNombre, useArtistasActivos } from '@/funcionalidades/artistas/hooks'
import BarraFiltros from '@/funcionalidades/eventos/componentes/BarraFiltros'
import GrillaEventos from '@/funcionalidades/eventos/componentes/GrillaEventos'
import { useEventosPorArtista, useEventosPublicados } from '@/funcionalidades/eventos/hooks'
import { EventCategorySchema } from '@/funcionalidades/eventos/tipos'
import { esEstado, mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'

export default function CatalogoEventos() {
  // Los filtros viven en la URL: se pueden compartir y sobreviven a recargar
  const [params, setParams] = useSearchParams()
  // El texto se guarda también en estado local: así el campo responde al instante mientras la URL se actualiza
  const [texto, setTexto] = useState(params.get('q') ?? '')
  const artista = params.get('artista') ?? ''
  // Un parámetro de URL es un dato externo: se valida antes de usarlo
  const categoriaValida = EventCategorySchema.safeParse(params.get('categoria'))
  const categoria = categoriaValida.success ? categoriaValida.data : undefined

  const publicados = useEventosPublicados()
  const porArtista = useEventosPorArtista(artista || undefined)
  const artistas = useArtistasActivos()
  // ?artista= llega por la URL y puede no existir: se resuelve con GET /artists/by-stage-name (FR-FE-ART-003)
  const artistaElegido = useArtistaPorNombre(artista || undefined)
  const consulta = artista ? porArtista : publicados

  const cambiar = (clave: string, valor: string) => {
    const nuevos = new URLSearchParams(params)
    if (valor) nuevos.set(clave, valor)
    else nuevos.delete(clave)
    setParams(nuevos, { replace: true })
  }

  const visibles = (consulta.data ?? []).filter(
      (evento) =>
          evento.name.toLowerCase().includes(texto.trim().toLowerCase()) && (!categoria || evento.category === categoria),
  )
  const hayFiltros = texto !== '' || artista !== '' || categoria !== undefined

  return (
      <div className="space-y-6">
        <h1 className={claseTitulo}>Cartelera</h1>

        <BarraFiltros
            texto={texto}
            categoria={categoria}
            artista={artista}
            artistas={artistas.data ?? []}
            onTexto={(valor) => {
              setTexto(valor)
              cambiar('q', valor)
            }}
            onCategoria={(valor) => cambiar('categoria', valor)}
            onArtista={(valor) => cambiar('artista', valor)}
        />

        {artistaElegido.data && (
            <p className={claseTextoSuave}>
              Eventos de{' '}
              <Link to={RUTAS.artista(artistaElegido.data.id)} className={claseEnlace}>
                {artistaElegido.data.stageName}
              </Link>{' '}
              · {artistaElegido.data.genre} · {artistaElegido.data.country}
            </p>
        )}
        {esEstado(artistaElegido.error, 404) && <Aviso tipo="info">No encontramos un artista con ese nombre.</Aviso>}

        {consulta.isPending && <Cargando />}
        {consulta.isError && <EstadoError mensaje={mensajeDeError(consulta.error)} onReintentar={consulta.refetch} />}

        {consulta.data && (
            <>
              <div className="flex items-center justify-between">
                <p className={claseTextoSuave} aria-live="polite">
                  {visibles.length} {visibles.length === 1 ? 'evento' : 'eventos'}
                </p>
                {hayFiltros && (
                    <Boton
                        variante="fantasma"
                        onClick={() => {
                          setTexto('')
                          setParams({}, { replace: true })
                        }}
                    >
                      Limpiar filtros
                    </Boton>
                )}
              </div>

              {visibles.length === 0 ? (
                  <EstadoVacio mensaje={hayFiltros ? 'Ningún evento coincide con los filtros' : 'Aún no hay eventos publicados'} />
              ) : (
                  <GrillaEventos eventos={visibles} />
              )}
            </>
        )}
      </div>
  )
}

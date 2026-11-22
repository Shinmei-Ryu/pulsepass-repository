import { useState } from 'react'
import { Link, useLocation, useParams } from 'react-router'
import Aviso from '@/componentes/Aviso'
import { BadgeEvento } from '@/componentes/Badge'
import Boton from '@/componentes/Boton'
import Cargando from '@/componentes/Cargando'
import { IconoFlechaIzquierda } from '@/componentes/Iconos'
import Modal from '@/componentes/Modal'
import Selector from '@/componentes/Selector'
import { claseEnlace, claseSubtitulo, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import AvatarArtista from '@/funcionalidades/artistas/componentes/AvatarArtista'
import { useArtistasActivos } from '@/funcionalidades/artistas/hooks'
import { useAsociarArtista, useEvento, usePublicarEvento } from '@/funcionalidades/eventos/hooks'
import { formatearFechaHora } from '@/lib/formato'
import { esEstado, mensajeDeError } from '@/lib/http'
import ErrorDeCarga from '@/layouts/ErrorDeCarga'
import { leerEstadoNavegacion, RUTAS } from '@/lib/rutas'
import NoEncontrada from './NoEncontrada'
import Tarjeta from '@/componentes/Tarjeta'

export default function AdminGestionEvento() {
  const { eventCode } = useParams<{ eventCode: string }>()
  const evento = useEvento(eventCode)
  const artistas = useArtistasActivos()
  const publicacion = usePublicarEvento()
  const asociacion = useAsociarArtista(eventCode ?? '')
  const [confirmando, setConfirmando] = useState(false)
  const [elegido, setElegido] = useState('')
  // AdminCrearEvento navega aquí con state.creado tras un 201
  const [recienCreado, setRecienCreado] = useState(leerEstadoNavegacion(useLocation().state).creado === true)

  if (evento.isPending) return <Cargando />
  if (evento.isError) {
    return esEstado(evento.error, 404) ? (
      <NoEncontrada titulo="Evento no encontrado" mensaje="No existe un evento con ese código." />
    ) : (
      <ErrorDeCarga error={evento.error} onReintentar={evento.refetch} />
    )
  }

  const datos = evento.data
  if (!datos) return null

  // Se ofrecen solo los artistas que aún no están en el evento
  const disponibles = (artistas.data ?? []).filter((artista) => !datos.artists.includes(artista.stageName))

  const publicar = () => {
    publicacion.mutate(datos.eventCode, { onSuccess: () => setConfirmando(false) })
  }

  const asociar = () => {
    const artista = disponibles.find((a) => String(a.id) === elegido)
    if (artista) asociacion.mutate(artista, { onSuccess: () => setElegido('') })
  }

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <Link
        to={RUTAS.adminEventos}
        className={`${claseEnlace} inline-flex min-h-11 items-center gap-1 text-sm`}
      >
        <IconoFlechaIzquierda className="size-4" />
        Eventos
      </Link>

      {recienCreado && (
        <Aviso tipo="exito" onCerrar={() => setRecienCreado(false)}>
          Evento creado. Asocia artistas y publícalo cuando esté listo.
        </Aviso>
      )}

      <header className="flex items-start justify-between gap-2">
        <div>
          <h1 className={claseTitulo}>{datos.name}</h1>
          <p className={claseTextoSuave}>
            <span className="font-mono">{datos.eventCode}</span> · {datos.category} ·{' '}
            {formatearFechaHora(datos.eventDate)} · Mín. {datos.minimumAge} · {datos.venueName}
          </p>
        </div>
        <BadgeEvento estado={datos.status} />
      </header>

      <Tarjeta como="section" className="space-y-3 p-5">
        <h2 className={claseSubtitulo}>Publicación</h2>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <p className={claseTextoSuave}>
            {datos.status === 'DRAFT'
              ? 'El evento será visible al público una vez publicado.'
              : 'Solo un evento en borrador se puede publicar.'}
          </p>
          <Boton disabled={datos.status !== 'DRAFT'} onClick={() => setConfirmando(true)}>
            Publicar evento
          </Boton>
        </div>
        {publicacion.isSuccess && <Aviso tipo="exito">Evento publicado.</Aviso>}
      </Tarjeta>

      <Tarjeta como="section" className="space-y-3 p-5">
        <h2 className={claseSubtitulo}>Artistas asociados</h2>
        {datos.artists.length === 0 ? (
          <p className={claseTextoSuave}>Este evento aún no tiene artistas.</p>
        ) : (
          <ul aria-label="Artistas asociados" className="space-y-2">
            {datos.artists.map((nombre) => (
              <li key={nombre} className="flex items-center gap-2 text-sm font-medium">
                <AvatarArtista nombre={nombre} tamano="sm" />
                {nombre}
              </li>
            ))}
          </ul>
        )}
        <div className="flex items-start gap-2">
          <Selector
            etiqueta="Agregar artista"
            className="flex-1"
            value={elegido}
            onChange={(e) => setElegido(e.target.value)}
          >
            <option value="">Selecciona un artista</option>
            {disponibles.map((artista) => (
              <option key={artista.id} value={artista.id}>
                {artista.stageName}
              </option>
            ))}
          </Selector>
          <Boton
            variante="secundario"
            className="mt-6"
            disabled={elegido === ''}
            cargando={asociacion.isPending}
            onClick={asociar}
          >
            Asociar
          </Boton>
        </div>
        <div aria-live="polite">
          {asociacion.isError && <Aviso tipo="error">{mensajeDeError(asociacion.error)}</Aviso>}
        </div>
      </Tarjeta>

      <Link to={RUTAS.ticketsPagados(datos.eventCode)} className={claseEnlace}>
        Ver tickets pagados
      </Link>

      {confirmando && (
        <Modal
          titulo="Publicar evento"
          onCerrar={() => {
            setConfirmando(false)
            publicacion.reset()
          }}
        >
          <p className="text-sm text-slate-700 dark:text-slate-300">
            ¿Publicar <strong>{datos.name}</strong>? Desde ese momento será visible y se podrán comprar
            entradas.
          </p>
          {publicacion.isError && (
            <div className="mt-3">
              <Aviso tipo="error">{mensajeDeError(publicacion.error)}</Aviso>
            </div>
          )}
          <div className="mt-4 flex justify-end gap-2">
            <Boton variante="secundario" onClick={() => setConfirmando(false)}>
              Volver
            </Boton>
            <Boton cargando={publicacion.isPending} onClick={publicar}>
              Publicar
            </Boton>
          </div>
        </Modal>
      )}
    </div>
  )
}

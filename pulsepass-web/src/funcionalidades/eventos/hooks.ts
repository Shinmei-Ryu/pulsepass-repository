import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { claves, MINUTO, SEGUNDO } from '@/lib/claves'
import type { ArtistResponse } from '@/funcionalidades/artistas/tipos'
import {
  asociarArtista,
  crearEvento,
  obtenerEvento,
  obtenerEventosPorArtista,
  obtenerEventosPublicados,
  publicarEvento,
} from './api'

export function useEventosPublicados() {
  return useQuery({
    queryKey: claves.eventosPublicados,
    queryFn: obtenerEventosPublicados,
    staleTime: MINUTO,
  })
}

export function useEvento(eventCode: string | undefined) {
  return useQuery({
    queryKey: claves.evento(eventCode ?? ''),
    queryFn: () => obtenerEvento(eventCode ?? ''),
    enabled: Boolean(eventCode),
    staleTime: 30 * SEGUNDO,
  })
}

export function useEventosPorArtista(stageName: string | undefined) {
  return useQuery({
    queryKey: claves.eventosPorArtista(stageName ?? ''),
    queryFn: () => obtenerEventosPorArtista(stageName ?? ''),
    enabled: Boolean(stageName),
    staleTime: MINUTO,
  })
}

export function useCrearEvento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: crearEvento,
    // El 201 ya trae el evento completo: se siembra la caché y la página de gestión no vuelve a pedirlo
    onSuccess: (evento) => queryClient.setQueryData(claves.evento(evento.eventCode), evento),
  })
}

export function usePublicarEvento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: publicarEvento,
    onSuccess: (evento) => {
      queryClient.invalidateQueries({ queryKey: claves.evento(evento.eventCode) })
      queryClient.invalidateQueries({ queryKey: claves.eventosPublicados })
    },
  })
}

export function useAsociarArtista(eventCode: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (artista: ArtistResponse) => asociarArtista(eventCode, artista.id),
    onSuccess: (_evento, artista) => {
      queryClient.invalidateQueries({ queryKey: claves.evento(eventCode) })
      queryClient.invalidateQueries({ queryKey: claves.eventosPorArtista(artista.stageName) })
    },
  })
}

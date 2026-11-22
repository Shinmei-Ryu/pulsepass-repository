import { useQuery } from '@tanstack/react-query'
import { claves, MINUTO } from '@/lib/claves'
import { obtenerArtista, obtenerArtistaPorNombre, obtenerArtistasActivos } from './api'

export function useArtistasActivos() {
    return useQuery({
        queryKey: claves.artistasActivos,
        queryFn: obtenerArtistasActivos,
        staleTime: 5 * MINUTO,
    })
}

export function useArtista(id: number | undefined) {
    return useQuery({
        queryKey: claves.artista(id ?? 0),
        queryFn: () => obtenerArtista(id ?? 0),
        enabled: id !== undefined,
        staleTime: 5 * MINUTO,
    })
}

export function useArtistaPorNombre(stageName: string | undefined) {
    return useQuery({
        queryKey: claves.artistaPorNombre(stageName ?? ''),
        queryFn: () => obtenerArtistaPorNombre(stageName ?? ''),
        enabled: Boolean(stageName),
        staleTime: 5 * MINUTO,
    })
}

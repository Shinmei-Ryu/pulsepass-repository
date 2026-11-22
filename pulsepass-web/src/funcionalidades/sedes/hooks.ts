import { useQuery } from '@tanstack/react-query'
import { claves, MINUTO } from '@/lib/claves'
import { obtenerSede, obtenerSedesActivas } from './api'

export function useSedesActivas() {
    return useQuery({
        queryKey: claves.sedesActivas,
        queryFn: obtenerSedesActivas,
        staleTime: 5 * MINUTO,
    })
}

// code puede faltar mientras carga el evento: enabled evita pedir /venues/undefined
export function useSede(code: string | undefined) {
    return useQuery({
        queryKey: claves.sede(code ?? ''),
        queryFn: () => obtenerSede(code ?? ''),
        enabled: Boolean(code),
        staleTime: 5 * MINUTO,
    })
}

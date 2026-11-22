import { useMutation, useQuery } from '@tanstack/react-query'
import { claves, MINUTO } from '@/lib/claves'
import { obtenerUsuarioPorEmail, obtenerUsuarioPorUsername, registrarUsuario } from './api'

export function useRegistrarUsuario() {
  return useMutation({ mutationFn: registrarUsuario })
}

export function useUsuarioPorEmail(email: string | undefined) {
  return useQuery({
    queryKey: claves.usuario(email ?? ''),
    queryFn: () => obtenerUsuarioPorEmail(email ?? ''),
    enabled: Boolean(email),
    staleTime: MINUTO,
  })
}

export function useUsuarioPorUsername(username: string | undefined) {
  return useQuery({
    queryKey: claves.usuarioPorUsername(username ?? ''),
    queryFn: () => obtenerUsuarioPorUsername(username ?? ''),
    enabled: Boolean(username),
    staleTime: MINUTO,
  })
}

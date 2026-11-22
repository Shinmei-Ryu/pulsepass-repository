import { QueryClient } from '@tanstack/react-query'
import { ApiError } from './http'

export function crearQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        // 4xx no se reintenta (no va a cambiar); red y 5xx, una sola vez
        retry: (intentos: number, error: unknown) => {
          const esperado = error instanceof ApiError && error.status >= 400 && error.status < 500
          return !esperado && intentos < 1
        },
        refetchOnWindowFocus: false,
      },
      // Una compra jamás se reintenta sola
      mutations: { retry: false },
    },
  })
}

import { setupServer } from 'msw/node'
import { handlers } from './handlers'

// Intercepta fetch dentro de Node: ninguna prueba toca la red real
export const server = setupServer(...handlers)

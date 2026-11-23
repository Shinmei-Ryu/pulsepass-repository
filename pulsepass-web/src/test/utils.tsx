import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { AuthProvider } from '@/funcionalidades/auth/AuthContext'
import type { Rol } from '@/funcionalidades/auth/tipos'
import { rutas } from '@/router'

interface Opciones {
  // Sin rol, la prueba corre como visitante anónimo
  rol?: Rol
}

// Monta la aplicación completa (caché, sesión y rutas reales) en la ruta indicada
export function renderizar(rutaInicial: string, { rol }: Opciones = {}) {
  if (rol) {
    sessionStorage.setItem(
      'pulsepass.sesion',
      // Date.now() es el reloj fijo de setup.ts, no la hora real
      JSON.stringify({ token: `token-${rol}`, expiraEn: Date.now() + 15 * 60 * 1000 }),
    )
  }

  // Un cliente nuevo por prueba: nada de caché compartida entre pruebas, y sin reintentos
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  })
  const router = createMemoryRouter(rutas, { initialEntries: [rutaInicial] })
  const usuario = userEvent.setup()

  render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </QueryClientProvider>,
  )

  return { router, queryClient, usuario }
}

// La sesión se confirma con /auth/me de forma asíncrona: antes de actuar como usuario,
// se espera a que la barra muestre el menú de usuario
export async function esperarSesion() {
  await screen.findByRole('button', { name: /Menú de usuario/ })
}

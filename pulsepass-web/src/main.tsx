import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider } from 'react-router/dom'
import '@fontsource/inter/400.css'
import '@fontsource/inter/500.css'
import '@fontsource/poppins/600.css'
import '@fontsource/poppins/700.css'
import './index.css'
import { AuthProvider } from './funcionalidades/auth/AuthContext'
import { crearQueryClient } from './lib/queryClient'
import { router } from './router'

const queryClient = crearQueryClient()
const raiz = document.getElementById('root')
if (!raiz) throw new Error('No existe el elemento #root')

// Orden de proveedores: caché → sesión → rutas. AuthProvider usa la caché; las rutas usan la sesión.
createRoot(raiz).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
)

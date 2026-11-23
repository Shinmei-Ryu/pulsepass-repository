import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// El navegador solo habla con Vite; Vite reenvía /api a Spring Boot (no hay CORS en el backend)
const api = { '/api': 'http://localhost:8080' }

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: { alias: { '@': '/src' } },
  server: { proxy: api },
  preview: { proxy: api },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
    css: false,
  },
})

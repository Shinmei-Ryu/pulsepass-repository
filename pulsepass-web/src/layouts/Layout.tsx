import { useEffect } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router'
import { useAuth } from '@/funcionalidades/auth/hooks'
import { RUTAS } from '@/lib/rutas'
import Footer from './Footer'
import Navbar from './Navbar'

export default function Layout() {
    const { motivoCierre, olvidarMotivo } = useAuth()
    const location = useLocation()
    const navigate = useNavigate()

    // PRD 12.2 y 13: cuando la sesión se cierra por vencimiento (reloj o 401), se va a /login con el motivo,
    // también desde una página pública. AuthProvider vive fuera del enrutador: por eso se navega aquí.
    useEffect(() => {
        if (motivoCierre !== 'expirada') return
        // El motivo se olvida solo cuando ya se llegó a /login. Si se olvidara antes, RutaProtegida se volvería
        // a dibujar sin él y su <Navigate> mandaría a /login otra vez, sin el aviso, ganándole a esta navegación.
        if (location.pathname === RUTAS.login) {
            olvidarMotivo()
            return
        }
        navigate(RUTAS.login, {
            replace: true,
            state: { desde: location.pathname + location.search, motivo: 'expirada' },
        })
    }, [motivoCierre, olvidarMotivo, location.pathname, location.search, navigate])

    return (
        <div className="flex min-h-screen flex-col bg-slate-50 text-slate-900 dark:bg-slate-950 dark:text-slate-100">
            {/* Primer elemento enfocable: permite saltar la navegación con el teclado */}
            <a
                href="#contenido"
                className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:rounded-lg focus:bg-white focus:px-4 focus:py-2 focus:text-marca-700"
            >
                Saltar al contenido
            </a>

            <Navbar />

            <main id="contenido" className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
                <Outlet />
            </main>

            <Footer />
        </div>
    )
}


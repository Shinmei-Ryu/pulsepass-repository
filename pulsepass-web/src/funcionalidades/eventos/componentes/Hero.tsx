import { Link } from 'react-router'
import { claseBoton } from '@/componentes/estilos'
import { RUTAS } from '@/lib/rutas'

interface HeroProps {
    // "Crear cuenta" solo tiene sentido para quien no ha iniciado sesión
    mostrarRegistro: boolean
}

// Encabezado de la portada (PRD 9.3)
export default function Hero({ mostrarRegistro }: HeroProps) {
    return (
        <section className="rounded-2xl bg-linear-to-r from-marca-600 to-acento-500 px-6 py-10 text-white sm:px-10">
            <h1 className="max-w-md font-titulo text-3xl font-bold sm:text-4xl">Descubre los mejores eventos</h1>
            <p className="mt-3 text-sm text-white/90">Música, cultura, deporte y tecnología en un solo lugar.</p>
            <div className="mt-6 flex flex-col gap-3 sm:flex-row">
                <Link to={RUTAS.eventos} className={claseBoton('secundario')}>
                    Ver cartelera
                </Link>
                {mostrarRegistro && (
                    <Link
                        to={RUTAS.registro}
                        className="inline-flex min-h-11 items-center justify-center rounded-lg border border-white px-4 text-sm font-medium text-white hover:bg-white/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
                    >
                        Crear cuenta
                    </Link>
                )}
            </div>
        </section>
    )
}

import { Link } from 'react-router'
import { claseEnlace } from '@/componentes/estilos'
import { RUTAS } from '@/lib/rutas'

// Pie de todas las páginas (PRD 9.2)
export default function Footer() {
    return (
        <footer className="border-t border-slate-200 dark:border-slate-800">
            <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-2 px-4 py-4 text-xs text-slate-500 dark:text-slate-400">
                <p>© 2026 PulsePass · Caso de estudio académico</p>
                <p className="flex gap-3">
                    <Link to={RUTAS.sedes} className={claseEnlace}>
                        Sedes
                    </Link>
                    <Link to={RUTAS.artistas} className={claseEnlace}>
                        Artistas
                    </Link>
                </p>
            </div>
        </footer>
    )
}

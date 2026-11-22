// Uno de los tres estados que toda pantalla con datos debe definir además del de éxito (FE-014)
export default function Cargando({ etiqueta = 'Cargando…' }: { etiqueta?: string }) {
    return (
        <div role="status" aria-busy="true" className="animate-pulse space-y-3 py-4">
            <span className="sr-only">{etiqueta}</span>
            <div className="h-32 rounded-xl bg-slate-200 dark:bg-slate-700" />
            <div className="h-4 w-2/3 rounded bg-slate-200 dark:bg-slate-700" />
            <div className="h-4 w-1/2 rounded bg-slate-200 dark:bg-slate-700" />
        </div>
    )
}
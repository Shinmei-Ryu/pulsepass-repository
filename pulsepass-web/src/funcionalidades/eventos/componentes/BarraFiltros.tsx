import Campo from '@/componentes/Campo'
import Selector from '@/componentes/Selector'
import type { ArtistResponse } from '@/funcionalidades/artistas/tipos'
import { CATEGORIA } from '../categorias'
import { CATEGORIAS, type EventCategory } from '../tipos'

interface BarraFiltrosProps {
    texto: string
    categoria: EventCategory | undefined
    artista: string
    artistas: ArtistResponse[]
    onTexto: (valor: string) => void
    onCategoria: (valor: string) => void
    onArtista: (valor: string) => void
}

// Solo dibuja los filtros: la página decide qué hacer con cada cambio (guardarlo en la URL)
export default function BarraFiltros({ texto, categoria, artista, artistas, onTexto, onCategoria, onArtista }: BarraFiltrosProps) {
    return (
        <form role="search" className="grid gap-3 sm:grid-cols-4" onSubmit={(evento) => evento.preventDefault()}>
            <Campo
                etiqueta="Buscar por nombre"
                type="search"
                placeholder="Buscar por nombre…"
                value={texto}
                onChange={(evento) => onTexto(evento.target.value)}
                className="sm:col-span-2"
            />
            <Selector etiqueta="Categoría" value={categoria ?? ''} onChange={(evento) => onCategoria(evento.target.value)}>
                <option value="">Todas</option>
                {CATEGORIAS.map((valor) => (
                    <option key={valor} value={valor}>
                        {CATEGORIA[valor].etiqueta}
                    </option>
                ))}
            </Selector>
            {/* La búsqueda por artista exige el nombre exacto: por eso es una lista y no texto libre */}
            <Selector etiqueta="Artista" value={artista} onChange={(evento) => onArtista(evento.target.value)}>
                <option value="">Todos</option>
                {artistas.map((a) => (
                    <option key={a.id} value={a.stageName}>
                        {a.stageName}
                    </option>
                ))}
            </Selector>
        </form>
    )
}

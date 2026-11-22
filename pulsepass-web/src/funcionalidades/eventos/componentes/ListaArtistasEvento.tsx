import { Link } from 'react-router'
import { claseEnlace, claseSubtitulo, claseTextoSuave } from '@/componentes/estilos'
import AvatarArtista from '@/funcionalidades/artistas/componentes/AvatarArtista'
import { useArtistasActivos } from '@/funcionalidades/artistas/hooks'
import { RUTAS } from '@/lib/rutas'

// La API entrega nombres de artistas; el id para enlazar sale de la lista de activos (D3)
export default function ListaArtistasEvento({ artistas }: { artistas: string[] }) {
    const activos = useArtistasActivos()

    return (
        <section aria-labelledby="artistas-evento">
            <h2 id="artistas-evento" className={claseSubtitulo}>
                Artistas
            </h2>
            {artistas.length === 0 ? (
                <p className={claseTextoSuave}>Aún no hay artistas confirmados.</p>
            ) : (
                <ul className="mt-2 flex flex-wrap gap-3">
                    {artistas.map((nombre) => {
                        const artista = activos.data?.find((a) => a.stageName === nombre)
                        return (
                            <li key={nombre} className="flex items-center gap-2 text-sm font-medium">
                                <AvatarArtista nombre={nombre} tamano="sm" />
                                {artista ? (
                                    <Link to={RUTAS.artista(artista.id)} className={claseEnlace}>
                                        {nombre}
                                    </Link>
                                ) : (
                                    nombre
                                )}
                            </li>
                        )
                    })}
                </ul>
            )}
        </section>
    )
}

import clsx from 'clsx'
import { iniciales } from '@/lib/formato'

const COLORES = [
    'bg-indigo-100 text-indigo-800',
    'bg-green-100 text-green-800',
    'bg-sky-100 text-sky-800',
    'bg-rose-100 text-rose-800',
    'bg-amber-100 text-amber-800',
] as const

const TAMANOS = { sm: 'size-8 text-xs', md: 'size-12 text-base', lg: 'size-20 text-2xl' } as const

interface AvatarArtistaProps {
    nombre: string
    tamano?: keyof typeof TAMANOS
}

export default function AvatarArtista({ nombre, tamano = 'md' }: AvatarArtistaProps) {
    // El mismo nombre siempre recibe el mismo color
    const color = COLORES[nombre.length % COLORES.length] ?? COLORES[0]
    return (
        <span
            aria-hidden="true"
            className={clsx('inline-flex shrink-0 items-center justify-center rounded-full font-semibold', color, TAMANOS[tamano])}
        >
      {iniciales(nombre)}
    </span>
    )
}

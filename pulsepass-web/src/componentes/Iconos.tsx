import type { ComponentProps, ReactNode } from 'react'

// Íconos de trazo propios (DES-01: sin emojis). Decorativos: aria-hidden.
type IconoProps = ComponentProps<'svg'>

function Base({ children, ...resto }: IconoProps & { children: ReactNode }) {
    return (
        <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth={2}
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
            {...resto}
        >
            {children}
        </svg>
    )
}

export const IconoMusica = (p: IconoProps) => (
    <Base {...p}>
        <path d="M9 18V5l12-2v13" />
        <circle cx="6" cy="18" r="3" />
        <circle cx="18" cy="16" r="3" />
    </Base>
)
export const IconoMonitor = (p: IconoProps) => (
    <Base {...p}>
        <rect x="3" y="4" width="18" height="12" rx="2" />
        <path d="M8 20h8M12 16v4" />
    </Base>
)
export const IconoBalon = (p: IconoProps) => (
    <Base {...p}>
        <circle cx="12" cy="12" r="9" />
        <path d="M12 3v18M3 12h18" />
    </Base>
)
export const IconoEdificio = (p: IconoProps) => (
    <Base {...p}>
        <path d="M3 21h18M5 21V10M9 21V10M15 21V10M19 21V10M3 10l9-6 9 6" />
    </Base>
)
export const IconoBirrete = (p: IconoProps) => (
    <Base {...p}>
        <path d="M2 9l10-5 10 5-10 5-10-5z" />
        <path d="M6 11v5c3 2 9 2 12 0v-5" />
    </Base>
)
export const IconoEstrella = (p: IconoProps) => (
    <Base {...p}>
        <path d="M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9L12 3z" />
    </Base>
)
export const IconoCalendario = (p: IconoProps) => (
    <Base {...p}>
        <rect x="3" y="5" width="18" height="16" rx="2" />
        <path d="M16 3v4M8 3v4M3 10h18" />
    </Base>
)
export const IconoUbicacion = (p: IconoProps) => (
    <Base {...p}>
        <path d="M12 21s7-6.1 7-11a7 7 0 10-14 0c0 4.9 7 11 7 11z" />
        <circle cx="12" cy="10" r="2.5" />
    </Base>
)
export const IconoUsuario = (p: IconoProps) => (
    <Base {...p}>
        <circle cx="12" cy="8" r="4" />
        <path d="M4 21c0-4 4-6 8-6s8 2 8 6" />
    </Base>
)
export const IconoMenu = (p: IconoProps) => (
    <Base {...p}>
        <path d="M4 6h16M4 12h16M4 18h16" />
    </Base>
)
export const IconoCerrar = (p: IconoProps) => (
    <Base {...p}>
        <path d="M6 6l12 12M18 6L6 18" />
    </Base>
)
export const IconoAlerta = (p: IconoProps) => (
    <Base {...p}>
        <path d="M12 3l10 18H2L12 3z" />
        <path d="M12 10v5M12 18h.01" />
    </Base>
)
export const IconoCheck = (p: IconoProps) => (
    <Base {...p}>
        <path d="M5 13l4 4L19 7" />
    </Base>
)
export const IconoBuscar = (p: IconoProps) => (
    <Base {...p}>
        <circle cx="11" cy="11" r="7" />
        <path d="M21 21l-4.3-4.3" />
    </Base>
)
export const IconoBandeja = (p: IconoProps) => (
    <Base {...p}>
        <path d="M3 13l3-8h12l3 8v6H3v-6z" />
        <path d="M3 13h5l1 3h6l1-3h5" />
    </Base>
)
export const IconoFlechaIzquierda = (p: IconoProps) => (
    <Base {...p}>
        <path d="M19 12H5M11 6l-6 6 6 6" />
    </Base>
)
export const IconoSol = (p: IconoProps) => (
    <Base {...p}>
        <circle cx="12" cy="12" r="4" />
        <path d="M12 2v2M12 20v2M2 12h2M20 12h2M5 5l1.5 1.5M17.5 17.5L19 19M19 5l-1.5 1.5M6.5 17.5L5 19" />
    </Base>
)
export const IconoLuna = (p: IconoProps) => (
    <Base {...p}>
        <path d="M20 14.5A8 8 0 019.5 4 8 8 0 1020 14.5z" />
    </Base>
)
export const IconoOjo = (p: IconoProps) => (
    <Base {...p}>
        <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z" />
        <circle cx="12" cy="12" r="3" />
    </Base>
)
export const IconoOjoTachado = (p: IconoProps) => (
    <Base {...p}>
        <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7z" />
        <circle cx="12" cy="12" r="3" />
        <path d="M3 3l18 18" />
    </Base>
)
export const IconoChevronAbajo = (p: IconoProps) => (
    <Base {...p}>
        <path d="M6 9l6 6 6-6" />
    </Base>
)

import type { ReactNode } from 'react'
import clsx from 'clsx'
import { claseTarjeta } from './estilos'

// Etiquetas HTML que puede tener una tarjeta. Se elige la que dé sentido al contenido:
// article para algo que se entiende solo (un evento, un ticket), section para una parte de la página, etc.
type EtiquetaTarjeta = 'div' | 'article' | 'section' | 'aside' | 'header' | 'li'

interface TarjetaProps {
  children: ReactNode
  className?: string
  como?: EtiquetaTarjeta
  id?: string
}

// Contenedor visual (PRD 9.1): fondo, borde y esquinas redondeadas, en claro y oscuro
export default function Tarjeta({ children, className, como: Etiqueta = 'div', id }: TarjetaProps) {
  return (
    <Etiqueta id={id} className={clsx(claseTarjeta, className)}>
      {children}
    </Etiqueta>
  )
}

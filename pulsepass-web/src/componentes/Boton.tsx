import type { ComponentProps } from 'react'
import { claseBoton, type VarianteBoton } from './estilos'

interface BotonProps extends ComponentProps<'button'> {
    variante?: VarianteBoton
    cargando?: boolean
}

export default function Boton({
                                  variante = 'primario',
                                  cargando = false,
                                  type = 'button',
                                  disabled,
                                  className,
                                  children,
                                  ...resto
                              }: BotonProps) {
    return (
        <button
            type={type}
            // Deshabilitar mientras carga impide el doble envío
            disabled={disabled || cargando}
            aria-busy={cargando}
            className={claseBoton(variante, className)}
            {...resto}
        >
            {cargando ? 'Cargando…' : children}
        </button>
    )
}

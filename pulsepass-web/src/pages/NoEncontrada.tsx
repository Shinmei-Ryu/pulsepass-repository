import { Link } from 'react-router'
import { claseBoton, claseTextoSuave } from '@/componentes/estilos'
import { RUTAS } from '@/lib/rutas'
import Tarjeta from '@/componentes/Tarjeta'

interface NoEncontradaProps {
  titulo?: string
  mensaje?: string
}

// Se usa como página de la ruta "*" y también dentro de otras páginas cuando la API responde 404
export default function NoEncontrada({
  titulo = '404',
  mensaje = 'No encontramos esta página.',
}: NoEncontradaProps) {
  return (
    <Tarjeta como="section" className="mx-auto flex max-w-md flex-col items-center gap-4 p-8 text-center">
      <h1 className="font-titulo text-marca-600 text-4xl font-bold">{titulo}</h1>
      <p className={claseTextoSuave}>{mensaje}</p>
      <Link to={RUTAS.inicio} className={claseBoton('primario')}>
        Volver al inicio
      </Link>
    </Tarjeta>
  )
}

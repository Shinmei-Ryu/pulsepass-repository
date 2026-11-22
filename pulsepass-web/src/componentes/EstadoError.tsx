import Boton from './Boton'
import { IconoAlerta } from './Iconos'
import Tarjeta from './Tarjeta'

// Uno de los tres estados que toda pantalla con datos debe definir además del de éxito (FE-014)
export default function EstadoError({
  mensaje,
  onReintentar,
}: {
  mensaje: string
  onReintentar?: () => void
}) {
  return (
    <Tarjeta className="flex flex-col items-center gap-3 p-8 text-center">
      <div role="alert" className="flex items-center gap-2 text-sm text-red-700 dark:text-red-400">
        <IconoAlerta className="size-4 shrink-0" />
        {mensaje}
      </div>
      {onReintentar && <Boton onClick={onReintentar}>Reintentar</Boton>}
    </Tarjeta>
  )
}

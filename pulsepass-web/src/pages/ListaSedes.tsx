import { Link } from 'react-router'
import { BadgeActivo } from '@/componentes/Badge'
import Cargando from '@/componentes/Cargando'
import EstadoError from '@/componentes/EstadoError'
import EstadoVacio from '@/componentes/EstadoVacio'
import { IconoUbicacion } from '@/componentes/Iconos'
import { claseTarjeta, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import { useSedesActivas } from '@/funcionalidades/sedes/hooks'
import { mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'

export default function ListaSedes() {
  const sedes = useSedesActivas()

  return (
      <div className="space-y-6">
        <h1 className={claseTitulo}>Sedes</h1>
        {sedes.isPending && <Cargando />}
        {sedes.isError && <EstadoError mensaje={mensajeDeError(sedes.error)} onReintentar={sedes.refetch} />}
        {sedes.data && sedes.data.length === 0 && <EstadoVacio mensaje="Aún no hay sedes activas" />}
        {sedes.data && sedes.data.length > 0 && (
            <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {sedes.data.map((sede) => (
                  <li key={sede.code}>
                    <Link
                        to={RUTAS.sede(sede.code)}
                        className={`${claseTarjeta} block space-y-2 p-4 hover:shadow-md focus-visible:outline-2 focus-visible:outline-marca-600`}
                    >
                <span className="flex items-start justify-between gap-2">
                  <span className="text-sm font-semibold">{sede.name}</span>
                  <BadgeActivo activo={sede.active} />
                </span>
                      <span className={`${claseTextoSuave} flex items-center gap-1.5`}>
                  <IconoUbicacion className="size-4" />
                        {sede.city} · Cap. {sede.capacity}
                </span>
                    </Link>
                  </li>
              ))}
            </ul>
        )}
      </div>
  )
}

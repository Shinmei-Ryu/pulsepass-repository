import { Link, useParams } from 'react-router'
import { BadgeActivo } from '@/componentes/Badge'
import Dato from '@/componentes/Dato'
import Cargando from '@/componentes/Cargando'
import EstadoError from '@/componentes/EstadoError'
import { IconoFlechaIzquierda } from '@/componentes/Iconos'
import { claseEnlace, claseTitulo } from '@/componentes/estilos'
import { useSede } from '@/funcionalidades/sedes/hooks'
import { esEstado, mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'
import NoEncontrada from './NoEncontrada'
import Tarjeta from '@/componentes/Tarjeta'

export default function DetalleSede() {
  const { code } = useParams<{ code: string }>()
  const sede = useSede(code)

  if (sede.isPending) return <Cargando />
  if (sede.isError) {
    return esEstado(sede.error, 404) ? (
        <NoEncontrada titulo="Sede no disponible" mensaje="No encontramos esta sede." />
    ) : (
        <EstadoError mensaje={mensajeDeError(sede.error)} onReintentar={sede.refetch} />
    )
  }

  const datos = sede.data
  if (!datos) return null

  return (
      <div className="mx-auto max-w-3xl space-y-4">
        <Link to={RUTAS.sedes} className={`${claseEnlace} inline-flex min-h-11 items-center gap-1 text-sm`}>
          <IconoFlechaIzquierda className="size-4" />
          Sedes
        </Link>
        <Tarjeta como="article" className="space-y-3 p-5">
          <div className="flex items-start justify-between gap-2">
            <h1 className={claseTitulo}>{datos.name}</h1>
            <BadgeActivo activo={datos.active} />
          </div>
          <dl>
            <Dato etiqueta="Código">
              <span className="font-mono">{datos.code}</span>
            </Dato>
            <Dato etiqueta="Ciudad">{datos.city}</Dato>
            <Dato etiqueta="Dirección">{datos.address}</Dato>
            <Dato etiqueta="Capacidad">{datos.capacity} personas</Dato>
          </dl>
        </Tarjeta>
      </div>
  )
}

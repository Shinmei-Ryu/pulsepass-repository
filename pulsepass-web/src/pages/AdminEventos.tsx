import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from 'react-router'
import { z } from 'zod'
import Aviso from '@/componentes/Aviso'
import Boton from '@/componentes/Boton'
import Campo from '@/componentes/Campo'
import { claseBoton, claseSubtitulo, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import FilaEventoCreado from '@/funcionalidades/admin/componentes/FilaEventoCreado'
import { leerEventosCreados } from '@/funcionalidades/admin/eventosCreados'
import { RUTAS } from '@/lib/rutas'

const BusquedaSchema = z.object({
  eventCode: z.string().trim().min(1, 'Escribe el código del evento').regex(/^[A-Za-z0-9._-]+$/, 'Código no válido'),
})
type Busqueda = z.infer<typeof BusquedaSchema>

export default function AdminEventos() {
  const navigate = useNavigate()
  const creados = leerEventosCreados()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Busqueda>({ resolver: zodResolver(BusquedaSchema) })

  return (
      <div className="mx-auto max-w-3xl space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h1 className={claseTitulo}>Administración de eventos</h1>
          <Link to={RUTAS.adminNuevoEvento} className={claseBoton('primario')}>
            Nuevo evento
          </Link>
        </div>

        <form
            onSubmit={handleSubmit((datos) => navigate(RUTAS.adminEvento(datos.eventCode)))}
            noValidate
            className="flex items-start gap-2"
        >
          <Campo
              etiqueta="Buscar evento por código"
              autoComplete="off"
              className="flex-1"
              error={errors.eventCode?.message}
              {...register('eventCode')}
          />
          <Boton type="submit" variante="secundario" className="mt-6">
            Abrir
          </Boton>
        </form>

        <section aria-labelledby="creados" className="space-y-3">
          <h2 id="creados" className={claseSubtitulo}>
            Creados en esta sesión
          </h2>
          {creados.length === 0 ? (
              <p className={claseTextoSuave}>Todavía no has creado eventos en esta sesión.</p>
          ) : (
              <ul className="space-y-2">
                {creados.map((codigo) => (
                    <FilaEventoCreado key={codigo} eventCode={codigo} />
                ))}
              </ul>
          )}
          <Aviso tipo="info">Nota: la API no ofrece un listado completo de eventos (GAP-01). Busca los demás por su código.</Aviso>
        </section>
      </div>
  )
}

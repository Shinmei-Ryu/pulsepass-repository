import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from 'react-router'
import AreaTexto from '@/componentes/AreaTexto'
import Aviso from '@/componentes/Aviso'
import Boton from '@/componentes/Boton'
import Campo from '@/componentes/Campo'
import Selector from '@/componentes/Selector'
import { claseBoton, claseTarjeta, claseTitulo } from '@/componentes/estilos'
import { recordarEventoCreado } from '@/funcionalidades/admin/eventosCreados'
import { CATEGORIA } from '@/funcionalidades/eventos/categorias'
import { useCrearEvento } from '@/funcionalidades/eventos/hooks'
import { CATEGORIAS, CrearEventoSchema, type CrearEventoForm } from '@/funcionalidades/eventos/tipos'
import { useSedesActivas } from '@/funcionalidades/sedes/hooks'
import { repartirErrores } from '@/lib/formularios'
import { esEstado } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'

const CAMPOS = ['eventCode', 'name', 'description', 'category', 'minimumAge', 'eventDate', 'venueCode'] as const

export default function AdminCrearEvento() {
  const navigate = useNavigate()
  const sedes = useSedesActivas()
  const creacion = useCrearEvento()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    control,
    formState: { errors },
  } = useForm<CrearEventoForm>({
    resolver: zodResolver(CrearEventoSchema),
    defaultValues: { eventCode: '', name: '', description: '', category: 'MUSIC', minimumAge: 0, eventDate: '', venueCode: '' },
  })

  // useWatch y no watch(): watch() impide que React Compiler optimice el componente (aviso de oxlint)
  const largoDescripcion = String(useWatch({ control, name: 'description' }) ?? '').length

  const enviar = (datos: CrearEventoForm) => {
    setErrorGeneral(null)
    creacion.mutate(datos, {
      onSuccess: (evento) => {
        recordarEventoCreado(evento.eventCode)
        navigate(RUTAS.adminEvento(evento.eventCode), { state: { creado: true } })
      },
      onError: (error) => {
        // 403: la guarda dejó pasar pero el servidor no (rol revocado). El servidor manda.
        if (esEstado(error, 403)) navigate(RUTAS.sinPermisos, { replace: true })
        else setErrorGeneral(repartirErrores(error, CAMPOS, (campo, mensaje) => setError(campo, { message: mensaje })))
      },
    })
  }

  return (
      <div className="mx-auto max-w-3xl space-y-4">
        <h1 className={claseTitulo}>Nuevo evento</h1>

        <form onSubmit={handleSubmit(enviar)} noValidate className={`${claseTarjeta} space-y-4 p-5`}>
          <div aria-live="polite">{errorGeneral && <Aviso tipo="error">{errorGeneral}</Aviso>}</div>

          <Campo etiqueta="Código del evento" autoComplete="off" error={errors.eventCode?.message} {...register('eventCode')} />
          <Campo etiqueta="Nombre" autoComplete="off" error={errors.name?.message} {...register('name')} />
          <AreaTexto
              etiqueta="Descripción"
              ayuda={`${largoDescripcion}/500`}
              error={errors.description?.message}
              {...register('description')}
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <Selector etiqueta="Categoría" error={errors.category?.message} {...register('category')}>
              {CATEGORIAS.map((valor) => (
                  <option key={valor} value={valor}>
                    {CATEGORIA[valor].etiqueta}
                  </option>
              ))}
            </Selector>
            {/* valueAsNumber: el input entrega texto y el esquema espera un número */}
            <Campo
                etiqueta="Edad mínima"
                type="number"
                min={0}
                error={errors.minimumAge?.message}
                {...register('minimumAge', { valueAsNumber: true })}
            />
          </div>
          <Campo etiqueta="Fecha y hora" type="datetime-local" error={errors.eventDate?.message} {...register('eventDate')} />
          <Selector etiqueta="Sede" error={errors.venueCode?.message} {...register('venueCode')}>
            <option value="">{sedes.isPending ? 'Cargando sedes…' : 'Selecciona una sede'}</option>
            {sedes.data?.map((sede) => (
                <option key={sede.code} value={sede.code}>
                  {sede.name} ({sede.code})
                </option>
            ))}
          </Selector>

          <div className="flex justify-end gap-2">
            <Link to={RUTAS.adminEventos} className={claseBoton('secundario')}>
              Cancelar
            </Link>
            <Boton type="submit" cargando={creacion.isPending}>
              Crear evento
            </Boton>
          </div>
        </form>
      </div>
  )
}

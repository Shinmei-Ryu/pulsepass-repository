import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useLocation, useNavigate } from 'react-router'
import { z } from 'zod'
import Aviso from '@/componentes/Aviso'
import { BadgeTicket } from '@/componentes/Badge'
import Boton from '@/componentes/Boton'
import Campo from '@/componentes/Campo'
import Cargando from '@/componentes/Cargando'
import FormularioCodigoEvento from '@/funcionalidades/eventos/componentes/FormularioCodigoEvento'
import { claseSubtitulo, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import { useTicket, useUsarTicket } from '@/funcionalidades/tickets/hooks'
import { CodigoTicketSchema } from '@/funcionalidades/tickets/tipos'
import { esEstado, mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'
import Tarjeta from '@/componentes/Tarjeta'

const BusquedaSchema = z.object({ codigo: CodigoTicketSchema })
type Busqueda = z.infer<typeof BusquedaSchema>

export default function ValidacionIngreso() {
  const navigate = useNavigate()
  // El enlace "Tickets pagados" de la barra llega con este ancla: el foco va al código del evento
  const enPagados = useLocation().hash === '#tickets-pagados'
  // El código buscado es estado local; el ticket en sí vive en la caché de TanStack Query
  const [buscado, setBuscado] = useState<string | undefined>(undefined)
  const ticket = useTicket(buscado)
  const uso = useUsarTicket()

  const busqueda = useForm<Busqueda>({ resolver: zodResolver(BusquedaSchema) })

  const buscar = (datos: Busqueda) => {
    uso.reset()
    setBuscado(datos.codigo)
  }

  const encontrado = ticket.data

  const marcar = (codigo: string) => {
    uso.mutate(codigo, { onSuccess: () => busqueda.reset() })
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <h1 className={claseTitulo}>Validación de ingreso</h1>

      <form onSubmit={busqueda.handleSubmit(buscar)} noValidate className="flex items-start gap-2">
        <Campo
          etiqueta="Código de ticket"
          autoFocus={!enPagados}
          autoComplete="off"
          className="flex-1"
          error={busqueda.formState.errors.codigo?.message}
          {...busqueda.register('codigo')}
        />
        <Boton type="submit" className="mt-6">
          Buscar
        </Boton>
      </form>

      <div aria-live="polite" className="space-y-3">
        {buscado && ticket.isPending && <Cargando etiqueta="Buscando ticket…" />}

        {ticket.isError && (
          <Aviso tipo="error">
            {esEstado(ticket.error, 404) ? 'Ticket no encontrado.' : mensajeDeError(ticket.error)}
          </Aviso>
        )}

        {encontrado && (
          <Tarjeta como="article" className="space-y-3 p-5">
            <div className="flex items-start justify-between gap-2">
              <div>
                <h2 className={claseSubtitulo}>{encontrado.eventName}</h2>
                <p className={claseTextoSuave}>
                  {encontrado.type} · Titular: {encontrado.userEmail}
                </p>
                <p className="font-mono text-sm break-all">{encontrado.ticketCode}</p>
              </div>
              <BadgeTicket
                estado={uso.data?.ticketCode === encontrado.ticketCode ? uso.data.status : encontrado.status}
              />
            </div>
            <Boton className="w-full" cargando={uso.isPending} onClick={() => marcar(encontrado.ticketCode)}>
              Marcar como usado
            </Boton>
          </Tarjeta>
        )}

        {uso.isSuccess && <Aviso tipo="exito">Ingreso registrado.</Aviso>}
        {/* Un 409 aquí es una situación esperada (ya usado, cancelado): tono de advertencia, no de fallo */}
        {uso.isError && (
          <Aviso tipo={esEstado(uso.error, 409) ? 'advertencia' : 'error'}>
            {esEstado(uso.error, 404) ? 'Ticket no encontrado.' : mensajeDeError(uso.error)}
          </Aviso>
        )}
      </div>

      <Tarjeta como="section" id="tickets-pagados" className="space-y-3 p-5">
        <h2 className={claseSubtitulo}>Tickets pagados por evento</h2>
        <FormularioCodigoEvento
          textoBoton="Ver tickets"
          enfocar={enPagados}
          onEnviar={(eventCode) => navigate(RUTAS.ticketsPagados(eventCode))}
        />
      </Tarjeta>
    </div>
  )
}

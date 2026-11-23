import { Link, useNavigate, useParams } from 'react-router'
import Aviso from '@/componentes/Aviso'
import { BadgeTicket } from '@/componentes/Badge'
import Boton from '@/componentes/Boton'
import Cargando from '@/componentes/Cargando'
import EstadoVacio from '@/componentes/EstadoVacio'
import { IconoFlechaIzquierda } from '@/componentes/Iconos'
import PrecioCOP from '@/componentes/PrecioCOP'
import Tabla, { type Columna } from '@/componentes/Tabla'
import { claseEnlace, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import FormularioCodigoEvento from '@/funcionalidades/eventos/componentes/FormularioCodigoEvento'
import { useEvento } from '@/funcionalidades/eventos/hooks'
import { useTicketsPagados, useUsarTicket } from '@/funcionalidades/tickets/hooks'
import type { TicketResponse } from '@/funcionalidades/tickets/tipos'
import { esEstado, mensajeDeError } from '@/lib/http'
import ErrorDeCarga from '@/layouts/ErrorDeCarga'
import { RUTAS } from '@/lib/rutas'
import NoEncontrada from './NoEncontrada'

export default function TicketsPagadosEvento() {
  const { eventCode } = useParams<{ eventCode: string }>()
  const navigate = useNavigate()
  // La lista de pagados devuelve 200 [] aunque el evento no exista: el evento se consulta aparte
  const evento = useEvento(eventCode)
  const tickets = useTicketsPagados(evento.data ? eventCode : undefined)
  const uso = useUsarTicket()

  if (evento.isPending) return <Cargando />
  if (evento.isError) {
    return esEstado(evento.error, 404) ? (
      <NoEncontrada titulo="Evento no encontrado" mensaje="No existe un evento con ese código." />
    ) : (
      <ErrorDeCarga error={evento.error} onReintentar={evento.refetch} />
    )
  }

  const columnas: Columna<TicketResponse>[] = [
    { encabezado: 'Código', celda: (t) => <span className="font-mono break-all">{t.ticketCode}</span> },
    { encabezado: 'Tipo', celda: (t) => t.type },
    { encabezado: 'Titular', celda: (t) => t.userEmail },
    { encabezado: 'Precio', celda: (t) => <PrecioCOP valor={t.price} /> },
    { encabezado: 'Estado', celda: (t) => <BadgeTicket estado={t.status} /> },
    {
      encabezado: 'Acción',
      celda: (t) => (
        <Boton
          disabled={uso.isPending}
          aria-label={`Usar ticket ${t.ticketCode}`}
          onClick={() => uso.mutate(t.ticketCode)}
        >
          Usar
        </Boton>
      ),
    },
  ]

  return (
    <div className="space-y-4">
      <Link
        to={RUTAS.staffValidacion}
        className={`${claseEnlace} inline-flex min-h-11 items-center gap-1 text-sm`}
      >
        <IconoFlechaIzquierda className="size-4" />
        Validación de ingreso
      </Link>
      <h1 className={claseTitulo}>Tickets pagados · {evento.data?.eventCode}</h1>
      <p className={claseTextoSuave}>{evento.data?.name}</p>

      {/* key: al cambiar de evento el formulario vuelve a quedar vacío */}
      <FormularioCodigoEvento
        key={eventCode}
        textoBoton="Ir"
        onEnviar={(codigo) => navigate(RUTAS.ticketsPagados(codigo))}
      />

      <div aria-live="polite">
        {uso.isSuccess && <Aviso tipo="exito">Ingreso registrado.</Aviso>}
        {uso.isError && (
          <Aviso tipo={esEstado(uso.error, 409) ? 'advertencia' : 'error'}>{mensajeDeError(uso.error)}</Aviso>
        )}
      </div>

      {tickets.isPending && <Cargando />}
      {tickets.isError && <ErrorDeCarga error={tickets.error} onReintentar={tickets.refetch} />}
      {tickets.data && tickets.data.length === 0 && <EstadoVacio mensaje="No hay tickets pagados" />}
      {tickets.data && tickets.data.length > 0 && (
        <>
          <p className={claseTextoSuave}>Total: {tickets.data.length}</p>
          <Tabla
            titulo={`Tickets pagados de ${evento.data?.eventCode ?? ''}`}
            columnas={columnas}
            filas={tickets.data}
            claveDe={(t) => t.ticketCode}
          />
        </>
      )}
    </div>
  )
}

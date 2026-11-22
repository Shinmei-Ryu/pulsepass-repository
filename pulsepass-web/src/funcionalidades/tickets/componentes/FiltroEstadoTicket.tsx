import FiltroChips from '@/componentes/FiltroChips'

const FILTROS = [
    { valor: 'TODOS', texto: 'Todos' },
    { valor: 'PAID', texto: 'Pagados' },
    { valor: 'USED', texto: 'Usados' },
    { valor: 'CANCELLED', texto: 'Cancelados' },
] as const

export type FiltroTicket = (typeof FILTROS)[number]['valor']

interface FiltroEstadoTicketProps {
    valor: FiltroTicket
    onCambiar: (valor: FiltroTicket) => void
}

export default function FiltroEstadoTicket({ valor, onCambiar }: FiltroEstadoTicketProps) {
    return <FiltroChips etiqueta="Filtrar por estado" opciones={FILTROS} valor={valor} onCambiar={onCambiar} />
}

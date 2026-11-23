import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { claves, SEGUNDO } from '@/lib/claves'
import { useAuth } from '@/funcionalidades/auth/hooks'
import {
  cancelarTicket,
  comprarTicket,
  obtenerTicket,
  obtenerTicketsDeUsuario,
  obtenerTicketsPagados,
  usarTicket,
} from './api'
import type { TicketType } from './tipos'

function useEmailDeSesion(): string | undefined {
  const { sesion } = useAuth()
  return sesion.estado === 'autenticado' ? sesion.usuario.email : undefined
}

export function useMisTickets() {
  const email = useEmailDeSesion()
  return useQuery({
    queryKey: claves.ticketsDeUsuario(email ?? ''),
    queryFn: () => obtenerTicketsDeUsuario(email ?? ''),
    enabled: Boolean(email),
    staleTime: 15 * SEGUNDO,
  })
}

export function useTicket(ticketCode: string | undefined) {
  return useQuery({
    queryKey: claves.ticket(ticketCode ?? ''),
    queryFn: () => obtenerTicket(ticketCode ?? ''),
    enabled: Boolean(ticketCode),
    staleTime: 15 * SEGUNDO,
  })
}

export function useTicketsPagados(eventCode: string | undefined) {
  return useQuery({
    queryKey: claves.ticketsPagados(eventCode ?? ''),
    queryFn: () => obtenerTicketsPagados(eventCode ?? ''),
    enabled: Boolean(eventCode),
    staleTime: 15 * SEGUNDO,
  })
}

export function useComprarTicket() {
  const queryClient = useQueryClient()
  const email = useEmailDeSesion()

  return useMutation({
    // El correo sale siempre de la sesión: quien llama solo elige evento y tipo (§24.3)
    mutationFn: (datos: { eventCode: string; type: TicketType }) => {
      if (!email) return Promise.reject(new Error('No hay sesión activa'))
      return comprarTicket({ userEmail: email, ...datos })
    },
    onSuccess: (ticket) => {
      queryClient.invalidateQueries({ queryKey: claves.ticketsDeUsuario(ticket.userEmail) })
      queryClient.invalidateQueries({ queryKey: claves.evento(ticket.eventCode) })
      // Si se llenó el aforo, el evento sale de la cartelera
      queryClient.invalidateQueries({ queryKey: claves.eventosPublicados })
    },
  })
}

export function useCancelarTicket() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: cancelarTicket,
    onSuccess: (ticket) => {
      queryClient.invalidateQueries({ queryKey: claves.ticketsDeUsuario(ticket.userEmail) })
      queryClient.invalidateQueries({ queryKey: claves.ticket(ticket.ticketCode) })
      queryClient.invalidateQueries({ queryKey: claves.ticketsPagados(ticket.eventCode) })
    },
  })
}

export function useUsarTicket() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: usarTicket,
    onSuccess: (ticket) => {
      queryClient.invalidateQueries({ queryKey: claves.ticket(ticket.ticketCode) })
      queryClient.invalidateQueries({ queryKey: claves.ticketsPagados(ticket.eventCode) })
    },
  })
}

// Datos de prueba con la misma forma que devuelve la API real
export const artistas = [
  { id: 1, stageName: 'Solar Beat', country: 'Colombia', genre: 'Electrónica', active: true },
  { id: 2, stageName: 'Neon Waves', country: 'Colombia', genre: 'Synthwave', active: true },
]

export const sede = {
  id: 1,
  code: 'VEN-SMR-01',
  name: 'Marina Convention Center',
  city: 'Santa Marta',
  address: 'Carrera 1 # 22-58',
  capacity: 3,
  active: true,
}

export const eventoResumen = {
  id: 1,
  eventCode: 'CMF-2026',
  name: 'Caribbean Music Fest 2026',
  category: 'MUSIC',
  status: 'PUBLISHED',
  eventDate: '2026-12-12T20:00:00',
  venueName: 'Marina Convention Center',
}

export const eventoDetalle = {
  ...eventoResumen,
  description: 'Tres noches de música caribeña frente a la bahía.',
  minimumAge: 18,
  venueCode: 'VEN-SMR-01',
  artists: ['Solar Beat'],
}

export const usuarios = {
  USER: { id: 10, username: 'andrea', email: 'andrea@pulsepass.test', roles: ['USER'] },
  STAFF: { id: 11, username: 'laura', email: 'laura@pulsepass.test', roles: ['STAFF'] },
  ADMIN: { id: 12, username: 'admin', email: 'admin@pulsepass.test', roles: ['ADMIN'] },
}

export const perfil = {
  id: 10,
  username: 'andrea',
  email: 'andrea@pulsepass.test',
  active: true,
  firstName: 'Andrea',
  lastName: 'Ruiz',
  city: 'Santa Marta',
  birthDate: '1998-04-12',
}

const baseTicket = {
  type: 'VIP',
  price: 240000,
  purchaseDate: '2026-11-05T14:32:10.123456',
  userEmail: 'andrea@pulsepass.test',
  eventCode: 'CMF-2026',
  eventName: 'Caribbean Music Fest 2026',
}

export const ticketPagado = { ...baseTicket, id: 1, ticketCode: 'TCK-CMF-2026-AAA111', status: 'PAID' }
export const ticketUsado = { ...baseTicket, id: 2, ticketCode: 'TCK-CMF-2026-BBB222', status: 'USED' }
export const ticketCancelado = {
  ...baseTicket,
  id: 3,
  ticketCode: 'TCK-CMF-2026-CCC333',
  status: 'CANCELLED',
}
export const tickets = [ticketPagado, ticketUsado, ticketCancelado]

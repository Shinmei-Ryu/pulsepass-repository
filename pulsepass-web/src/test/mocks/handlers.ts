import { http as red, HttpResponse } from 'msw'
import { artistas, eventoDetalle, eventoResumen, perfil, sede, ticketPagado, tickets, usuarios } from './datos'

// Mismo cuerpo de error que el backend
export function errorApi(status: number, message: string, details: Record<string, string> = {}) {
    return HttpResponse.json(
        { timestamp: '2026-11-16T10:00:00', status, error: 'Error', message, details },
        { status },
    )
}

// El asterisco inicial acepta cualquier origen. El orden importa: las rutas fijas van antes que las de parámetro.
export const handlers = [
    // --- Autenticación ---
    red.post('*/api/auth/login', () =>
        HttpResponse.json({ accessToken: 'token-USER', tokenType: 'Bearer', expiresIn: 900 }),
    ),
    red.get('*/api/auth/me', ({ request }) => {
        // utils.tsx guarda "token-<ROL>": así cada prueba elige con qué rol entra
        const rol = request.headers.get('Authorization')?.replace('Bearer token-', '')
        if (rol === 'USER' || rol === 'STAFF' || rol === 'ADMIN') return HttpResponse.json(usuarios[rol])
        return errorApi(401, 'Authentication is required')
    }),

    // --- Eventos ---
    red.get('*/api/events/published', () => HttpResponse.json([eventoResumen])),
    red.get('*/api/events/by-artist', () => HttpResponse.json([eventoResumen])),
    red.get('*/api/events/:eventCode/tickets/paid', () => HttpResponse.json([ticketPagado])),
    red.get('*/api/events/:eventCode', ({ params }) =>
        params.eventCode === 'CMF-2026' ? HttpResponse.json(eventoDetalle) : errorApi(404, `Event not found: ${params.eventCode}`),
    ),
    red.post('*/api/events', async ({ request }) => {
        const cuerpo = (await request.json()) as { eventCode: string; name: string }
        return HttpResponse.json(
            { ...eventoDetalle, eventCode: cuerpo.eventCode, name: cuerpo.name, status: 'DRAFT', artists: [] },
            { status: 201 },
        )
    }),
    red.patch('*/api/events/:eventCode/publish', () => HttpResponse.json({ ...eventoDetalle, status: 'PUBLISHED' })),
    red.post('*/api/events/:eventCode/artists/:artistId', () =>
        HttpResponse.json({ ...eventoDetalle, artists: ['Solar Beat', 'Neon Waves'] }),
    ),

    // --- Artistas ---
    red.get('*/api/artists/active', () => HttpResponse.json(artistas)),
    red.get('*/api/artists/by-stage-name', () => HttpResponse.json(artistas[0])),
    red.get('*/api/artists/:id', ({ params }) =>
        params.id === '1' ? HttpResponse.json(artistas[0]) : errorApi(404, `Artist not found: ${params.id}`),
    ),

    // --- Sedes ---
    red.get('*/api/venues/active', () => HttpResponse.json([sede])),
    red.get('*/api/venues/:code', ({ params }) =>
        params.code === sede.code ? HttpResponse.json(sede) : errorApi(404, `Venue not found: ${params.code}`),
    ),

    // --- Usuarios ---
    red.post('*/api/users', () => HttpResponse.json(perfil, { status: 201 })),
    red.get('*/api/users/by-email', () => HttpResponse.json(perfil)),
    red.get('*/api/users/by-username', () => HttpResponse.json(perfil)),

    // --- Tickets ---
    red.get('*/api/tickets/by-user', () => HttpResponse.json(tickets)),
    red.post('*/api/tickets', () => HttpResponse.json(ticketPagado, { status: 201 })),
    red.patch('*/api/tickets/:ticketCode/cancel', () => HttpResponse.json({ ...ticketPagado, status: 'CANCELLED' })),
    red.patch('*/api/tickets/:ticketCode/use', () => HttpResponse.json({ ...ticketPagado, status: 'USED' })),
    red.get('*/api/tickets/:ticketCode', ({ params }) => {
        const ticket = tickets.find((t) => t.ticketCode === params.ticketCode)
        return ticket ? HttpResponse.json(ticket) : errorApi(404, `Ticket not found: ${params.ticketCode}`)
    }),
]

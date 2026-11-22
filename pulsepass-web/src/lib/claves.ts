// Claves de caché de TanStack Query (§14.1 del PRD). Un solo lugar para consultas e invalidaciones.
export const claves = {
    eventosPublicados: ['eventos', 'publicados'] as const,
    evento: (eventCode: string) => ['evento', eventCode] as const,
    eventosPorArtista: (stageName: string) => ['eventos', 'artista', stageName] as const,
    artistasActivos: ['artistas', 'activos'] as const,
    artista: (id: number) => ['artista', id] as const,
    artistaPorNombre: (stageName: string) => ['artista', 'nombre', stageName] as const,
    sedesActivas: ['sedes', 'activas'] as const,
    sede: (code: string) => ['sede', code] as const,
    usuario: (email: string) => ['usuario', email] as const,
    usuarioPorUsername: (username: string) => ['usuario', 'username', username] as const,
    ticketsDeUsuario: (email: string) => ['tickets', 'usuario', email] as const,
    ticket: (ticketCode: string) => ['ticket', ticketCode] as const,
    ticketsPagados: (eventCode: string) => ['tickets', 'pagados', eventCode] as const,
}

export const SEGUNDO = 1000
export const MINUTO = 60 * SEGUNDO

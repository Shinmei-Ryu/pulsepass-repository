const fechaHora = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' })
const soloFecha = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium' })
const moneda = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 })

// El backend envía LocalDateTime sin zona ("2026-12-12T20:00:00"): new Date lo interpreta en hora local
export const formatearFechaHora = (iso: string) => fechaHora.format(new Date(iso))
export const formatearFecha = (iso: string) => soloFecha.format(new Date(iso))
// Para fechas sin hora (YYYY-MM-DD), como la de nacimiento: new Date('1998-04-12') se leería en UTC
// y en Colombia mostraría el día anterior. Se construye la fecha local a partir de sus partes.
export function formatearFechaSimple(iso: string): string {
    const [anio = 0, mes = 1, dia = 1] = iso.split('-').map(Number)
    return soloFecha.format(new Date(anio, mes - 1, dia))
}
export const formatearCOP = (valor: number) => moneda.format(valor)

const dos = (n: number) => String(n).padStart(2, '0')

// Fecha local de hoy como YYYY-MM-DD (toISOString daría la fecha en UTC)
export function hoyISO(): string {
    const d = new Date()
    return `${d.getFullYear()}-${dos(d.getMonth() + 1)}-${dos(d.getDate())}`
}

export function iniciales(nombre: string): string {
    const partes = nombre.trim().split(/\s+/)
    const primera = partes[0]?.[0] ?? ''
    const segunda = partes.length > 1 ? (partes[partes.length - 1]?.[0] ?? '') : (partes[0]?.[1] ?? '')
    return (primera + segunda).toUpperCase()
}

import { Navigate } from 'react-router'
import EstadoError from '@/componentes/EstadoError'
import { esEstado, mensajeDeError } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'

interface ErrorDeCargaProps {
    error: unknown
    onReintentar: () => void
}

// Error al cargar una página completa (PRD 9.19 y 13): un 403 de la API lleva a la página 403,
// aunque la guarda de ruta haya dejado pasar. El servidor manda.
export default function ErrorDeCarga({ error, onReintentar }: ErrorDeCargaProps) {
    if (esEstado(error, 403)) return <Navigate to={RUTAS.sinPermisos} replace />
    return <EstadoError mensaje={mensajeDeError(error)} onReintentar={onReintentar} />
}

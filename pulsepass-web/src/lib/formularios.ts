import { ApiError, mensajeDeError } from './http'

// Reparte un 400 del backend: cada clave de details va a su campo; lo demás, al aviso general.
// Devuelve el mensaje general, o null si todo quedó asignado a campos.
export function repartirErrores<C extends string>(
  error: unknown,
  campos: readonly C[],
  ponerError: (campo: C, mensaje: string) => void,
): string | null {
  if (!(error instanceof ApiError) || error.status !== 400) return mensajeDeError(error)

  let sinCampo = 0
  for (const [clave, mensaje] of Object.entries(error.details)) {
    const campo = campos.find((c) => c === clave)
    if (campo) ponerError(campo, mensaje)
    else sinCampo++
  }
  const total = Object.keys(error.details).length
  return total === 0 || sinCampo > 0 ? 'Revisa los datos del formulario.' : null
}

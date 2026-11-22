import { useEffect, useState } from 'react'
import { z } from 'zod'

const TemaSchema = z.enum(['claro', 'oscuro'])
type Tema = z.infer<typeof TemaSchema>

const CLAVE = 'pulsepass.tema'

function temaInicial(): Tema {
    try {
        // La preferencia de tema sí puede vivir en localStorage; el token, nunca
        const guardado = TemaSchema.safeParse(localStorage.getItem(CLAVE))
        if (guardado.success) return guardado.data
    } catch {
        // almacenamiento no disponible: se sigue con la preferencia del sistema
    }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'oscuro' : 'claro'
}

export function useTema() {
    const [tema, setTema] = useState<Tema>(temaInicial)

    useEffect(() => {
        document.documentElement.classList.toggle('dark', tema === 'oscuro')
        try {
            localStorage.setItem(CLAVE, tema)
        } catch {
            // sin almacenamiento, el tema dura lo que dure la pestaña
        }
    }, [tema])

    const alternar = () => setTema((actual) => (actual === 'oscuro' ? 'claro' : 'oscuro'))
    return { tema, alternar }
}

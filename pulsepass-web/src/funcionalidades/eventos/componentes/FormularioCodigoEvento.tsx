import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import Boton from '@/componentes/Boton'
import Campo from '@/componentes/Campo'

const EventoSchema = z.object({
    eventCode: z.string().trim().min(1, 'Escribe el código del evento').regex(/^[A-Za-z0-9._-]+$/, 'Código no válido'),
})
type EventoForm = z.infer<typeof EventoSchema>

interface FormularioCodigoEventoProps {
    textoBoton: string
    onEnviar: (eventCode: string) => void
    enfocar?: boolean
}

// Pide el código de un evento: se usa para llegar a sus tickets pagados (PRD 9.15: "Evento [ ] [Ir]")
export default function FormularioCodigoEvento({ textoBoton, onEnviar, enfocar = false }: FormularioCodigoEventoProps) {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<EventoForm>({ resolver: zodResolver(EventoSchema) })

    return (
        <form onSubmit={handleSubmit((datos) => onEnviar(datos.eventCode))} noValidate className="flex items-start gap-2">
            <Campo
                etiqueta="Código del evento"
                autoComplete="off"
                autoFocus={enfocar}
                className="flex-1"
                error={errors.eventCode?.message}
                {...register('eventCode')}
            />
            <Boton type="submit" variante="secundario" className="mt-6">
                {textoBoton}
            </Boton>
        </form>
    )
}

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import Aviso from '@/componentes/Aviso'
import Boton from '@/componentes/Boton'
import Campo from '@/componentes/Campo'
import CampoContrasena from '@/componentes/CampoContrasena'
import { repartirErrores } from '@/lib/formularios'
import { esEstado } from '@/lib/http'
import { useAuth } from '../hooks'
import { LoginSchema, type LoginForm } from '../tipos'

// El formulario solo inicia sesión; a dónde ir después lo decide la página (onIngresado)
export default function FormularioLogin({ onIngresado }: { onIngresado: () => void }) {
    const { iniciarSesion } = useAuth()
    const [errorGeneral, setErrorGeneral] = useState<string | null>(null)

    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<LoginForm>({ resolver: zodResolver(LoginSchema) })

    const enviar = async (datos: LoginForm) => {
        setErrorGeneral(null)
        try {
            await iniciarSesion(datos.identifier, datos.password)
            onIngresado()
        } catch (error) {
            // El backend no dice si falló el usuario o la contraseña, y la interfaz tampoco
            if (esEstado(error, 401)) setErrorGeneral('Credenciales inválidas.')
            else setErrorGeneral(repartirErrores(error, ['identifier', 'password'] as const, (campo, mensaje) => setError(campo, { message: mensaje })))
        }
    }

    return (
        // noValidate: los mensajes los pone zod, no el navegador
        <form onSubmit={handleSubmit(enviar)} noValidate className="space-y-4">
            <Campo etiqueta="Usuario o correo" autoComplete="username" error={errors.identifier?.message} {...register('identifier')} />
            <CampoContrasena
                etiqueta="Contraseña"
                autoComplete="current-password"
                error={errors.password?.message}
                {...register('password')}
            />
            <div aria-live="polite">{errorGeneral && <Aviso tipo="error">{errorGeneral}</Aviso>}</div>
            <Boton type="submit" cargando={isSubmitting} className="w-full">
                Ingresar
            </Boton>
        </form>
    )
}

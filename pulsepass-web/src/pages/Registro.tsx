import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from 'react-router'
import Aviso from '@/componentes/Aviso'
import Boton from '@/componentes/Boton'
import Campo from '@/componentes/Campo'
import CampoContrasena from '@/componentes/CampoContrasena'
import { claseEnlace, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import { useRegistrarUsuario } from '@/funcionalidades/usuarios/hooks'
import { RegistroSchema, type RegistroForm } from '@/funcionalidades/usuarios/tipos'
import { hoyISO } from '@/lib/formato'
import { repartirErrores } from '@/lib/formularios'
import { esEstado } from '@/lib/http'
import { RUTAS } from '@/lib/rutas'
import Tarjeta from '@/componentes/Tarjeta'

const CAMPOS = ['username', 'email', 'firstName', 'lastName', 'birthDate', 'password'] as const

export default function Registro() {
  const navigate = useNavigate()
  const registro = useRegistrarUsuario()
  const [errorGeneral, setErrorGeneral] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    setError,
    control,
    formState: { errors },
  } = useForm<RegistroForm>({ resolver: zodResolver(RegistroSchema) })

  // Indicador visual, no una regla: la única regla es la del esquema (12 caracteres, 72 bytes).
  // useWatch y no watch(): watch() impide que React Compiler optimice el componente (aviso de oxlint)
  const largo = String(useWatch({ control, name: 'password' }) ?? '').length
  const fortaleza = largo >= 20 ? 'alta' : largo >= 12 ? 'media' : 'baja'
  const BARRA = {
    baja: 'w-1/3 bg-red-500',
    media: 'w-2/3 bg-amber-500',
    alta: 'w-full bg-emerald-500',
  } as const

  const enviar = (datos: RegistroForm) => {
    setErrorGeneral(null)
    registro.mutate(datos, {
      onSuccess: () => navigate(RUTAS.login, { state: { registrado: true } }),
      onError: (error) => {
        // El 409 del backend incluye el correo o el usuario: aquí siempre se muestra un texto genérico
        if (esEstado(error, 409)) setErrorGeneral('El usuario o correo ya está registrado.')
        else setErrorGeneral(repartirErrores(error, CAMPOS, (campo, mensaje) => setError(campo, { message: mensaje })))
      },
    })
  }

  return (
      <Tarjeta como="section" className="mx-auto max-w-md space-y-4 p-6">
        <h1 className={`${claseTitulo} text-center`}>Crea tu cuenta</h1>

        <form onSubmit={handleSubmit(enviar)} noValidate className="space-y-4">
          <Campo etiqueta="Nombre de usuario" autoComplete="username" error={errors.username?.message} {...register('username')} />
          <Campo etiqueta="Correo electrónico" type="email" autoComplete="email" error={errors.email?.message} {...register('email')} />
          <div className="grid gap-4 sm:grid-cols-2">
            <Campo etiqueta="Nombres" autoComplete="given-name" error={errors.firstName?.message} {...register('firstName')} />
            <Campo etiqueta="Apellidos" autoComplete="family-name" error={errors.lastName?.message} {...register('lastName')} />
          </div>
          <Campo
              etiqueta="Fecha de nacimiento"
              type="date"
              max={hoyISO()}
              autoComplete="bday"
              error={errors.birthDate?.message}
              {...register('birthDate')}
          />
          <div className="space-y-1">
            <CampoContrasena
                etiqueta="Contraseña"
                autoComplete="new-password"
                ayuda={`Entre 12 caracteres y 72 bytes. Fortaleza: ${fortaleza}`}
                error={errors.password?.message}
                {...register('password')}
            />
            {/* Barra decorativa: el texto "Fortaleza" de la ayuda ya lo dice para lectores de pantalla */}
            {largo > 0 && (
                <div aria-hidden="true" className="h-1.5 rounded-full bg-slate-200 dark:bg-slate-700">
                  <div className={`h-full rounded-full ${BARRA[fortaleza]}`} />
                </div>
            )}
          </div>
          <div aria-live="polite">{errorGeneral && <Aviso tipo="error">{errorGeneral}</Aviso>}</div>
          <Boton type="submit" cargando={registro.isPending} className="w-full">
            Crear cuenta
          </Boton>
        </form>

        <p className={`${claseTextoSuave} text-center`}>
          ¿Ya tienes cuenta?{' '}
          <Link to={RUTAS.login} className={claseEnlace}>
            Inicia sesión
          </Link>
        </p>
      </Tarjeta>
  )
}


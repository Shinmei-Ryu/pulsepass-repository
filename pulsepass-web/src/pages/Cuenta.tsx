import { Link, useNavigate } from 'react-router'
import { BadgeActivo } from '@/componentes/Badge'
import Boton from '@/componentes/Boton'
import Dato from '@/componentes/Dato'
import Cargando from '@/componentes/Cargando'
import { claseBoton, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import AvatarArtista from '@/funcionalidades/artistas/componentes/AvatarArtista'
import { tieneRol, useAuth } from '@/funcionalidades/auth/hooks'
import { useUsuarioPorEmail, useUsuarioPorUsername } from '@/funcionalidades/usuarios/hooks'
import ErrorDeCarga from '@/layouts/ErrorDeCarga'
import { formatearFechaSimple } from '@/lib/formato'
import { RUTAS } from '@/lib/rutas'
import Tarjeta from '@/componentes/Tarjeta'

export default function Cuenta() {
  const { sesion, cerrarSesion } = useAuth()
  const navigate = useNavigate()
  const usuarioSesion = sesion.estado === 'autenticado' ? sesion.usuario : undefined

  // Dos consultas al mismo perfil: cubren las operaciones 13 (por correo) y 14 (por username)
  const porEmail = useUsuarioPorEmail(usuarioSesion?.email)
  const porUsername = useUsuarioPorUsername(usuarioSesion?.username)

  if (!usuarioSesion) return null
  const perfil = porEmail.data ?? porUsername.data
  const nombre = [perfil?.firstName, perfil?.lastName].filter(Boolean).join(' ') || usuarioSesion.username

  const salir = () => {
    cerrarSesion('manual')
    navigate(RUTAS.inicio)
  }

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      <h1 className={claseTitulo}>Mi cuenta</h1>

      <Tarjeta como="section" className="space-y-4 p-5">
        <div className="flex items-center gap-4">
          <AvatarArtista nombre={nombre} />
          <div>
            <p className="font-titulo text-lg font-semibold">{nombre}</p>
            <p className={claseTextoSuave}>
              @{usuarioSesion.username} · {usuarioSesion.email}
            </p>
          </div>
        </div>

        {porEmail.isPending && <Cargando />}
        {porEmail.isError && <ErrorDeCarga error={porEmail.error} onReintentar={porEmail.refetch} />}

        <dl>
          <Dato etiqueta="Rol">{usuarioSesion.roles.join(', ')}</Dato>
          {perfil?.birthDate && <Dato etiqueta="Nacimiento">{formatearFechaSimple(perfil.birthDate)}</Dato>}
          {perfil?.city && <Dato etiqueta="Ciudad">{perfil.city}</Dato>}
          {perfil && (
            <Dato etiqueta="Estado">
              <BadgeActivo activo={perfil.active} />
            </Dato>
          )}
        </dl>
      </Tarjeta>

      <div className="flex flex-wrap justify-between gap-2">
        {tieneRol(sesion, 'USER') && (
          <Link to={RUTAS.misTickets} className={claseBoton('primario')}>
            Ver mis tickets
          </Link>
        )}
        <Boton variante="secundario" onClick={salir}>
          Cerrar sesión
        </Boton>
      </div>
    </div>
  )
}

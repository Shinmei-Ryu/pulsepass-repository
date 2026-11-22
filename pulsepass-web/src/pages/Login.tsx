import { Link, useLocation, useNavigate } from 'react-router'
import Aviso from '@/componentes/Aviso'
import Tarjeta from '@/componentes/Tarjeta'
import { claseEnlace, claseTextoSuave, claseTitulo } from '@/componentes/estilos'
import FormularioLogin from '@/funcionalidades/auth/componentes/FormularioLogin'
import { destinoSeguro, leerEstadoNavegacion, RUTAS } from '@/lib/rutas'

export default function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const { registrado, motivo } = leerEstadoNavegacion(location.state)

  return (
    <Tarjeta como="section" className="mx-auto max-w-sm space-y-4 p-6">
      <h1 className={`${claseTitulo} text-center`}>Inicia sesión</h1>

      {registrado && <Aviso tipo="exito">Cuenta creada, inicia sesión.</Aviso>}
      {motivo === 'expirada' && <Aviso tipo="info">Tu sesión expiró. Inicia sesión de nuevo.</Aviso>}

      {/* replace: "atrás" no debe devolver al formulario de login */}
      <FormularioLogin onIngresado={() => navigate(destinoSeguro(location.state), { replace: true })} />

      <p className={`${claseTextoSuave} text-center`}>
        ¿No tienes cuenta?{' '}
        <Link to={RUTAS.registro} className={claseEnlace}>
          Regístrate
        </Link>
      </p>
    </Tarjeta>
  )
}

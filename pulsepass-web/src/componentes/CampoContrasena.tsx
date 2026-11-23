import { useState } from 'react'
import Campo, { type CampoProps } from './Campo'
import { IconoOjo, IconoOjoTachado } from './Iconos'

// Campo de contraseña con botón para verla (diseños de Login y Registro)
// Recibe las mismas props que Campo; el "type" lo decide este componente
export default function CampoContrasena(props: CampoProps) {
  const [visible, setVisible] = useState(false)

  return (
    <div className="relative">
      {/* [&_input]:pr-12 deja espacio para el botón dentro del campo */}
      <Campo {...props} type={visible ? 'text' : 'password'} className="[&_input]:pr-12" />
      <button
        type="button"
        aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
        aria-pressed={visible}
        onClick={() => setVisible((actual) => !actual)}
        className="focus-visible:outline-marca-600 absolute top-6 right-0 flex size-11 items-center justify-center rounded-lg text-slate-500 hover:text-slate-900 focus-visible:outline-2 dark:text-slate-400 dark:hover:text-white"
      >
        {visible ? <IconoOjoTachado className="size-5" /> : <IconoOjo className="size-5" />}
      </button>
    </div>
  )
}

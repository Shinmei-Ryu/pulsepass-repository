import type { ComponentType } from 'react'
import {
  IconoBalon,
  IconoBirrete,
  IconoEdificio,
  IconoEstrella,
  IconoMonitor,
  IconoMusica,
} from '@/componentes/Iconos'
import type { EventCategory } from './tipos'

interface AparienciaCategoria {
  etiqueta: string
  fondo: string
  texto: string
  Icono: ComponentType<{ className?: string }>
}

// La API no entrega imágenes: cada categoría tiene un panel de color con su ícono (DES-02)
export const CATEGORIA: Record<EventCategory, AparienciaCategoria> = {
  MUSIC: {
    etiqueta: 'Música',
    fondo: 'bg-indigo-100 dark:bg-indigo-950',
    texto: 'text-indigo-700 dark:text-indigo-300',
    Icono: IconoMusica,
  },
  TECHNOLOGY: {
    etiqueta: 'Tecnología',
    fondo: 'bg-sky-100 dark:bg-sky-950',
    texto: 'text-sky-700 dark:text-sky-300',
    Icono: IconoMonitor,
  },
  SPORTS: {
    etiqueta: 'Deportes',
    fondo: 'bg-green-100 dark:bg-green-950',
    texto: 'text-green-700 dark:text-green-300',
    Icono: IconoBalon,
  },
  CULTURE: {
    etiqueta: 'Cultura',
    fondo: 'bg-rose-100 dark:bg-rose-950',
    texto: 'text-rose-700 dark:text-rose-300',
    Icono: IconoEdificio,
  },
  EDUCATION: {
    etiqueta: 'Educación',
    fondo: 'bg-amber-100 dark:bg-amber-950',
    texto: 'text-amber-700 dark:text-amber-300',
    Icono: IconoBirrete,
  },
  ENTERTAINMENT: {
    etiqueta: 'Entretenimiento',
    fondo: 'bg-violet-100 dark:bg-violet-950',
    texto: 'text-violet-700 dark:text-violet-300',
    Icono: IconoEstrella,
  },
}

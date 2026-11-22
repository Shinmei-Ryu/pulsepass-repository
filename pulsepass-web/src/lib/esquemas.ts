import { z } from 'zod'

// LocalDateTime de Java: sin zona; segundos y fracción opcionales
export const FechaHoraLocalSchema = z.string().regex(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d{1,9})?)?$/)

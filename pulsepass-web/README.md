# PulsePass Web

Cliente web de PulsePass: cartelera de eventos, compra de entradas, validación de ingreso y administración.

## Requisitos

- Node.js 22.22 o superior
- La API de PulsePass corriendo en `http://localhost:8080` (ver el README de la raíz)

## Puesta en marcha

```powershell
npm install
Copy-Item .env.example .env
npm run dev
```

La aplicación queda en `http://localhost:5173`. Vite reenvía `/api` a la API, así que no hace falta configurar CORS.

## Comandos

| Comando              | Qué hace                                    |
| -------------------- | ------------------------------------------- |
| `npm run dev`        | Servidor de desarrollo                      |
| `npm test`           | Todas las pruebas, una sola vez             |
| `npm run test:watch` | Pruebas en modo observador                  |
| `npm run lint`       | ESLint                                      |
| `npm run build`      | Comprobación de tipos y build de producción |
| `npm run preview`    | Sirve el build, con el mismo proxy          |
| `npm run format`     | Prettier                                    |

## Variables de entorno

| Variable            | Valor por defecto | Descripción       |
| ------------------- | ----------------- | ----------------- |
| `VITE_API_BASE_URL` | `/api`            | Prefijo de la API |

No hay secretos en el front-end. Todo lo que empieza por `VITE_` queda dentro del JavaScript público.

## Rutas principales

| Ruta                                                                  | Página                                                    | Quién entra          |
| --------------------------------------------------------------------- | --------------------------------------------------------- | -------------------- |
| `/`                                                                   | Inicio                                                    | Todos                |
| `/eventos`                                                            | Cartelera, con filtros `?q=`, `?categoria=` y `?artista=` | Todos                |
| `/eventos/:eventCode`                                                 | Detalle de evento y compra                                | Todos (compra: USER) |
| `/artistas`, `/artistas/:id`                                          | Artistas                                                  | Todos                |
| `/sedes`, `/sedes/:code`                                              | Sedes                                                     | Todos                |
| `/login`, `/registro`                                                 | Acceso y registro                                         | Solo sin sesión      |
| `/cuenta`                                                             | Mi cuenta                                                 | Con sesión           |
| `/mis-tickets`, `/mis-tickets/:ticketCode`                            | Tickets propios, detalle y cancelación                    | USER                 |
| `/compra/confirmacion/:ticketCode`                                    | Confirmación de compra                                    | USER                 |
| `/staff/validacion`                                                   | Validación de ingreso                                     | STAFF, ADMIN         |
| `/staff/eventos/:eventCode/tickets`                                   | Tickets pagados de un evento                              | STAFF, ADMIN         |
| `/admin/eventos`, `/admin/eventos/nuevo`, `/admin/eventos/:eventCode` | Administración de eventos                                 | ADMIN                |
| `/403`, cualquier otra                                                | Sin permisos y no encontrada                              | Todos                |

Las guardas de ruta son comodidad: quien autoriza cada operación es la API.

## Roles de demostración

El registro público siempre crea cuentas `USER`. STAFF y ADMIN se asignan en la base local (README de la raíz, sección 12). Las cuentas del entorno de desarrollo se crean con la etapa 0 del laboratorio:

| Usuario  | Rol                  | Para qué sirve en la demostración                  |
| -------- | -------------------- | -------------------------------------------------- |
| `andrea` | USER                 | Comprar, ver y cancelar tickets                    |
| `carlos` | USER                 | Segundo comprador; probar que no ve tickets ajenos |
| `mateo`  | USER (menor de edad) | Ver el rechazo por edad mínima                     |
| `laura`  | STAFF                | Validar ingresos y ver tickets pagados             |
| `admin`  | ADMIN                | Crear, publicar y asociar artistas                 |

La clave se define al crear las cuentas y no se versiona.

## Estructura

```text
src/
├── componentes/        UI genérica
├── funcionalidades/    un dominio por carpeta: tipos, api, hooks, componentes
├── layouts/            barra, layout y guardas de ruta
├── lib/                http, rutas, formato, claves de caché
├── pages/              una página por ruta
└── test/               utilidades y respuestas simuladas (MSW)
```

## Decisiones

- **Sesión.** El token vive en memoria y en `sessionStorage`; nunca en `localStorage`. Los roles se piden a `/api/auth/me`.
- **Datos del servidor.** TanStack Query. Ningún dato de la API se copia a `useState` ni a contexto.
- **Validación.** Toda respuesta de la API pasa por un esquema zod antes de usarse.
- **Autorización.** Las guardas de ruta mejoran la experiencia; quien autoriza es la API.

## Limitaciones conocidas

- La API no lista todos los eventos. El panel de administración recuerda los creados en la pestaña y permite buscar por código.
- Los mensajes de reglas de negocio (`409`) se muestran en inglés, como los envía la API.
- La API no entrega imágenes; las tarjetas usan color e ícono por categoría.
- No hay renovación de token: a los 15 minutos la sesión expira.
- Los tickets muestran el correo del titular, no su nombre: la API no entrega ese dato.

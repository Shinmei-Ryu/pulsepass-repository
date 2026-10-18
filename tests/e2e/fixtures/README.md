# Fixtures E2E

Datos que **no se pueden crear por HTTP** (la API solo expone GET para venues y no permite asignar roles al registrar).
Todo es exclusivo de la base `pulsepass_e2e`; cada script aborta si la base actual tiene otro nombre.

| Archivo | Cuándo | Qué hace |
|---|---|---|
| `seed-e2e.sql` | Tras TC-PREP-002/003/004 | Crea 3 venues (`E2EV-<runId>` activo, `E2EVT-<runId>` aforo 1, `E2EVI-<runId>` inactivo) y promueve las cuentas `admin.<runId>@example.test` → ADMIN y `staff.<runId>@example.test` → STAFF. |
| `deactivate-user-e2e.sql` | Entre TC-AUTH-013a y TC-AUTH-013 | Pone `active = false` al usuario `inactive.<runId>@example.test`. |
| `revoke-staff-e2e.sql` | Justo antes de TC-SEC-017 (al final) | Devuelve a STAFF al rol USER. |

## Datos que ya existen por migración
- Artistas: `V2__insert_initial_artists` (p. ej. *Solar Beat*). Se descubren con `GET /api/artists/active`.

## Convenciones
- Dominio de correos `example.test` (nunca entregable) y datos derivados de `runId` → cada corrida es independiente.
- Los scripts usan variables de psql (`:'run_id'`). Cómo ejecutarlos: ver `../README.md`, paso 6.
- Las columnas asumidas son las del esquema V1/V4 (`venues`, `users`, `user_roles`). Si su `user_roles.role` es un tipo enum de PostgreSQL, el literal sigue funcionando; si falla con *column "role" is of type …*, cambie el literal a `'ADMIN'::<nombre_del_tipo>`.
- Para repetir la corrida sin recrear la base: nuevo `runId` (`r002`) y repetir desde TC-PREP-001. Para empezar limpio: borrar el contenedor de la base (ver README).

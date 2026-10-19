# Pruebas E2E manuales con Postman — PulsePass (Fase 5)

Esta carpeta contiene la suite **manual** que recorre las 22 rutas de PulsePass (20 de negocio + `POST /api/auth/login` + `GET /api/auth/me`)
desde la interfaz de Postman: roles, propiedad (IDOR), JWT y errores. Reglas del PRD que se respetan:
sin scripts en Postman (pestañas *Pre-request* y *Tests* vacías), sin Runner/Newman/CLI, un **Send** por request, tokens copiados a mano
y ningún secreto en el repositorio.

## Contenido
| Ruta | Para qué sirve |
|---|---|
| `postman/PulsePass-Manual-E2E.postman_collection.json` | Colección v2.1 (carpetas 00–09, un request por caso, descripción con ID/objetivo/precondiciones/esperado). |
| `postman/PulsePass-local.postman_environment.json` | Plantilla de variables. Secretos vacíos. |
| `fixtures/` | SQL exclusivo de pruebas (venues, promoción de roles, desactivar usuario, revocar rol). |
| `checklist-e2e-manual.md` | Orden de ejecución, flujos A–E, AC-01..AC-10 y DoD. |
| `registro-ejecucion-manual.md` | Bitácora a diligenciar (todo inicia en `NOT RUN`). |
| `matriz-cobertura.md` | 22 rutas × actores + trazabilidad. |

## 1. Requisitos
- Docker Desktop (ya lo usan los tests con Testcontainers), JDK 21, Maven wrapper del proyecto y Postman (cuenta no necesaria: *Skip and go to the app*).

## 2. Base de datos dedicada `pulsepass_e2e`
PowerShell:

    docker run -d --name pulsepass-e2e-db -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=pulsepass_e2e -p 5433:5432 postgres:18-alpine

(Puerto 5433 para no chocar con un PostgreSQL local en el 5432.) Para empezar de cero en cualquier momento:
`docker rm -f pulsepass-e2e-db` y repetir el comando.

## 3. Arrancar la API apuntando a esa base
En la **misma** terminal PowerShell, desde la raíz del proyecto (las variables solo viven en esa terminal):

    $bytes = New-Object byte[] 32
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    $env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
    $env:DB_URL = "jdbc:postgresql://localhost:5433/pulsepass_e2e"
    $env:DB_USER = "postgres"
    $env:DB_PASSWORD = "postgres"
    .\mvnw.cmd spring-boot:run

Flyway aplica V1–V4 al arrancar. No guarde el secreto en ningún archivo. Compruebe en `http://localhost:8080/api/venues/active` (responde `[]` antes del seed).

## 4. Postman: importar y configurar
1. *Import* → seleccione los dos JSON de `postman/`.
2. Arriba a la derecha elija el ambiente **PulsePass — local E2E**.
3. Abra *Environments* → ese ambiente y complete en la columna **Current value** (no en *Initial*): `userAPassword`, `userBPassword`, `adminPassword`, `staffPassword` (por ejemplo `E2e-Prueba#2026`, cumpliendo la política de password de la API). Puede cambiar `runId` (solo letras/números).
4. Guarde (Ctrl+S). Los tokens se llenan más adelante.
5. Para ver el JWT en la respuesta: pestaña *Body* → *Pretty*. Seleccione el texto del valor de `accessToken` (sin comillas), cópielo y péguelo en *Environments* → variable de token → **Current value**. Los tokens duran 15 min: si recibe 401 inesperados, repita el login de ese actor.

## 5. Secuencia (resumen; detalle en `checklist-e2e-manual.md`)
1. Carpeta **00**: TC-PREP-001 (conectividad) y registro de los candidatos ADMIN/STAFF/INACTIVO (TC-PREP-002..004).
2. **Paso SQL** (sección 6, `seed-e2e.sql`).
3. Carpetas **01 → 06** en orden. Copie `artistId`/`artistStageName` (TC-BIZ-002a), los cuatro tokens (TC-AUTH-002…) y los tickets (`ticketReadCode`, `ticketCancelableCode`, `ticketUsableCode`).
4. Carpeta **07** (negativos), **08** (validaciones) y **09** (flujos A–E).
5. Al final: paso SQL de revocación y **TC-SEC-017**.
6. Registre cada resultado en `registro-ejecucion-manual.md`.

Antes de cada Send protegido, verifique en *Authorization* qué token usa el request (está en su descripción) y su vigencia.

## 6. Ejecutar los SQL de fixtures (PowerShell)
Usando el mismo `runId` que en Postman (ej. `r001`):

    Get-Content .\tests\e2e\fixtures\seed-e2e.sql -Raw | docker exec -i pulsepass-e2e-db psql -U postgres -d pulsepass_e2e -v run_id=r001

Igual para `deactivate-user-e2e.sql` y `revoke-staff-e2e.sql`. Cada uno imprime una verificación; si aparece *Abortado: la base actual…*, apunta a otra base: deténgase.

## 7. Casos especiales (si no se hacen: BLOCKED, nunca PASS)
- **TC-AUTH-009 (JWT alterado)**: copie `userAToken` en `tamperedToken` y cambie un carácter **en la mitad** del tercer segmento (la firma). No toque el último carácter.
- **TC-AUTH-010 (expirado)**: detenga la API y arránquela con `$env:PULSEPASS_SECURITY_JWT_ACCESSTOKENTTL = "PT10S"`; haga login de USER_A, copie el token en `expiredToken`, espere más de 10 s y envíe el request. Luego vuelva a arrancar la API **sin** esa variable (mantenga el mismo `PULSEPASS_JWT_SECRET_BASE64`; los demás tokens siguen válidos hasta que venzan).
- **TC-AUTH-011 (otra clave/issuer)**: construya offline (por ejemplo en un generador JWT) un token HS256 con una clave inventada **solo para pruebas** (nunca el secreto real), `iss` distinto de `pulsepass-api`, `sub` cualquiera y `exp` futuro; guárdelo en `foreignKeyToken`.
- **TC-AUTH-013 / TC-SEC-017**: requieren los SQL intermedios indicados en el request.
- **TC-AUTH-015**: documente la política observada para un token inválido en una ruta pública.

## 8. Decisiones documentadas
- Recurso ajeno → **403** (`expectedForeignResourceStatus`). Borrador visto por visitante → **404**.
- `POST /api/tickets` es solo USER (STAFF/ADMIN → 403). STAFF lee y usa tickets con alcance global (limitación heredada).
- BusinessRuleException y duplicados → **409**. Los códigos esperados se derivan del diseño de las Fases 3–4; si en la primera ejecución un código real difiere, registre FAIL y decida si es defecto del API o corrección del catálogo (no ajuste silenciosamente).
- Una denegación 403 en recursos mutables siempre va seguida de un GET autorizado que prueba que no hubo cambios (casos `…b`).
- Cada operación irreversible (cancelar/usar) usa un ticket distinto; los tickets de lectura/denegación nunca se mutan.
- Los casos de TC-HTTP-* y los “Revisión manual” de cada request cubren la inspección de ErrorResponse (`timestamp`, `status`, `error`, `message`, `details`; `status` = HTTP real, sin stack traces ni secretos).
- No se versiona `tests/e2e/.gitignore` (el `.gitignore` raíz del proyecto se ignora a sí mismo y a sus hermanos); las reglas van en el `.gitignore` raíz (ver `gitignore-e2e.txt` entregado junto a esta carpeta).

## 9. Seguridad de los artefactos
- Nunca exporte el ambiente con *Current value* completos ni lo suba; los JSON del repositorio tienen secretos vacíos.
- No pegue JWT ni passwords en la bitácora ni en capturas.
- Rotación: cada corrida usa un secreto JWT nuevo y descartable.

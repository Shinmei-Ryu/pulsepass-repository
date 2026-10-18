# Checklist de ejecución manual — PulsePass E2E

Marcar `[x]` solo cuando el request se envió, se revisó Status/Body y se anotó el resultado en la bitácora. Orden = orden de las carpetas de la colección, salvo **TC-SEC-017**, que va al final.

## 0. Antes de empezar (DoR)

- [ ] API construye y arranca sobre PostgreSQL `pulsepass_e2e` (Flyway V1–V4 aplicado).
- [ ] `PULSEPASS_JWT_SECRET_BASE64` aleatorio y exclusivo de E2E (≥ 32 bytes).
- [ ] Colección y ambiente importados; ambiente `PulsePass — local E2E` seleccionado.
- [ ] `runId`, passwords y fechas definidos como valores locales (Current value).
- [ ] Política acordada: recurso ajeno → 403; borrador para visitante → 404.

## 1. Secuencia de requests


### 00 Preparación / conectividad

- [ ] **TC-PREP-001** — Conectividad: GET venues activos (ruta pública) → esperado 200
- [ ] **TC-PREP-002** — Registrar candidato ADMIN (se promueve por SQL) → esperado 201
- [ ] **TC-PREP-003** — Registrar candidato STAFF (se promueve por SQL) → esperado 201
- [ ] **TC-PREP-004** — Registrar candidato USER_INACTIVO (se desactiva por SQL) → esperado 201
  - ⛔ PASO MANUAL: aplicar `fixtures/seed-e2e.sql` (promueve STAFF/ADMIN y crea los venues). Ver fixtures/README.md.

### 01 Venues

- [ ] **TC-BIZ-001a** — Venue por código (fixture activo) → esperado 200
- [ ] **TC-BIZ-001b** — Venues activos → esperado 200

### 02 Artists

- [ ] **TC-BIZ-002a** — Artistas activos (copiar artistId) → esperado 200
- [ ] **TC-BIZ-002b** — Artista por ID → esperado 200
- [ ] **TC-BIZ-002c** — Artista por stage name → esperado 200

### 03 Auth y usuarios

- [ ] **TC-AUTH-001** — Registrar USER_A válido → esperado 201
- [ ] **TC-BIZ-006a** — Registrar USER_B válido → esperado 201
- [ ] **TC-BIZ-006b** — Registro duplicado (email de USER_A) → 409 → esperado 409
- [ ] **TC-AUTH-002** — Login USER_A con username (copiar token) → esperado 200
- [ ] **TC-AUTH-002b** — Login USER_B con username (copiar token) → esperado 200
- [ ] **TC-AUTH-002c** — Login STAFF (copiar token) → esperado 200
- [ ] **TC-AUTH-002d** — Login ADMIN (copiar token) → esperado 200
- [ ] **TC-AUTH-003** — Login USER_A con email → esperado 200
- [ ] **TC-AUTH-006** — /me con token de USER_A → esperado 200
- [ ] **TC-AUTH-006b** — /me con token de USER_B → esperado 200
- [ ] **TC-AUTH-006c** — /me con token de STAFF → esperado 200
- [ ] **TC-AUTH-006d** — /me con token de ADMIN → esperado 200

### 04 Usuarios / consulta y propiedad

- [ ] **TC-USR-001** — USER_A consulta su propio perfil por email → esperado 200
- [ ] **TC-USR-002** — USER_A consulta su propio perfil por username → esperado 200
- [ ] **TC-USR-003** — ADMIN consulta a USER_A por email → esperado 200
- [ ] **TC-USR-004** — ADMIN consulta a USER_B por username → esperado 200

### 05 Events / administración y visibilidad

- [ ] **TC-BIZ-003** — ADMIN crea evento PUBLICADO-objetivo (publishedEventCode) → esperado 201
- [ ] **TC-BIZ-003b** — ADMIN crea evento BORRADOR (draftEventCode) → esperado 201
- [ ] **TC-BIZ-003c** — ADMIN crea evento POR PUBLICAR (eventToPublishCode) → esperado 201
- [ ] **TC-BIZ-003d** — ADMIN crea evento con edad mínima 99 (eventAgeCode) → esperado 201
- [ ] **TC-BIZ-003e** — ADMIN crea evento en venue de aforo 1 (eventCapacityCode) → esperado 201
- [ ] **TC-BIZ-003f** — ADMIN crea evento en venue inactivo → regla de negocio → esperado 409
- [ ] **TC-BIZ-005** — ADMIN asocia artista al evento publicable → esperado 200
- [ ] **TC-BIZ-005c** — ADMIN asocia artista al evento borrador → esperado 200
- [ ] **TC-BIZ-004s1** — ADMIN publica el evento principal (setup de compras) → esperado 200
- [ ] **TC-BIZ-004s2** — ADMIN publica el evento de edad (setup) → esperado 200
- [ ] **TC-BIZ-004s3** — ADMIN publica el evento de aforo (setup) → esperado 200
- [ ] **TC-BIZ-004a** — Público: evento publicado por código → esperado 200
- [ ] **TC-BIZ-004b** — Público: listado de eventos publicados (antes de publicar el pendiente) → esperado 200
- [ ] **TC-BIZ-005b** — Público: eventos por artista → esperado 200
- [ ] **TC-BIZ-004** — ADMIN publica el evento pendiente (eventToPublishCode) → esperado 200
- [ ] **TC-BIZ-004c** — Público: listado refleja el evento recién publicado → esperado 200

### 06 Tickets / compra, consulta y ciclo de vida

- [ ] **TC-BIZ-007a** — USER_A compra ticket GENERAL (ticketReadCode) → esperado 201
- [ ] **TC-BIZ-007b** — USER_A compra ticket STUDENT (ticketCancelableCode) → esperado 201
- [ ] **TC-BIZ-007c** — USER_A compra ticket VIP (ticketUsableCode) → esperado 201
- [ ] **TC-BIZ-008a** — USER_A lee su ticket por código → esperado 200
- [ ] **TC-BIZ-008b** — USER_A lista sus tickets → esperado 200
- [ ] **TC-BIZ-008c** — STAFF lee el ticket de USER_A → esperado 200
- [ ] **TC-BIZ-008d** — ADMIN lee el ticket de USER_A → esperado 200
- [ ] **TC-BIZ-008e** — ADMIN lista los tickets de USER_A → esperado 200
- [ ] **TC-BIZ-009a** — STAFF lista tickets pagados del evento → esperado 200
- [ ] **TC-BIZ-009b** — ADMIN lista tickets pagados del evento → esperado 200
- [ ] **TC-BIZ-010** — USER_A cancela su ticket cancelable → esperado 200
- [ ] **TC-BIZ-010b** — Verificar: ticket cancelado persistido → esperado 200
- [ ] **TC-BIZ-011** — Cancelar de nuevo el mismo ticket → 409 → esperado 409
- [ ] **TC-BIZ-011b** — STAFF intenta usar el ticket cancelado → 409 → esperado 409
- [ ] **TC-BIZ-012** — STAFF marca como usado el ticket usable (equivale a TC-SEC-014) → esperado 200
- [ ] **TC-BIZ-012b** — Verificar: ticket usado persistido → esperado 200
- [ ] **TC-BIZ-013** — Marcar usado por segunda vez → 409 → esperado 409
- [ ] **TC-BIZ-013b** — USER_A intenta cancelar un ticket ya usado → 409 → esperado 409
- [ ] **TC-BIZ-009c** — STAFF: tickets pagados tras cancelar/usar → esperado 200

### 07 Seguridad / escenarios negativos › 07a Autenticación y JWT

- [ ] **TC-AUTH-004** — Login con password erróneo → 401 → esperado 401
- [ ] **TC-AUTH-005** — Login con identificador inexistente → 401 → esperado 401
- [ ] **TC-AUTH-007** — Ruta protegida sin header Authorization → 401 → esperado 401
- [ ] **TC-AUTH-008** — Bearer inventado/malformado → 401 → esperado 401
- [ ] **TC-AUTH-009** — JWT alterado (un carácter) → 401 → esperado 401 _(especial)_
- [ ] **TC-AUTH-010** — JWT expirado → 401 → esperado 401 _(especial)_
- [ ] **TC-AUTH-011** — JWT firmado con otra clave / issuer distinto → 401 → esperado 401 _(especial)_
- [ ] **TC-AUTH-012** — Token por query string en lugar de Bearer → 401 → esperado 401
- [ ] **TC-AUTH-013a** — Login del usuario que será desactivado (copiar token) → esperado 200
  - ⛔ PASO MANUAL: aplicar `fixtures/deactivate-user-e2e.sql` antes de TC-AUTH-013.
- [ ] **TC-AUTH-013** — Usuario inactivo con token previo → 401 → esperado 401 _(especial)_
- [ ] **TC-AUTH-014** — Token de USER en endpoint de ADMIN → 403 → esperado 403
- [ ] **TC-AUTH-015** — JWT inválido presente en endpoint público → esperado 200 o 401 (política) _(especial)_
- [ ] **TC-SEC-016** — Registro con role=ADMIN inyectado → esperado 201 (campo ignorado) o 400
- [ ] **TC-SEC-016b** — Login del usuario inyectado (copiar token) → esperado 200
- [ ] **TC-SEC-016c** — /me del usuario inyectado: solo USER → esperado 200

### 07 Seguridad / escenarios negativos › 07b Autorización, roles y propiedad

- [ ] **TC-SEC-001a** — Anónimo crea evento → 401 → esperado 401
- [ ] **TC-SEC-001b** — USER_A crea evento → 403 → esperado 403
- [ ] **TC-SEC-002a** — USER_A publica evento borrador → 403 → esperado 403
- [ ] **TC-SEC-002b** — STAFF publica evento borrador → 403 → esperado 403
- [ ] **TC-SEC-003** — USER_A asocia artista → 403 → esperado 403
- [ ] **TC-SEC-002c** — Verificar: el borrador sigue en DRAFT (ADMIN) → esperado 200
- [ ] **TC-SEC-004** — Anónimo consulta borrador por código → esperado 404
- [ ] **TC-SEC-005** — Anónimo busca eventos por artista (con borrador asociado) → esperado 200
- [ ] **TC-SEC-006** — USER_B consulta a USER_A por email → 403 → esperado 403
- [ ] **TC-SEC-007** — USER_B consulta a USER_A por username → 403 → esperado 403
- [ ] **TC-SEC-008** — USER_A compra usando el email de USER_B → 403 → esperado 403
- [ ] **TC-SEC-008b** — Verificar: USER_B no tiene tickets (ADMIN) → esperado 200
- [ ] **TC-SEC-009** — USER_B lee el ticket de USER_A → 403 → esperado 403
- [ ] **TC-SEC-010** — USER_B lista tickets del email de A → 403 → esperado 403
- [ ] **TC-SEC-011** — USER_B cancela el ticket de A → 403 → esperado 403
- [ ] **TC-SEC-011b** — Verificar: ticket de A sin cambios tras 011 → esperado 200
- [ ] **TC-SEC-012** — USER_B marca usado el ticket de A → 403 → esperado 403
- [ ] **TC-SEC-012b** — Verificar: ticket de A sin cambios tras 012 → esperado 200
- [ ] **TC-SEC-013** — USER_A lista tickets pagados del evento → 403 → esperado 403
- [ ] **TC-SEC-013b** — USER_A intenta marcar uso (rol insuficiente) → esperado 403
- [ ] **TC-SEC-013c** — STAFF compra ticket → 403 (POST /api/tickets es solo USER) → esperado 403

### 08 Validaciones y errores HTTP

- [ ] **TC-HTTP-001** — JSON inválido en registro → 400 → esperado 400
- [ ] **TC-HTTP-002** — Registro con campos obligatorios ausentes → 400 → esperado 400
- [ ] **TC-HTTP-003** — Registro con email mal formado → 400 → esperado 400
- [ ] **TC-HTTP-004** — Login con campos vacíos → 400 → esperado 400
- [ ] **TC-HTTP-005** — Categoría de evento inexistente (enum inválido) → 400 → esperado 400
- [ ] **TC-HTTP-006** — Tipo de ticket inexistente (enum inválido) → 400 → esperado 400
- [ ] **TC-HTTP-007** — Encabezado X-Request-Id se refleja en la respuesta de error → esperado 404
- [ ] **TC-BIZ-015a** — Venue inexistente → 404 → esperado 404
- [ ] **TC-BIZ-015b** — Artista inexistente → 404 → esperado 404
- [ ] **TC-BIZ-015c** — Evento inexistente → 404 → esperado 404
- [ ] **TC-BIZ-015d** — Ticket inexistente → 404 → esperado 404
- [ ] **TC-BIZ-015e** — Verificar: el evento del venue inactivo no existe → 404 → esperado 404
- [ ] **TC-BIZ-015f** — Crear evento con venue inexistente → 404 → esperado 404
- [ ] **TC-BIZ-015g** — Crear evento con código duplicado → 409 → esperado 409
- [ ] **TC-BIZ-015h** — Crear evento con fecha pasada → regla de negocio → esperado 409
- [ ] **TC-BIZ-015i** — Asociar artista inexistente → 404 → esperado 404
- [ ] **TC-BIZ-015j** — Asociar el mismo artista dos veces → 409 → esperado 409
- [ ] **TC-BIZ-004d** — Publicar un evento ya publicado → 409 → esperado 409
- [ ] **TC-BIZ-015k** — Publicar evento inexistente → 404 → esperado 404
- [ ] **TC-BIZ-014a** — Compra de evento NO publicado (borrador) → 409 → esperado 409
- [ ] **TC-BIZ-014b** — Compra con edad insuficiente (mínimo 99) → 409 → esperado 409
- [ ] **TC-BIZ-014c1** — USER_A compra el único cupo del evento de aforo 1 → esperado 201
- [ ] **TC-BIZ-014c** — USER_B compra con aforo agotado → 409 → esperado 409
- [ ] **TC-BIZ-014d** — Compra para evento inexistente → 404 → esperado 404

### 09 Flujos de negocio › Flujo A — Descubrimiento público

- [ ] **FLW-A1** — A1 · Venues activos → esperado 200
- [ ] **FLW-A2** — A2 · Venue por código → esperado 200
- [ ] **FLW-A3** — A3 · Artistas activos → esperado 200
- [ ] **FLW-A4a** — A4 · Artista por ID → esperado 200
- [ ] **FLW-A4b** — A4 · Artista por stage name → esperado 200
- [ ] **FLW-A5** — A5 · Eventos publicados (sin borradores) → esperado 200
- [ ] **FLW-A6a** — A6 · Evento publicado por código → esperado 200
- [ ] **FLW-A6b** — A6 · Borrador por código (no se expone) → esperado 404
- [ ] **FLW-A7** — A7 · Eventos por artista (solo publicados) → esperado 200

### 09 Flujos de negocio › Flujo B — Registro y JWT

- [ ] **FLW-B1** — B1 · /me con USER_A → esperado 200
- [ ] **FLW-B2** — B2 · /me con USER_B → esperado 200
- [ ] **FLW-B3** — B3 · /me con STAFF → esperado 200
- [ ] **FLW-B4** — B4 · /me con ADMIN → esperado 200
- [ ] **FLW-B5** — B5 · Login con password incorrecta → esperado 401
- [ ] **FLW-B6** — B6 · Sin token → esperado 401
- [ ] **FLW-B7** — B7 · Rol insuficiente (USER_A crea evento) → esperado 403

### 09 Flujos de negocio › Flujo C — Catálogo (ADMIN)

- [ ] **FLW-C1** — C1 · ADMIN crea evento (newEventCode) → esperado 201
- [ ] **FLW-C2a** — C2 · ADMIN lee el borrador → esperado 200
- [ ] **FLW-C2b** — C2 · Listado público aún no lo incluye → esperado 200
- [ ] **FLW-C3** — C3 · ADMIN asocia artista → esperado 200
- [ ] **FLW-C4** — C4 · ADMIN publica → esperado 200
- [ ] **FLW-C5** — C5 · Lectura pública sin Bearer → esperado 200
- [ ] **FLW-C6a** — C6 · Aparece en publicados → esperado 200
- [ ] **FLW-C6b** — C6 · Aparece por artista → esperado 200
- [ ] **FLW-C7a** — C7 · USER_A no puede publicar → esperado 403
- [ ] **FLW-C7b** — C7 · STAFF no puede crear → esperado 403

### 09 Flujos de negocio › Flujo D — Compra, propiedad, cancelación y uso

- [ ] **FLW-D1** — D1 · USER_A compra T1 (ticketD1Code) → esperado 201
- [ ] **FLW-D2** — D2 · USER_A lee T1 → esperado 200
- [ ] **FLW-D3** — D3 · USER_B lee T1 → 403 → esperado 403
- [ ] **FLW-D4** — D4 · USER_B cancela T1 → 403 (sin mutación) → esperado 403
- [ ] **FLW-D5** — D5 · USER_A cancela T1 → 200 → esperado 200
- [ ] **FLW-D6** — D6 · USER_A compra T2 (ticketD2Code) → esperado 201
- [ ] **FLW-D7** — D7 · STAFF lista pagados → esperado 200
- [ ] **FLW-D8** — D8 · USER_B intenta usar T2 → 403 → esperado 403
- [ ] **FLW-D9** — D9 · STAFF usa T2 → 200 → esperado 200
- [ ] **FLW-D10** — D10 · STAFF usa T2 otra vez → 409 → esperado 409

### 09 Flujos de negocio › Flujo E — Regresión de errores

- [ ] **FLW-E1** — E1 · 400 JSON inválido → esperado 400
- [ ] **FLW-E2** — E2 · 401 sin token → esperado 401
- [ ] **FLW-E3** — E3 · 403 privilegios insuficientes → esperado 403
- [ ] **FLW-E4** — E4 · 404 recurso ausente → esperado 404
- [ ] **FLW-E5** — E5 · 409 duplicado → esperado 409
- [ ] **FLW-E6** — E6 · 409 transición no admitida → esperado 409

### FINAL (después de la carpeta 09)

- ⛔ PASO MANUAL: aplicar `fixtures/revoke-staff-e2e.sql` y NO volver a hacer login de STAFF antes de enviar este request.
- [ ] **TC-SEC-017** — STAFF con token antiguo tras revocar su rol → 403 (EJECUTAR AL FINAL) → esperado 403 _(especial)_

## 2. Flujos del PRD (cap. 9)

- [ ] Flujo A — Descubrimiento público (carpeta 09 › Flujo A).
- [ ] Flujo B — Registro y JWT (TC-AUTH-001, TC-BIZ-006a/b, logins, /me, TC-SEC-016 y carpeta 09 › Flujo B).
- [ ] Flujo C — Catálogo ADMIN (carpeta 05 y 09 › Flujo C).
- [ ] Flujo D — Compra, propiedad, cancelación y uso (carpeta 06 y 09 › Flujo D; un ticket por operación irreversible).
- [ ] Flujo E — Regresión de errores (carpetas 07, 08 y 09 › Flujo E). Ningún 500 en escenarios nominales.

## 3. Criterios de aceptación

- [ ] AC-01 — Registro y login → 201, 200, /me sin password
- [ ] AC-02 — ADMIN crea/asocia/publica y el evento es público con artista
- [ ] AC-03 — USER sin privilegio: 403 sin mutación
- [ ] AC-04 — Propiedad: USER_B no ve ni cancela tickets de A
- [ ] AC-05 — Compra ajena rechazada sin ticket creado
- [ ] AC-06 — STAFF usa ticket; segunda transición rechazada
- [ ] AC-07 — JWT manipulado/malformado/expirado → 401
- [ ] AC-08 — Borradores ocultos al visitante
- [ ] AC-09 — ErrorResponse completo y sin datos internos
- [ ] AC-10 — 22 rutas enviadas manualmente, todas con resultado

## 4. Definition of Done

- [ ] Colección v2.1 importable, sin scripts (pestañas Pre-request y Tests vacías).
- [ ] 22/22 rutas con caso nominal ejecutado.
- [ ] Logins de A, B, STAFF y ADMIN con tokens copiados a variables locales.
- [ ] Casos 400, 401, 403, 404 y 409 ejecutados.
- [ ] Borradores y propiedad verificados; compra ajena bloqueada sin mutación.
- [ ] Estados de eventos y tickets confirmados con GET posterior.
- [ ] ErrorResponse revisado manualmente.
- [ ] Bitácora diligenciada (PASS/FAIL/BLOCKED con esperado y observado).
- [ ] Ambiente y evidencias sin tokens ni contraseñas.
- [ ] Defectos y bloqueos registrados; nada omitido figura como aprobado.
- [ ] Matriz de cobertura disponible (`matriz-cobertura.md`).

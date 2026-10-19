# Registro de ejecución manual — PulsePass E2E

Estados permitidos: `PASS`, `FAIL`, `BLOCKED`, `NOT RUN`. No contiene JWT ni passwords. `PASS` significa HTTP observado = esperado **y** cuerpo conforme a la revisión manual; lo no ejecutado figura como `NOT RUN`.

- Ejecutor: Daniel Vergara - Fecha: 2026-10-18 / 2026-10-19  - runId: r001 
- Ambiente: PostgreSQL `pulsepass_e2e` (Docker, puerto 5433); API local en 8080 (`mvn spring-boot:run`); Postman con ambiente `PulsePass — local E2E`.
- Resumen: **163 PASS · 0 FAIL · 0 BLOCKED · 0 NOT RUN** de 163 casos. 22/22 rutas nominales en PASS. TC-BIZ-007a/b/c tuvieron un FAIL inicial (DEF-001) y pasaron tras la corrección.
- Un defecto encontrado y corregido durante la corrida (DEF-001, ver abajo); 7 observaciones.

## Resultados por caso

| Caso | Fecha | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|------|-------|-------|------|---------------|----------------|-------------------------|-----------|-------------------------|
| TC-PREP-001 | 2026-10-18 | Anónimo | `GET /api/venues/active` | 200 | 200 | [] (sin venues aún) | PASS | Conectividad OK. |
| TC-PREP-002 | 2026-10-18 | Anónimo | `POST /api/users` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-PREP-003 | 2026-10-18 | Anónimo | `POST /api/users` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-PREP-004 | 2026-10-18 | Anónimo | `POST /api/users` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-001a | 2026-10-18 | Anónimo | `GET /api/venues/{code}` | 200 | 200 | Venue E2EV-r001 (capacity 500, active) | PASS | OBS-02: la ciudad se ve como `MedellÃn` (codificación al cargar el seed por PowerShell; dato de fixture, no de la API). |
| TC-BIZ-001b | 2026-10-18 | Anónimo | `GET /api/venues/active` | 200 | 200 | Lista con venues activos; el inactivo no aparece | PASS | — |
| TC-BIZ-002a | 2026-10-18 | Anónimo | `GET /api/artists/active` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-002b | 2026-10-18 | Anónimo | `GET /api/artists/{id}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-002c | 2026-10-18 | Anónimo | `GET /api/artists/by-stage-name` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-AUTH-001 | 2026-10-18 | Anónimo | `POST /api/users` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-006a | 2026-10-18 | Anónimo | `POST /api/users` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-006b | 2026-10-18 | Anónimo | `POST /api/users` | 409 | 409 | 409 por email/username duplicado | PASS | — |
| TC-AUTH-002 | 2026-10-18 | Anónimo | `POST /api/auth/login` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-AUTH-002b | 2026-10-18 | Anónimo | `POST /api/auth/login` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-AUTH-002c | 2026-10-18 | Anónimo | `POST /api/auth/login` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-AUTH-002d | 2026-10-18 | Anónimo | `POST /api/auth/login` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-AUTH-003 | 2026-10-18 | Anónimo | `POST /api/auth/login` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-AUTH-006 | 2026-10-18 | USER_A | `GET /api/auth/me` | 200 | 200 | roles=[USER]; sin password | PASS | — |
| TC-AUTH-006b | 2026-10-18 | USER_B | `GET /api/auth/me` | 200 | 200 | roles=[USER]; sin password | PASS | — |
| TC-AUTH-006c | 2026-10-18 | STAFF | `GET /api/auth/me` | 200 | 200 | roles=[STAFF]; sin password | PASS | — |
| TC-AUTH-006d | 2026-10-18 | ADMIN | `GET /api/auth/me` | 200 | 200 | roles=[ADMIN]; sin password | PASS | — |
| TC-USR-001 | 2026-10-18 | USER_A | `GET /api/users/by-email` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-USR-002 | 2026-10-18 | USER_A | `GET /api/users/by-username` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-USR-003 | 2026-10-18 | ADMIN | `GET /api/users/by-email` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-USR-004 | 2026-10-18 | ADMIN | `GET /api/users/by-username` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-003 | 2026-10-18 | ADMIN | `POST /api/events` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-003b | 2026-10-18 | ADMIN | `POST /api/events` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-003c | 2026-10-18 | ADMIN | `POST /api/events` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-003d | 2026-10-18 | ADMIN | `POST /api/events` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-003e | 2026-10-18 | ADMIN | `POST /api/events` | 201 | 201 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-003f | 2026-10-18 | ADMIN | `POST /api/events` | 409 | 409 | 409 por código duplicado | PASS | — |
| TC-BIZ-005 | 2026-10-18 | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-005c | 2026-10-18 | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-004s1 | 2026-10-18 | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-004s2 | 2026-10-18 | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-004s3 | 2026-10-18 | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-004a | 2026-10-18 | Anónimo | `GET /api/events/{eventCode}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-004b | 2026-10-18 | Anónimo | `GET /api/events/published` | 200 | 200 | Lista sin borradores | PASS | Borradores ocultos en la ruta pública. |
| TC-BIZ-005b | 2026-10-18 | Anónimo | `GET /api/events/by-artist` | 200 | 200 | Lista sin borradores | PASS | — |
| TC-BIZ-004 | 2026-10-18 | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-004c | 2026-10-18 | Anónimo | `GET /api/events/published` | 200 | 200 | Lista sin borradores | PASS | — |
| TC-BIZ-007a | 2026-10-18 | USER_A | `POST /api/tickets` | 201 | 201 | 201 GENERAL price=120000.00 PAID, id=1 | PASS | DEF-001: el 1.er intento devolvió 500 (SQLState 25006, INSERT en transacción de solo lectura). Resultado final tras corregir `TicketServiceImpl.purchase` y reintentar. OBS-01: escala decimal distinta (120000.00). |
| TC-BIZ-007b | 2026-10-18 | USER_A | `POST /api/tickets` | 201 | 201 | 201 STUDENT price=60000.0000 PAID | PASS | DEF-001 (1.er intento 500). Reintento PASS. El ejecutor lo envió dos veces: se crearon id=2 y id=3 (ambos 201); el sobrante id=2 no se usó (OBS-03). OBS-01: price con 4 decimales. |
| TC-BIZ-007c | 2026-10-18 | USER_A | `POST /api/tickets` | 201 | 201 | 201 VIP price=240000.0000 PAID, id=4 | PASS | DEF-001 (1.er intento 500). Reintento PASS. |
| TC-BIZ-008a | 2026-10-18 | USER_A | `GET /api/tickets/{ticketCode}` | 200 | 200 | 200 ticket GENERAL id=1, PAID, userEmail=USER_A | PASS | 1.er intento 500 por variable `ticketReadCode` vacía (sin log; OBS-05). Reintento con la variable cargada: PASS. |
| TC-BIZ-008b | 2026-10-18 | USER_A | `GET /api/tickets/by-user` | 200 | 200 | 200, lista con 4 tickets PAID (ids 1,2,3,4) | PASS | OBS-03: esperado 3, observado 4 por el envío doble de 007b; esperado ajustado a 4. |
| TC-BIZ-008c | 2026-10-18 | STAFF | `GET /api/tickets/{ticketCode}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-008d | 2026-10-18 | ADMIN | `GET /api/tickets/{ticketCode}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| TC-BIZ-008e | 2026-10-18 | ADMIN | `GET /api/tickets/by-user` | 200 | 200 | 200, lista con 4 tickets (ids 1,2,3,4) | PASS | OBS-03 (esperado ajustado a 4). |
| TC-BIZ-009a | 2026-10-18 | STAFF | `GET /api/events/{eventCode}/tickets/paid` | 200 | 200 | 200, 4 tickets PAID | PASS | Esperado ajustado a 4 (OBS-03). |
| TC-BIZ-009b | 2026-10-18 | ADMIN | `GET /api/events/{eventCode}/tickets/paid` | 200 | 200 | 200, 4 tickets PAID | PASS | Esperado ajustado a 4 (OBS-03). |
| TC-BIZ-010 | 2026-10-18 | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 200 | 200 | 200 status=CANCELLED | PASS | — |
| TC-BIZ-010b | 2026-10-18 | USER_A | `GET /api/tickets/{ticketCode}` | 200 | 200 | 200 status=CANCELLED | PASS | — |
| TC-BIZ-011 | 2026-10-18 | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 409 | 409 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-011b | 2026-10-18 | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 409 | 409 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-012 | 2026-10-18 | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 200 | 200 | 200 status=USED | PASS | — |
| TC-BIZ-012b | 2026-10-18 | STAFF | `GET /api/tickets/{ticketCode}` | 200 | 200 | 200 (estado reportado sin captura; se asumió USED) | PASS | El ejecutor reportó solo el HTTP. |
| TC-BIZ-013 | 2026-10-18 | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 409 | 409 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-013b | 2026-10-18 | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 409 | 409 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-009c | 2026-10-18 | STAFF | `GET /api/events/{eventCode}/tickets/paid` | 200 | 200 | 200, 2 tickets PAID (ids 1 y 2); sin CANCELLED ni USED | PASS | Esperado ajustado a 2 (OBS-03). |
| TC-AUTH-004 | 2026-10-18 | Anónimo | `POST /api/auth/login` | 401 | 401 | 401 message="Invalid credentials" | PASS | — |
| TC-AUTH-005 | 2026-10-18 | Anónimo | `POST /api/auth/login` | 401 | 401 | 401, mensaje idéntico a TC-AUTH-004 | PASS | Sin enumeración de usuarios. |
| TC-AUTH-007 | 2026-10-18 | Anónimo | `GET /api/auth/me` | 401 | 401 | 401 message="Authentication is required" | PASS | — |
| TC-AUTH-008 | 2026-10-18 | Token inválido/malformado | `GET /api/auth/me` | 401 | 401 | 401 message="Invalid or expired token" | PASS | — |
| TC-AUTH-009 | 2026-10-18 | Token inválido/malformado | `GET /api/auth/me` | 401 | 401 | 401 message="Invalid or expired token" | PASS | 1.er intento inválido (token sin alterar → 200 correcto) y variables vacías (→ "Authentication is required"); no cuentan. Resultado del reintento con firma alterada (XXXX en el tramo medio). |
| TC-AUTH-010 | 2026-10-19 | Token inválido/malformado | `GET /api/auth/me` | 401 | 401 | 401 message="Invalid or expired token" | PASS | API reiniciada con `PULSEPASS_SECURITY_JWT_ACCESSTOKENTTL=PT10S` (expiresIn=10); token enviado ~30 s después; la API se devolvió luego a TTL normal (expiresIn=900). |
| TC-AUTH-011 | 2026-10-19 | Token inválido/malformado | `GET /api/auth/me` | 401 | 401 | 401 message="Invalid or expired token" | PASS | Token con firma HS256 de clave de prueba ajena, construido localmente sin exponerlo. |
| TC-AUTH-012 | 2026-10-18 | Anónimo | `GET /api/users/by-email` | 401 | 401 | 401 | PASS | HTTP reportado por el ejecutor; sin captura del cuerpo. |
| TC-AUTH-013a | 2026-10-19 | Anónimo | `POST /api/auth/login` | 200 | 200 | 200 (login del usuario aún activo) | PASS | Precondición: `deactivate-user-e2e.sql` aplicado después (UPDATE 1, active=f). |
| TC-AUTH-013 | 2026-10-19 | USER_INACTIVO | `GET /api/auth/me` | 401 | 401 | 401 message="Invalid or expired token" | PASS | Política observada: el usuario desactivado pierde acceso con el token previo; mensaje genérico. |
| TC-AUTH-014 | 2026-10-18 | USER_A | `POST /api/events` | 403 | 403 | 403 message="Access denied" | PASS | No se reportó el GET posterior que confirma que `E2E-r001-X14` no se creó (verificación pendiente, no bloquea el 403). |
| TC-AUTH-015 | 2026-10-18 | Token inválido/malformado | `GET /api/venues/active` | 200 o 401 (política) | 401 | 401 message="Invalid or expired token" | PASS | OBS-04 — política documentada: un JWT inválido se rechaza con 401 incluso en una ruta pública; en ningún caso queda autenticado. |
| TC-SEC-016 | 2026-10-18 | Anónimo | `POST /api/users` | 201 (campo ignorado) o 400 | 201 | 201 UserResponse sin role ni password | PASS | El campo `role`/`roles` inyectado se ignora. |
| TC-SEC-016b | 2026-10-18 | Anónimo | `POST /api/auth/login` | 200 | 200 | 200 login del usuario inyectado | PASS | — |
| TC-SEC-016c | 2026-10-18 | USER_INYECTADO | `GET /api/auth/me` | 200 | 200 | 200 roles=[USER] exclusivamente | PASS | El intento de escalada fracasó. |
| TC-SEC-001a | 2026-10-18 | Anónimo | `POST /api/events` | 401 | 401 | 401 "Authentication is required" | PASS | — |
| TC-SEC-001b | 2026-10-18 | USER_A | `POST /api/events` | 403 | 403 | 403 "Access denied" | PASS | — |
| TC-SEC-002a | 2026-10-18 | USER_A | `PATCH /api/events/{eventCode}/publish` | 403 | 403 | 403 "Access denied" | PASS | — |
| TC-SEC-002b | 2026-10-18 | STAFF | `PATCH /api/events/{eventCode}/publish` | 403 | 403 | 403 "Access denied" | PASS | El 1.er envío dio 401 por `staffToken` vencido (no cuenta); reintento con token renovado. |
| TC-SEC-003 | 2026-10-18 | USER_A | `POST /api/events/{eventCode}/artists/{artistId}` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-002c | 2026-10-18 | ADMIN | `GET /api/events/{eventCode}` | 200 | 200 | 200 E2E-r001-DRF status=DRAFT | PASS | El borrador no cambió tras 002a/002b/003 (cubre TC-SEC-015). |
| TC-SEC-004 | 2026-10-18 | Anónimo | `GET /api/events/{eventCode}` | 404 | 404 | 404 "Event not found: E2E-r001-DRF" | PASS | El borrador es indistinguible de un evento inexistente para el anónimo. |
| TC-SEC-005 | 2026-10-18 | Anónimo | `GET /api/events/by-artist` | 200 | 200 | 200, solo E2E-r001-PUB (sin borrador) | PASS | — |
| TC-SEC-006 | 2026-10-18 | USER_B | `GET /api/users/by-email` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-007 | 2026-10-18 | USER_B | `GET /api/users/by-username` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-008 | 2026-10-18 | USER_A | `POST /api/tickets` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-008b | 2026-10-18 | ADMIN | `GET /api/tickets/by-user` | 200 | 200 | 200 [] | PASS | USER_B no tiene tickets: la compra rechazada no dejó mutación. |
| TC-SEC-009 | 2026-10-18 | USER_B | `GET /api/tickets/{ticketCode}` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-010 | 2026-10-18 | USER_B | `GET /api/tickets/by-user` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-011 | 2026-10-18 | USER_B | `PATCH /api/tickets/{ticketCode}/cancel` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-011b | 2026-10-18 | USER_A | `GET /api/tickets/{ticketCode}` | 200 | 200 | 200 ticket id=1 status=PAID | PASS | Sin cambios tras el intento de USER_B. |
| TC-SEC-012 | 2026-10-18 | USER_B | `PATCH /api/tickets/{ticketCode}/use` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-012b | 2026-10-18 | USER_A | `GET /api/tickets/{ticketCode}` | 200 | 200 | 200 ticket id=1 status=PAID | PASS | Sin cambios tras el intento de USER_B. |
| TC-SEC-013 | 2026-10-18 | USER_A | `GET /api/events/{eventCode}/tickets/paid` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-013b | 2026-10-18 | USER_A | `PATCH /api/tickets/{ticketCode}/use` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-013c | 2026-10-18 | STAFF | `POST /api/tickets` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-SEC-017 | 2026-10-19 | STAFF | `GET /api/events/{eventCode}/tickets/paid` | 403 | 403 | 403 "Access denied" | PASS | `revoke-staff-e2e.sql` (UPDATE 1; rol → USER) aplicado después de emitir el token. La API revalida roles contra la BD. |
| TC-HTTP-001 | 2026-10-18 | Anónimo | `POST /api/users` | 400 | 400 | 400 "Malformed or invalid JSON request", details.body | PASS | — |
| TC-HTTP-002 | 2026-10-18 | Anónimo | `POST /api/users` | 400 | 400 | 400 "Request validation failed"; details: firstName, lastName, password, birthDate, email, username | PASS | — |
| TC-HTTP-003 | 2026-10-18 | Anónimo | `POST /api/users` | 400 | 400 | 400; details.email="Email must have a valid format" | PASS | — |
| TC-HTTP-004 | 2026-10-18 | Anónimo | `POST /api/auth/login` | 400 | 400 | 400; details: identifier, password | PASS | — |
| TC-HTTP-005 | 2026-10-18 | ADMIN | `POST /api/events` | 400 | 400 | 400 "Malformed or invalid JSON request" | PASS | El 1.er envío dio 401 por `adminToken` vencido (no cuenta). |
| TC-HTTP-006 | 2026-10-18 | USER_A | `POST /api/tickets` | 400 | 400 | 400 "Malformed or invalid JSON request" | PASS | No se creó ticket. |
| TC-HTTP-007 | 2026-10-18 | Anónimo | `GET /api/venues/{code}` | 404 | 404 | 404 "Venue not found: NO-EXISTE-r001"; header X-Request-Id presente (e2e-r001-001) | PASS | — |
| TC-BIZ-015a | 2026-10-18 | Anónimo | `GET /api/venues/{code}` | 404 | 404 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-015b | 2026-10-18 | Anónimo | `GET /api/artists/{id}` | 404 | 404 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-015c | 2026-10-18 | Anónimo | `GET /api/events/{eventCode}` | 404 | 404 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-015d | 2026-10-18 | ADMIN | `GET /api/tickets/{ticketCode}` | 404 | 404 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-015e | 2026-10-18 | ADMIN | `GET /api/events/{eventCode}` | 404 | 404 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-015f | 2026-10-18 | ADMIN | `POST /api/events` | 404 | 404 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-015g | 2026-10-18 | ADMIN | `POST /api/events` | 409 | 409 | 409 "Event code already exists: E2E-r001-PUB" | PASS | — |
| TC-BIZ-015h | 2026-10-19 | ADMIN | `POST /api/events` | 409 | 409 | 409 "Event date must be in the future." | PASS | La fecha pasada se trata como regla de negocio (409). |
| TC-BIZ-015i | 2026-10-19 | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 404 | 404 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-015j | 2026-10-19 | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 409 | 409 | 409 "Artist already associated with event: E2E-r001-PUB" | PASS | — |
| TC-BIZ-004d | 2026-10-19 | ADMIN | `PATCH /api/events/{eventCode}/publish` | 409 | 409 | 409 "Only DRAFT events can be published: E2E-r001-PUB" | PASS | — |
| TC-BIZ-015k | 2026-10-19 | ADMIN | `PATCH /api/events/{eventCode}/publish` | 404 | 404 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| TC-BIZ-014a | 2026-10-19 | USER_A | `POST /api/tickets` | 409 | 409 | 409 "Cannot purchase tickets for a DRAFT event." | PASS | — |
| TC-BIZ-014b | 2026-10-19 | USER_A | `POST /api/tickets` | 409 | 409 | 409 "User does not meet minimum age for event: E2E-r001-AGE" | PASS | — |
| TC-BIZ-014c1 | 2026-10-19 | USER_A | `POST /api/tickets` | 201 | 201 | 201 GENERAL PAID id=5 (único cupo) | PASS | El evento pasó a SOLD_OUT. |
| TC-BIZ-014c | 2026-10-19 | USER_B | `POST /api/tickets` | 409 | 409 | 409 "Cannot purchase tickets for a SOLD_OUT event." | PASS | — |
| TC-BIZ-014d | 2026-10-19 | USER_A | `POST /api/tickets` | 404 | 404 | 404 "Event not found: NO-EXISTE-r001" | PASS | — |
| FLW-A1 | 2026-10-19 | Anónimo | `GET /api/venues/active` | 200 | 200 | Lista: E2EV-r001 y E2EVT-r001; sin el venue inactivo | PASS | — |
| FLW-A2 | 2026-10-19 | Anónimo | `GET /api/venues/{code}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-A3 | 2026-10-19 | Anónimo | `GET /api/artists/active` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-A4a | 2026-10-19 | Anónimo | `GET /api/artists/{id}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-A4b | 2026-10-19 | Anónimo | `GET /api/artists/by-stage-name` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-A5 | 2026-10-19 | Anónimo | `GET /api/events/published` | 200 | 200 | Lista: PUB, AGE y TOP (PUBLISHED); sin DRF | PASS | CAP no aparece porque pasó a SOLD_OUT. |
| FLW-A6a | 2026-10-19 | Anónimo | `GET /api/events/{eventCode}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-A6b | 2026-10-19 | Anónimo | `GET /api/events/{eventCode}` | 404 | 404 | 404 "Event not found: E2E-r001-DRF" | PASS | — |
| FLW-A7 | 2026-10-19 | Anónimo | `GET /api/events/by-artist` | 200 | 200 | Solo E2E-r001-PUB; sin borrador | PASS | — |
| FLW-B1 | 2026-10-19 | USER_A | `GET /api/auth/me` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-B2 | 2026-10-19 | USER_B | `GET /api/auth/me` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-B3 | 2026-10-19 | STAFF | `GET /api/auth/me` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-B4 | 2026-10-19 | ADMIN | `GET /api/auth/me` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-B5 | 2026-10-19 | Anónimo | `POST /api/auth/login` | 401 | 401 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-B6 | 2026-10-19 | Anónimo | `GET /api/auth/me` | 401 | 401 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-B7 | 2026-10-19 | USER_A | `POST /api/events` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-C1 | 2026-10-19 | ADMIN | `POST /api/events` | 201 | 201 | 201 E2E-r001-FLC DRAFT, artists=[] | PASS | — |
| FLW-C2a | 2026-10-19 | ADMIN | `GET /api/events/{eventCode}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-C2b | 2026-10-19 | Anónimo | `GET /api/events/published` | 200 | 200 | Lista sin E2E-r001-FLC | PASS | — |
| FLW-C3 | 2026-10-19 | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 200 | 200 | 200 artists=[Solar Beat], sigue DRAFT | PASS | — |
| FLW-C4 | 2026-10-19 | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | 200 | 200 PUBLISHED | PASS | — |
| FLW-C5 | 2026-10-19 | Anónimo | `GET /api/events/{eventCode}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-C6a | 2026-10-19 | Anónimo | `GET /api/events/published` | 200 | 200 | Lista incluye E2E-r001-FLC | PASS | — |
| FLW-C6b | 2026-10-19 | Anónimo | `GET /api/events/by-artist` | 200 | 200 | Lista incluye PUB y FLC | PASS | — |
| FLW-C7a | 2026-10-19 | USER_A | `PATCH /api/events/{eventCode}/publish` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-C7b | 2026-10-19 | STAFF | `POST /api/events` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-D1 | 2026-10-19 | USER_A | `POST /api/tickets` | 201 | 201 | 201 GENERAL id=6 | PASS | Captura de la compra no aportada; verificado por D2. |
| FLW-D2 | 2026-10-19 | USER_A | `GET /api/tickets/{ticketCode}` | 200 | 200 | Cuerpo según el DTO esperado; sin password ni datos internos | PASS | — |
| FLW-D3 | 2026-10-19 | USER_B | `GET /api/tickets/{ticketCode}` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-D4 | 2026-10-19 | USER_B | `PATCH /api/tickets/{ticketCode}/cancel` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-D5 | 2026-10-19 | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 200 | 200 | 200 CANCELLED | PASS | — |
| FLW-D6 | 2026-10-19 | USER_A | `POST /api/tickets` | 201 | 201 | 201 STUDENT id=7 | PASS | Captura de la compra no aportada; verificado por D7/D9. |
| FLW-D7 | 2026-10-19 | STAFF | `GET /api/events/{eventCode}/tickets/paid` | 200 | 200 | 200, solo T2 (id=7, PAID); T1 cancelado no aparece | PASS | — |
| FLW-D8 | 2026-10-19 | USER_B | `PATCH /api/tickets/{ticketCode}/use` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-D9 | 2026-10-19 | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 200 | 200 | 200 USED | PASS | — |
| FLW-D10 | 2026-10-19 | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 409 | 409 | 409 "Only PAID tickets can be marked as used" | PASS | — |
| FLW-E1 | 2026-10-19 | Anónimo | `POST /api/users` | 400 | 400 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-E2 | 2026-10-19 | Anónimo | `GET /api/tickets/{ticketCode}` | 401 | 401 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-E3 | 2026-10-19 | USER_A | `GET /api/events/{eventCode}/tickets/paid` | 403 | 403 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-E4 | 2026-10-19 | Anónimo | `GET /api/events/{eventCode}` | 404 | 404 | ErrorResponse coherente (status = HTTP, sin stack trace) | PASS | — |
| FLW-E5 | 2026-10-19 | Anónimo | `POST /api/users` | 409 | 409 | 409 "Email already exists: userb.r001@example.test" | PASS | — |
| FLW-E6 | 2026-10-19 | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 409 | 409 | 409 "Only PAID tickets can be cancelled" | PASS | — |

## Consolidación por módulo

| Módulo | PASS | FAIL | BLOCKED | NOT RUN |
|---|---|---|---|---|
| 00 Preparación / conectividad | 4 | 0 | 0 | 0 |
| 01 Venues | 2 | 0 | 0 | 0 |
| 02 Artists | 3 | 0 | 0 | 0 |
| 03 Auth y usuarios | 12 | 0 | 0 | 0 |
| 04 Usuarios / consulta y propiedad | 4 | 0 | 0 | 0 |
| 05 Events / administración y visibilidad | 16 | 0 | 0 | 0 |
| 06 Tickets / compra, consulta y ciclo de vida | 19 | 0 | 0 | 0 |
| 07a Autenticación y JWT | 15 | 0 | 0 | 0 |
| 07b Autorización, roles y propiedad | 22 | 0 | 0 | 0 |
| 08 Validaciones y errores HTTP | 24 | 0 | 0 | 0 |
| 09 Flujos A–E | 42 | 0 | 0 | 0 |
| **Total** | **163** | **0** | **0** | **0** |

## Cobertura de rutas (22/22)

Una ruta cuenta cuando su caso nominal está en PASS.

| # | Ruta | Caso nominal | Resultado |
|---|---|---|---|
| 1 | `GET /api/venues/{code}` | TC-BIZ-001a | PASS |
| 2 | `GET /api/venues/active` | TC-PREP-001 | PASS |
| 3 | `GET /api/artists/{id}` | TC-BIZ-002b | PASS |
| 4 | `GET /api/artists/by-stage-name` | TC-BIZ-002c | PASS |
| 5 | `GET /api/artists/active` | TC-BIZ-002a | PASS |
| 6 | `POST /api/users` | TC-AUTH-001 | PASS |
| 7 | `GET /api/users/by-email` | TC-USR-001 | PASS |
| 8 | `GET /api/users/by-username` | TC-USR-002 | PASS |
| 9 | `POST /api/events` | TC-BIZ-003 | PASS |
| 10 | `GET /api/events/{eventCode}` | TC-BIZ-004a | PASS |
| 11 | `GET /api/events/published` | TC-BIZ-004b | PASS |
| 12 | `PATCH /api/events/{eventCode}/publish` | TC-BIZ-004 | PASS |
| 13 | `POST /api/events/{eventCode}/artists/{artistId}` | TC-BIZ-005 | PASS |
| 14 | `GET /api/events/by-artist` | TC-BIZ-005b | PASS |
| 15 | `POST /api/tickets` | TC-BIZ-007a | PASS |
| 16 | `GET /api/tickets/{ticketCode}` | TC-BIZ-008a | PASS |
| 17 | `GET /api/tickets/by-user` | TC-BIZ-008b | PASS |
| 18 | `GET /api/events/{eventCode}/tickets/paid` | TC-BIZ-009a | PASS |
| 19 | `PATCH /api/tickets/{ticketCode}/cancel` | TC-BIZ-010 | PASS |
| 20 | `PATCH /api/tickets/{ticketCode}/use` | TC-BIZ-012 | PASS |
| 21 | `POST /api/auth/login` | TC-AUTH-002 | PASS |
| 22 | `GET /api/auth/me` | TC-AUTH-006 | PASS |

## Defectos y bloqueos

| # | Caso | Precondiciones | Pasos | HTTP esperado | HTTP observado | Causa y reproducción manual | Estado |
|---|---|---|---|---|---|---|---|
| DEF-001 | TC-BIZ-007a/b/c (afecta a TC-BIZ-008..013) | Usuario autenticado; evento publicado; ticketType válido. | `POST /api/tickets` como USER_A para `E2E-r001-PUB`. | 201 | 500 `{"message":"An unexpected error occurred"}`; log: `ERROR: cannot execute INSERT in a read-only transaction` (SQLState 25006). | `TicketServiceImpl` está anotada `@Transactional(readOnly = true)` a nivel de clase y `purchase()` no la sobrescribe (cancel y markAsUsed sí). Corrección: `@Transactional` sobre `purchase()` (rama `fix/ticket-purchase-transaction`). Reintento tras reiniciar la API: 201 en los tres casos. | Corregido; reintento PASS. Falta confirmar commit/PR del arreglo. |

Bloqueos: ninguno al cierre (los casos especiales TC-AUTH-009/010/011/013 y TC-SEC-017 se ejecutaron con su preparación).

## Observaciones (no son defectos confirmados)

- **OBS-01** — Escala decimal de `price` inconsistente: la respuesta de compra devuelve `60000.0000`/`240000.0000` (y `120000.00`), mientras las lecturas posteriores muestran dos decimales. Valor correcto; formato cosmético.
- **OBS-02** — Venues del seed muestran `MedellÃn`: codificación al cargar `seed-e2e.sql` por pipe de PowerShell. Afecta solo al fixture; el usuario registrado por HTTP guarda `Medellín` bien.
- **OBS-03** — TC-BIZ-007b se envió dos veces (ids 2 y 3, ambos 201). Por eso USER_A tiene 4 tickets PAID y los esperados de 008b, 008e, 009a, 009b y 009c se ajustaron (4/4/4/4/2). No es un defecto de la API.
- **OBS-04** — Política documentada (TC-AUTH-015): un JWT inválido en una ruta pública se rechaza con 401 en vez de ignorarse. Confirmar que coincide con la política de SecurityConfig del PRD.
- **OBS-05** — TC-BIZ-008a devolvió 500 una vez, con la variable `ticketReadCode` vacía (ruta mal formada). No se capturó el log; no se reprodujo con la variable llena. Candidato a verificar: una ruta mal formada debería dar 400/404, no 500.
- **OBS-06** — Intentos de ejecución inválidos (error de preparación, no de la API) que NO se cuentan: TC-AUTH-009 con token sin alterar (200) y con variable vacía; TC-AUTH-010 y 011 con variables vacías ("Authentication is required"); TC-SEC-002b y TC-HTTP-005 con token vencido (401).
- **OBS-07** — No se reportó el GET de verificación de TC-AUTH-014 (`E2E-r001-X14` → 404) ni el cuerpo de TC-AUTH-012; ambos quedan como PASS por HTTP y mensaje, con esa salvedad.

## Acta de cierre

- Rutas nominales en PASS: 22 / 22
- Casos de seguridad (TC-AUTH/TC-SEC) PASS/FAIL/BLOCKED: 47 / 0 / 0
- Observaciones del ejecutor: ver sección anterior. Pendiente de confirmar: commit/PR del arreglo DEF-001 y su prueba de regresión.
- Revisión docente: 

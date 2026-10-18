# Registro de ejecución manual — PulsePass E2E

Diligenciar **después de cada Send**. Estados permitidos: `PASS`, `FAIL`, `BLOCKED`, `NOT RUN`. No pegar JWT, passwords ni capturas con datos privados. Lo no ejecutado permanece `NOT RUN`; nunca se marca como aprobado.

- Ejecutor: ____________________  - Fecha: ____________  - runId: ________  - Commit probado: ________
- Ambiente (BD / puerto): ____________________

## Resultados por caso

| Caso | Fecha/hora | Actor | Ruta | HTTP esperado | HTTP observado | JSON / estado observado | Resultado | Evidencia / observación |
|------|-----------|-------|------|---------------|----------------|-------------------------|-----------|-------------------------|
| TC-PREP-001 | pendiente | Anónimo | `GET /api/venues/active` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-PREP-002 | pendiente | Anónimo | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-PREP-003 | pendiente | Anónimo | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-PREP-004 | pendiente | Anónimo | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-001a | pendiente | Anónimo | `GET /api/venues/{code}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-001b | pendiente | Anónimo | `GET /api/venues/active` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-002a | pendiente | Anónimo | `GET /api/artists/active` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-002b | pendiente | Anónimo | `GET /api/artists/{id}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-002c | pendiente | Anónimo | `GET /api/artists/by-stage-name` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-001 | pendiente | Anónimo | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-006a | pendiente | Anónimo | `POST /api/users` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-006b | pendiente | Anónimo | `POST /api/users` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-002 | pendiente | Anónimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-002b | pendiente | Anónimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-002c | pendiente | Anónimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-002d | pendiente | Anónimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-003 | pendiente | Anónimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-006 | pendiente | USER_A | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-006b | pendiente | USER_B | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-006c | pendiente | STAFF | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-006d | pendiente | ADMIN | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-USR-001 | pendiente | USER_A | `GET /api/users/by-email` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-USR-002 | pendiente | USER_A | `GET /api/users/by-username` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-USR-003 | pendiente | ADMIN | `GET /api/users/by-email` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-USR-004 | pendiente | ADMIN | `GET /api/users/by-username` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-003 | pendiente | ADMIN | `POST /api/events` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-003b | pendiente | ADMIN | `POST /api/events` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-003c | pendiente | ADMIN | `POST /api/events` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-003d | pendiente | ADMIN | `POST /api/events` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-003e | pendiente | ADMIN | `POST /api/events` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-003f | pendiente | ADMIN | `POST /api/events` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-005 | pendiente | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-005c | pendiente | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004s1 | pendiente | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004s2 | pendiente | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004s3 | pendiente | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004a | pendiente | Anónimo | `GET /api/events/{eventCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004b | pendiente | Anónimo | `GET /api/events/published` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-005b | pendiente | Anónimo | `GET /api/events/by-artist` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004 | pendiente | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004c | pendiente | Anónimo | `GET /api/events/published` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-007a | pendiente | USER_A | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-007b | pendiente | USER_A | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-007c | pendiente | USER_A | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-008a | pendiente | USER_A | `GET /api/tickets/{ticketCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-008b | pendiente | USER_A | `GET /api/tickets/by-user` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-008c | pendiente | STAFF | `GET /api/tickets/{ticketCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-008d | pendiente | ADMIN | `GET /api/tickets/{ticketCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-008e | pendiente | ADMIN | `GET /api/tickets/by-user` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-009a | pendiente | STAFF | `GET /api/events/{eventCode}/tickets/paid` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-009b | pendiente | ADMIN | `GET /api/events/{eventCode}/tickets/paid` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-010 | pendiente | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-010b | pendiente | USER_A | `GET /api/tickets/{ticketCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-011 | pendiente | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-011b | pendiente | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-012 | pendiente | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-012b | pendiente | STAFF | `GET /api/tickets/{ticketCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-013 | pendiente | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-013b | pendiente | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-009c | pendiente | STAFF | `GET /api/events/{eventCode}/tickets/paid` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-004 | pendiente | Anónimo | `POST /api/auth/login` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-005 | pendiente | Anónimo | `POST /api/auth/login` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-007 | pendiente | Anónimo | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-008 | pendiente | Token inválido/malformado | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-009 | pendiente | Token inválido/malformado | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-010 | pendiente | Token inválido/malformado | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-011 | pendiente | Token inválido/malformado | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-012 | pendiente | Anónimo | `GET /api/users/by-email` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-013a | pendiente | Anónimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-013 | pendiente | USER_INACTIVO | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-014 | pendiente | USER_A | `POST /api/events` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-AUTH-015 | pendiente | Token inválido/malformado | `GET /api/venues/active` | 200 o 401 (política) | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-016 | pendiente | Anónimo | `POST /api/users` | 201 (campo ignorado) o 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-016b | pendiente | Anónimo | `POST /api/auth/login` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-016c | pendiente | USER_INYECTADO | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-001a | pendiente | Anónimo | `POST /api/events` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-001b | pendiente | USER_A | `POST /api/events` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-002a | pendiente | USER_A | `PATCH /api/events/{eventCode}/publish` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-002b | pendiente | STAFF | `PATCH /api/events/{eventCode}/publish` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-003 | pendiente | USER_A | `POST /api/events/{eventCode}/artists/{artistId}` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-002c | pendiente | ADMIN | `GET /api/events/{eventCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-004 | pendiente | Anónimo | `GET /api/events/{eventCode}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-005 | pendiente | Anónimo | `GET /api/events/by-artist` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-006 | pendiente | USER_B | `GET /api/users/by-email` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-007 | pendiente | USER_B | `GET /api/users/by-username` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-008 | pendiente | USER_A | `POST /api/tickets` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-008b | pendiente | ADMIN | `GET /api/tickets/by-user` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-009 | pendiente | USER_B | `GET /api/tickets/{ticketCode}` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-010 | pendiente | USER_B | `GET /api/tickets/by-user` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-011 | pendiente | USER_B | `PATCH /api/tickets/{ticketCode}/cancel` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-011b | pendiente | USER_A | `GET /api/tickets/{ticketCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-012 | pendiente | USER_B | `PATCH /api/tickets/{ticketCode}/use` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-012b | pendiente | USER_A | `GET /api/tickets/{ticketCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-013 | pendiente | USER_A | `GET /api/events/{eventCode}/tickets/paid` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-013b | pendiente | USER_A | `PATCH /api/tickets/{ticketCode}/use` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-013c | pendiente | STAFF | `POST /api/tickets` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-SEC-017 | pendiente | STAFF | `GET /api/events/{eventCode}/tickets/paid` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-001 | pendiente | Anónimo | `POST /api/users` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-002 | pendiente | Anónimo | `POST /api/users` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-003 | pendiente | Anónimo | `POST /api/users` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-004 | pendiente | Anónimo | `POST /api/auth/login` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-005 | pendiente | ADMIN | `POST /api/events` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-006 | pendiente | USER_A | `POST /api/tickets` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| TC-HTTP-007 | pendiente | Anónimo | `GET /api/venues/{code}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015a | pendiente | Anónimo | `GET /api/venues/{code}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015b | pendiente | Anónimo | `GET /api/artists/{id}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015c | pendiente | Anónimo | `GET /api/events/{eventCode}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015d | pendiente | ADMIN | `GET /api/tickets/{ticketCode}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015e | pendiente | ADMIN | `GET /api/events/{eventCode}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015f | pendiente | ADMIN | `POST /api/events` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015g | pendiente | ADMIN | `POST /api/events` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015h | pendiente | ADMIN | `POST /api/events` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015i | pendiente | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015j | pendiente | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-004d | pendiente | ADMIN | `PATCH /api/events/{eventCode}/publish` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-015k | pendiente | ADMIN | `PATCH /api/events/{eventCode}/publish` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014a | pendiente | USER_A | `POST /api/tickets` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014b | pendiente | USER_A | `POST /api/tickets` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014c1 | pendiente | USER_A | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014c | pendiente | USER_B | `POST /api/tickets` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| TC-BIZ-014d | pendiente | USER_A | `POST /api/tickets` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-A1 | pendiente | Anónimo | `GET /api/venues/active` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-A2 | pendiente | Anónimo | `GET /api/venues/{code}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-A3 | pendiente | Anónimo | `GET /api/artists/active` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-A4a | pendiente | Anónimo | `GET /api/artists/{id}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-A4b | pendiente | Anónimo | `GET /api/artists/by-stage-name` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-A5 | pendiente | Anónimo | `GET /api/events/published` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-A6a | pendiente | Anónimo | `GET /api/events/{eventCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-A6b | pendiente | Anónimo | `GET /api/events/{eventCode}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-A7 | pendiente | Anónimo | `GET /api/events/by-artist` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-B1 | pendiente | USER_A | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-B2 | pendiente | USER_B | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-B3 | pendiente | STAFF | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-B4 | pendiente | ADMIN | `GET /api/auth/me` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-B5 | pendiente | Anónimo | `POST /api/auth/login` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-B6 | pendiente | Anónimo | `GET /api/auth/me` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-B7 | pendiente | USER_A | `POST /api/events` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C1 | pendiente | ADMIN | `POST /api/events` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C2a | pendiente | ADMIN | `GET /api/events/{eventCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C2b | pendiente | Anónimo | `GET /api/events/published` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C3 | pendiente | ADMIN | `POST /api/events/{eventCode}/artists/{artistId}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C4 | pendiente | ADMIN | `PATCH /api/events/{eventCode}/publish` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C5 | pendiente | Anónimo | `GET /api/events/{eventCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C6a | pendiente | Anónimo | `GET /api/events/published` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C6b | pendiente | Anónimo | `GET /api/events/by-artist` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C7a | pendiente | USER_A | `PATCH /api/events/{eventCode}/publish` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-C7b | pendiente | STAFF | `POST /api/events` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D1 | pendiente | USER_A | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D2 | pendiente | USER_A | `GET /api/tickets/{ticketCode}` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D3 | pendiente | USER_B | `GET /api/tickets/{ticketCode}` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D4 | pendiente | USER_B | `PATCH /api/tickets/{ticketCode}/cancel` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D5 | pendiente | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D6 | pendiente | USER_A | `POST /api/tickets` | 201 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D7 | pendiente | STAFF | `GET /api/events/{eventCode}/tickets/paid` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D8 | pendiente | USER_B | `PATCH /api/tickets/{ticketCode}/use` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D9 | pendiente | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 200 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-D10 | pendiente | STAFF | `PATCH /api/tickets/{ticketCode}/use` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-E1 | pendiente | Anónimo | `POST /api/users` | 400 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-E2 | pendiente | Anónimo | `GET /api/tickets/{ticketCode}` | 401 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-E3 | pendiente | USER_A | `GET /api/events/{eventCode}/tickets/paid` | 403 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-E4 | pendiente | Anónimo | `GET /api/events/{eventCode}` | 404 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-E5 | pendiente | Anónimo | `POST /api/users` | 409 | pendiente | pendiente | NOT RUN | pendiente |
| FLW-E6 | pendiente | USER_A | `PATCH /api/tickets/{ticketCode}/cancel` | 409 | pendiente | pendiente | NOT RUN | pendiente |

## Consolidación por módulo

| Módulo | PASS | FAIL | BLOCKED | NOT RUN |
|---|---|---|---|---|
| 00 Preparación | | | | |
| 01 Venues | | | | |
| 02 Artists | | | | |
| 03 Auth y usuarios | | | | |
| 04 Usuarios | | | | |
| 05 Events | | | | |
| 06 Tickets | | | | |
| 07a Autenticación y JWT | | | | |
| 07b Autorización/IDOR | | | | |
| 08 Validaciones y errores | | | | |
| 09 Flujos A–E | | | | |

## Cobertura de rutas (22/22)

Marcar la ruta cuando su caso nominal esté en PASS.

| # | Ruta | Caso nominal | Resultado |
|---|---|---|---|
| 1 | `GET /api/venues/{code}` | TC-BIZ-001a | NOT RUN |
| 2 | `GET /api/venues/active` | TC-PREP-001 | NOT RUN |
| 3 | `GET /api/artists/{id}` | TC-BIZ-002b | NOT RUN |
| 4 | `GET /api/artists/by-stage-name` | TC-BIZ-002c | NOT RUN |
| 5 | `GET /api/artists/active` | TC-BIZ-002a | NOT RUN |
| 6 | `POST /api/users` | TC-AUTH-001 | NOT RUN |
| 7 | `GET /api/users/by-email` | TC-USR-001 | NOT RUN |
| 8 | `GET /api/users/by-username` | TC-USR-002 | NOT RUN |
| 9 | `POST /api/events` | TC-BIZ-003 | NOT RUN |
| 10 | `GET /api/events/{eventCode}` | TC-BIZ-004a | NOT RUN |
| 11 | `GET /api/events/published` | TC-BIZ-004b | NOT RUN |
| 12 | `PATCH /api/events/{eventCode}/publish` | TC-BIZ-004 | NOT RUN |
| 13 | `POST /api/events/{eventCode}/artists/{artistId}` | TC-BIZ-005 | NOT RUN |
| 14 | `GET /api/events/by-artist` | TC-BIZ-005b | NOT RUN |
| 15 | `POST /api/tickets` | TC-BIZ-007a | NOT RUN |
| 16 | `GET /api/tickets/{ticketCode}` | TC-BIZ-008a | NOT RUN |
| 17 | `GET /api/tickets/by-user` | TC-BIZ-008b | NOT RUN |
| 18 | `GET /api/events/{eventCode}/tickets/paid` | TC-BIZ-009a | NOT RUN |
| 19 | `PATCH /api/tickets/{ticketCode}/cancel` | TC-BIZ-010 | NOT RUN |
| 20 | `PATCH /api/tickets/{ticketCode}/use` | TC-BIZ-012 | NOT RUN |
| 21 | `POST /api/auth/login` | TC-AUTH-002 | NOT RUN |
| 22 | `GET /api/auth/me` | TC-AUTH-006 | NOT RUN |

## Defectos y bloqueos

| # | Caso | Precondiciones | Pasos | HTTP esperado | HTTP observado | Reproducción manual | Estado |
|---|---|---|---|---|---|---|---|
| | | | | | | | |

## Acta de cierre

- Rutas nominales en PASS: __ / 22
- Casos de seguridad (TC-AUTH/TC-SEC) PASS/FAIL/BLOCKED: __ / __ / __
- Observaciones del ejecutor: 
- Revisión docente: 

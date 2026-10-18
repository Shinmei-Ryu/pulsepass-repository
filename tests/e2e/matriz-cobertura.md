# Matriz de cobertura: 22 rutas × actores

Cada celda lista los casos de la colección que ejercitan esa ruta con ese actor (sin contar los flujos A–E de la carpeta 09, que repiten escenarios). La columna **Caso nominal** indica el caso que prueba la ruta en su camino feliz. Estado de la ejecución real: ver `registro-ejecucion-manual.md`.

| # | Ruta | Caso nominal | Anónimo | Token inválido/malformado | USER propio | USER ajeno | STAFF | ADMIN |
|---|------|--------------|---|---|---|---|---|---|
| 1 | `GET /api/venues/{code}` | TC-BIZ-001a | TC-BIZ-001a<br>TC-HTTP-007<br>TC-BIZ-015a | — | — | — | — | — |
| 2 | `GET /api/venues/active` | TC-PREP-001 | TC-PREP-001<br>TC-BIZ-001b | TC-AUTH-015 | — | — | — | — |
| 3 | `GET /api/artists/{id}` | TC-BIZ-002b | TC-BIZ-002b<br>TC-BIZ-015b | — | — | — | — | — |
| 4 | `GET /api/artists/by-stage-name` | TC-BIZ-002c | TC-BIZ-002c | — | — | — | — | — |
| 5 | `GET /api/artists/active` | TC-BIZ-002a | TC-BIZ-002a | — | — | — | — | — |
| 6 | `POST /api/users` | TC-AUTH-001 | TC-PREP-002<br>TC-PREP-003<br>TC-PREP-004<br>TC-AUTH-001<br>TC-BIZ-006a<br>TC-BIZ-006b<br>TC-SEC-016<br>TC-HTTP-001<br>TC-HTTP-002<br>TC-HTTP-003 | — | — | — | — | — |
| 7 | `GET /api/users/by-email` | TC-USR-001 | TC-AUTH-012 | — | TC-USR-001 | TC-SEC-006 | — | TC-USR-003 |
| 8 | `GET /api/users/by-username` | TC-USR-002 | — | — | TC-USR-002 | TC-SEC-007 | — | TC-USR-004 |
| 9 | `POST /api/events` | TC-BIZ-003 | TC-SEC-001a | — | TC-AUTH-014<br>TC-SEC-001b | — | — | TC-BIZ-003<br>TC-BIZ-003b<br>TC-BIZ-003c<br>TC-BIZ-003d<br>TC-BIZ-003e<br>TC-BIZ-003f<br>TC-HTTP-005<br>TC-BIZ-015f<br>TC-BIZ-015g<br>TC-BIZ-015h |
| 10 | `GET /api/events/{eventCode}` | TC-BIZ-004a | TC-BIZ-004a<br>TC-SEC-004<br>TC-BIZ-015c | — | — | — | — | TC-SEC-002c<br>TC-BIZ-015e |
| 11 | `GET /api/events/published` | TC-BIZ-004b | TC-BIZ-004b<br>TC-BIZ-004c | — | — | — | — | — |
| 12 | `PATCH /api/events/{eventCode}/publish` | TC-BIZ-004 | — | — | TC-SEC-002a | — | TC-SEC-002b | TC-BIZ-004s1<br>TC-BIZ-004s2<br>TC-BIZ-004s3<br>TC-BIZ-004<br>TC-BIZ-004d<br>TC-BIZ-015k |
| 13 | `POST /api/events/{eventCode}/artists/{artistId}` | TC-BIZ-005 | — | — | TC-SEC-003 | — | — | TC-BIZ-005<br>TC-BIZ-005c<br>TC-BIZ-015i<br>TC-BIZ-015j |
| 14 | `GET /api/events/by-artist` | TC-BIZ-005b | TC-BIZ-005b<br>TC-SEC-005 | — | — | — | — | — |
| 15 | `POST /api/tickets` | TC-BIZ-007a | — | — | TC-BIZ-007a<br>TC-BIZ-007b<br>TC-BIZ-007c<br>TC-HTTP-006<br>TC-BIZ-014a<br>TC-BIZ-014b<br>TC-BIZ-014c1<br>TC-BIZ-014c<br>TC-BIZ-014d | TC-SEC-008 | TC-SEC-013c | — |
| 16 | `GET /api/tickets/{ticketCode}` | TC-BIZ-008a | — | — | TC-BIZ-008a<br>TC-BIZ-010b<br>TC-SEC-011b<br>TC-SEC-012b | TC-SEC-009 | TC-BIZ-008c<br>TC-BIZ-012b | TC-BIZ-008d<br>TC-BIZ-015d |
| 17 | `GET /api/tickets/by-user` | TC-BIZ-008b | — | — | TC-BIZ-008b | TC-SEC-010 | — | TC-BIZ-008e<br>TC-SEC-008b |
| 18 | `GET /api/events/{eventCode}/tickets/paid` | TC-BIZ-009a | — | — | TC-SEC-013 | — | TC-BIZ-009a<br>TC-BIZ-009c<br>TC-SEC-017 | TC-BIZ-009b |
| 19 | `PATCH /api/tickets/{ticketCode}/cancel` | TC-BIZ-010 | — | — | TC-BIZ-010<br>TC-BIZ-011<br>TC-BIZ-013b | TC-SEC-011 | — | — |
| 20 | `PATCH /api/tickets/{ticketCode}/use` | TC-BIZ-012 | — | — | TC-SEC-013b | TC-SEC-012 | TC-BIZ-011b<br>TC-BIZ-012<br>TC-BIZ-013 | — |
| 21 | `POST /api/auth/login` | TC-AUTH-002 | TC-AUTH-002<br>TC-AUTH-002b<br>TC-AUTH-002c<br>TC-AUTH-002d<br>TC-AUTH-003<br>TC-AUTH-004<br>TC-AUTH-005<br>TC-AUTH-013a<br>TC-SEC-016b<br>TC-HTTP-004 | — | — | — | — | — |
| 22 | `GET /api/auth/me` | TC-AUTH-006 | TC-AUTH-007 | TC-AUTH-008<br>TC-AUTH-009<br>TC-AUTH-010<br>TC-AUTH-011 | TC-AUTH-006<br>TC-AUTH-006b<br>TC-AUTH-013<br>TC-SEC-016c | — | TC-AUTH-006c | TC-AUTH-006d |

## Decisiones de política documentadas

- Recurso ajeno (usuario o ticket de otro): **403** (`expectedForeignResourceStatus = 403`). No se acepta 404 indistintamente.
- Evento en **DRAFT** consultado por visitante: **404** (se oculta su existencia). ADMIN lo consulta con 200 (TC-SEC-015).
- `POST /api/tickets` es exclusivo del rol USER: STAFF y ADMIN reciben 403 (TC-SEC-013c).
- STAFF tiene alcance global sobre tickets (limitación heredada, no control por evento).
- Reglas de negocio violadas (`BusinessRuleException`) y duplicados: **409**.

## Trazabilidad requisito → caso → ruta

| Requisito | Casos |
|---|---|
| FR-E2E-001/002 (colección importable, 22 rutas) | TC-PREP-001, columna *Caso nominal* de esta matriz |
| FR-E2E-003/004 (logins y /me por actor) | TC-AUTH-002, 002b, 002c, 002d, TC-AUTH-006, 006b, 006c, 006d |
| FR-E2E-005/006 (HTTP/JSON e identificadores copiados) | Campo *Copiar manualmente* en TC-BIZ-002a, TC-BIZ-007a/b/c, FLW-D1, FLW-D6 |
| FR-E2E-007/008 (matriz de acceso, estado antes/después) | TC-SEC-001..017 y sus verificaciones *b* |
| FR-E2E-009 (ErrorResponse) | Todos los casos 4xx; TC-HTTP-001..007 |
| FR-E2E-010/016 (fixtures y repetición) | fixtures/README.md, `runId` |
| FR-E2E-011/012 (checklist y bitácora) | checklist-e2e-manual.md, registro-ejecucion-manual.md |
| FR-E2E-013 (sin tokens ni contraseñas exportados) | Ambiente con secretos vacíos; .gitignore raíz |
| FR-E2E-014/015 (defectos y bloqueos) | Sección *Defectos y bloqueos* de la bitácora |
| AC-01 | TC-AUTH-001, 002, 006 |
| AC-02 | TC-BIZ-003, 005, 004s1, 004a, 005b; FLW-C1..C6 |
| AC-03 | TC-SEC-001b, 002a, 003, TC-AUTH-014 |
| AC-04 | TC-SEC-009, 010, 011, 011b, 012, 012b |
| AC-05 | TC-SEC-008, 008b |
| AC-06 | TC-BIZ-012, 012b, 013 |
| AC-07 | TC-AUTH-008, 009, 010, 011 |
| AC-08 | TC-SEC-004, 005, 015 (vía TC-SEC-002c), FLW-A5/A6b |
| AC-09 | TC-HTTP-001..007 y revisión de ErrorResponse en cada 4xx |
| AC-10 | Bitácora completa con 22/22 rutas nominales |

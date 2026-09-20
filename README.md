# PulsePass - Persistencia con Spring Boot

Plataforma para descubrir eventos y administrar entradas de conciertos, festivales, conferencias y actividades culturales.

## Descripción del Proyecto

PulsePass es un caso de estudio académico enfocado en la capa de persistencia, desarrollado con Java 21, Spring Boot 4.1.1, Spring Data JPA, Hibernate, Flyway y PostgreSQL.

El proyecto implementa la capa de persistencia para gestionar:

- Venues (recintos)
- Eventos y su asociación a un venue
- Artistas y su participación en múltiples eventos
- Usuarios y su perfil individual
- Tickets, con precio, tipo, estado y fecha de compra

## Modelo de Datos

### Entidades Principales

```
Venue  1 ───────── N  Event
Event  N ───────── M  Artist
User   1 ───────── 1  UserProfile
User   1 ───────── N  Ticket
Event  1 ───────── N  Ticket
```

### Tablas

| Tabla | Propósito |
|---|---|
| `venues` | Recintos donde se realizan eventos |
| `events` | Eventos publicados o en preparación |
| `artists` | Catálogo de artistas |
| `event_artists` | Relación N:M eventos-artistas |
| `users` | Usuarios de la plataforma |
| `user_profiles` | Perfil individual de cada usuario |
| `tickets` | Entradas compradas/reservadas |

## Relaciones

**1:N - Venue → Event**
- Un venue puede tener múltiples eventos
- Foreign key: `venue_id`

**N:M - Event ↔ Artist**
- Tabla asociativa: `event_artists`
- Un evento puede tener múltiples artistas
- Un artista puede participar en múltiples eventos
- Lado dueño: `Event` (declara `@JoinTable`)

**1:1 - User → UserProfile**
- Un usuario tiene exactamente un perfil
- Foreign key: `user_id` (UNIQUE), lado dueño: `UserProfile`

**1:N - User → Ticket**
- Un usuario puede tener múltiples tickets
- Foreign key: `user_id`

**1:N - Event → Ticket**
- Un evento puede tener múltiples tickets
- Foreign key: `event_id`

## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA
- Flyway (migraciones)
- Testcontainers (testing)
- PostgreSQL
- Maven

## Estructura del Proyecto

```
pulsepass/
├── pom.xml
├── README.md
├── src/main/java/com/pulse/pass/
│   ├── PulsePassApplication.java
│   ├── domain/
│   │   ├── Venue.java
│   │   ├── Event.java
│   │   ├── EventCategory.java
│   │   ├── EventStatus.java
│   │   ├── Artist.java
│   │   ├── User.java
│   │   ├── UserProfile.java
│   │   ├── Ticket.java
│   │   ├── TicketType.java
│   │   └── TicketStatus.java
│   └── repository/
│       ├── VenueRepository.java
│       ├── EventRepository.java
│       ├── ArtistRepository.java
│       ├── UserRepository.java
│       ├── UserProfileRepository.java
│       └── TicketRepository.java
├── src/main/resources/
│   ├── application.yaml
│   └── db/migration/
│       ├── V1__create_schema.sql
│       ├── V2__insert_initial_artists.sql
│       └── V3__add_streaming_url_to_event.sql
└── src/test/java/com/pulse/pass/persistence/
    ├── FlywayMigrationIT.java
    ├── VenuePersistenceTest.java
    ├── EventRepositoryIT.java
    ├── EventArtistIT.java
    ├── UserProfileIT.java
    ├── TicketRepositoryIT.java
    └── EventSearchIT.java
```

## Configuración

`application.yaml`:

```yaml
spring:
  application:
    name: PulsePass

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/pulsepass}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration
```

`ddl-auto: validate` porque Flyway es responsable de crear y evolucionar el esquema; Hibernate solo valida que las entidades JPA coincidan con la base de datos, evitando inconsistencias entre las migraciones SQL y el código Java.

## Migraciones Flyway

**V1__create_schema.sql** — Crea las 7 tablas (`venues`, `events`, `artists`, `event_artists`, `users`, `user_profiles`, `tickets`) con primary keys, foreign keys, unique constraints, check constraints (`capacity > 0`, categorías/estados válidos, `price >= 0`) e índices.

**V2__insert_initial_artists.sql** — Inserta el catálogo inicial de 5 artistas: Solar Beat, Neon Waves, Caribbean Sound, Ocean Drive, Digital Pulse.

**V3__add_streaming_url_to_event.sql** — Evolución del esquema: agrega la columna opcional `streaming_url` (`VARCHAR(500)`, nullable) a `events` mediante `ALTER TABLE`, sin tocar V1.

Flyway ejecuta los scripts en orden de versión al arrancar el contexto y registra el historial en `flyway_schema_history`, garantizando que el esquema esté siempre sincronizado con el código.

## Testcontainers

Los tests de integración corren contra una instancia **real** de PostgreSQL en Docker, no contra H2 ni mocks. El proyecto usa el patrón de contenedor declarado **directamente dentro de cada clase de test**, sin una configuración compartida:

```java
@Testcontainers
@SpringBootTest
class MiClaseIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    // ...
}
```

`@Container` + `@ServiceConnection` levantan el contenedor y lo conectan automáticamente como datasource de la aplicación, sin necesidad de definir `spring.datasource.*` a mano. Cada una de las 7 clases del paquete `persistence` declara su propio contenedor de esta forma.

## Query Methods Implementados

**VenueRepository**
```java
Optional<Venue> findByCode(String code);
```

**EventRepository**
```java
Optional<Event> findByEventCode(String eventCode);
List<Event> findByStatusOrderByEventDateAsc(EventStatus status);
List<Event> findByVenue_Code(String venueCode);
```

**ArtistRepository**
```java
Optional<Artist> findByStageName(String stageName);
```

**UserRepository**
```java
Optional<User> findByUsername(String username);
Optional<User> findByEmailIgnoreCase(String email);
```

**UserProfileRepository**
```java
Optional<UserProfile> findByUser_Id(Long userId);
```

**TicketRepository**
```java
Optional<Ticket> findByTicketCode(String ticketCode);
List<Ticket> findByUser_Email(String email);
List<Ticket> findByUser_EmailAndStatus(String email, TicketStatus status);
List<Ticket> findByEvent_EventCodeAndStatus(String eventCode, TicketStatus status);
List<Ticket> findByEvent_EventDateAfterOrderByEvent_EventDateAsc(LocalDateTime date);
```

## Consultas JPQL

**Eventos por artista, sin duplicados**
```java
@Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN e.artists a
        WHERE a.stageName = :stageName
        """)
List<Event> findByArtistStageName(String stageName);
```
Uso: recomendaciones y búsqueda por artista (FR-SRC-001).

**Eventos por ciudad y artista**
```java
@Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN e.venue v
        JOIN e.artists a
        WHERE v.city = :city
          AND a.stageName = :stageName
        """)
List<Event> findByCityAndArtistStageName(String city, String stageName);
```
Uso: filtrar eventos combinando venue (ciudad) y artista (FR-SRC-002).

**Eventos recomendados (publicados, futuros, por ciudad y texto de artista)**
```java
@Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN e.venue v
        JOIN e.artists a
        WHERE e.status = com.pulse.pass.domain.EventStatus.PUBLISHED
          AND e.eventDate > :date
          AND v.city = :city
          AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistText, '%'))
        ORDER BY e.eventDate ASC
        """)
List<Event> findRecommendedEvents(LocalDateTime date, String city, String artistText);
```
Uso: motor de recomendaciones con 4 filtros combinados, texto case-insensitive y orden cronológico (FR-SRC-003 / UC-09).

**Conteo de tickets pagados por evento**
```java
@Query("""
        SELECT COUNT(t)
        FROM Ticket t
        JOIN t.event e
        WHERE e.eventCode = :eventCode
          AND t.status = com.pulse.pass.domain.TicketStatus.PAID
        """)
long countPaidByEventCode(String eventCode);
```
Uso: métrica de ventas confirmadas por evento (FR-TKT-008 / AC-008).

## Ejecución

### Requisitos previos
- Java 21 JDK
- Docker (para Testcontainers)
- Maven Wrapper incluido (`./mvnw`)

### Ejecutar tests
```bash
./mvnw clean test
```
Testcontainers levanta un contenedor PostgreSQL efímero (`postgres:18-alpine`) por cada clase de test; no se necesita base de datos configurada manualmente. El `pom.xml` incluye configuración explícita de `maven-surefire-plugin` para que `mvn test` reconozca también las clases con sufijo `IT` (convención reservada por defecto para Failsafe), de modo que las 7 clases de integración corren junto al smoke test con un único comando.

### Ejecutar con PostgreSQL local
```bash
# 1. Levantar PostgreSQL
docker run --name pulsepass-postgres -e POSTGRES_DB=pulsepass \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres:latest

# 2. Configurar variables de entorno
export DB_URL=jdbc:postgresql://localhost:5432/pulsepass
export DB_USER=postgres
export DB_PASSWORD=postgres

# 3. Ejecutar la aplicación
./mvnw spring-boot:run
```

## Cobertura de Pruebas

| Clase | Requisitos cubiertos |
|---|---|
| `FlywayMigrationIT` | NFR-002/003: migraciones aplicadas, catálogo inicial de artistas |
| `VenuePersistenceTest` | FR-VEN-\*, AC-001, QT-003 |
| `EventRepositoryIT` | FR-EVT-\*, FR-VEN-004, AC-002, AC-006 |
| `EventArtistIT` | FR-ART-\*, QT-005, AC-003 |
| `UserProfileIT` | FR-USR-\*, QT-004, AC-004 |
| `TicketRepositoryIT` | FR-TKT-\*, FR-SRC-004, QT-006/007/009, AC-005/008 |
| `EventSearchIT` | FR-SRC-001/002/003, AC-007 |

## Convenciones del Proyecto

- Sin Lombok (getters/setters generados manualmente)
- `BigDecimal` para precios
- `LocalDate` para fechas, `LocalDateTime` para timestamps
- `Enum` para estados y tipos (`EventCategory`, `EventStatus`, `TicketType`, `TicketStatus`)
- Nombres en inglés (convención Spring)
- Métodos helper (`addEvent`, `addArtist`, `assignProfile`) mantienen la bidireccionalidad de las relaciones donde aplica
- Sin `cascade` en las relaciones: cada entidad se persiste explícitamente con su propio repositorio, en el orden correcto (padre antes que hijo)

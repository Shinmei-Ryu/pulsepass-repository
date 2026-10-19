-- ============================================================================
-- PulsePass E2E manual -- fixtures (EXCLUSIVO para la base pulsepass_e2e)
-- Ejecutar DESPUÉS de TC-PREP-002/003/004 (usuarios registrados por la API).
-- Uso (psql): variable run_id = el mismo valor de {{runId}} en Postman.
-- No contiene passwords: las cuentas se crearon por HTTP y ya tienen hash BCrypt.
-- ============================================================================
\set ON_ERROR_STOP on

-- Seguridad: solo se ejecuta contra la base dedicada de E2E.
DO $$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'Abortado: la base actual es %, se esperaba pulsepass_e2e', current_database();
    END IF;
END $$;

-- 1) Venues de prueba (la API solo tiene GET para venues).
INSERT INTO venues (code, name, city, address, capacity, active)
VALUES ('E2EV-'  || :'run_id', 'Venue E2E activo ' || :'run_id',   'Medellín', 'Calle 1 # 1-1', 500, TRUE),
       ('E2EVT-' || :'run_id', 'Venue E2E aforo 1 '  || :'run_id', 'Medellín', 'Calle 2 # 2-2', 1,   TRUE),
       ('E2EVI-' || :'run_id', 'Venue E2E inactivo ' || :'run_id', 'Medellín', 'Calle 3 # 3-3', 100, FALSE);

-- 2) Promoción de roles fuera de HTTP (el registro público solo crea USER).
UPDATE user_roles SET role = 'ADMIN'
 WHERE role = 'USER'
   AND user_id = (SELECT id FROM users WHERE email = 'admin.' || :'run_id' || '@example.test');

UPDATE user_roles SET role = 'STAFF'
 WHERE role = 'USER'
   AND user_id = (SELECT id FROM users WHERE email = 'staff.' || :'run_id' || '@example.test');

-- 3) Verificación: debe mostrar ADMIN y STAFF, y los 3 venues.
SELECT u.username, r.role
  FROM users u JOIN user_roles r ON r.user_id = u.id
 WHERE u.email IN ('admin.' || :'run_id' || '@example.test', 'staff.' || :'run_id' || '@example.test');

SELECT code, capacity, active FROM venues WHERE code LIKE 'E2EV%-' || :'run_id' ORDER BY code;

-- PulsePass E2E -- revoca el rol STAFF para TC-SEC-017 (ejecutar AL FINAL de la corrida).
\set ON_ERROR_STOP on

-- Seguridad: solo se ejecuta contra la base dedicada de E2E.
DO $$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'Abortado: la base actual es %, se esperaba pulsepass_e2e', current_database();
    END IF;
END $$;

UPDATE user_roles SET role = 'USER'
 WHERE role = 'STAFF'
   AND user_id = (SELECT id FROM users WHERE email = 'staff.' || :'run_id' || '@example.test');

SELECT u.username, r.role
  FROM users u JOIN user_roles r ON r.user_id = u.id
 WHERE u.email = 'staff.' || :'run_id' || '@example.test';

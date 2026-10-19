-- PulsePass E2E -- desactiva al usuario de TC-AUTH-013 (después de copiar su token).
\set ON_ERROR_STOP on

-- Seguridad: solo se ejecuta contra la base dedicada de E2E.
DO $$
BEGIN
    IF current_database() <> 'pulsepass_e2e' THEN
        RAISE EXCEPTION 'Abortado: la base actual es %, se esperaba pulsepass_e2e', current_database();
    END IF;
END $$;

UPDATE users SET active = FALSE
 WHERE email = 'inactive.' || :'run_id' || '@example.test';

SELECT username, active FROM users WHERE email = 'inactive.' || :'run_id' || '@example.test';

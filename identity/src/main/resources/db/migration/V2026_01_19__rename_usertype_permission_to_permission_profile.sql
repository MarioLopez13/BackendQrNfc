-- Migración: Renombrar user_type_permission a permission_profile
-- Cambiar campo user_type (ENUM) a profile_name (VARCHAR)

-- 1. Renombrar la tabla
ALTER TABLE identity.user_type_permission RENAME TO permission_profile;

-- 2. Renombrar la columna user_type a profile_name y cambiar tipo a VARCHAR
ALTER TABLE identity.permission_profile
    ALTER COLUMN user_type TYPE VARCHAR(100) USING user_type::VARCHAR;

ALTER TABLE identity.permission_profile
    RENAME COLUMN user_type TO profile_name;

-- 3. Crear índice para búsquedas por profile_name
CREATE INDEX IF NOT EXISTS idx_permission_profile_name
    ON identity.permission_profile(profile_name);

-- 4. Crear índice compuesto para evitar duplicados
CREATE UNIQUE INDEX IF NOT EXISTS idx_permission_profile_unique
    ON identity.permission_profile(profile_name, permission_id);

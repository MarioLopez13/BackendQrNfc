-- Índices para optimizar UserMe queries
-- Ejecutar en la base de datos de identity

-- Índice para búsqueda de permisos por usuario
CREATE INDEX IF NOT EXISTS idx_user_permission_business_user_id
    ON user_permission_business(user_id);

-- Índice compuesto para búsqueda de permisos por usuario y negocio
CREATE INDEX IF NOT EXISTS idx_user_permission_business_user_business
    ON user_permission_business(user_id, business_id);

-- Índice para búsqueda de usuario por keycloak_id (si no existe)
CREATE INDEX IF NOT EXISTS idx_user_system_keycloak_id
    ON user_system(key_cloak_id);

-- Índice para negocios activos
CREATE INDEX IF NOT EXISTS idx_business_status
    ON business(status);

-- Índice compuesto para business_module
CREATE INDEX IF NOT EXISTS idx_business_module_business_id
    ON business_module(business_id);

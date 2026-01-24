-- Migration: Create usuarios table for authentication and authorization
-- Version: V14
-- Description: Creates the usuarios table for user management and security

CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(200) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Add unique constraint on email
CREATE UNIQUE INDEX IF NOT EXISTS uq_usuarios_email ON usuarios(email);

-- Add index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- Add comments
COMMENT ON TABLE usuarios IS 'Tabla de usuarios para autenticación y autorización';
COMMENT ON COLUMN usuarios.id IS 'ID único del usuario';
COMMENT ON COLUMN usuarios.nombre_completo IS 'Nombre completo del usuario';
COMMENT ON COLUMN usuarios.email IS 'Email del usuario (único)';
COMMENT ON COLUMN usuarios.password IS 'Contraseña hasheada del usuario';
COMMENT ON COLUMN usuarios.rol IS 'Rol del usuario (ADMIN, GESTOR, VISOR, etc.)';
COMMENT ON COLUMN usuarios.activo IS 'Indica si el usuario está activo';

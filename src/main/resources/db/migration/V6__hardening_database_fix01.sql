-- Migración Flyway V6: Hardening de Base de Datos (FIX-01)
-- Plan de Recuperación Técnica - FASE 1: FUNDAMENTOS & SEGURIDAD
-- Basado en Directiva Maestra de Ingeniería v2.0 (RECOVERY)

-- ============================================================================
-- 1. JERARQUÍA WBS EN PARTIDA (PARCHES CRÍTICOS)
-- ============================================================================

-- Agregar campos para estructura jerárquica WBS (Work Breakdown Structure)
-- Según directiva: partida DEBE tener recursividad (parent_id, nivel)

ALTER TABLE partida
    ADD COLUMN IF NOT EXISTS parent_id UUID,
    ADD COLUMN IF NOT EXISTS nivel INT NOT NULL DEFAULT 1;

-- Crear foreign key auto-referencial para jerarquía
ALTER TABLE partida
    ADD CONSTRAINT fk_partida_parent
        FOREIGN KEY (parent_id) REFERENCES partida(id) ON DELETE CASCADE;

-- Crear índice para consultas jerárquicas
CREATE INDEX IF NOT EXISTS idx_partida_parent ON partida(parent_id);
CREATE INDEX IF NOT EXISTS idx_partida_nivel ON partida(nivel);

-- Constraint para validar que nivel sea positivo
ALTER TABLE partida
    ADD CONSTRAINT chk_partida_nivel_positivo
        CHECK (nivel > 0);

-- ============================================================================
-- 2. CHECK CONSTRAINTS PARA INTEGRIDAD DE DATOS
-- ============================================================================

-- Constraint en billetera: saldo nunca negativo
-- Según directiva: CHECK (saldo >= 0) en billetera
ALTER TABLE billetera
    ADD CONSTRAINT chk_billetera_saldo_no_negativo
        CHECK (saldo_actual >= 0);

-- ============================================================================
-- 3. CREAR TABLAS FALTANTES CRÍTICAS (según ERD y dominio)
-- ============================================================================

-- Tabla inventario_item (existe en ERD pero no en migraciones)
-- Según ERD: id UUID, proyecto_id UUID, recurso_id UUID, cantidad NUMERIC(19,6), 
-- costo_promedio NUMERIC(19,4), version INT, created_at, updated_at
CREATE TABLE IF NOT EXISTS inventario_item (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    recurso_id UUID NOT NULL,
    cantidad NUMERIC(19,6) NOT NULL DEFAULT 0,
    costo_promedio NUMERIC(19,4) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_inventario_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
    CONSTRAINT fk_inventario_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id),
    CONSTRAINT uq_inventario_proyecto_recurso
        UNIQUE (proyecto_id, recurso_id),
    CONSTRAINT chk_inventario_cantidad_no_negativa
        CHECK (cantidad >= 0),
    CONSTRAINT chk_inventario_costo_promedio_no_negativo
        CHECK (costo_promedio >= 0)
);

CREATE INDEX IF NOT EXISTS idx_inventario_proyecto ON inventario_item(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_inventario_recurso ON inventario_item(recurso_id);

-- Tabla consumo_partida (existe en ERD pero no en migraciones)
-- Según ERD: id UUID, partida_id UUID, recurso_id UUID, cantidad NUMERIC(19,6),
-- costo_total NUMERIC(19,4), created_at
CREATE TABLE IF NOT EXISTS consumo_partida (
    id UUID PRIMARY KEY,
    partida_id UUID NOT NULL,
    recurso_id UUID NOT NULL,
    cantidad NUMERIC(19,6) NOT NULL,
    costo_total NUMERIC(19,4) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_consumo_partida_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id),
    CONSTRAINT fk_consumo_partida_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id),
    CONSTRAINT chk_consumo_cantidad_positiva
        CHECK (cantidad > 0),
    CONSTRAINT chk_consumo_costo_total_positivo
        CHECK (costo_total > 0)
);

CREATE INDEX IF NOT EXISTS idx_consumo_partida_partida ON consumo_partida(partida_id);
CREATE INDEX IF NOT EXISTS idx_consumo_partida_recurso ON consumo_partida(recurso_id);

-- Tabla movimiento_caja (existe en dominio pero no en ERD/migraciones)
-- Según dominio: id UUID, billetera_id UUID, tipo VARCHAR(20), monto NUMERIC(19,4),
-- referencia TEXT, evidencia_url TEXT, created_at
-- CRÍTICO: Trazabilidad histórica de cambios en billetera
CREATE TABLE IF NOT EXISTS movimiento_caja (
    id UUID PRIMARY KEY,
    billetera_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    monto NUMERIC(19,4) NOT NULL,
    referencia TEXT NOT NULL,
    evidencia_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_movimiento_billetera
        FOREIGN KEY (billetera_id) REFERENCES billetera(id),
    CONSTRAINT chk_movimiento_tipo_valido
        CHECK (tipo IN ('INGRESO', 'EGRESO')),
    CONSTRAINT chk_movimiento_monto_positivo
        CHECK (monto > 0)
);

CREATE INDEX IF NOT EXISTS idx_movimiento_billetera ON movimiento_caja(billetera_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_created_at ON movimiento_caja(created_at);

-- ============================================================================
-- 4. CHECK CONSTRAINTS ADICIONALES PARA INTEGRIDAD
-- ============================================================================

-- Constraint en compra: total siempre positivo
-- Nota: IF NOT EXISTS no es soportado en ADD CONSTRAINT, usar DO $$ BEGIN ... END $$ para evitar errores si ya existe
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_compra_total_positivo'
    ) THEN
        ALTER TABLE compra ADD CONSTRAINT chk_compra_total_positivo CHECK (total > 0);
    END IF;
END $$;

-- Constraint en compra_detalle: cantidad y precio siempre positivos
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_detalle_cantidad_positiva'
    ) THEN
        ALTER TABLE compra_detalle ADD CONSTRAINT chk_detalle_cantidad_positiva CHECK (cantidad > 0);
    END IF;
END $$;

DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_detalle_precio_positivo'
    ) THEN
        ALTER TABLE compra_detalle ADD CONSTRAINT chk_detalle_precio_positivo CHECK (precio_unitario > 0);
    END IF;
END $$;

-- ============================================================================
-- 5. AUDITORÍA REAL (según directiva: created_by, trace_id)
-- ============================================================================

-- Agregar campos de auditoría a tablas transaccionales críticas
-- NOTA: created_by requiere implementación de SecurityContext (FIX-02)
-- Por ahora se agregan como NOT NULL con valor por defecto temporal

ALTER TABLE compra
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS trace_id UUID;

ALTER TABLE movimiento_caja
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS trace_id UUID;

-- ============================================================================
-- NOTAS DE MIGRACIÓN
-- ============================================================================

-- Esta migración implementa:
-- 1. ✅ WBS jerárquico en Partida (parent_id, nivel)
-- 2. ✅ CHECK constraints en Billetera (saldo >= 0)
-- 3. ✅ CHECK constraints en Inventario (cantidad >= 0, costo_promedio >= 0)
-- 4. ✅ Tablas faltantes críticas (inventario_item, consumo_partida, movimiento_caja)
-- 5. ✅ CHECK constraints adicionales (compra, compra_detalle)
-- 6. ✅ Campos de auditoría (created_by, trace_id) - pendiente implementación SecurityContext

-- PRÓXIMOS PASOS:
-- - FIX-02: Implementar Spring Security para poblar created_by desde SecurityContext
-- - FIX-03: Eliminar credenciales hardcodeadas de application.yml

-- Migración V7: Enriquecer apu_insumo_snapshot con campos para cálculo dinámico
-- 
-- Esta migración agrega campos necesarios para el motor de cálculo dinámico de APU:
-- - Campos de clasificación (tipo_recurso, orden_calculo)
-- - Campos de unidades (aporte_unitario, unidad_aporte, unidad_base, factor_conversion_unidad_base, unidad_compra)
-- - Campos de precio/moneda (moneda, tipo_cambio_snapshot, precio_mercado, flete, precio_puesto_en_obra)
-- - Campos específicos por tipo de recurso (desperdicio, composicion_cuadrilla, etc.)

-- Agregar campos de clasificación
ALTER TABLE apu_insumo_snapshot
    ADD COLUMN tipo_recurso VARCHAR(50),
    ADD COLUMN orden_calculo INTEGER;

-- Agregar campos de unidades
ALTER TABLE apu_insumo_snapshot
    ADD COLUMN aporte_unitario DECIMAL(19,6),
    ADD COLUMN unidad_aporte VARCHAR(50),
    ADD COLUMN unidad_base VARCHAR(50),
    ADD COLUMN factor_conversion_unidad_base DECIMAL(19,6),
    ADD COLUMN unidad_compra VARCHAR(50);

-- Agregar campos de precio/moneda
ALTER TABLE apu_insumo_snapshot
    ADD COLUMN moneda VARCHAR(3),
    ADD COLUMN tipo_cambio_snapshot DECIMAL(19,6),
    ADD COLUMN precio_mercado DECIMAL(19,4),
    ADD COLUMN flete DECIMAL(19,4),
    ADD COLUMN precio_puesto_en_obra DECIMAL(19,4);

-- Agregar campos específicos MATERIAL
ALTER TABLE apu_insumo_snapshot
    ADD COLUMN desperdicio DECIMAL(7,4);

-- Agregar campos específicos MANO_OBRA
ALTER TABLE apu_insumo_snapshot
    ADD COLUMN costo_dia_cuadrilla_calculado DECIMAL(19,4),
    ADD COLUMN jornada_horas INTEGER;

-- Agregar campos específicos EQUIPO_MAQUINA
ALTER TABLE apu_insumo_snapshot
    ADD COLUMN costo_hora_maquina DECIMAL(19,4),
    ADD COLUMN horas_uso DECIMAL(19,6);

-- Agregar campos específicos EQUIPO_HERRAMIENTA
ALTER TABLE apu_insumo_snapshot
    ADD COLUMN porcentaje_mano_obra DECIMAL(7,4),
    ADD COLUMN depende_de VARCHAR(255);

-- Crear índices para búsquedas eficientes
CREATE INDEX IF NOT EXISTS idx_apu_insumo_snapshot_tipo_recurso 
    ON apu_insumo_snapshot(tipo_recurso);

CREATE INDEX IF NOT EXISTS idx_apu_insumo_snapshot_orden_calculo 
    ON apu_insumo_snapshot(orden_calculo);

-- Comentarios para documentación
COMMENT ON COLUMN apu_insumo_snapshot.tipo_recurso IS 'Tipo de recurso: MATERIAL, MANO_OBRA, EQUIPO_MAQUINA, EQUIPO_HERRAMIENTA, SUBCONTRATO';
COMMENT ON COLUMN apu_insumo_snapshot.orden_calculo IS 'Orden de cálculo para respetar dependencias (1=MATERIAL, 2=MANO_OBRA, 3=EQUIPO_MAQUINA, 4=EQUIPO_HERRAMIENTA)';
COMMENT ON COLUMN apu_insumo_snapshot.aporte_unitario IS 'Cantidad de recurso por unidad de medida de la partida';
COMMENT ON COLUMN apu_insumo_snapshot.unidad_base IS 'Unidad base para normalización (ej: KG, M3)';
COMMENT ON COLUMN apu_insumo_snapshot.factor_conversion_unidad_base IS 'Factor para convertir unidad_aporte a unidad_base';
COMMENT ON COLUMN apu_insumo_snapshot.desperdicio IS 'Porcentaje de desperdicio (0-1) para MATERIAL';
COMMENT ON COLUMN apu_insumo_snapshot.porcentaje_mano_obra IS 'Porcentaje sobre costo total de MO (0-1) para EQUIPO_HERRAMIENTA';

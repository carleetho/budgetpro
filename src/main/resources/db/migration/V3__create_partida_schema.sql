-- Migración Flyway V3: Crear tabla partida (Work Breakdown Structure)
-- Basado en el ERD Físico Definitivo (08_erd_fisico_definitivo_sql.md)

-- Crear tabla partida
CREATE TABLE partida (
    id UUID PRIMARY KEY,
    presupuesto_id UUID NOT NULL,
    padre_id UUID, -- Opcional, para jerarquía recursiva
    item VARCHAR(50) NOT NULL, -- Código WBS: "01.01", "02.01.05"
    descripcion TEXT NOT NULL,
    unidad VARCHAR(20), -- Opcional si es título
    metrado NUMERIC(19,6) NOT NULL DEFAULT 0, -- Cantidad presupuestada. 0 si es título
    nivel INT NOT NULL, -- Profundidad en el árbol: 1, 2, 3...
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_partida_presupuesto
        FOREIGN KEY (presupuesto_id) REFERENCES presupuesto(id) ON DELETE CASCADE,
    CONSTRAINT fk_partida_padre
        FOREIGN KEY (padre_id) REFERENCES partida(id) ON DELETE CASCADE
);

-- Crear índices para optimización de consultas
CREATE INDEX idx_partida_presupuesto ON partida(presupuesto_id);
CREATE INDEX idx_partida_padre ON partida(padre_id);
CREATE INDEX idx_partida_item ON partida(presupuesto_id, item); -- Índice compuesto para búsqueda por presupuesto e item

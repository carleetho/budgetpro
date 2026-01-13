-- Migración Flyway V12: Crear tablas para Motor de Alertas Paramétricas (Budget Pro Intelligence)
-- Basado en metodología Suárez Salazar (Cap. 2.1340 - Alertas Paramétricas)

-- Crear tipo enumerado para nivel de alerta
CREATE TYPE nivel_alerta AS ENUM (
    'INFO',
    'WARNING',
    'CRITICA'
);

-- Crear tipo enumerado para tipo de alerta
CREATE TYPE tipo_alerta_parametrica AS ENUM (
    'MAQUINARIA_COSTO_HORARIO',
    'ACERO_RATIO_CONCRETO',
    'CONCRETO_TAMANO_AGREGADO'
);

-- Crear tabla configuracion_alerta (configuración de reglas paramétricas)
CREATE TABLE configuracion_alerta (
    id UUID PRIMARY KEY,
    tipo_alerta tipo_alerta_parametrica NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    parametro_minimo NUMERIC(19,6), -- Valor mínimo aceptable (opcional)
    parametro_maximo NUMERIC(19,6), -- Valor máximo aceptable (opcional)
    activa BOOLEAN NOT NULL DEFAULT true,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_configuracion_alerta_tipo UNIQUE (tipo_alerta)
);

CREATE INDEX idx_configuracion_alerta_tipo ON configuracion_alerta(tipo_alerta);
CREATE INDEX idx_configuracion_alerta_activa ON configuracion_alerta(activa);

-- Crear tabla analisis_presupuesto (resultado de análisis de un presupuesto)
CREATE TABLE analisis_presupuesto (
    id UUID PRIMARY KEY,
    presupuesto_id UUID NOT NULL,
    fecha_analisis TIMESTAMP NOT NULL DEFAULT now(),
    total_alertas INT NOT NULL DEFAULT 0,
    alertas_criticas INT NOT NULL DEFAULT 0,
    alertas_warning INT NOT NULL DEFAULT 0,
    alertas_info INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_analisis_presupuesto
        FOREIGN KEY (presupuesto_id) REFERENCES presupuesto(id) ON DELETE CASCADE
);

CREATE INDEX idx_analisis_presupuesto_presupuesto ON analisis_presupuesto(presupuesto_id);
CREATE INDEX idx_analisis_presupuesto_fecha ON analisis_presupuesto(fecha_analisis);

-- Crear tabla alerta_parametrica (alertas generadas por el análisis)
CREATE TABLE alerta_parametrica (
    id UUID PRIMARY KEY,
    analisis_id UUID NOT NULL,
    tipo_alerta tipo_alerta_parametrica NOT NULL,
    nivel nivel_alerta NOT NULL,
    partida_id UUID, -- Partida relacionada (opcional)
    recurso_id UUID, -- Recurso relacionado (opcional)
    mensaje TEXT NOT NULL,
    valor_detectado NUMERIC(19,6), -- Valor que disparó la alerta
    valor_esperado_min NUMERIC(19,6), -- Valor mínimo esperado (opcional)
    valor_esperado_max NUMERIC(19,6), -- Valor máximo esperado (opcional)
    sugerencia TEXT, -- Sugerencia de corrección
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_alerta_analisis
        FOREIGN KEY (analisis_id) REFERENCES analisis_presupuesto(id) ON DELETE CASCADE,
    CONSTRAINT fk_alerta_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id) ON DELETE SET NULL,
    CONSTRAINT fk_alerta_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id) ON DELETE SET NULL
);

CREATE INDEX idx_alerta_analisis ON alerta_parametrica(analisis_id);
CREATE INDEX idx_alerta_tipo ON alerta_parametrica(tipo_alerta);
CREATE INDEX idx_alerta_nivel ON alerta_parametrica(nivel);
CREATE INDEX idx_alerta_partida ON alerta_parametrica(partida_id);
CREATE INDEX idx_alerta_recurso ON alerta_parametrica(recurso_id);

-- Insertar configuraciones por defecto
INSERT INTO configuracion_alerta (id, tipo_alerta, nombre, descripcion, parametro_minimo, parametro_maximo, activa) VALUES
    (gen_random_uuid(), 'MAQUINARIA_COSTO_HORARIO', 
     'Alerta: Maquinaria sin costo horario', 
     'Detecta equipos propios (ACTIVO) con costo_horario = 0. Sugiere depreciación para evitar descapitalización.',
     NULL, 0, true),
    (gen_random_uuid(), 'ACERO_RATIO_CONCRETO',
     'Alerta: Ratio Acero/Concreto fuera de rango',
     'Valida que el ratio Kg Acero / m3 Concreto esté entre 80-150 kg/m3 para estructuras estándar.',
     80, 150, true),
    (gen_random_uuid(), 'CONCRETO_TAMANO_AGREGADO',
     'Alerta: Tamaño de agregado inadecuado',
     'Valida que el agregado (grava) no exceda 1/5 del ancho del elemento estructural.',
     NULL, NULL, true);

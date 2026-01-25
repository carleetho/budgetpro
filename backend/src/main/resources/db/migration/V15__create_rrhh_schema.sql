-- V15__create_rrhh_schema.sql
-- Migration schema for Human Resources (RRHH) Module covering Employees, Attendance, Payroll, and Crews.

-- 1. EMPLEADOS: Core personnel data (Global/Tenant level, tracked by active assignment)
CREATE TABLE empleados (
    id UUID PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    numero_identificacion VARCHAR(50) NOT NULL, -- Encrypted/Hashed logic in app, but unique constraint here
    email VARCHAR(150),
    telefono VARCHAR(50),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO', -- ACTIVO, INACTIVO, SUSPENDIDO
    atributos JSONB DEFAULT '{}',
    
    -- Metadata
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic Locking
    created_by UUID NOT NULL,
    
    CONSTRAINT uk_empleado_identificacion UNIQUE (numero_identificacion),
    CONSTRAINT ck_empleado_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'SUSPENDIDO'))
);

-- 2. HISTORIAL_LABORAL: Temporal tracking of position and salary
-- "fecha_fin IS NULL" implies the record is currently active.
CREATE TABLE historial_laboral (
    id UUID PRIMARY KEY,
    empleado_id UUID NOT NULL,
    
    cargo VARCHAR(100) NOT NULL,
    salario_base DECIMAL(19, 4) NOT NULL,
    tipo_contrato VARCHAR(50) NOT NULL,
    unidad_salario VARCHAR(20) NOT NULL DEFAULT 'MENSUAL', -- HORIE, DIARIO, MENSUAL
    
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE, -- NULL for current/active
    motivo_cambio VARCHAR(255),
    
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_historial_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id),
    CONSTRAINT ck_salario_positivo CHECK (salario_base >= 0),
    CONSTRAINT ck_fechas_validas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

-- Rule: Only one active salary record per employee
CREATE UNIQUE INDEX idx_historial_laboral_activo 
    ON historial_laboral (empleado_id) 
    WHERE fecha_fin IS NULL;

-- 3. ASIGNACIONES_PROYECTO: Links employees to specific projects for a duration
CREATE TABLE asignaciones_proyecto (
    id UUID PRIMARY KEY,
    empleado_id UUID NOT NULL,
    proyecto_id UUID NOT NULL, -- References existing PROYECTOS table
    
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    rol_proyecto VARCHAR(50),
    
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_asignacion_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id),
    CONSTRAINT fk_asignacion_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    CONSTRAINT ck_asignacion_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

-- 4. CUADRILLAS: Work crews managed within a project
CREATE TABLE cuadrillas (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    capataz_id UUID, -- Leader of the crew (optional or mandated by app logic)
    
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA', -- ACTIVA, DISUELTA
    
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0, -- Optimistic Locking
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_cuadrilla_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    CONSTRAINT fk_cuadrilla_capataz FOREIGN KEY (capataz_id) REFERENCES empleados(id),
    CONSTRAINT uk_cuadrilla_codigo_proyecto UNIQUE (proyecto_id, codigo),
    CONSTRAINT ck_cuadrilla_estado CHECK (estado IN ('ACTIVA', 'DISUELTA'))
);

-- 5. CUADRILLA_MIEMBROS: Members of a crew
CREATE TABLE cuadrilla_miembros (
    id UUID PRIMARY KEY,
    cuadrilla_id UUID NOT NULL,
    empleado_id UUID NOT NULL,
    
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT NOW(),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_miembro_cuadrilla FOREIGN KEY (cuadrilla_id) REFERENCES cuadrillas(id),
    CONSTRAINT fk_miembro_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id),
    CONSTRAINT uk_miembro_activo_cuadrilla UNIQUE (empleado_id, cuadrilla_id) -- Prevent duplicate active membership in same crew (simplification)
);

-- 6. ASISTENCIA_REGISTROS: Daily attendance and time tracking
CREATE TABLE asistencia_registros (
    id UUID PRIMARY KEY,
    empleado_id UUID NOT NULL,
    proyecto_id UUID NOT NULL,
    
    fecha DATE NOT NULL,
    hora_entrada TIME NOT NULL,
    hora_salida TIME, -- Nullable if currently working
    hora_inicio_break TIME,
    hora_fin_break TIME,
    
    horas_trabajadas DECIMAL(5, 2), -- Calculated or manually overridden
    horas_extras DECIMAL(5, 2),
    observaciones TEXT,
    
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, APROBADO, RECHAZADO
    
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_asistencia_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id),
    CONSTRAINT fk_asistencia_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    CONSTRAINT ck_asistencia_horas CHECK (hora_salida IS NULL OR hora_salida > hora_entrada),
    -- Prevent overlapping attendance records for same employee on same day/start time
    CONSTRAINT uk_asistencia_overlap UNIQUE (empleado_id, fecha, hora_entrada) 
);

-- 7. CONFIGURACION_LABORAL_EXTENDIDA: FSR and other labor params per project
CREATE TABLE configuracion_laboral_extendida (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    
    fecha_vigencia_inicio DATE NOT NULL,
    fecha_vigencia_fin DATE,
    
    -- Using JSONB for flexible configuration (FSR factors, tax rates, benefits)
    fsr_config JSONB NOT NULL DEFAULT '{}', 
    beneficios_adicionales JSONB DEFAULT '{}',
    
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_config_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    CONSTRAINT ck_config_fechas CHECK (fecha_vigencia_fin IS NULL OR fecha_vigencia_fin >= fecha_vigencia_inicio)
);

-- 8. NOMINAS: Payroll Period Headers
CREATE TABLE nominas (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    
    periodo_inicio DATE NOT NULL,
    periodo_fin DATE NOT NULL,
    descripcion VARCHAR(200),
    
    estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR', -- BORRADOR, CALCULADA, APROBADA, PAGADA
    
    total_bruto DECIMAL(19, 2) DEFAULT 0,
    total_neto DECIMAL(19, 2) DEFAULT 0,
    cantidad_empleados INTEGER DEFAULT 0,
    
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_nomina_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    CONSTRAINT ck_nomina_periodo CHECK (periodo_fin >= periodo_inicio),
    CONSTRAINT ck_nomina_estado CHECK (estado IN ('BORRADOR', 'CALCULADA', 'APROBADA', 'PAGADA'))
);

-- 9. NOMINA_DETALLES: Individual payslips/lines within a payroll
CREATE TABLE nomina_detalles (
    id UUID PRIMARY KEY,
    nomina_id UUID NOT NULL,
    empleado_id UUID NOT NULL,
    
    salario_base_calculado DECIMAL(19, 2) NOT NULL DEFAULT 0,
    horas_regulares DECIMAL(5, 2) DEFAULT 0,
    horas_extras DECIMAL(5, 2) DEFAULT 0,
    monto_horas_extras DECIMAL(19, 2) DEFAULT 0,
    
    total_deducciones DECIMAL(19, 2) DEFAULT 0,
    total_bonificaciones DECIMAL(19, 2) DEFAULT 0,
    neto_pagar DECIMAL(19, 2) NOT NULL DEFAULT 0,
    
    -- JSONB for detailed breakdown of every calculation line item
    detalles_calculo JSONB NOT NULL DEFAULT '{}',
    
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_detalle_nomina FOREIGN KEY (nomina_id) REFERENCES nominas(id),
    CONSTRAINT fk_detalle_empleado FOREIGN KEY (empleado_id) REFERENCES empleados(id),
    CONSTRAINT uk_nomina_empleado UNIQUE (nomina_id, empleado_id) -- Only one payslip per employee per payroll
);

-- 10. ASIGNACIONES_ACTIVIDAD: Linking Crews to Budget Items (Partidas) for Costing
CREATE TABLE asignaciones_actividad (
    id UUID PRIMARY KEY,
    cuadrilla_id UUID NOT NULL,
    partida_id UUID NOT NULL, -- References existing PARTIDAS table
    
    fecha DATE NOT NULL,
    horas_asignadas DECIMAL(5, 2) NOT NULL,
    
    observaciones VARCHAR(255),
    
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_actividad_cuadrilla FOREIGN KEY (cuadrilla_id) REFERENCES cuadrillas(id),
    CONSTRAINT fk_actividad_partida FOREIGN KEY (partida_id) REFERENCES partidas(id),
    CONSTRAINT ck_horas_asignadas_validas CHECK (horas_asignadas > 0 AND horas_asignadas <= 24)
);

-- INDEXES for Performance
CREATE INDEX idx_empleados_estado ON empleados(estado);
CREATE INDEX idx_empleados_nombre ON empleados(apellido, nombre);

CREATE INDEX idx_historial_empleado ON historial_laboral(empleado_id);
CREATE INDEX idx_historial_fechas ON historial_laboral(fecha_inicio, fecha_fin);

CREATE INDEX idx_asistencia_empleado_fecha ON asistencia_registros(empleado_id, fecha);
CREATE INDEX idx_asistencia_proyecto ON asistencia_registros(proyecto_id);

CREATE INDEX idx_cuadrillas_proyecto ON cuadrillas(proyecto_id);
CREATE INDEX idx_cuadrilla_miembros_empleado ON cuadrilla_miembros(empleado_id);

CREATE INDEX idx_nominas_proyecto_periodo ON nominas(proyecto_id, periodo_inicio);
CREATE INDEX idx_nomina_detalles_nomina ON nomina_detalles(nomina_id);

CREATE INDEX idx_actividad_cuadrilla_partida ON asignaciones_actividad(cuadrilla_id, partida_id);
CREATE INDEX idx_actividad_fecha ON asignaciones_actividad(fecha);

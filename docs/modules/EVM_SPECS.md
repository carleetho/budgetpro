
## 1. Propósito del Módulo

El módulo EVM evalúa la salud financiera y operativa del Proyecto  
en tiempo real, utilizando datos ejecutados y no percepciones.

Su función es emitir diagnósticos objetivos  
sobre costo, tiempo y viabilidad futura.

EVM no predice.  
EVM diagnostica.

**Regla de endurecimiento:**

-   El módulo EVM **no opera de forma autónoma**.
    
-   EVM depende obligatoriamente de:
    
    -   Presupuesto CONGELADO,
        
    -   Cronograma CONGELADO,
        
    -   Ejecución real registrada.
        
-   Si alguno de estos elementos no existe,  
    EVM **no puede emitir diagnóstico válido**.
    

**Regla de propagación ascendente:**

-   Los diagnósticos emitidos por EVM **gobiernan el comportamiento de los módulos superiores**.
    
-   Ningún módulo puede:
    
    -   suavizar,
        
    -   reinterpretar,
        
    -   contradecir  
        el estado de salud declarado por EVM.
        
-   EVM actúa como **módulo de veredicto**, no como módulo consultivo.
    

----------

## 2. Variables Fundamentales

El sistema define las siguientes variables canónicas:

### BAC (Budget At Completion)

Presupuesto total congelado del Proyecto.

**Reglas:**

-   El BAC incluye:
    
    -   presupuesto original congelado,
        
    -   Órdenes de Cambio aprobadas.
        
-   El BAC **no incluye Excepciones operativas**.
    
-   El BAC nunca se reescribe; solo se ajusta mediante capas formales.
    

**Regla de propagación:**

-   Toda modificación del BAC:
    
    -   debe originarse exclusivamente en el módulo CAMBIOS,
        
    -   debe propagarse al módulo PRESUPUESTO como nueva referencia contractual,
        
    -   debe reflejarse inmediatamente en EVM sin reinterpretación.
        

----------

### AC (Actual Cost)

Costo real ejecutado acumulado.

Se alimenta de:

-   Compras,
    
-   Salidas de Inventario,
    
-   Planillas de Mano de Obra,
    
-   Multas por atraso,
    
-   Excepciones operativas.
    

**Reglas:**

-   AC refleja costo real incurrido,  
    independientemente de:
    
    -   pago,
        
    -   aprobación externa,
        
    -   flujo de caja.
        
-   AC incluye:
    
    -   sobrecostos de mercado,
        
    -   desperdicios,
        
    -   reprocesos,
        
    -   penalidades contractuales.
        

**Regla de propagación:**

-   Ningún módulo puede:
    
    -   diferir,
        
    -   postergar,
        
    -   ocultar  
        costos que alimentan AC.
        
-   AC es **dato forense**, no negociable.
    

----------

### EV (Earned Value)

Valor del trabajo realmente ejecutado,  
medido en unidades físicas,  
valorizadas al precio presupuestado.

**Reglas:**

-   EV solo puede generarse si:
    
    -   existe avance físico registrado,
        
    -   el avance está vinculado a una Partida válida,
        
    -   el avance tiene contexto temporal.
        
-   EV **no utiliza precios reales**.
    
-   EV siempre se valora a precio contractual del APU congelado.
    

**Regla de propagación:**

-   EV depende directamente de:
    
    -   TIEMPO (avance dentro de ventana),
        
    -   PRESUPUESTO (partidas válidas),
        
    -   RRHH e INVENTARIOS (ejecución coherente).
        
-   Si alguno de estos módulos presenta incoherencias,  
    **EV debe invalidarse**, no corregirse.
    

----------

### PV (Planned Value)

Valor del trabajo que debió ejecutarse  
según el cronograma a la fecha del sistema.

**Reglas:**

-   PV se deriva exclusivamente del:
    
    -   Cronograma CONGELADO,
        
    -   calendarios definidos,
        
    -   prórrogas aprobadas.
        
-   PV no puede calcularse sin cronograma congelado válido.
    

**Regla de propagación:**

-   Toda Prórroga registrada en el módulo TIEMPO:
    
    -   modifica PV,
        
    -   impacta SPI,
        
    -   puede alterar el estado de salud del Proyecto.
        

----------

## 3. Condiciones de Validez

Los indicadores EVM solo son válidos si:

-   el Presupuesto está CONGELADO,
    
-   existe cronograma aprobado y congelado,
    
-   existe ejecución registrada (AC),
    
-   existe avance físico validado (EV).
    

**Regla de endurecimiento:**

-   Si alguna condición no se cumple,  
    el sistema debe declarar:
    

> **EVM = NO DISPONIBLE**

-   El sistema **no debe estimar, interpolar ni suponer datos faltantes**.
    
-   La indisponibilidad de EVM es una señal de control,  
    no un error del sistema.
    

**Regla de propagación:**

-   Si EVM = NO DISPONIBLE:
    
    -   los módulos superiores pierden capacidad de diagnóstico,
        
    -   cualquier decisión tomada se considera decisión a ciegas.
        

----------

## 4. Cálculo de Indicadores

### 4.1 CPI — Cost Performance Index

CPI = EV / AC

Interpretación:

-   CPI < 1.00 → sobrecosto.
    
-   CPI = 1.00 → en línea.
    
-   CPI > 1.00 → bajo costo (poco común).
    

**Reglas adicionales:**

-   Si AC = 0:
    
    -   CPI = 1.00 por convención,
        
    -   se marca como indicador preliminar,
        
    -   se bloquean proyecciones asociadas.
        

----------

### 4.2 SPI — Schedule Performance Index

SPI = EV / PV

Interpretación:

-   SPI < 1.00 → atraso.
    
-   SPI = 1.00 → en línea.
    
-   SPI > 1.00 → adelanto.
    

**Reglas adicionales:**

-   Si PV = 0:
    
    -   SPI = 1.00 por convención,
        
    -   se marca como indicador preliminar.
        
-   SPI solo es significativo con cronograma congelado válido.
    

----------

## 5. Proyecciones

### 5.1 EAC — Estimate At Completion

EAC = BAC / CPI

Representa el costo esperado al finalizar el Proyecto  
si la tendencia actual se mantiene.

**Regla de endurecimiento:**

-   EAC es proyección matemática, no promesa contractual.
    
-   Alta volatilidad del CPI marca EAC como **estimación inestable**.
    

----------

### 5.2 VAC — Variance At Completion

VAC = BAC - EAC

-   VAC negativo indica pérdida esperada.
    
-   VAC representa impacto directo en la utilidad final.
    
-   VAC **no se suaviza ni se oculta**.
    

----------

### 5.3 TCPI — To Complete Performance Index

TCPI = (BAC - EV) / (BAC - AC)

**Reglas:**

-   TCPI > 1.00 indica exigencia futura mayor al desempeño histórico.
    
-   TCPI elevado es señal temprana de inviabilidad.
    

----------

## 6. Estados de Salud del Proyecto

### 6.1 Estado SALUDABLE

-   CPI ≥ 0.95
    
-   SPI ≥ 0.95
    
-   TCPI ≤ 1.05
    

----------

### 6.2 Estado EN RIESGO

-   CPI entre 0.85 y 0.95, o
    
-   SPI entre 0.85 y 0.95, o
    
-   TCPI entre 1.05 y 1.20
    

Regla:

-   Activa análisis de causa raíz, no maquillaje.
    

----------

### 6.3 Estado CRÍTICO

-   CPI < 0.85, o
    
-   SPI < 0.85, o
    
-   TCPI > 1.20
    

**Regla de propagación obligatoria:**

-   PROYECTO debe marcarse como Proyecto en Riesgo Financiero.
    
-   PRESUPUESTO no puede ocultar desviaciones.
    
-   COMPRAS, INVENTARIOS y RRHH operan bajo alerta reforzada.
    
-   TIEMPO debe exponer impacto de atrasos y multas.
    
-   CAMBIOS no puede ejecutarse automáticamente.
    

----------

## 7. Acciones Obligatorias por Estado

### SALUDABLE

-   Continuar ejecución normal.
    

### EN RIESGO

-   Análisis de causa raíz.
    
-   Evaluar cambios o ajustes.
    

### CRÍTICO

-   Declarar estado crítico.
    
-   Bloquear decisiones no esenciales.
    
-   Exigir redefinición contractual o aceptación de pérdida.
    

----------

## 8. Relación con Excepciones

-   Las Excepciones impactan directamente AC.
    
-   Excepciones frecuentes deterioran CPI y TCPI.
    
-   El historial de Excepciones es insumo de análisis.
    

**Regla de propagación:**

-   El historial de Excepciones debe ser visible para:
    
    -   CAMBIOS,
        
    -   PRESUPUESTO,
        
    -   PROYECTO.
        

----------

## 9. Auditoría y Trazabilidad

Todo cálculo EVM debe poder reconstruirse  
desde datos históricos inmutables.

No se permiten:

-   recálculos manuales,
    
-   ajustes retroactivos,
    
-   reinterpretaciones subjetivas.
    

**Regla:**

-   Indicador no auditable = indicador inválido.
    

----------

## 10. Principios de Diseño No Negociables

-   EVM dice la verdad.
    
-   No maquilla indicadores.
    
-   El juicio matemático prevalece.
    

----------

## 11. Criterios de Violación

Se considera violación grave si:

-   se recalcula EVM con datos editados,
    
-   se oculta estado CRÍTICO,
    
-   se manipula EV,
    
-   se suavizan umbrales.
    

Estas violaciones destruyen la credibilidad del sistema.

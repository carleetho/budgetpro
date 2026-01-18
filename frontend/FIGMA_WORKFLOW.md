# 🎨 Flujo de Trabajo con Figma MCP

## 📋 Resumen del Flujo

### 1. **TÚ seleccionas en Figma**
   - Abres tu diseño en Figma
   - Seleccionas un frame o componente que quieres implementar
   - Ejemplo: Un card de "Resumen Financiero" mejorado

### 2. **YO obtengo el contexto del diseño**
   - Uso la herramienta `get_design_context` para extraer:
     - Estructura HTML/JSX
     - Estilos (colores, espaciado, tipografía)
     - Componentes usados
     - Layout y posicionamiento

### 3. **YO genero código adaptado a tu proyecto**
   - Combino el diseño de Figma con:
     - Tus componentes existentes (Shadcn UI)
     - Tu estructura de archivos
     - Tus tipos TypeScript
     - Tus estilos Enterprise (Tailwind compacto)

## 🔄 Ejemplo Práctico: Mejorar el Card de Resumen Financiero

### Escenario
Quieres mejorar el card de "Resumen Financiero" en la página de Presupuesto con un diseño más visual de Figma.

### Paso 1: En Figma
1. Diseña un card mejorado con:
   - Icono más grande
   - Gráfico de barras pequeño
   - Desglose por categorías
   - Animaciones sutiles

2. Selecciona el frame completo

### Paso 2: En Cursor (conmigo)
Tú me dices:
```
"Tengo seleccionado en Figma un card de resumen financiero mejorado. 
Quiero implementarlo en la página de presupuesto, usando mis componentes 
de Shadcn y manteniendo el estilo Enterprise compacto."
```

### Paso 3: Yo ejecuto
1. **Obtengo el diseño**: Uso `get_design_context` para extraer el diseño
2. **Analizo tu código**: Reviso `PresupuestoPage.tsx` y componentes existentes
3. **Genero código adaptado**: Creo un componente que:
   - Usa tus `Card`, `CardHeader`, `CardContent` de Shadcn
   - Mantiene tus tipos TypeScript (`ItemPresupuesto`)
   - Respeta tus estilos Enterprise (text-xs, colores grid-*)
   - Integra con tu lógica existente

### Paso 4: Resultado
- Nuevo componente `FinancialSummaryCard.tsx`
- Integrado en `PresupuestoPage.tsx`
- Mantiene la funcionalidad existente
- Aplica el nuevo diseño de Figma

## 🛠️ Herramientas Disponibles del MCP

### Para Diseños (Figma Design)
- **`get_design_context`**: Extrae código React + Tailwind del diseño
- **`get_variable_defs`**: Obtiene variables de diseño (colores, spacing)
- **`get_screenshot`**: Captura screenshot para preservar layout
- **`get_metadata`**: Obtiene estructura básica (útil para diseños grandes)

### Para Prototipos (Figma Make)
- **Recursos Make**: Puedo obtener archivos completos de proyectos Make
- Útil para: Extender prototipos a producción

### Para Mapeo de Componentes
- **`get_code_connect_map`**: Mapea componentes Figma → Código
- **`add_code_connect_map`**: Crea mapeos nuevos
- Útil para: Reutilizar tus componentes existentes

### Para Reglas de Diseño
- **`create_design_system_rules`**: Crea reglas para traducir diseños
- Útil para: Asegurar consistencia en generación de código

## 💡 Mejores Prácticas

### ✅ Hacer
- Selecciona frames completos (no elementos sueltos)
- Menciona qué componentes quieres usar: "usando mis componentes de Shadcn"
- Especifica el framework: "en React con TypeScript"
- Indica el estilo: "manteniendo el estilo Enterprise compacto"

### ❌ Evitar
- Selecciones muy grandes (puede exceder límites de contexto)
- Diseños sin estructura clara
- No mencionar tus componentes existentes

## 🎯 Ejemplo de Prompt Efectivo

```
"Tengo seleccionado en Figma un diseño de tabla de partidas mejorado.
Quiero implementarlo usando:
- Mi componente TreeDataGrid existente
- Mis estilos Enterprise (text-xs, colores grid-*)
- Mis tipos TypeScript (ItemPresupuesto)
- Mantener la funcionalidad de expansión/colapso

El diseño incluye:
- Headers con iconos
- Filas con hover mejorado
- Badges de estado más visibles
```

## 📝 Notas Importantes

1. **Solo Desktop MCP**: La selección en tiempo real solo funciona con el servidor local (Figma Desktop)
2. **Remote MCP**: Requiere links a frames específicos
3. **Code Connect**: Configúralo para mejor reutilización de componentes
4. **Variables**: Puedo extraer tus variables de diseño para mantener consistencia

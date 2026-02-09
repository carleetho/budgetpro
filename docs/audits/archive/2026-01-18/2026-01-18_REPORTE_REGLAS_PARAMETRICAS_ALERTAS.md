# Reporte — Reglas Paramétricas (Alertas)

Fecha: 2026-01-18  
Alcance: `backend/src/main/java/**/alertas/**`

## Inventario de reglas

| Regla | Ubicación | Dominio sugerido | Severidad |
| --- | --- | --- | --- |
| Maquinaria: costo_horario = 0 en equipo propio | `AnalizadorParametricoService.analizarMaquinaria` | Finanzas/Alertas Paramétricas | CRITICA |
| Ratio Acero/Concreto fuera de rango (80–150 kg/m³) | `AnalizadorParametricoService.analizarAceroConcreto` | Finanzas/Alertas Paramétricas | WARNING |
| Tamaño de agregado > 1/5 del ancho del elemento | `AnalizadorParametricoService.analizarTamanoAgregado` | Finanzas/Alertas Paramétricas | WARNING |


# MOCK_CATALOG_ADAPTER

## Activación

El adaptador mock se habilita con los perfiles de Spring `test` o `mock`.

## Datos base

Recursos:
- MAT-001 (CAPECO) – CEMENTO PORTLAND TIPO I – BOL – 25.50
- MAT-002 (CAPECO) – ACERO CORRUGADO 3/8" – KG – 4.20

APU:
- APU-001 (CAPECO) con 2 insumos referenciando MAT-001 y MAT-002

## Escenarios

- Simular fallo: `setFailNext(true)` lanza `CatalogServiceException` en la próxima llamada.
- Simular inactividad: `setRecursoActivo(externalId, catalogSource, false)`.
- Latencia: `setDelayMs(ms)` para sleep controlado.

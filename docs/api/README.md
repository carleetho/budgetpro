# API contract (OpenAPI)

Fuente de verdad en runtime: `GET /v3/api-docs.yaml` (Springdoc).

## Regenerar localmente

```bash
# Terminal 1: backend con Postgres
cd backend && ./mvnw spring-boot:run

# Terminal 2:
./scripts/generate-openapi-spec.sh
git add docs/api/openapi.yaml && git commit -m "chore(api): refresh OpenAPI spec"
```

Variable opcional: `OPENAPI_BASE_URL` (default `http://localhost:8080`).

## CI

Workflow `.github/workflows/api-validation.yml` arranca Postgres + backend, regenera el YAML y valida la sintaxis OpenAPI. Si el archivo versionado diverge del generado, el job falla (hay que commitear el refresh).

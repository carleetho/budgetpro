# Canonical Notebook Validation Checklist

## General Requirements

- [ ] Filename follows convention: `[MODULE]_MODULE_CANONICAL.md` or `[TOPIC]_RADIOGRAPHY.md`
- [ ] Metadata header present (Status, Owner, Last Updated)
- [ ] No placeholders remaining (e.g., `[Insert Name Here]`)
- [ ] Diagrams render correctly in Mermaid
- [ ] JSON schemas are valid

## Module Notebooks (11 Sections)

1. **Maturity Roadmap**: Defined for Current, +1 Month, +3 Months?
2. **Invariants**: Categorized as ✅, 🟡, or 🔴?
3. **Domain Events**: Event names and payloads defined?
4. **State Constraints**: State machine diagram included?
5. **Data Contracts**: Entity schemas and JSON schemas present?
6. **Use Cases**: Prioritized (P0/P1/P2) and status marked?
7. **Domain Services**: Responsibilities and key methods listed?
8. **REST Endpoints**: Paths and methods specified?
9. **Observability**: Metrics and logs defined?
10. **Integration Points**: Inputs (Consumes) and outputs (Exposes) defined?
11. **Technical Debt**: Risks identified with severity?

## Radiography Notebooks

- [ ] Covers cross-cutting concerns (not just a single module)?
- [ ] Includes "As-Is" architecture diagram?
- [ ] Lists specific recommendations?

## AI Compatibility

- [ ] Context is clear and "grounded" (no ambiguous terms)?
- [ ] JSON schemas are complete enough for an AI to generate a DTO?

## 12. AI Agent Guidance

**Context Loading Priority:**

- **Critical**: `[THIS_MODULE]_MODULE_CANONICAL.md`, `ARCHITECTURAL_CONTRACTS_CURRENT.md`
- **Important**: `DOMAIN_INVARIANTS_CURRENT.md` (if complex logic)

**Decision Rules:**

- **Invariants**: Strict enforcement of Section 2.
- **State**: No transitions outside of Section 4 diagrams.
- **Ambiguity**: Stop if you encounter `[AMBIGUITY_DETECTED]`.

**Common Assumptions to Avoid:**

- ❌ **Dont**: Assume standard CRUD if Invariants imply complex validation.
- ✅ **Do**: Check Section 5 (Data Contracts) for mandatory fields.

**Cross-Module Dependencies:**

- If touching [Integration Point], load `[OTHER_MODULE]_MODULE_CANONICAL.md`.

# Cursor Integration Guide

> **Goal**: Generate code that aligns with Canonical specs, not assumptions.

## 1. Concepts

Cursor understands your codebase better when you explicitly point it to the "Rules of the Road". The Canonical Notebooks are those rules.

## 2. Integration Workflow

### Step 1: Identify your Domain

Before chatting or generating code, determine which module you are touching (e.g., `Presupuesto`).

### Step 2: Load Context

Use the `@` symbol in Chat or Cmd+K to reference:

1. **The Module Spec**: e.g., `@PRESUPUESTO_MODULE_CANONICAL.md`
2. **The Architecture**: `@ARCHITECTURAL_CONTRACTS_CURRENT.md` (Always recommended)

### Step 3: Prompt with Intent

> "Implement the 'Clone Budget' use case following the constraints in @PRESUPUESTO_MODULE_CANONICAL.md and adhering to the layers in @ARCHITECTURAL_CONTRACTS_CURRENT.md"

## 3. Advanced Tips

- **.cursorrules**: We have configured the workspace to prioritize these docs.
- **Ambiguity**: If Cursor makes up a field not in the spec, check the spec. If missing, update the spec first!
- **NotebookLM**: For complex cross-module questions, ask NotebookLM first, then paste the answer into Cursor.

## 4. Troubleshooting

- **Hallucinations**: If Cursor invents logic, it usually means the Context window didn't have the constraint. Add `DOMAIN_INVARIANTS_CURRENT.md`.

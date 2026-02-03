# Onboarding Checkpoints

> **Instructions**: Use NotebookLM to test your knowledge.

## Checkpoint 1: Architecture

_Goal: Can you explain the separation of concerns?_

**Ask NotebookLM:**

1. _"What is the difference between Domain and Application layers?"_
2. _"Where should I put a DTO?"_

**Self-Check:**

- [ ] I know why we don't put annotations on Domain entities.
- [ ] I know where `PresupuestoRepository` implementation lives (Infra).

## Checkpoint 2: Business Rules

_Goal: Do you know the constraints?_

**Ask NotebookLM:**

1. _"What happens when I freeze a budget?"_
2. _"Can I modify a Partida after approval?"_

**Self-Check:**

- [ ] I can point to the Invariant ID that prevents modification (e.g. P-01).

## Checkpoint 3: Data Flow

_Goal: Do you know how data moves?_

**Ask NotebookLM:**

1. _"How does a Purchase affect Inventory?"_
2. _"Trace the state change of an Estimacion."_

**Self-Check:**

- [ ] I understand the Event Bus mechanism for cross-module sync.

# Canonical Notebooks Knowledge Engine

## Overview

The Canonical Notebooks system is the **authoritative knowledge layer** for BudgetPro. It consists of structured markdown documents that serve as the single source of truth for business logic, architectural contracts, and module evolution roadmaps.

These notebooks are designed to:

- **Eliminate AI assumptions**: Provide grounded context for AI coding assistants (Cursor, Antigravity).
- **Accelerate onboarding**: Enable developers to understand module states in minutes.
- **Enforce architecture**: Prescribe the exact implementation path for each module.

## Directory Structure

- `radiography/`: Cross-cutting analyses of the current system state (5 notebooks).
- `modules/`: Prescriptive 3-month roadmaps for each of the 9 modules.
- `templates/`: Reusable markdown structures for creating consistent notebooks.

## Usage

### For Developers

1. **Before coding**: Open the relevant module notebook in `modules/`.
2. **During coding**: Keep the notebook open as a reference or add it to your AI assistant's context.
3. **After coding**: If you implemented a missing invariant, update the notebook status from 🔴 to ✅.

### For AI Assistants

Users should add the relevant `CANONICAL.md` files to the AI's context. The AI must strictly follow the "Invariants" and "Data Contracts" sections.

## Updates & Sync

- Notebooks are versioned in Git alongside the code.
- Updates to business logic in code **must** be accompanied by a notebook update.
- Drift > 10% requires immediate remediation.

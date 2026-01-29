# AXIOM Orchestrator Installation Guide

Follow these steps to integrate AXIOM into your local development workflow.

## Prerequisites

- **Python 3.8+**: Ensure Python is available in your PATH.
- **Git**: The orchestrator relies on git commands for file discovery.
- **AXIOM Config**: An `axiom.config.yaml` file should exist at the repository root.

## Installation Steps

1. **Navigate to root**: Open your terminal at the project root.
2. **Run the installer**:
   ```bash
   bash tools/axiom/install_hook.sh
   ```
3. **Verify Installation**: Check that `.git/hooks/pre-commit` exists and is executable.

## Usage

Once installed, AXIOM runs automatically every time you execute `git commit`.

### Dry-Run Mode

To see what AXIOM would do WITHOUT actually blocking your commit, you can use the `--dry-run` flag if your hook is configured to accept it, or run the script manually:

```bash
python3 tools/axiom/pre_commit_hook.py --dry-run
```

### Bypassing AXIOM

If you have an emergency that requires bypassing the hook, use the standard git flag:

```bash
git commit -n  # or --no-verify
```

_Note: This is discouraged. Use override keywords (see below) if you need to authorize a large change._

## Override Keywords

AXIOM allows you to bypass specific rules or zones by including keywords in your commit message:

| Keyword                | Effect                                                 |
| ---------------------- | ------------------------------------------------------ |
| `OVERRIDE_ESTIMACION`  | Bypasses limits for the Estimacion domain.             |
| `OVERRIDE_PRESUPUESTO` | Bypasses limits for the Presupuesto domain.            |
| `BIGBANG_APPROVED`     | Bypasses global "blast radius" limits (large commits). |

## Troubleshooting

- **Permission Denied**: Ensure `install_hook.sh` and `pre_commit_hook.py` have execution bits set (`chmod +x`).
- **Python Not Found**: Ensure `python3` is available. On Windows, you might need to adjust the shebang or use `py`.
- **Git Errors**: Ensure you are inside a initialized git repository.

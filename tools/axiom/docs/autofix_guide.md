# AXIOM Auto-Fixer Configuration & Usage

The AXIOM Auto-Fixer Engine allows for automated remediation of specific security and structural violations.

## Configuration

Automated fixes are configured in `axiom.config.yaml` under the `auto_fix` section.

```yaml
auto_fix:
  enabled: true # Master toggle for the auto-fix engine
  safe_only: false # If true, only fixes marked as "safe" will be applied
```

### Validator-Specific Fixes

#### Security Validator (.gitignore corrections)

The `security_validator` can automatically add missing entries to `.gitignore` files.

```yaml
validators:
  security_validator:
    required_gitignore_entries:
      - ".env"
      - "*.log"
      - ".gemini"
      - "node_modules"
      - "target"
```

## How it Works

The engine follows a transactional workflow to ensure repository integrity:

1. **Detection**: Validators identify fixable issues and provide structured `fix_data`.
2. **Execution**: Enabled fixers apply changes (e.g., appending entries to `.gitignore`).
3. **Safety Backup**: Fixers create a timestamped backup (`.file.backup.timestamp`) before modification.
4. **Re-validation**: All validators are re-run on modified files.
5. **Atomic Decision**:
   - If **new blocking violations** are introduced, the changes are rolled back automatically.
   - If re-validation passes, the changes are committed and backups are cleaned up.

## Rollback & Safety

If a fix needs to be reverted manually, the engine's internal `rollback()` mechanism ensures original files are restored from backups if the session hasn't completed. In production use, the automatic rollback ensures that no broken state is ever committed to the repository.

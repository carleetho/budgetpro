# Migration: From `secure-commit.sh` to `SecurityValidator`

AXIOM now features a native Python-based `SecurityValidator` that replaces the legacy `secure-commit.sh` bash script. This migration provides better performance, complex regex support, and cross-platform compatibility.

## What's New?

- **Integrated Pipeline**: Security checks now run alongside other AXIOM validators.
- **Strictness Levels**: Configure how strictly security rules should block commits (`strict`, `standard`, `permissive`).
- **Standardized Reporting**: Security findings appear in the same reports as architectural violations.
- **Maven Integration**: Automatic compilation check for Java changes.

## Migration Steps

1. **Update Configuration**:
   Ensure `axiom.config.yaml` contains the `security_validator` section:

   ```yaml
   validators:
     security_validator:
       enabled: true
       strictness: "standard"
       checks:
         gitignore: true
         credentials: true
         file_integrity: true
   ```

2. **Reinstall Hook (Optional)**:
   If you haven't already, install the AXIOM pre-commit hook:

   ```bash
   bash tools/axiom/install_hook.sh
   ```

3. **Retire Legacy Script**:
   You can now safely ignore or remove `secure-commit.sh` as AXIOM handles these responsibilities.

## FAQ

### How do I bypass a false positive?

Use the standard AXIOM override keywords in your commit message (e.g., `BIGBANG_APPROVED` or custom keywords if configured).

### Can I disable specific checks?

Yes, use the `checks` toggle in `axiom.config.yaml`.

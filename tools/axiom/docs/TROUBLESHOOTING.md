# AXIOM AI Integration Troubleshooting

Common issues and solutions when working with the AI Assistant Integration.

## Sync Script Issues

### 1. "Configuration file not found"

**Error**: `FileNotFoundError: Configuration file not found at: tools/axiom/axiom.config.yaml`

- **Cause**: The `axiom.config.yaml` file is missing from the project root or the specified path.
- **Solution**: Run the AXIOM installer (`tools/axiom/install.sh`) to restore the default configuration, or copy `tools/axiom/axiom.config.yaml` manually.

### 2. "Config validation failed" (Schema Error)

**Error**: `jsonschema.exceptions.ValidationError: 'system' is a required property`

- **Cause**: Your `axiom.config.yaml` is missing required sections defined in the schema.
- **Solution**: Ensure your config matches `tools/axiom/schema/axiom-config.schema.json`. See [CONFIGURATION_GUIDE.md](CONFIGURATION_GUIDE.md) for required fields.

### 3. "Generated content exceeds word count limit"

**Warning**: `Content exceeds word limit! (2500 > 2000)`

- **Cause**: Your configuration (especially lessons learned or prohibitions) is too verbose.
- **Solution**: AI assistants have context windows. Summarize your rules. Remove low-priority history or merge similar prohibitions.

## AI Assistant Issues

### 1. Assistant Ignores Rules

- **Cause**: The `.cursorrules` file might not be in the project root, or the assistant (e.g., older versions) doesn't support it.
- **Solution**:
  - Ensure `.cursorrules` is at the very root of the workspace.
  - **Cursor**: Check "Rules for AI" settings.
  - **Windsurf/Antigravity**: Ensure the file is indexed.

### 2. "I cannot modify files in RED ZONE"

- **Cause**: The AI is correctly following the instruction to be careful, but you _want_ it to modify them.
- **Solution**: Instruct the AI to "Plan the change first" as per the Operation Protocol, or use an Override Keyword in your prompt (e.g., "I authorize this change under BIGBANG_APPROVED context").

### 3. Language Mismatch

- **Issue**: AI responds in English but project is Spanish.
- **Solution**: usage `system.role` to enforce language. Example: `role: "Experto Java que responde SIEMPRE en Español"`.

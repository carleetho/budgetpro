#!/bin/bash
# AXIOM Git Hook Installer
# Installs the pre-commit hook that triggers the AXIOM validation pipeline.

set -e

# 1. Verification of the environment
if [ ! -d ".git" ]; then
    echo "Error: .git directory not found. Are you in the repository root?"
    exit 1
fi

HOOK_PATH=".git/hooks/pre-commit"
SOURCE_PATH="tools/axiom/pre_commit_hook.py"

# 2. Check if the entry point script exists
if [ ! -f "$SOURCE_PATH" ]; then
    echo "Error: $SOURCE_PATH not found. Ensure the AXIOM toolset is correctly located."
    exit 1
fi

# 3. Backup existing hook if present
if [ -f "$HOOK_PATH" ]; then
    echo "Backing up existing pre-commit hook to $HOOK_PATH.backup"
    mv "$HOOK_PATH" "$HOOK_PATH.backup"
fi

# 4. Create the new pre-commit hook
echo "Installing AXIOM pre-commit hook..."
cat <<EOF > "$HOOK_PATH"
#!/bin/bash
# Trigger AXIOM validation pipeline
# Installed by AXIOM install_hook.sh

# Run the AXIOM orchestrator
python3 \$(git rev-parse --show-toplevel)/$SOURCE_PATH "\$@"
EOF

# 5. Ensure the hook is executable
chmod +x "$HOOK_PATH"

echo "Success: AXIOM pre-commit hook installed."
echo "You can now run a dry-run test with: git commit --allow-empty -m \"Test AXIOM\" --no-verify (or just call the Python script directly)"

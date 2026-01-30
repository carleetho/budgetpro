#!/bin/bash
# Blast Radius Validator Git Hook Installer
# Installs the pre-commit hook that triggers the blast radius validation.

set -e

# 1. Verification of the environment
if [ ! -d ".git" ]; then
    echo "Error: .git directory not found. Are you in the repository root?"
    exit 1
fi

HOOK_PATH=".git/hooks/pre-commit-blast-radius"
VALIDATOR_JAR="tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar"
REPO_ROOT=$(git rev-parse --show-toplevel)

# 2. Check if the validator JAR exists
if [ ! -f "$VALIDATOR_JAR" ]; then
    echo "⚠️  Warning: Blast radius validator JAR not found at $VALIDATOR_JAR"
    echo "Building validator..."
    cd "$REPO_ROOT/tools/blast-radius-validator"
    ./mvnw package -DskipTests
    cd "$REPO_ROOT"
    
    if [ ! -f "$VALIDATOR_JAR" ]; then
        echo "❌ Error: Failed to build validator JAR"
        exit 1
    fi
    echo "✅ Validator JAR built successfully"
fi

# 3. Create the pre-commit hook
echo "Installing blast radius validator pre-commit hook..."
cat <<EOF > "$HOOK_PATH"
#!/bin/bash
# Blast Radius Validator Pre-commit Hook
# Installed by tools/blast-radius-validator/install-hook.sh

REPO_ROOT=\$(git rev-parse --show-toplevel)
VALIDATOR_JAR="\$REPO_ROOT/tools/blast-radius-validator/target/blast-radius-validator-1.0.0-SNAPSHOT.jar"

# Check if JAR exists
if [ ! -f "\$VALIDATOR_JAR" ]; then
    echo "⚠️  Blast radius validator JAR not found. Skipping validation."
    echo "To enable validation, run: cd tools/blast-radius-validator && ./mvnw package"
    exit 0
fi

# Run the validator
echo "🔍 Validating blast radius of staged changes..."
java -jar "\$VALIDATOR_JAR" "\$REPO_ROOT"

EXIT_CODE=\$?

if [ \$EXIT_CODE -eq 1 ]; then
    echo ""
    echo "❌ Blast radius validation FAILED"
    echo "The staged changes exceed the configured limits."
    echo ""
    echo "Options:"
    echo "  1. Reduce the number of staged files"
    echo "  2. Split your changes into smaller commits"
    echo "  3. Add 'BIGBANG_APPROVED' to your commit message to override (use with caution)"
    echo ""
    exit 1
elif [ \$EXIT_CODE -eq 2 ]; then
    echo ""
    echo "❌ Blast radius validator ERROR"
    echo "There was an error running the validator. Check the messages above."
    echo ""
    exit 1
fi

exit 0
EOF

# 4. Ensure the hook is executable
chmod +x "$HOOK_PATH"

# 5. Integrate with existing pre-commit hook if it exists
MAIN_HOOK=".git/hooks/pre-commit"
if [ -f "$MAIN_HOOK" ]; then
    # Check if already integrated
    if ! grep -q "pre-commit-blast-radius" "$MAIN_HOOK"; then
        echo "Integrating with existing pre-commit hook..."
        # Backup existing hook
        cp "$MAIN_HOOK" "$MAIN_HOOK.backup"
        # Add call to blast-radius hook
        cat <<'INTEGRATION' >> "$MAIN_HOOK"

# Blast Radius Validator
if [ -f ".git/hooks/pre-commit-blast-radius" ]; then
    .git/hooks/pre-commit-blast-radius || exit 1
fi
INTEGRATION
        echo "✅ Integrated with existing pre-commit hook"
    else
        echo "✅ Already integrated with pre-commit hook"
    fi
else
    # Create main pre-commit hook that calls blast-radius
    echo "Creating main pre-commit hook..."
    cp "$HOOK_PATH" "$MAIN_HOOK"
    echo "✅ Created main pre-commit hook"
fi

echo ""
echo "✅ Success: Blast radius validator pre-commit hook installed."
echo ""
echo "The hook will run automatically on every commit."
echo "To bypass (not recommended): git commit --no-verify"
echo ""

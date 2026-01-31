#!/bin/bash
# ============================================================================
# Semgrep Rule Catalog Generator
# ============================================================================
#
# This script auto-generates a comprehensive catalog of all Semgrep rules
# by parsing rule files in .semgrep/rules/ directory.
#
# Usage:
#   ./.semgrep/scripts/generate-catalog.sh
#
# Output:
#   .semgrep/RULE_CATALOG.md
#
# ============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
RULES_DIR="$PROJECT_ROOT/.semgrep/rules"
OUTPUT_FILE="$PROJECT_ROOT/.semgrep/RULE_CATALOG.md"

echo "Generating Semgrep Rule Catalog..."
echo "Rules directory: $RULES_DIR"
echo "Output file: $OUTPUT_FILE"

# Initialize output file
cat > "$OUTPUT_FILE" << 'EOF'
# Semgrep Rule Catalog

This document provides a comprehensive catalog of all Semgrep rules in the BudgetPro project.

**Auto-generated** - Do not edit manually. Run `.semgrep/scripts/generate-catalog.sh` to update.

---

EOF

echo "**Last Updated**: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Count total rules
TOTAL_RULES=$(find "$RULES_DIR" -name "*.yaml" -exec grep -c "^  - id:" {} + | awk '{s+=$1} END {print s}')
TOTAL_FILES=$(find "$RULES_DIR" -name "*.yaml" | wc -l)

echo "**Total Rules**: $TOTAL_RULES" >> "$OUTPUT_FILE"
echo "**Total Rule Files**: $TOTAL_FILES" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Function to extract rule information
extract_rule_info() {
    local file=$1
    local category=$(basename $(dirname "$file"))
    
    echo "Processing: $file (category: $category)"
    
    # Extract all rule IDs from the file
    grep "^  - id:" "$file" | sed 's/^  - id: //' | while read -r rule_id; do
        # Extract severity for this specific rule
        # This is a simplified extraction - may need refinement for complex files
        severity=$(awk "/- id: $rule_id/,/severity:/" "$file" | grep "severity:" | head -1 | awk '{print $2}')
        
        # Extract message (first line only)
        message=$(awk "/- id: $rule_id/,/message:/" "$file" | grep -A 1 "message:" | tail -1 | sed 's/^[[:space:]]*//' | sed 's/|$//')
        
        # Clean up message
        message=$(echo "$message" | sed 's/^"//' | sed 's/"$//')
        
        echo "$category|$rule_id|$severity|$message|$file"
    done
}

# Process each category
for category_dir in "$RULES_DIR"/*; do
    if [ -d "$category_dir" ]; then
        category=$(basename "$category_dir")
        
        echo "" >> "$OUTPUT_FILE"
        echo "## $(echo $category | tr '[:lower:]' '[:upper:]')" >> "$OUTPUT_FILE"
        echo "" >> "$OUTPUT_FILE"
        
        # Create table header
        echo "| Rule ID | Severity | Description | File |" >> "$OUTPUT_FILE"
        echo "|---------|----------|-------------|------|" >> "$OUTPUT_FILE"
        
        # Process all YAML files in category
        find "$category_dir" -name "*.yaml" | sort | while read -r rule_file; do
            extract_rule_info "$rule_file" | while IFS='|' read -r cat rule_id severity message file; do
                # Create relative path for file link
                rel_file="${file#$PROJECT_ROOT/}"
                file_basename=$(basename "$file")
                
                # Truncate message if too long
                if [ ${#message} -gt 80 ]; then
                    message="${message:0:77}..."
                fi
                
                # Determine severity emoji
                case "$severity" in
                    ERROR)
                        severity_display="🔴 ERROR"
                        ;;
                    WARNING)
                        severity_display="⚠️  WARNING"
                        ;;
                    INFO)
                        severity_display="ℹ️  INFO"
                        ;;
                    *)
                        severity_display="$severity"
                        ;;
                esac
                
                echo "| \`$rule_id\` | $severity_display | $message | [$file_basename](file:///$file) |" >> "$OUTPUT_FILE"
            done
        done
    fi
done

# Add summary section
cat >> "$OUTPUT_FILE" << 'EOF'

---

## Summary by Severity

EOF

# Count by severity
ERROR_COUNT=$(grep -r "severity: ERROR" "$RULES_DIR" | wc -l)
WARNING_COUNT=$(grep -r "severity: WARNING" "$RULES_DIR" | wc -l)
INFO_COUNT=$(grep -r "severity: INFO" "$RULES_DIR" | wc -l)

echo "- 🔴 **ERROR**: $ERROR_COUNT rules (blocking)" >> "$OUTPUT_FILE"
echo "- ⚠️  **WARNING**: $WARNING_COUNT rules (non-blocking)" >> "$OUTPUT_FILE"
echo "- ℹ️  **INFO**: $INFO_COUNT rules (informational)" >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Add summary by category
cat >> "$OUTPUT_FILE" << 'EOF'
## Summary by Category

EOF

for category_dir in "$RULES_DIR"/*; do
    if [ -d "$category_dir" ]; then
        category=$(basename "$category_dir")
        count=$(find "$category_dir" -name "*.yaml" -exec grep -c "^  - id:" {} + | awk '{s+=$1} END {print s}')
        echo "- **$(echo $category | tr '[:lower:]' '[:upper:]')**: $count rules" >> "$OUTPUT_FILE"
    fi
done

echo "" >> "$OUTPUT_FILE"

# Add enforcement context
cat >> "$OUTPUT_FILE" << 'EOF'
## Enforcement Context

All rules are executed in the following contexts:

### Local Development
- **Config**: `.semgrep/config/local.yaml`
- **Enforcement**: All findings are warnings (non-blocking)
- **Purpose**: Early feedback without interrupting workflow

### Pull Request
- **Config**: `.semgrep/config/pr.yaml`
- **Enforcement**: ERROR blocks merge, WARNING allows merge
- **Purpose**: Quality gate before integration

### Main Branch
- **Config**: `.semgrep/config/main.yaml`
- **Enforcement**: ERROR blocks push, WARNING logged
- **Purpose**: Protection of main branch + metrics collection

---

## Documentation

- [Semgrep Developer Guide](file:///PROJECT_ROOT/docs/semgrep-guide.md)
- [Immutability Validator](file:///PROJECT_ROOT/.semgrep/docs/immutability-validator.md)
- [Exception Guidelines](file:///PROJECT_ROOT/.semgrep/docs/exception-guidelines.md)

---

**Generated by**: `.semgrep/scripts/generate-catalog.sh`
EOF

# Replace PROJECT_ROOT placeholder with actual path
sed -i "s|file:///PROJECT_ROOT|file://$PROJECT_ROOT|g" "$OUTPUT_FILE"

echo ""
echo "✅ Rule catalog generated successfully!"
echo "   Output: $OUTPUT_FILE"
echo "   Total rules: $TOTAL_RULES"
echo "   Total files: $TOTAL_FILES"
echo ""
echo "To view the catalog:"
echo "   cat $OUTPUT_FILE"

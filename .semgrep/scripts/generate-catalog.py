#!/usr/bin/env python3
"""
Semgrep Rule Catalog Generator

Generates a comprehensive markdown catalog of all Semgrep rules by parsing
YAML files in .semgrep/rules/ directory.
"""

import os
import yaml
import sys
from pathlib import Path
from datetime import datetime
from collections import defaultdict

def extract_rules_from_file(file_path):
    """Extract all rules from a Semgrep YAML file."""
    try:
        with open(file_path, 'r') as f:
            data = yaml.safe_load(f)
        
        if not data or 'rules' not in data:
            return []
        
        rules = []
        for rule in data['rules']:
            rule_info = {
                'id': rule.get('id', 'unknown'),
                'severity': rule.get('severity', 'UNKNOWN'),
                'message': rule.get('message', '').strip().split('\n')[0][:80],
                'file': str(file_path)
            }
            rules.append(rule_info)
        
        return rules
    except Exception as e:
        print(f"Error processing {file_path}: {e}", file=sys.stderr)
        return []

def generate_catalog(rules_dir, output_file):
    """Generate the rule catalog markdown file."""
    
    # Collect all rules by category
    rules_by_category = defaultdict(list)
    total_rules = 0
    total_files = 0
    severity_counts = defaultdict(int)
    
    # Walk through rules directory
    for root, dirs, files in os.walk(rules_dir):
        for file in sorted(files):
            if file.endswith('.yaml'):
                total_files += 1
                file_path = Path(root) / file
                category = Path(root).relative_to(rules_dir).parts[0] if Path(root) != rules_dir else 'other'
                
                rules = extract_rules_from_file(file_path)
                for rule in rules:
                    rules_by_category[category].append(rule)
                    severity_counts[rule['severity']] += 1
                    total_rules += 1
    
    # Generate markdown
    with open(output_file, 'w') as f:
        f.write("# Semgrep Rule Catalog\n\n")
        f.write("This document provides a comprehensive catalog of all Semgrep rules in the BudgetPro project.\n\n")
        f.write("**Auto-generated** - Do not edit manually. Run `.semgrep/scripts/generate-catalog.py` to update.\n\n")
        f.write("---\n\n")
        f.write(f"**Last Updated**: {datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S UTC')}\n\n")
        f.write(f"**Total Rules**: {total_rules}\n")
        f.write(f"**Total Rule Files**: {total_files}\n\n")
        
        # Write rules by category
        for category in sorted(rules_by_category.keys()):
            f.write(f"\n## {category.upper()}\n\n")
            f.write("| Rule ID | Severity | Description | File |\n")
            f.write("|---------|----------|-------------|------|\n")
            
            for rule in sorted(rules_by_category[category], key=lambda x: x['id']):
                severity_emoji = {
                    'ERROR': '🔴 ERROR',
                    'WARNING': '⚠️  WARNING',
                    'INFO': 'ℹ️  INFO'
                }.get(rule['severity'], rule['severity'])
                
                file_name = Path(rule['file']).name
                file_link = f"file://{rule['file']}"
                
                f.write(f"| `{rule['id']}` | {severity_emoji} | {rule['message']} | [{file_name}]({file_link}) |\n")
        
        # Write summary
        f.write("\n---\n\n")
        f.write("## Summary by Severity\n\n")
        f.write(f"- 🔴 **ERROR**: {severity_counts.get('ERROR', 0)} rules (blocking)\n")
        f.write(f"- ⚠️  **WARNING**: {severity_counts.get('WARNING', 0)} rules (non-blocking)\n")
        f.write(f"- ℹ️  **INFO**: {severity_counts.get('INFO', 0)} rules (informational)\n\n")
        
        f.write("## Summary by Category\n\n")
        for category in sorted(rules_by_category.keys()):
            count = len(rules_by_category[category])
            f.write(f"- **{category.upper()}**: {count} rules\n")
        
        # Write enforcement context
        f.write("\n## Enforcement Context\n\n")
        f.write("All rules are executed in the following contexts:\n\n")
        f.write("### Local Development\n")
        f.write("- **Config**: `.semgrep/config/local.yaml`\n")
        f.write("- **Enforcement**: All findings are warnings (non-blocking)\n")
        f.write("- **Purpose**: Early feedback without interrupting workflow\n\n")
        f.write("### Pull Request\n")
        f.write("- **Config**: `.semgrep/config/pr.yaml`\n")
        f.write("- **Enforcement**: ERROR blocks merge, WARNING allows merge\n")
        f.write("- **Purpose**: Quality gate before integration\n\n")
        f.write("### Main Branch\n")
        f.write("- **Config**: `.semgrep/config/main.yaml`\n")
        f.write("- **Enforcement**: ERROR blocks push, WARNING logged\n")
        f.write("- **Purpose**: Protection of main branch + metrics collection\n\n")
        
        # Write documentation links
        project_root = Path(rules_dir).parent
        f.write("---\n\n")
        f.write("## Documentation\n\n")
        f.write(f"- [Semgrep Developer Guide](file://{project_root}/docs/semgrep-guide.md)\n")
        f.write(f"- [Immutability Validator](file://{project_root}/.semgrep/docs/immutability-validator.md)\n")
        f.write(f"- [Exception Guidelines](file://{project_root}/.semgrep/docs/exception-guidelines.md)\n\n")
        f.write("---\n\n")
        f.write("**Generated by**: `.semgrep/scripts/generate-catalog.py`\n")

if __name__ == '__main__':
    script_dir = Path(__file__).parent
    project_root = script_dir.parent.parent
    rules_dir = project_root / '.semgrep' / 'rules'
    output_file = project_root / '.semgrep' / 'RULE_CATALOG.md'
    
    print(f"Generating Semgrep Rule Catalog...")
    print(f"Rules directory: {rules_dir}")
    print(f"Output file: {output_file}")
    
    generate_catalog(rules_dir, output_file)
    
    print(f"\n✅ Rule catalog generated successfully!")
    print(f"   Output: {output_file}")

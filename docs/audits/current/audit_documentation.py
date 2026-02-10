import re
import os
import glob
from collections import defaultdict

# Configuration
DOCS_DIR = 'docs/canonical/modules'
CODE_DIR = 'backend/src'
OUTPUT_PATH = 'docs/audits/current/SYNCHRONIZATION_AUDIT_REPORT.md'
INVENTORY_PATH = 'docs/audits/INVENTARIO_REGLAS_EXISTENTES_FASE1.md'

def get_documented_rules():
    """Scans canonical notebooks for REGLA-XXX tags."""
    documented_rules = defaultdict(list)
    
    # Get all markdown files in docs dir
    files = glob.glob(os.path.join(DOCS_DIR, '*.md'))
    
    print(f"Scanning {len(files)} document files...")
    
    for file_path in files:
        filename = os.path.basename(file_path)
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            # Find all REGLA-XXX
            matches = re.findall(r'(REGLA-\d{3})', content)
            for rule_id in matches:
                documented_rules[rule_id].append(filename)
                
    return documented_rules

def get_implemented_rules():
    """Scans source code for REGLA-XXX tags."""
    implemented_rules = defaultdict(list)
    
    print(f"Scanning code files in {CODE_DIR}...")
    
    for root, dirs, files in os.walk(CODE_DIR):
        for file in files:
            if file.endswith(('.java', '.sql', '.xml', '.yml', '.yaml')):
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()
                        matches = re.findall(r'(REGLA-\d{3})', content)
                        for rule_id in matches:
                            implemented_rules[rule_id].append(os.path.relpath(file_path, CODE_DIR))
                except Exception as e:
                    # Ignore binary or undecodable files
                    pass
                    
    return implemented_rules

def get_inventory_rules():
    """Reads the verifiable list of 161 rules to audit."""
    rules = set()
    if os.path.exists(INVENTORY_PATH):
        with open(INVENTORY_PATH, 'r', encoding='utf-8') as f:
            content = f.read()
            matches = re.findall(r'^## (REGLA-\d{3})', content, re.MULTILINE)
            rules.update(matches)
    return sorted(list(rules))

def generate_report(documented, implemented, inventory):
    inventory_set = set(inventory)
    documented_set = set(documented.keys())
    implemented_set = set(implemented.keys())
    
    # Analysis
    synchronized = inventory_set.intersection(documented_set).intersection(implemented_set)
    doc_drift = inventory_set - documented_set # Scheduled but not in docs
    code_drift = inventory_set - implemented_set # Scheduled but not in code (tagged)
    extra_documented = documented_set - inventory_set
    extra_implemented = implemented_set - inventory_set
    
    sync_percentage = (len(synchronized) / len(inventory)) * 100 if inventory else 0
    
    report = f"""# Documentation Synchronization Audit Report

**Date:** {os.popen('date').read().strip()}
**Audit Tool:** audit_documentation.py
**Rules Audited:** {len(inventory)}

## Executive Summary
- **Synchronized Rules:** {len(synchronized)}/{len(inventory)} ({sync_percentage:.1f}%)
- **Rules with Drift (Code):** {len(code_drift)}
- **Rules with Drift (Docs):** {len(doc_drift)}
- **Undocumented Code Rules:** {len(extra_implemented)} (Rules in code but not in Inventory)
- **Documented but Not Implemented:** {len(code_drift)}

## Synchronization Status

| Metric | Count | Details |
|--------|-------|---------|
| Total Rules in Inventory | {len(inventory)} | Baseline |
| Fully Synchronized | {len(synchronized)} | Verified in Docs & Code |
| Missing in Code (Drift) | {len(code_drift)} | Documented but tag missing in code |
| Missing in Docs (Drift) | {len(doc_drift)} | In inventory but not in canonical notebooks |

## Rules with Drift (Documentation ≠ Code)

### Code Drift (Documented but missing REGLA specific tag in code)
These rules are in the inventory/docs but the 'REGLA-XXX' tag wasn't found in the codebase.
This implies the code might implement it, but trace is missing.

| ID | Documentation Ref | Suggested Remediation |
|---|---|---|
"""
    
    for rule in sorted(code_drift):
        doc_refs = ", ".join(documented.get(rule, ["N/A"]))
        report += f"| {rule} | {doc_refs} | Add `{rule}` comment to implementation |\n"
        
    report += """
### Documentation Drift (In Inventory but NOT in Canonical Docs)
Should be 0 if Task 21 was successful.

| ID | Remediation |
|---|---|
"""
    for rule in sorted(doc_drift):
        report += f"| {rule} | Add to appropriate canonical notebook |\n"
        
    report += """
## Undocumented Code Rules (Should be 0)
Rules found in code but not present in the master inventory.

| ID | File Location |
|---|---|
"""
    for rule in sorted(extra_implemented):
         locs = ", ".join(implemented[rule][:2]) # Limit to 2 files
         report += f"| {rule} | {locs} |\n"

    report += """
## Remediation Plan
1.  **Tagging Campaign**: For the {len(code_drift)} rules missing code tags, add `// REGLA-XXX` comments to the implementation source.
2.  **Documentation Update**: For any rules missing from docs, add them immediately.
"""

    return report

if __name__ == "__main__":
    print("Starting Audit...")
    
    # 1. Get Baseline
    inventory = get_inventory_rules()
    print(f"Inventory contains {len(inventory)} rules.")
    
    # 2. Key Scans
    documented = get_documented_rules()
    print(f"Found {len(documented)} documented rules.")
    
    implemented = get_implemented_rules()
    print(f"Found {len(implemented)} implemented rules (with tags).")
    
    # 3. Report
    report_content = generate_report(documented, implemented, inventory)
    
    with open(OUTPUT_PATH, 'w', encoding='utf-8') as f:
        f.write(report_content)
        
    print(f"Audit Complete. Report generated at {OUTPUT_PATH}")

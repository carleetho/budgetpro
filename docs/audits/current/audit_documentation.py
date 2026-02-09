import re
import os
from collections import defaultdict

inventory_path = 'docs/audits/INVENTARIO_REGLAS_EXISTENTES_FASE1.md'
output_path = 'docs/audits/current/DOCUMENTATION_COVERAGE_REPORT.md'

def parse_inventory(file_path):
    rules = {}
    current_rule = None
    current_section = None
    
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    for line in lines:
        line = line.strip()
        
        # Detect Rule ID
        match = re.search(r'^## (REGLA-\d{3})', line)
        if match:
            current_rule = match.group(1)
            rules[current_rule] = {
                'id': current_rule,
                'modules': [],
                'origins': [],
                'description': ''
            }
            current_section = None
            continue
            
        if not current_rule:
            continue
            
        # Detect Sections
        if line.startswith('- Módulo(s) afectado(s)'):
            current_section = 'modules'
            continue
        elif line.startswith('- Origen técnico EXACTO:'):
            current_section = 'origin'
            continue
        elif line.startswith('- Descripción exacta de la regla'):
            current_section = 'description'
            continue
        elif line.startswith('- Evidencia:') or line.startswith('- Tipo:') or line.startswith('- Estado:'):
            current_section = None
            continue
            
        # Capture Content
        if current_section == 'modules':
            if line.startswith('- '):
                rules[current_rule]['modules'].append(line[2:].strip())
        elif current_section == 'origin':
            if line.startswith('- archivo:'):
                path = line.split('`')[1] if '`' in line else line.replace('- archivo:', '').strip()
                rules[current_rule]['origins'].append(path)
        elif current_section == 'description':
             if line.startswith('- '):
                rules[current_rule]['description'] = line[2:].strip()

    return rules

def analyze_coverage(rules):
    covered_count = 0
    missing_rules = []
    module_stats = defaultdict(lambda: {'total': 0, 'covered': 0})
    
    for rule_id, data in rules.items():
        is_covered = any('docs/' in origin or '.md' in origin for origin in data['origins'])
        
        if is_covered:
            covered_count += 1
        else:
            missing_rules.append(data)
            
        # Add to module stats
        # Normalize module names broadly
        for mod in data['modules']:
            mod_key = mod.split('/')[0].strip() # specialized handling if needed
            module_stats[mod_key]['total'] += 1
            if is_covered:
                module_stats[mod_key]['covered'] += 1
                
    return covered_count, missing_rules, module_stats

def generate_report(rules, covered_count, missing_rules, module_stats):
    total_rating = (covered_count / len(rules)) * 100 if rules else 0
    
    content = f"""# Documentation Coverage Audit Report

## Executive Summary

- **Audit Date**: 2026-02-08
- **Total Existing Rules**: {len(rules)}
- **Documented Rules (Canonical)**: {covered_count}
- **Undocumented Rules (Code-Only)**: {len(missing_rules)}
- **Documentation Coverage**: {total_rating:.1f}%

## Coverage Gap Analysis

The following rules exist in the codebase (as enforced by validators or logic) but are NOT explicitly referenced in the canonical documentation (`docs/modules/*.md`).

| Rule ID | Module | Description |
|---------|--------|-------------|
"""
    
    for rule in missing_rules:
        modules = ", ".join(rule['modules'])
        desc = rule['description'][:100] + "..." if len(rule['description']) > 100 else rule['description']
        content += f"| {rule['id']} | {modules} | {desc} |\n"

    content += """
## Coverage by Module

| Module | Total Rules | Documented | Coverage % |
|--------|-------------|------------|------------|
"""
    
    for mod, stats in sorted(module_stats.items()):
        total = stats['total']
        covered = stats['covered']
        pct = (covered / total) * 100 if total > 0 else 0
        content += f"| {mod} | {total} | {covered} | {pct:.1f}% |\n"
        
    content += """
## Recommendations

1.  **Backfill Canonical Specs**: The {len(missing_rules)} missing rules should be explicitly added to their respective `_SPECS.md` files.
2.  **Enforce Traceability**: Use the `REGLA-XXX` tags in the documentation to allow automated auditing in the future.
3.  **Sync Code and Docs**: Ensure that any change in validators is reflected in the canonical documents.
"""

    return content

if __name__ == "__main__":
    if not os.path.exists(inventory_path):
        print(f"Error: Inventory file not found at {inventory_path}")
        exit(1)
        
    rules = parse_inventory(inventory_path)
    covered, missing, stats = analyze_coverage(rules)
    report = generate_report(rules, covered, missing, stats)
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(report)
        
    print(f"Report generated at {output_path}")
    print(f"Coverage: {covered}/{len(rules)} ({covered/len(rules)*100:.1f}%)")

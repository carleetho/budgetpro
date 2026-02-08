#!/usr/bin/env python3
"""
AXIOM Domain Hardening Rule Generator
Dynamically generates Semgrep rules from .domain-validator.yaml configuration

Usage:
    python tools/generate_domain_rules.py                    # Generate rules
    python tools/generate_domain_rules.py --dry-run          # Print without writing
    python tools/generate_domain_rules.py --report-coverage  # Show coverage metrics
    python tools/generate_domain_rules.py --context presupuesto  # Enable specific context only

Author: AXIOM Hardening Infrastructure Team
Version: 1.0
"""

import argparse
import glob
import json
import sys
import yaml
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Set

# Constants
CONFIG_FILE = ".domain-validator.yaml"
OUTPUT_FILE = ".semgrep/generated-domain-hardening.yml"
DOMAIN_BASE_PATH = "backend/src/main/java/com/budgetpro/domain"


def load_config(config_path: str) -> Dict:
    """Load and parse the domain validator configuration"""
    try:
        with open(config_path, 'r', encoding='utf-8') as f:
            config = yaml.safe_load(f)
        print(f"✅ Configuration loaded from {config_path}")
        return config
    except FileNotFoundError:
        print(f"❌ ERROR: Configuration file not found: {config_path}", file=sys.stderr)
        sys.exit(1)
    except yaml.YAMLError as e:
        print(f"❌ ERROR: Invalid YAML in {config_path}: {e}", file=sys.stderr)
        sys.exit(1)


def discover_domain_files(file_patterns: List[str], exclusions: List[str] = None) -> Set[str]:
    """Discover domain Java files matching the given patterns"""
    files = set()
    exclusions = exclusions or []
    
    for pattern in file_patterns:
        # For patterns like "backend/src/.../**/*.java", we can use them directly
        # Path().glob() handles ** for recursive matching
        matched = list(Path(".").glob(pattern))
        files.update([str(f) for f in matched if f.is_file()])
    
    # Apply exclusions
    for exclusion in exclusions:
        excluded = list(Path(".").glob(exclusion + "/**/*.java"))
        excluded_files = set(str(f) for f in excluded if f.is_file())
        files -= excluded_files
    
    return files


def generate_entity_final_fields_rule(context: Dict, rule_config: Dict) -> Dict:
    """Generate Semgrep rule for entity final fields validation"""
    severity = rule_config['severity_strict'] if context['strict_mode'] else rule_config['severity_warning']
    context_name = context['name']
    
    message = rule_config['message'].replace('{context}', context_name)
    
    return {
        'id': f"budgetpro.domain.immutability.entity-final-fields.{context_name}",
        'patterns': [
            {'pattern': 'private $TYPE $FIELD;'},
            {'pattern-not': 'private final $TYPE $FIELD;'},
            {
                'pattern-inside': 
                    'class $CLASS {\n  ...\n}'
            }
        ],
        'paths': {
            'include': context['file_patterns'],
            'exclude': context.get(' exclusions', [])
        },
        'message': message,
        'languages': ['java'],
        'severity': severity,
        'metadata': {
            **rule_config.get('metadata', {}),
            'context': context_name,
            'strict_mode': context['strict_mode']
        }
    }


def generate_snapshot_no_setters_rule(context: Dict, rule_config: Dict) -> Dict:
    """Generate Semgrep rule for snapshot immutability validation"""
    severity = rule_config['severity_strict'] if context['strict_mode'] else rule_config['severity_warning']
    context_name = context['name']
    
    message = rule_config['message'].replace('{context}', context_name)
    
    return {
        'id': f"budgetpro.domain.immutability.snapshot-no-setters.{context_name}",
        'patterns': [
            {'pattern': 'public void $METHOD(...) { ... }'},
            {
                'metavariable-regex': {
                    'metavariable': '$METHOD',
                    'regex': '^set[A-Z].*'
                }
            },
            {
                'pattern-inside': 
                    'class $CLASS {\n  ...\n}'
            },
            {
                'metavariable-regex': {
                    'metavariable': '$CLASS',
                    'regex': '.*Snapshot'
                }
            }
        ],
        'paths': {
            'include': context['file_patterns'],
            'exclude': context.get('exclusions', [])
        },
        'message': message,
        'languages': ['java'],
        'severity': severity,
        'metadata': {
            **rule_config.get('metadata', {}),
            'context': context_name,
            'strict_mode': context['strict_mode']
        }
    }


def generate_valueobject_no_setters_rule(context: Dict, rule_config: Dict) -> Dict:
    """Generate Semgrep rule for value object immutability"""
    severity = rule_config['severity_strict'] if context['strict_mode'] else rule_config['severity_warning']
    context_name = context['name']
    
    message = rule_config['message'].replace('{context}', context_name)
    
    return {
        'id': f"budgetpro.domain.immutability.valueobject-no-setters.{context_name}",
        'patterns': [
            {'pattern': 'public void set$FIELD(...) { ... }'},
            {
                'pattern-inside': 
                    'class $CLASS {\n  ...\n}'
            }
        ],
        'paths': {
            'include': [p.replace("/**/*.java", "/**/model/*Id.java") for p in context['file_patterns']],
            'exclude': context.get('exclusions', [])
        },
        'message': message,
        'languages': ['java'],
        'severity': severity,
        'metadata': {
            **rule_config.get('metadata', {}),
            'context': context_name,
            'strict_mode': context['strict_mode'],
            'note': 'Targets Id classes and value objects in model packages'
        }
    }


def generate_collection_encapsulation_rule(context: Dict, rule_config: Dict) -> Dict:
    """Generate Semgrep rule for collection encapsulation"""
    severity = rule_config['severity_strict'] if context['strict_mode'] else rule_config['severity_warning']
    context_name = context['name']
    
    message = rule_config['message'].replace('{context}', context_name)
    
    return {
        'id': f"budgetpro.domain.immutability.collection-encapsulation.{context_name}",
        'patterns': [
            {'pattern': 'return $COLLECTION;'},
            {
                'pattern-inside': 
                    'public List<$TYPE> get$METHOD() {\n  ...\n}'
            },
            {'pattern-not': 'return Collections.unmodifiableList(...);'},
            {'pattern-not': 'return List.copyOf(...);'}
        ],
        'paths': {
            'include': context['file_patterns'],
            'exclude': context.get('exclusions', [])
        },
        'message': message,
        'languages': ['java'],
        'severity': severity,
        'metadata': {
            **rule_config.get('metadata', {}),
            'context': context_name,
            'strict_mode': context['strict_mode']
        }
    }


RULE_GENERATORS = {
    'entity-final-fields': generate_entity_final_fields_rule,
    'snapshot-no-setters': generate_snapshot_no_setters_rule,
    'valueobject-no-setters': generate_valueobject_no_setters_rule,
    'collection-encapsulation': generate_collection_encapsulation_rule
}


def generate_rules(config: Dict, context_filter: List[str] = None) -> List[Dict]:
    """Generate all Semgrep rules from configuration"""
    rules = []
    bounded_contexts = config.get('bounded_contexts', [])
    immutability_rules = config.get('immutability_rules', [])
    
    # Filter contexts if specified
    if context_filter:
        bounded_contexts = [c for c in bounded_contexts if c['name'] in context_filter]
    
    for context in bounded_contexts:
        for rule_config in immutability_rules:
            rule_id = rule_config['id']
            if rule_id in RULE_GENERATORS:
                generator = RULE_GENERATORS[rule_id]
                rule = generator(context, rule_config)
                rules.append(rule)
            else:
                print(f"⚠️  WARNING: No generator for rule type: {rule_id}", file=sys.stderr)
    
    return rules


def write_rules(rules: List[Dict], output_path: str):
    """Write generated rules to Semgrep YAML file"""
    output = {
        'rules': rules
    }
    
    # Add header comment
    header_comment = f"""# Auto-generated by tools/generate_domain_rules.py
# DO NOT EDIT MANUALLY - Regenerate using: python tools/generate_domain_rules.py
# Generated on: {datetime.now().isoformat()}
# Configuration: .domain-validator.yaml

"""
    
    # Ensure output directory exists
    output_dir = Path(output_path).parent
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Write YAML
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(header_comment)
        yaml.dump(output, f, default_flow_style=False, sort_keys=False, allow_unicode=True)
    
    print(f"✅ Generated {len(rules)} rules → {output_path}")


def calculate_coverage(config: Dict) -> Dict:
    """Calculate hardening coverage metrics"""
    total_files = 0
    hardened_files = 0
    context_metrics = []
    
    for context in config.get('bounded_contexts', []):
        files = discover_domain_files(context['file_patterns'], context.get('exclusions', []))
        num_files = len(files)
        total_files += num_files
        
        if context['strict_mode']:
            hardened_files += num_files
        
        context_metrics.append({
            'context': context['name'],
            'files': num_files,
            'strict_mode': context['strict_mode'],
            'coverage': '100%' if context['strict_mode'] else '0%'
        })
    
    coverage_pct = (hardened_files / total_files * 100) if total_files > 0 else 0
    
    return {
        'total_files': total_files,
        'hardened_files': hardened_files,
        'coverage_percentage': round(coverage_pct, 1),
        'contexts': context_metrics
    }


def report_coverage(config: Dict):
    """Print coverage report to stdout"""
    metrics = calculate_coverage(config)
    
    print("\n" + "="*60)
    print("📊 AXIOM DOMAIN HARDENING COVERAGE REPORT")
    print("="*60)
    print(f"\n**Overall**: {metrics['hardened_files']}/{metrics['total_files']} files ({metrics['coverage_percentage']}%)")
    print(f"**Generated**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
    
    print("| Context         | Files | Hardened | Status |")
    print("|-----------------|-------|----------|--------|")
    
    for ctx in metrics['contexts']:
        status = "✅ Strict" if ctx['strict_mode'] else "⬜ Planned"
        print(f"| {ctx['context']:<15} | {ctx['files']:>5} | {ctx['coverage']:>8} | {status} |")
    
    print("\n" + "="*60 + "\n")
    
    # Output JSON for CI parsing
    print("\n### JSON_METRICS (for CI parsing)")
    print(json.dumps(metrics, indent=2))


def main():
    parser = argparse.ArgumentParser(
        description="Generate AXIOM domain hardening Semgrep rules from configuration"
    )
    parser.add_argument(
        '--dry-run',
        action='store_true',
        help="Print rules without writing to file"
    )
    parser.add_argument(
        '--report-coverage',
        action='store_true',
        help="Display coverage metrics"
    )
    parser.add_argument(
        '--context',
        action='append',
        dest='contexts',
        help="Generate rules for specific context(s) only"
    )
    parser.add_argument(
        '--config',
        default=CONFIG_FILE,
        help=f"Configuration file path (default: {CONFIG_FILE})"
    )
    parser.add_argument(
        '--output',
        default=OUTPUT_FILE,
        help=f"Output file path (default: {OUTPUT_FILE})"
    )
    
    args = parser.parse_args()
    
    # Load configuration
    config = load_config(args.config)
    
    # Report coverage if requested
    if args.report_coverage:
        report_coverage(config)
        return
    
    # Generate rules
    print(f"\n🔧Generating domain hardening rules...")
    rules = generate_rules(config, args.contexts)
    print(f"✅ Generated {len(rules)} Semgrep rules")
    
    # Write or print
    if args.dry_run:
        print("\n" + "="*60)
        print("DRY-RUN: Generated rules (not written to file)")
        print("="*60)
        print(yaml.dump({'rules': rules}, default_flow_style=False, sort_keys=False))
    else:
        write_rules(rules, args.output)
        
        # Show brief coverage
        metrics = calculate_coverage(config)
        print(f"\n📊 Coverage: {metrics['hardened_files']}/{metrics['total_files']} files ({metrics['coverage_percentage']}%)")


if __name__ == "__main__":
    main()

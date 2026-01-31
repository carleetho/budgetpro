import os
import json
import argparse
from collections import defaultdict

def discover_domain_files(base_path, expected_contexts=None):
    """
    Recursively scans the domain directory and groups .java files by bounded context.
    """
    if expected_contexts is None:
        expected_contexts = {"finanzas", "proyecto", "catalogo", "recurso", "rrhh", "logistica", "shared"}
    
    domain_path = os.path.join(base_path, "backend/src/main/java/com/budgetpro/domain")
    if not os.path.exists(domain_path):
        print(f"Error: Domain path not found at {domain_path}")
        return None

    inventory = defaultdict(list)
    metrics = defaultdict(int)
    
    discovered_contexts = set()

    for root, _, files in os.walk(domain_path):
        for file in files:
            if file.endswith(".java"):
                full_path = os.path.join(root, file)
                # Extract relative path from domain root
                rel_path = os.path.relpath(full_path, domain_path)
                # The first part of the relative path is the bounded context
                context = rel_path.split(os.sep)[0]
                
                inventory[context].append({
                    "name": file,
                    "path": full_path,
                    "relative_path": rel_path
                })
                metrics[context] += 1
                discovered_contexts.add(context)

    # Validate all expected contexts are discovered (at least check if they exist in results)
    missing_contexts = expected_contexts - discovered_contexts
    
    result = {
        "metadata": {
            "base_path": base_path,
            "domain_path": domain_path,
            "total_files": sum(metrics.values()),
            "expected_contexts": list(expected_contexts),
            "discovered_contexts": list(discovered_contexts),
            "missing_contexts": list(missing_contexts)
        },
        "metrics": dict(metrics),
        "inventory": dict(inventory)
    }
    
    return result

def main():
    parser = argparse.ArgumentParser(description="Discover and group domain Java files.")
    parser.add_argument("--repo-root", required=True, help="Path to the repository root")
    parser.add_argument("--config", default=".domain-validator.yaml", help="Path to configuration file")
    parser.add_argument("--output", default="domain_inventory.json", help="Output JSON file")
    
    args = parser.parse_args()
    
    # Load configuration
    config_path = os.path.join(args.repo_root, args.config)
    if os.path.exists(config_path):
        import yaml
        with open(config_path, 'r') as f:
            config = yaml.safe_load(f)
        domain_config = config.get('domain', {})
        expected_contexts = set(domain_config.get('expected_contexts', []))
    else:
        # Fallback to defaults if no config file
        expected_contexts = {"finanzas", "proyecto", "catalogo", "recurso", "rrhh", "logistica", "shared"}
    
    print(f"Scanning domain in {args.repo_root}...")
    print(f"Expected contexts: {', '.join(sorted(expected_contexts))}")
    
    result = discover_domain_files(args.repo_root, expected_contexts)
    
    print(f"Scanning domain in {args.repo_root}...")
    result = discover_domain_files(args.repo_root)
    
    if result:
        with open(args.output, "w") as f:
            json.dump(result, f, indent=2)
        
        print(f"Discovery complete. Found {result['metadata']['total_files']} files.")
        print("Summary per context:")
        for context, count in result['metrics'].items():
            print(f"  - {context}: {count} files")
        
        if result['metadata']['missing_contexts']:
            print(f"WARNING: Missing expected contexts: {', '.join(result['metadata']['missing_contexts'])}")
        
        print(f"Inventory saved to {args.output}")

if __name__ == "__main__":
    main()

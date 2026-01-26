import os
import json
import argparse
from collections import defaultdict

def discover_domain_files(base_path):
    """
    Recursively scans the domain directory and groups .java files by bounded context.
    """
    domain_path = os.path.join(base_path, "backend/src/main/java/com/budgetpro/domain")
    if not os.path.exists(domain_path):
        print(f"Error: Domain path not found at {domain_path}")
        return None

    inventory = defaultdict(list)
    metrics = defaultdict(int)
    
    # Expected bounded contexts
    expected_contexts = {"finanzas", "proyecto", "catalogo", "recurso", "rrhh", "logistica", "shared"}
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
    parser.add_argument("--repo-root", default="/home/wazoox/Desktop/budgetpro-backend", help="Path to the repository root")
    parser.add_argument("--output", default="domain_inventory.json", help="Output JSON file")
    
    args = parser.parse_args()
    
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

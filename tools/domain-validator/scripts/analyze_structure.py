import os
import json
import re
import argparse
from collections import defaultdict

# Structural Audit Patterns
IMPL_PATTERN = re.compile(r"(\w+)(Impl|Implementation)\.java$")

def generate_relocation_commands(file_info, context):
    """
    Generates mv and sed commands for relocating a misplaced implementation file.
    """
    file_name = file_info["name"]
    rel_path = file_info["relative_path"] # e.g. finanzas/presupuesto/service/IntegrityHashServiceImpl.java
    
    # Extract package path from relative path (remove file name and convert to dots)
    pkg_parts = rel_path.split("/")[:-1]
    old_pkg = ".".join(["com.budgetpro.domain"] + pkg_parts)
    new_pkg = f"com.budgetpro.infrastructure.service.{context}"
    
    src_base = "backend/src/main/java"
    old_path = f"{src_base}/com/budgetpro/domain/{rel_path}"
    new_dir = f"{src_base}/com/budgetpro/infrastructure/service/{context}"
    new_path = f"{new_dir}/{file_name}"
    
    mv_cmd = f"mkdir -p {new_dir} && mv {old_path} {new_path}"
    sed_cmd = f"sed -i 's/package {old_pkg};/package {new_pkg};/' {new_path}"
    
    return {
        "destination_path": new_path,
        "new_package": new_pkg,
        "mv_command": mv_cmd,
        "sed_command": sed_cmd
    }

def analyze_structure(inventory):
    violations = []
    summary = defaultdict(int)
    
    for context, files in inventory.items():
        for file_info in files:
            name = file_info["name"]
            if IMPL_PATTERN.match(name):
                # Implementation file found in domain
                relocation = generate_relocation_commands(file_info, context)
                
                violations.append({
                    "context": context,
                    "file": name,
                    "path": file_info["path"],
                    "violation_type": "Ubicación Incorrecta",
                    "severity": "HIGH",
                    "relocation": relocation
                })
                summary[context] += 1
                
    return violations, summary

def main():
    parser = argparse.ArgumentParser(description="Analyze domain structure for misplaced implementation files.")
    parser.add_argument("--input", default="domain_inventory.json", help="Input inventory JSON file")
    parser.add_argument("--output", default="structure_report.json", help="Output report JSON file")
    
    args = parser.parse_args()
    
    if not os.path.exists(args.input):
        print(f"Error: Input file {args.input} not found.")
        return

    with open(args.input, "r") as f:
        data = json.load(f)

    inventory = data.get("inventory", {})
    
    print("Analyzing files for structural violations...")
    violations, summary = analyze_structure(inventory)

    result = {
        "metadata": {
            "total_files_analyzed": data["metadata"]["total_files"],
            "total_violations_found": len(violations),
            "affected_contexts": list(summary.keys())
        },
        "summary_per_context": dict(summary),
        "violations": violations
    }

    with open(args.output, "w") as f:
        json.dump(result, f, indent=2)

    print(f"Analysis complete. Found {len(violations)} structural violations.")
    for context, count in summary.items():
        print(f"  - {context}: {count} violations")
    
    print(f"Structure report saved to {args.output}")

if __name__ == "__main__":
    main()

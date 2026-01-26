import os
import json
import re
import argparse
from collections import defaultdict

# Regex Patterns
# Support sub-packages like com.budgetpro.domain.finanzas.presupuesto.model
IMPORT_PATTERN = re.compile(r"^import\s+(com\.budgetpro\.domain\.([\w.]+)\.model\.(\w+));")
FIELD_PATTERN = re.compile(r"^\s*private\s+(\w+)\s+(\w+);")

def identify_aggregates(inventory):
    """
    Identifies potential aggregate roots based on naming convention (Class and ClassId exist).
    """
    aggregates = defaultdict(set)
    for context, files in inventory.items():
        file_names = {f["name"].replace(".java", "") for f in files}
        for name in file_names:
            if f"{name}Id" in file_names:
                aggregates[context].add(name)
    return aggregates

def generate_refactoring_hint(context, file_name, violation_type, detail):
    """
    Generates refactoring snippets for aggregate coupling.
    """
    if violation_type == "AGGREGATE_COUPLING":
        field_type = detail["type"]
        field_name = detail["name"]
        id_type = f"{field_type}Id"
        id_field_name = f"{field_name}Id" if not field_name.endswith("Id") else field_name
        
        return {
            "issue": f"Direct reference to aggregate '{field_type}'",
            "refactored_domain": f"private {id_type} {id_field_name};",
            "application_logic": f"// In Application Service:\n// var {field_name} = {field_type.lower()}Repository.findById({id_field_name})\n//     .orElseThrow(() -> new EntityNotFoundException(...));"
        }
    return None

def analyze_coupling(inventory, aggregate_map):
    violations = []
    summary = defaultdict(int)
    
    # Flatten all aggregates for quick lookup
    all_aggregates = {}
    for ctx, aggs in aggregate_map.items():
        for agg in aggs:
            all_aggregates[agg] = ctx

    for context, files in inventory.items():
        for file_info in files:
            path = file_info["path"]
            file_violations = []
            
            try:
                with open(path, "r", encoding="utf-8") as f:
                    for i, line in enumerate(f, 1):
                        clean_line = line.strip()
                        
                        # 1. Detect Imports
                        import_match = IMPORT_PATTERN.match(clean_line)
                        if import_match:
                            imported_pkg = import_match.group(1)
                            full_ctx_path = import_match.group(2)
                            imported_ctx = full_ctx_path.split(".")[0] # Root context
                            imported_class = import_match.group(3)
                            
                            # Aggregate Coupling via Import
                            if imported_class in all_aggregates and imported_class != file_info["name"].replace(".java", ""):
                                # Filter out Repositories and Ports importing their own aggregate in the same context
                                is_port_repo = any(x in file_info["name"] for x in ["Repository", "Port"])
                                if not (is_port_repo and imported_ctx == context):
                                    file_violations.append({
                                        "type": "AGGREGATE_COUPLING",
                                        "severity": "HIGH",
                                        "line_number": i,
                                        "content": clean_line,
                                        "description": f"Import of aggregate root '{imported_class}'. Aggregates must only reference each other by ID."
                                    })
                            
                            # Cross-Context Entity Import (excluding IDs and Shared Kernel)
                            elif imported_ctx != context and imported_ctx != "shared" and not imported_class.endswith("Id"):
                                file_violations.append({
                                    "type": "CROSS_CONTEXT_IMPORT",
                                    "severity": "HIGH",
                                    "line_number": i,
                                    "content": clean_line,
                                    "description": f"Direct import of entity '{imported_class}' from context '{imported_ctx}'. Use IDs and Ports."
                                })
                        
                        # 2. Detect Direct Aggregate References in Fields
                        field_match = FIELD_PATTERN.match(line)
                        if field_match:
                            field_type = field_match.group(1)
                            field_name = field_match.group(2)
                            
                            if field_type in all_aggregates:
                                # It's an aggregate root.
                                if field_type != file_info["name"].replace(".java", ""):
                                    hint = generate_refactoring_hint(context, file_info["name"], "AGGREGATE_COUPLING", {"type": field_type, "name": field_name})
                                    file_violations.append({
                                        "type": "AGGREGATE_COUPLING",
                                        "severity": "HIGH",
                                        "line_number": i,
                                        "content": clean_line,
                                        "description": f"Direct object reference to aggregate '{field_type}'. Bounded contexts must only reference each other by ID.",
                                        "refactoring_hint": hint
                                    })

                if file_violations:
                    violations.append({
                        "context": context,
                        "file": file_info["name"],
                        "path": path,
                        "violations": file_violations
                    })
                    summary[context] += len(file_violations)

            except Exception as e:
                print(f"Error reading {path}: {e}")
                
    return violations, summary

def main():
    parser = argparse.ArgumentParser(description="Analyze cross-context aggregate coupling.")
    parser.add_argument("--input", default="domain_inventory.json", help="Input inventory JSON file")
    parser.add_argument("--output", default="coupling_report.json", help="Output report JSON file")
    
    args = parser.parse_args()
    
    if not os.path.exists(args.input):
        print(f"Error: Input file {args.input} not found.")
        return

    with open(args.input, "r") as f:
        data = json.load(f)

    inventory = data.get("inventory", {})
    
    print("Identifying Aggregate Roots...")
    aggregate_map = identify_aggregates(inventory)
    
    print("Analyzing coupling violations...")
    violations, summary = analyze_coupling(inventory, aggregate_map)

    result = {
        "metadata": {
            "total_files_analyzed": data["metadata"]["total_files"],
            "total_violations_found": sum(summary.values()),
            "identified_aggregates": {ctx: list(aggs) for ctx, aggs in aggregate_map.items()}
        },
        "summary_per_context": dict(summary),
        "violations": violations
    }

    with open(args.output, "w") as f:
        json.dump(result, f, indent=2)

    print(f"Analysis complete. Found {result['metadata']['total_violations_found']} coupling violations.")
    for context, count in summary.items():
        print(f"  - {context}: {count} violations")
    
    print(f"Coupling report saved to {args.output}")

if __name__ == "__main__":
    main()

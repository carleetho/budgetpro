import os
import json
import re
import argparse
from collections import defaultdict

# Violation Patterns
PATTERNS = {
    "VIOLATION_A": {
        "type": "SPRING_IMPORT",
        "regex": re.compile(r"^import\s+org\.springframework\..*;"),
        "description": "Critical: Spring Framework import in domain layer."
    },
    "VIOLATION_B": {
        "type": "INFRASTRUCTURE_IMPORT",
        "regex": re.compile(r"^import\s+com\.budgetpro\.infrastructure\..*;"),
        "description": "Critical: Infrastructure package import in domain layer."
    },
    "VIOLATION_C": {
        "type": "HEAVY_LIBRARY_IMPORT",
        "regex": re.compile(r"^import\s+(org\.apache\.poi|java\.sql|org\.apache\.http|com\.fasterxml\.jackson)\..*;"),
        "description": "Critical: Heavy third-party library or SQL import in domain layer."
    },
    "VIOLATION_D": {
        "type": "PERSISTENCE_ANNOTATION",
        "regex": re.compile(r"^\s*@(Entity|Table|Id|Column|ManyToOne|OneToMany)(\s|\(|$)"),
        "description": "Critical: JPA/Persistence annotation in domain layer."
    }
}

def analyze_file(file_path):
    violations = []
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            for i, line in enumerate(f, 1):
                clean_line = line.strip()
                if not clean_line:
                    continue
                
                for v_id, config in PATTERNS.items():
                    if config["regex"].search(clean_line if v_id != "VIOLATION_D" else line):
                        violations.append({
                            "type": config["type"],
                            "severity": "CRITICAL",
                            "line_number": i,
                            "content": clean_line,
                            "description": config["description"]
                        })
    except Exception as e:
        print(f"Error reading {file_path}: {e}")
    
    return violations

def main():
    parser = argparse.ArgumentParser(description="Analyze domain purity for Java files.")
    parser.add_argument("--input", default="domain_inventory.json", help="Input inventory JSON file")
    parser.add_argument("--output", default="purity_report.json", help="Output report JSON file")
    
    args = parser.parse_args()
    
    if not os.path.exists(args.input):
        print(f"Error: Input file {args.input} not found.")
        return

    with open(args.input, "r") as f:
        data = json.load(f)

    inventory = data.get("inventory", {})
    all_violations = []
    summary = defaultdict(int)

    print("Analyzing files for purity violations...")
    for context, files in inventory.items():
        for file_info in files:
            path = file_info["path"]
            violations = analyze_file(path)
            if violations:
                file_record = {
                    "context": context,
                    "file": file_info["name"],
                    "path": path,
                    "violations": violations
                }
                all_violations.append(file_record)
                summary[context] += len(violations)

    result = {
        "metadata": {
            "total_files_analyzed": data["metadata"]["total_files"],
            "total_violations_found": sum(summary.values()),
            "affected_contexts": list(summary.keys())
        },
        "summary_per_context": dict(summary),
        "violations": all_violations
    }

    with open(args.output, "w") as f:
        json.dump(result, f, indent=2)

    print(f"Analysis complete. Found {result['metadata']['total_violations_found']} violations.")
    for context, count in summary.items():
        print(f"  - {context}: {count} violations")
    
    print(f"Purity report saved to {args.output}")

if __name__ == "__main__":
    main()

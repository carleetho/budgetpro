
import csv
import os

# Define the file list path
FILE_LIST = "full_domain_list.txt"
OUTPUT_CSV = "docs/audits/2026-02-07_DOMAIN_FILE_INVENTORY.csv"

def get_bounded_context(path):
    # path starts with ./backend/src/main/java/com/budgetpro/domain/
    parts = path.split("/")
    if "domain" in parts:
        idx = parts.index("domain")
        if idx + 1 < len(parts):
            context = parts[idx + 1]
            subcontext = parts[idx + 2] if idx + 2 < len(parts) and parts[idx + 2] != "model" and parts[idx + 2] != "port" and parts[idx + 2] != "service" and "." not in parts[idx+2] else ""
            return f"{context}/{subcontext}" if subcontext else context
    return "unknown"

def is_hardened(path):
    # Hardened Rule 1: Presupuesto or Estimacion context (Critical)
    if "/domain/finanzas/presupuesto/" in path:
        return True, "Hardened (Critical Context)"
    if "/domain/finanzas/estimacion/" in path:
        return True, "Hardened (Critical Context)"
    
    # Hardened Rule 2: Snapshots (Error on Setters)
    if "Snapshot.java" in path:
        return True, "Hardened (Snapshot)"
        
    return False, "Unprotected"

def main():
    if not os.path.exists(FILE_LIST):
        print(f"Error: {FILE_LIST} not found")
        return

    with open(FILE_LIST, 'r') as f:
        files = [line.strip() for line in f if line.strip()]

    inventory = []
    stats = {
        "Total": 0,
        "Hardened": 0,
        "Unprotected": 0,
        "ByContext": {}
    }

    for path in files:
        context = get_bounded_context(path)
        hardened, status = is_hardened(path)
        
        inventory.append({
            "File Path": path,
            "Bounded Context": context,
            "Status": status
        })
        
        stats["Total"] += 1
        if hardened:
            stats["Hardened"] += 1
        else:
            stats["Unprotected"] += 1
            if context not in stats["ByContext"]:
                stats["ByContext"][context] = 0
            stats["ByContext"][context] += 1

    # Ensure output directory exists
    os.makedirs(os.path.dirname(OUTPUT_CSV), exist_ok=True)
    
    # Write CSV
    with open(OUTPUT_CSV, 'w', newline='') as csvfile:
        fieldnames = ["File Path", "Bounded Context", "Status"]
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        for row in inventory:
            writer.writerow(row)

    print(f"Inventory generated at {OUTPUT_CSV}")
    print("\n--- Statistics ---")
    print(f"Total Domain Files: {stats['Total']}")
    print(f"Hardened Files: {stats['Hardened']} ({stats['Hardened']/stats['Total']*100:.1f}%)")
    print(f"Unprotected Files: {stats['Unprotected']}")
    print("\n--- Unprotected by Context ---")
    for ctx, count in sorted(stats["ByContext"].items(), key=lambda x: x[1], reverse=True):
        print(f"{ctx}: {count}")

if __name__ == "__main__":
    main()

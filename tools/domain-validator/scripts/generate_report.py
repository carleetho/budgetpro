import json
import os
import argparse
from datetime import datetime

# Severity Emojis
SEVERITY = {
    "CRITICAL": "🔴 CRITICAL",
    "HIGH": "🟠 HIGH",
    "MEDIUM": "🟡 MEDIUM",
    "LOW": "🔵 LOW"
}

# Healthy Emojis
HEALTH = {
    "CLEAN": "✅ CLEAN",
    "STABLE": "🟠 STABLE",
    "CRITICAL": "🔴 CRITICAL"
}

def load_json(path):
    if os.path.exists(path):
        with open(path, "r") as f:
            return json.load(f)
    return None

def generate_report(inventory_data, purity_data, structure_data, coupling_data):
    all_contexts = ["finanzas", "proyecto", "catalogo", "recurso", "rrhh", "logistica", "shared"]
    
    # 1. Aggregate Violations
    aggregated_violations = []
    
    # Purity
    if purity_data:
        for file_rec in purity_data["violations"]:
            for v in file_rec["violations"]:
                aggregated_violations.append({
                    "context": file_rec["context"],
                    "file": file_rec["file"],
                    "type": v["type"],
                    "detail": v["content"],
                    "severity": v["severity"],
                    "action": "Extraer a Infrastructure/Port o eliminar framework."
                })

    # Structure
    if structure_data:
        for v in structure_data["violations"]:
            aggregated_violations.append({
                "context": v["context"],
                "file": v["file"],
                "type": v["violation_type"],
                "detail": f"Misplaced Impl in domain.",
                "severity": v["severity"],
                "action": f"Relocate to infrastructure.",
                "relocation": v["relocation"]
            })

    # Coupling
    if coupling_data:
        for file_rec in coupling_data["violations"]:
            for v in file_rec["violations"]:
                aggregated_violations.append({
                    "context": file_rec["context"],
                    "file": file_rec["file"],
                    "type": v["type"],
                    "detail": v["content"],
                    "severity": v["severity"],
                    "action": "Relacionar via ID-Reference.",
                    "refactoring": v.get("refactoring_hint")
                })

    # Sort: CRITICAL -> HIGH
    aggregated_violations.sort(key=lambda x: (x["severity"] != "CRITICAL", x["severity"] != "HIGH"))

    # 2. Build Markdown
    lines = []
    lines.append("# Domain Audit Report: Technical Debt & Architectural Purity")
    lines.append(f"*Fecha de Auditoría: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}*")
    lines.append("")
    lines.append("## Executive Summary")
    lines.append("Este informe consolida las violaciones de la arquitectura de cebolla (Onion Architecture) detectadas en la capa de dominio.")
    lines.append("")

    # Section 1: Violation Matrix
    lines.append("## 1. Violation Matrix")
    lines.append("| Módulo | Archivo | Tipo Violación | Detalle Técnico | Severidad | Acción Correctiva |")
    lines.append("| :--- | :--- | :--- | :--- | :--- | :--- |")
    for v in aggregated_violations:
        sev_label = SEVERITY.get(v["severity"], v["severity"])
        lines.append(f"| {v['context']} | {v['file']} | {v['type']} | `{v['detail']}` | {sev_label} | {v['action']} |")
    lines.append("")

    # Section 2: Health Status
    lines.append("## 2. Health Status by Bounded Context")
    lines.append("| Bounded Context | Status | Violations Breakdown | Risk Level |")
    lines.append("| :--- | :--- | :--- | :--- |")
    
    for ctx in all_contexts:
        ctx_violations = [v for v in aggregated_violations if v["context"] == ctx]
        critical_count = len([v for v in ctx_violations if v["severity"] == "CRITICAL"])
        high_count = len([v for v in ctx_violations if v["severity"] == "HIGH"])
        total = len(ctx_violations)
        
        status = HEALTH["CLEAN"]
        if critical_count > 0: status = HEALTH["CRITICAL"]
        elif total > 0: status = HEALTH["STABLE"]
        
        breakdown = f"Total: {total} (🔴{critical_count}, 🟠{high_count})"
        risk = "HIGH" if critical_count > 0 else ("MEDIUM" if high_count > 0 else "LOW")
        
        lines.append(f"| **{ctx}** | {status} | {breakdown} | {risk} |")
    lines.append("")

    # Section 3: Refactoring Action Plan
    lines.append("## 3. Refactoring Action Plan")
    
    # Subsection: Structural
    lines.append("### 3.1 File Relocations (Structural Fixes)")
    relocations = [v for v in aggregated_violations if "relocation" in v]
    if relocations:
        lines.append("Las siguientes clases concretas deben moverse a la capa de infraestructura:")
        lines.append("```bash")
        for v in relocations:
            lines.append(f"# Violación: {v['file']} en dominio")
            lines.append(f"{v['relocation']['mv_command']} && \\")
            lines.append(f"{v['relocation']['sed_command']}")
            lines.append("")
        lines.append("```")
    else:
        lines.append("No se detectaron archivos Impl mal ubicados.")

    # Subsection: Purity (Interface Extraction)
    lines.append("### 3.2 Observability Decoupling (Purity Fixes)")
    lines.append("Para resolver las violaciones de infraestructura (Purity Violations), se debe implementar el patrón Port:")
    lines.append("```java")
    lines.append("// 1. Definir interfaz en dominio")
    lines.append("package com.budgetpro.domain.shared.port.out;")
    lines.append("public interface DomainEventLogger {")
    lines.append("    void log(String message);")
    lines.append("}")
    lines.append("")
    lines.append("// 2. Inyectar en servicio de dominio")
    lines.append("public class IntegrityHashServiceImpl implements IntegrityHashService {")
    lines.append("    private final DomainEventLogger logger; // Decoupled")
    lines.append("    ...")
    lines.append("}")
    lines.append("```")

    # Subsection: Coupling
    lines.append("### 3.3 Aggregate Decoupling")
    couplings = [v for v in aggregated_violations if v["type"] == "AGGREGATE_COUPLING"]
    if couplings:
        lines.append("Se detectaron acoplamientos directos entre agregados. Aplicar patrón ID-Reference:")
        for v in couplings[:2]: # Show first 2 as examples
            h = v["refactoring"]
            if h:
                lines.append(f"#### {v['file']} -> {h['issue']}")
                lines.append("**Refactored Domain Field:**")
                lines.append(f"```java\n{h['refactored_domain']}\n```")
                lines.append("**Application Orchestration:**")
                lines.append(f"```java\n{h['application_logic']}\n```")
    
    return "\n".join(lines)

def main():
    parser = argparse.ArgumentParser(description="Generate consolidated audit report.")
    parser.add_argument("--inventory", default="domain_inventory.json")
    parser.add_argument("--purity", default="purity_report.json")
    parser.add_argument("--structure", default="structure_report.json")
    parser.add_argument("--coupling", default="coupling_report.json")
    parser.add_argument("--output", default="DOMAIN_AUDIT_REPORT.md")
    
    args = parser.parse_args()
    
    inventory = load_json(args.inventory)
    purity = load_json(args.purity)
    structure = load_json(args.structure)
    coupling = load_json(args.coupling)
    
    if not inventory:
        print("Error: Inventory file required.")
        return

    content = generate_report(inventory, purity, structure, coupling)
    
    with open(args.output, "w") as f:
        f.write(content)
    
    print(f"Consolidated report generated: {args.output}")

if __name__ == "__main__":
    main()

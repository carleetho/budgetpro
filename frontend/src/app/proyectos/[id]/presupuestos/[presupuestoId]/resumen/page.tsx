"use client";

import { usePresupuestoWorkspace } from "@/modules/presupuestos/context/PresupuestoWorkspaceContext";
import { PresupuestoTotalesSummary } from "@/modules/presupuestos/components/PresupuestoTotalesSummary";

/**
 * Totales y estado del presupuesto (vista de lectura rápida).
 */
export default function PresupuestoResumenPage() {
  const { budget } = usePresupuestoWorkspace();

  if (!budget) {
    return null;
  }

  return (
    <PresupuestoTotalesSummary costoTotal={budget.costoTotal} precioVenta={budget.precioVenta} />
  );
}

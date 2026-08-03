"use client";

import {
  PresupuestoWorkspaceProvider,
  PresupuestoWorkspaceShell,
} from "@/modules/presupuestos/context/PresupuestoWorkspaceContext";

/**
 * Workspace del presupuesto: contexto compartido + cabecera + subnavegación (Resumen | Partidas | Parámetros).
 */
export default function PresupuestoWorkspaceLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <PresupuestoWorkspaceProvider>
      <PresupuestoWorkspaceShell>{children}</PresupuestoWorkspaceShell>
    </PresupuestoWorkspaceProvider>
  );
}

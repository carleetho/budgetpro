"use client";

import { PresupuestoParametrosForm } from "@/modules/presupuestos/components/PresupuestoParametrosForm";
import { usePresupuestoWorkspace } from "@/modules/presupuestos/context/PresupuestoWorkspaceContext";

/** Parámetros económicos (borrador local en navegador; sin PATCH de cabecera en API). */
export default function PresupuestoParametrosPage() {
  const {
    presupuestoId,
    proyectoId,
    budget,
    proyecto,
    proyectoLoading,
    readOnly,
  } = usePresupuestoWorkspace();

  if (!budget) {
    return null;
  }

  return (
    <PresupuestoParametrosForm
      presupuestoId={presupuestoId}
      proyectoId={proyectoId}
      nombrePresupuestoApi={budget.nombre}
      estado={budget.estado}
      precioVentaApi={budget.precioVenta}
      proyecto={proyecto}
      proyectoLoading={proyectoLoading}
      readOnly={readOnly}
    />
  );
}

"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { PresupuestoDetail } from "@/modules/presupuestos/views/PresupuestoDetail";

/**
 * Detalle 1:N de un presupuesto del proyecto (`DATA_MODEL_CURRENT` §3.1).
 * Superficie: `GET /presupuestos/{id}` + WBS + aprobar.
 */
export default function PresupuestoDetailPage() {
  const params = useParams();
  const proyectoId = params.id as string;
  const presupuestoId = params.presupuestoId as string;

  return (
    <div className="space-y-4">
      <Button variant="outline" size="sm" asChild>
        <Link href={`/proyectos/${proyectoId}#presupuestos`}>← Volver a presupuestos</Link>
      </Button>
      <PresupuestoDetail presupuestoId={presupuestoId} />
    </div>
  );
}

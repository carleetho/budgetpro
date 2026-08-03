"use client";

import { useCallback, useState } from "react";
import { toast } from "sonner";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { APUDesigner } from "@/modules/presupuestos/components/APUDesigner";
import { PartidasTreeGrid } from "@/modules/presupuestos/components/PartidasTreeGrid";
import { usePresupuestoWorkspace } from "@/modules/presupuestos/context/PresupuestoWorkspaceContext";
import { partidaResponseToItemPresupuesto } from "@/modules/presupuestos/utils/partida-to-item-presupuesto";
import type { PartidaResponseDto } from "@/core/types/presupuesto-contract";
import type { AnalisisUnitario } from "@/core/types/apu";
import { ApuApiService } from "@/services/apu-api.service";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

/** Árbol WBS del presupuesto (partidas) + flujo de APU en hojas. */
export default function PresupuestoPartidasPage() {
  const { budget, wbs, readOnly, openAddChild, reload } = usePresupuestoWorkspace();

  const [apuOpen, setApuOpen] = useState(false);
  const [apuTarget, setApuTarget] = useState<PartidaResponseDto | null>(null);

  const handleAssignApu = useCallback((partida: PartidaResponseDto) => {
    setApuTarget(partida);
    setApuOpen(true);
  }, []);

  const handleSaveApu = useCallback(
    async (apu: AnalisisUnitario) => {
      if (!apuTarget) return;
      try {
        await ApuApiService.crear(apuTarget.id, {
          rendimiento: apu.rendimientoDiario ?? null,
          unidad: apuTarget.unidad ?? null,
          insumos: apu.detalles.map((d) => ({
            recursoId: d.recursoId,
            cantidad: d.rendimiento,
            precioUnitario: d.precio,
          })),
        });
        toast.success("APU registrado.");
        await reload();
        setApuOpen(false);
        setApuTarget(null);
      } catch (e) {
        console.error(e);
        if (BudgetProApiError.isInstance(e)) {
          toast.error(`[${e.businessCode}] ${e.message}`);
        } else {
          toast.error("No se pudo guardar el APU.");
        }
        throw e;
      }
    },
    [apuTarget, reload]
  );

  /** No limpiamos `apuTarget` al cerrar para evitar desmontar el diálogo antes del cierre animado; se resetea tras guardar OK. */
  const handleApuDialogChange = useCallback((open: boolean) => {
    setApuOpen(open);
  }, []);

  if (!budget) {
    return null;
  }

  const itemMapped = apuTarget ? partidaResponseToItemPresupuesto(apuTarget) : null;

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle>Partidas</CardTitle>
          <CardDescription>
            Estructura jerárquica del presupuesto. En cada partida hoja puedes abrir el análisis de precio
            unitario (APU).
            {budget.estado === "CONGELADO" && (
              <span className="text-amber-700 dark:text-amber-400 font-medium ml-1">
                Solo lectura: presupuesto congelado.
              </span>
            )}
            {budget.estado === "INVALIDADO" && (
              <span className="text-destructive font-medium ml-1">Presupuesto invalidado.</span>
            )}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <PartidasTreeGrid
            nodes={wbs}
            readOnly={readOnly}
            onAddChild={openAddChild}
            onAssignApu={handleAssignApu}
          />
        </CardContent>
      </Card>

      {itemMapped && (
        <APUDesigner
          key={apuTarget?.id}
          open={apuOpen}
          onOpenChange={handleApuDialogChange}
          partida={itemMapped}
          onSave={handleSaveApu}
        />
      )}
    </>
  );
}

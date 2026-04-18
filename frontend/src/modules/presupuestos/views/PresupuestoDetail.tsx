"use client";

import { useCallback, useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Loader2, ShieldCheck } from "lucide-react";
import { toast } from "sonner";
import { PartidasTreeGrid } from "@/modules/presupuestos/components/PartidasTreeGrid";
import { apiClient } from "@/services/api-client";
import { PresupuestoApiService } from "@/services/presupuesto-api.service";
import { PartidasWbsService } from "@/services/partidas-wbs.service";
import { BudgetNodeDialog } from "@/modules/presupuestos/components/BudgetNodeDialog";
import type { PresupuestoResponseDto, WbsNodeResponseDto } from "@/core/types/presupuesto-contract";
import type { CrearItemPresupuestoCommand } from "@/core/types/presupuesto";
import { isPresupuestoWbsReadOnly } from "@/core/types/presupuesto-contract";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

export interface PresupuestoDetailProps {
  presupuestoId: string;
  /** Opcional: `POST /api/v1/partidas` con `padreId` (superficie real; GF-02 sin DELETE/PUT). */
  onAddWbsChild?: (padrePartidaId: string) => void;
  onAssignApu?: (partidaId: string) => void;
}

function badgeVariant(
  estado: PresupuestoResponseDto["estado"]
): "default" | "secondary" | "destructive" | "outline" {
  switch (estado) {
    case "CONGELADO":
      return "secondary";
    case "INVALIDADO":
      return "destructive";
    default:
      return "outline";
  }
}

/**
 * Vista detalle presupuesto + WBS.
 * - [P-01]: si `estado === CONGELADO`, la grilla y acciones estructurales quedan en solo lectura.
 * - Errores 400/409/412/422: `BudgetProApiError` con `businessCode` → toast.
 */
export function PresupuestoDetail({
  presupuestoId,
  onAddWbsChild,
  onAssignApu,
}: PresupuestoDetailProps) {
  const [budget, setBudget] = useState<PresupuestoResponseDto | null>(null);
  const [wbs, setWbs] = useState<WbsNodeResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [approving, setApproving] = useState(false);

  // Dialog State
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [selectedPadreId, setSelectedPadreId] = useState<string | null>(null);
  const [selectedPadreLevel, setSelectedPadreLevel] = useState<number>(0);

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const [b, tree] = await Promise.all([
        PresupuestoApiService.obtenerPorId(presupuestoId),
        PartidasWbsService.obtenerArbol(presupuestoId),
      ]);
      setBudget(b);
      setWbs(tree);
    } catch (e) {
      console.error(e);
      if (BudgetProApiError.isInstance(e)) {
        toast.error(`[${e.businessCode}] ${e.message}`);
      } else {
        toast.error("No se pudo cargar el presupuesto o el WBS.");
      }
    } finally {
      setLoading(false);
    }
  }, [presupuestoId]);

  useEffect(() => {
    void loadAll();
  }, [loadAll]);

  const readOnly = budget ? isPresupuestoWbsReadOnly(budget.estado) : true;

  const handleAprobar = async () => {
    setApproving(true);
    try {
      await PresupuestoApiService.aprobar(presupuestoId);
      toast.success("Presupuesto aprobado (congelado).");
      await loadAll();
    } catch (e) {
      if (BudgetProApiError.isInstance(e)) {
        toast.error(`[${e.businessCode}] ${e.message}`);
      } else {
        toast.error("No se pudo aprobar el presupuesto.");
      }
    } finally {
      setApproving(false);
    }
  };

  const openAddChild = (padreId: string | null, padreLevel: number) => {
    setSelectedPadreId(padreId || null);
    setSelectedPadreLevel(padreLevel);
    setIsDialogOpen(true);
  };

  const handleCreatePartida = async (command: CrearItemPresupuestoCommand) => {
    try {
      // Map domain command to infrastructure request
      // Backend expects: Integer nivel (1-indexed), BigDecimal metrado (optional)
      const payload = {
        presupuestoId,
        padreId: command.padreId,
        item: command.codigo,
        descripcion: command.descripcion,
        unidad: command.unidad || null,
        metrado: command.metrado ?? 0,
        nivel: selectedPadreLevel + 1, // Integer nivel calculated from parent
      };

      await apiClient.post("/partidas", payload);
      toast.success("Item creado correctamente.");
      await loadAll();
    } catch (e) {
      console.error(e);
      toast.error("No se pudo crear el item.");
      throw e; // Propagate to dialog to keep it open on error
    }
  };

  if (loading && !budget) {
    return (
      <div className="flex items-center justify-center py-12 text-muted-foreground">
        <Loader2 className="h-8 w-8 animate-spin" />
        <span className="ml-2">Cargando presupuesto…</span>
      </div>
    );
  }

  if (!budget) {
    return (
      <p className="text-sm text-muted-foreground py-8 text-center">
        Presupuesto no disponible.
      </p>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">{budget.nombre}</h1>
          <p className="text-muted-foreground text-sm mt-1">
            Proyecto <span className="font-mono">{budget.proyectoId}</span>
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <Badge variant={badgeVariant(budget.estado)}>{budget.estado}</Badge>
          {budget.esContractual && (
            <Badge variant="default" className="gap-1">
              <ShieldCheck className="h-3 w-3" />
              Contractual
            </Badge>
          )}
          <Button
            type="button"
            onClick={() => void handleAprobar()}
            disabled={budget.estado !== "BORRADOR" || approving}
          >
            {approving ? <Loader2 className="h-4 w-4 animate-spin" /> : "Aprobar / congelar"}
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Resumen</CardTitle>
          <CardDescription>{'Campos expuestos por GET /presupuestos/{id}'}</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-2 sm:grid-cols-2 text-sm">
          <div>
            <span className="text-muted-foreground">Costo total (CD)</span>
            <p className="font-semibold font-mono">
              {Number(budget.costoTotal).toLocaleString("es-ES", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
              })}
            </p>
          </div>
          <div>
            <span className="text-muted-foreground">Precio de venta (PV)</span>
            <p className="font-semibold font-mono">
              {Number(budget.precioVenta).toLocaleString("es-ES", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
              })}
            </p>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>WBS (partidas)</CardTitle>
          <CardDescription>
            Árbol anidado desde `GET /partidas/wbs`.{" "}
            {budget.estado === "CONGELADO" && (
              <span className="text-amber-700 dark:text-amber-400 font-medium">
                Solo lectura: baseline congelada [P-01 / REGLA-046].
              </span>
            )}
            {budget.estado === "INVALIDADO" && (
              <span className="text-destructive font-medium ml-1">
                Presupuesto invalidado — edición bloqueada en UI.
              </span>
            )}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <PartidasTreeGrid
            nodes={wbs}
            readOnly={readOnly}
            onAddChild={openAddChild}
            onAssignApu={
              onAssignApu
                ? (partida) => {
                  onAssignApu(partida.id);
                }
                : undefined
            }
          />
        </CardContent>
      </Card>

      {budget && (
        <BudgetNodeDialog
          open={isDialogOpen}
          onOpenChange={setIsDialogOpen}
          proyectoId={budget.proyectoId}
          nivel={selectedPadreId ? "PARTIDA" : "CAPITULO"} // Lógica simple: raíz=capítulo, hijo=partida
          padreId={selectedPadreId}
          onSubmit={handleCreatePartida}
        />
      )}
    </div>
  );
}

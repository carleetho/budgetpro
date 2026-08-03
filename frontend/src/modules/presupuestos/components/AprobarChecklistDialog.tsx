"use client";

import Link from "next/link";
import { AlertTriangle, CheckCircle2, Loader2 } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";

export interface AprobarChecklistState {
  checking: boolean;
  leafCount: number;
  hojasSinApu: Array<{ id: string; item: string; descripcion: string }>;
  tieneCronograma: boolean;
  cronogramaError?: string | null;
}

interface AprobarChecklistDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  proyectoRouteId: string;
  presupuestoBasePath: string;
  state: AprobarChecklistState;
  approving: boolean;
  onConfirm: () => void;
}

export function AprobarChecklistDialog({
  open,
  onOpenChange,
  proyectoRouteId,
  presupuestoBasePath,
  state,
  approving,
  onConfirm,
}: AprobarChecklistDialogProps) {
  const apusOk = state.hojasSinApu.length === 0 && state.leafCount > 0;
  const cronoOk = state.tieneCronograma;
  const canApprove = apusOk && cronoOk && !state.checking && state.leafCount > 0;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>¿Listo para aprobar?</DialogTitle>
          <DialogDescription>
            El backend exige APUs en todas las partidas hoja y un programa de obra (cronograma)
            antes de congelar el presupuesto.
          </DialogDescription>
        </DialogHeader>

        {state.checking ? (
          <div className="flex items-center gap-2 py-8 text-muted-foreground justify-center">
            <Loader2 className="h-5 w-5 animate-spin" />
            Verificando prerrequisitos…
          </div>
        ) : (
          <div className="space-y-4 py-2">
            {state.leafCount === 0 && (
              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" />
                <AlertTitle>Sin partidas hoja</AlertTitle>
                <AlertDescription>
                  Crea al menos una partida hoja en el WBS.{" "}
                  <Link className="underline font-medium" href={`${presupuestoBasePath}/partidas`}>
                    Ir a partidas
                  </Link>
                </AlertDescription>
              </Alert>
            )}

            <div className="flex items-start gap-3 rounded-md border p-3">
              {apusOk ? (
                <CheckCircle2 className="h-5 w-5 text-green-600 shrink-0 mt-0.5" />
              ) : (
                <AlertTriangle className="h-5 w-5 text-amber-600 shrink-0 mt-0.5" />
              )}
              <div className="text-sm space-y-1">
                <p className="font-medium">
                  APUs en partidas hoja ({state.leafCount - state.hojasSinApu.length}/{state.leafCount})
                </p>
                {!apusOk && state.hojasSinApu.length > 0 && (
                  <>
                    <p className="text-muted-foreground">
                      Faltan {state.hojasSinApu.length}:{" "}
                      {state.hojasSinApu
                        .slice(0, 5)
                        .map((h) => h.item)
                        .join(", ")}
                      {state.hojasSinApu.length > 5 ? "…" : ""}
                    </p>
                    <Link
                      className="underline text-primary font-medium"
                      href={`${presupuestoBasePath}/partidas`}
                      onClick={() => onOpenChange(false)}
                    >
                      Completar APUs en Partidas
                    </Link>
                  </>
                )}
                {apusOk && <p className="text-muted-foreground">Todas las hojas tienen APU.</p>}
              </div>
            </div>

            <div className="flex items-start gap-3 rounded-md border p-3">
              {cronoOk ? (
                <CheckCircle2 className="h-5 w-5 text-green-600 shrink-0 mt-0.5" />
              ) : (
                <AlertTriangle className="h-5 w-5 text-amber-600 shrink-0 mt-0.5" />
              )}
              <div className="text-sm space-y-1">
                <p className="font-medium">Programa de obra (cronograma)</p>
                {cronoOk ? (
                  <p className="text-muted-foreground">Hay ProgramaObra para este proyecto.</p>
                ) : (
                  <>
                    <p className="text-muted-foreground">
                      {state.cronogramaError ??
                        "No existe cronograma. Sin él el backend rechaza la aprobación."}
                    </p>
                    <Link
                      className="underline text-primary font-medium"
                      href={`/proyectos/${proyectoRouteId}/cronograma`}
                      onClick={() => onOpenChange(false)}
                    >
                      Ir a Cronograma
                    </Link>
                  </>
                )}
              </div>
            </div>
          </div>
        )}

        <DialogFooter className="gap-2 sm:gap-0">
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={approving}>
            Cancelar
          </Button>
          <Button type="button" onClick={onConfirm} disabled={!canApprove || approving}>
            {approving ? <Loader2 className="h-4 w-4 animate-spin" /> : "Confirmar aprobación"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

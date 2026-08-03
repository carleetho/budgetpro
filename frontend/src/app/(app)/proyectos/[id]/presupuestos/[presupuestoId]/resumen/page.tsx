"use client";

import { useEffect, useState } from "react";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { usePresupuestoWorkspace } from "@/modules/presupuestos/context/PresupuestoWorkspaceContext";
import { PresupuestoTotalesSummary } from "@/modules/presupuestos/components/PresupuestoTotalesSummary";
import {
  PresupuestoService,
  type ControlCostosPartidaDto,
  type ExplosionInsumoDto,
} from "@/services/presupuesto.service";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

function flattenControl(partidas: ControlCostosPartidaDto[]): ControlCostosPartidaDto[] {
  return partidas.flatMap((p) => {
    const hijos = p.hijos ?? [];
    if (hijos.length === 0) return [p];
    return flattenControl(hijos);
  });
}

/**
 * Totales + control de costos y explosión de insumos (lectura).
 */
export default function PresupuestoResumenPage() {
  const { budget, presupuestoId } = usePresupuestoWorkspace();
  const [controlRows, setControlRows] = useState<ControlCostosPartidaDto[]>([]);
  const [insumos, setInsumos] = useState<ExplosionInsumoDto[]>([]);
  const [loadingExtra, setLoadingExtra] = useState(false);

  useEffect(() => {
    if (!presupuestoId) return;
    let cancelled = false;
    void (async () => {
      setLoadingExtra(true);
      try {
        const [control, explosion] = await Promise.all([
          PresupuestoService.obtenerControlCostos(presupuestoId).catch((e) => {
            console.error(e);
            return { partidas: [] as ControlCostosPartidaDto[] };
          }),
          PresupuestoService.obtenerExplosionInsumos(presupuestoId).catch((e) => {
            console.error(e);
            return { insumos: [] as ExplosionInsumoDto[] };
          }),
        ]);
        if (cancelled) return;
        setControlRows(flattenControl(control.partidas ?? []));
        setInsumos(explosion.insumos ?? []);
      } catch (e) {
        if (BudgetProApiError.isInstance(e)) {
          toast.error(`[${e.businessCode}] ${e.message}`);
        }
      } finally {
        if (!cancelled) setLoadingExtra(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [presupuestoId]);

  if (!budget) {
    return null;
  }

  return (
    <div className="space-y-6">
      <PresupuestoTotalesSummary costoTotal={budget.costoTotal} precioVenta={budget.precioVenta} />

      {loadingExtra && (
        <div className="flex items-center gap-2 text-muted-foreground text-sm">
          <Loader2 className="h-4 w-4 animate-spin" />
          Cargando control de costos y explosión…
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Control de costos</CardTitle>
          <CardDescription>Plan vs ejecución por partida hoja (`GET …/control-costos`).</CardDescription>
        </CardHeader>
        <CardContent>
          {controlRows.length === 0 ? (
            <p className="text-sm text-muted-foreground">Sin filas de control o sin datos aún.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Ítem</TableHead>
                  <TableHead>Descripción</TableHead>
                  <TableHead className="text-right">Metrado</TableHead>
                  <TableHead className="text-right">Gastado</TableHead>
                  <TableHead className="text-right">Saldo</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {controlRows.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell className="font-mono text-xs">{r.item}</TableCell>
                    <TableCell>{r.descripcion}</TableCell>
                    <TableCell className="text-right font-mono text-sm">
                      {Number(r.metrado ?? 0).toLocaleString("es-ES")}
                    </TableCell>
                    <TableCell className="text-right font-mono text-sm">
                      {Number(r.gastoAcumulado ?? 0).toLocaleString("es-ES")}
                    </TableCell>
                    <TableCell className="text-right font-mono text-sm">
                      {Number(r.saldo ?? 0).toLocaleString("es-ES")}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Explosión de insumos</CardTitle>
          <CardDescription>Agregado de recursos del presupuesto (`GET …/explosion-insumos`).</CardDescription>
        </CardHeader>
        <CardContent>
          {insumos.length === 0 ? (
            <p className="text-sm text-muted-foreground">Sin insumos explotados (¿faltan APUs?).</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Recurso</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead className="text-right">Cantidad</TableHead>
                  <TableHead className="text-right">PU</TableHead>
                  <TableHead className="text-right">Importe</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {insumos.map((ins, idx) => (
                  <TableRow key={ins.recursoId ?? `ins-${idx}`}>
                    <TableCell>{ins.recursoNombre ?? ins.recursoId ?? "—"}</TableCell>
                    <TableCell className="text-xs text-muted-foreground">{ins.tipo ?? "—"}</TableCell>
                    <TableCell className="text-right font-mono text-sm">
                      {Number(ins.cantidadTotal ?? 0).toLocaleString("es-ES")}
                    </TableCell>
                    <TableCell className="text-right font-mono text-sm">
                      {Number(ins.precioUnitario ?? 0).toLocaleString("es-ES")}
                    </TableCell>
                    <TableCell className="text-right font-mono text-sm">
                      {Number(ins.importeTotal ?? 0).toLocaleString("es-ES")}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

"use client";

import { useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { PresupuestoService } from "@/services/presupuesto.service";
import { PresupuestoApiService } from "@/services/presupuesto-api.service";
import { ProduccionService } from "@/services/produccion.service";
import { getTenantIdForApi } from "@/lib/jwt-tenant";
import type { PresupuestoResponseDto } from "@/core/types/presupuesto-contract";
import { presupuestoEstaActivo } from "@/core/types/presupuesto-contract";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

interface ReportePartidaDTO {
  id: string;
  item: string;
  descripcion: string;
  unidad?: string | null;
  nivel?: number | null;
  metrado?: number | null;
  gastoAcumulado?: number | null;
  saldo?: number | null;
  hijos?: ReportePartidaDTO[] | null;
}

interface PartidaRow {
  id: string;
  codigo: string;
  descripcion: string;
  metradoTotal: number;
  acumuladoActual: number;
}

interface RPCFormProps {
  proyectoId?: string;
}

const formatNumber = (value: number) =>
  value.toLocaleString("es-ES", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

const isValidUuid = (value: string) =>
  /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/.test(
    value
  );

const flattenPartidas = (partidas: ReportePartidaDTO[]): ReportePartidaDTO[] => {
  return partidas.flatMap((partida) => {
    const hijos = partida.hijos ?? [];
    if (hijos.length === 0) {
      return [partida];
    }
    return flattenPartidas(hijos);
  });
};

export default function RPCForm({ proyectoId }: RPCFormProps) {
  const params = useParams();
  const routeId = Array.isArray(params?.id) ? params.id[0] : params?.id;
  const resolvedProyectoId = proyectoId ?? routeId ?? "";

  const [presupuestos, setPresupuestos] = useState<PresupuestoResponseDto[]>([]);
  const [presupuestoId, setPresupuestoId] = useState<string>("");
  const [partidas, setPartidas] = useState<PartidaRow[]>([]);
  const [reportes, setReportes] = useState<
    Array<{ id: string; fechaReporte?: string; estado?: string }>
  >([]);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [avances, setAvances] = useState<Record<string, string>>({});

  useEffect(() => {
    const loadPresupuestos = async () => {
      if (!resolvedProyectoId || !isValidUuid(resolvedProyectoId)) {
        setLoadError("ID de proyecto no válido.");
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setLoadError(null);

      try {
        const todos = await PresupuestoApiService.listarTodosPorProyecto(
          getTenantIdForApi(),
          resolvedProyectoId
        );
        const activos = todos.filter((p) => presupuestoEstaActivo(p.estado));
        setPresupuestos(activos.length > 0 ? activos : todos);

        const preferido =
          activos.find((p) => p.estado === "CONGELADO") ??
          activos[0] ??
          (await PresupuestoService.obtenerActivo(resolvedProyectoId));

        if (preferido?.id) {
          setPresupuestoId(preferido.id);
        } else if (todos[0]?.id) {
          setPresupuestoId(todos[0].id);
        } else {
          setLoadError("No hay presupuestos en este proyecto.");
        }

        const lista = await ProduccionService.listar(resolvedProyectoId).catch(() => []);
        setReportes(
          (lista as Array<{ id: string; fechaReporte?: string; estado?: string }>) ?? []
        );
      } catch (error) {
        console.error(error);
        if (BudgetProApiError.isInstance(error)) {
          setLoadError(`[${error.businessCode}] ${error.message}`);
        } else {
          setLoadError(error instanceof Error ? error.message : "Error al cargar presupuestos.");
        }
      } finally {
        setIsLoading(false);
      }
    };

    void loadPresupuestos();
  }, [resolvedProyectoId]);

  useEffect(() => {
    if (!presupuestoId) return;
    let cancelled = false;
    void (async () => {
      try {
        const response = await PresupuestoService.obtenerControlCostos(presupuestoId);
        const hojas = flattenPartidas(response.partidas || []);
        if (cancelled) return;
        setPartidas(
          hojas.map((partida) => ({
            id: partida.id,
            codigo: partida.item,
            descripcion: partida.descripcion,
            metradoTotal: Number(partida.metrado ?? 0),
            acumuladoActual: Number(partida.gastoAcumulado ?? 0),
          }))
        );
        setAvances({});
      } catch (error) {
        console.error(error);
        if (!cancelled) {
          setPartidas([]);
          toast.error("No se pudo cargar el control de costos del presupuesto.");
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [presupuestoId]);

  const totalRows = useMemo(() => partidas.length, [partidas]);

  const handleAvanceChange = (partidaId: string, value: string) => {
    setAvances((prev) => ({
      ...prev,
      [partidaId]: value,
    }));
  };

  const handleSubmit = async () => {
    if (isSubmitting) return;
    setSubmitError(null);

    const items = Object.entries(avances)
      .map(([partidaId, value]) => ({
        partidaId,
        cantidad: Number.parseFloat(value),
      }))
      .filter((item) => Number.isFinite(item.cantidad) && item.cantidad > 0);

    if (items.length === 0) {
      toast.error("Ingresa un avance válido para al menos una partida.");
      return;
    }

    const fechaReporte = new Date().toISOString().slice(0, 10);

    setIsSubmitting(true);
    try {
      await ProduccionService.crear(resolvedProyectoId, {
        fechaReporte,
        items,
      });
      toast.success("Reporte de producción registrado correctamente.");
      setAvances({});
      const lista = await ProduccionService.listar(resolvedProyectoId).catch(() => []);
      setReportes(
        (lista as Array<{ id: string; fechaReporte?: string; estado?: string }>) ?? []
      );
    } catch (error) {
      const status = (error as { status?: number }).status;
      const message =
        error instanceof Error ? error.message : "Error al registrar el reporte de producción.";

      if (status === 409) {
        setSubmitError(message);
        return;
      }

      if (BudgetProApiError.isInstance(error)) {
        toast.error(`[${error.businessCode}] ${error.message}`);
      } else {
        toast.error(message);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        <span className="ml-2 text-muted-foreground">Cargando producción…</span>
      </div>
    );
  }

  if (loadError) {
    return (
      <Alert variant="destructive">
        <AlertTitle>Error al cargar</AlertTitle>
        <AlertDescription>{loadError}</AlertDescription>
      </Alert>
    );
  }

  return (
    <div className="space-y-6">
      {submitError && (
        <Alert variant="destructive">
          <AlertTitle>Regla de negocio</AlertTitle>
          <AlertDescription>{submitError}</AlertDescription>
        </Alert>
      )}

      <div className="space-y-2 max-w-md">
        <Label htmlFor="presupuesto-rpc">Presupuesto de referencia</Label>
        <select
          id="presupuesto-rpc"
          className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm"
          value={presupuestoId}
          onChange={(e) => setPresupuestoId(e.target.value)}
        >
          {presupuestos.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nombre} ({p.estado})
            </option>
          ))}
        </select>
        <p className="text-xs text-muted-foreground">
          Se usa el listado de presupuestos del proyecto (sin endpoint `/activo`).
        </p>
      </div>

      <div className="flex items-center justify-between text-sm text-muted-foreground">
        <span>{totalRows} partidas disponibles</span>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Código</TableHead>
              <TableHead>Descripción</TableHead>
              <TableHead className="text-right">Metrado Total</TableHead>
              <TableHead className="text-right">Acumulado Actual</TableHead>
              <TableHead className="text-right">Avance Hoy</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {partidas.map((partida) => (
              <TableRow key={partida.id}>
                <TableCell className="font-medium">{partida.codigo}</TableCell>
                <TableCell>{partida.descripcion}</TableCell>
                <TableCell className="text-right">{formatNumber(partida.metradoTotal)}</TableCell>
                <TableCell className="text-right">{formatNumber(partida.acumuladoActual)}</TableCell>
                <TableCell className="text-right">
                  <Input
                    type="number"
                    min={0}
                    step="0.01"
                    value={avances[partida.id] ?? ""}
                    onChange={(event) => handleAvanceChange(partida.id, event.target.value)}
                    className="max-w-[140px] ml-auto text-right"
                    placeholder="0.00"
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <div className="flex justify-end">
        <Button onClick={() => void handleSubmit()} disabled={isSubmitting || !presupuestoId}>
          {isSubmitting ? "Certificando..." : "Certificar Avance"}
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Reportes recientes</CardTitle>
          <CardDescription>`GET /proyectos/{"{id}"}/produccion`</CardDescription>
        </CardHeader>
        <CardContent>
          {reportes.length === 0 ? (
            <p className="text-sm text-muted-foreground">Aún no hay reportes.</p>
          ) : (
            <ul className="space-y-2">
              {reportes.slice(0, 10).map((r) => (
                <li key={r.id} className="flex items-center justify-between text-sm border-b py-2">
                  <span className="font-mono text-xs">{r.id.slice(0, 8)}…</span>
                  <span>{r.fechaReporte ?? "—"}</span>
                  <Badge variant="outline">{r.estado ?? "—"}</Badge>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

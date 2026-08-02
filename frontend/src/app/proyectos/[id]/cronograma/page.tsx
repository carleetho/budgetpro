"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { CronogramaService, type ActividadCronogramaDto } from "@/services/cronograma.service";
import { PartidasWbsService } from "@/services/partidas-wbs.service";
import { PresupuestoApiService } from "@/services/presupuesto-api.service";
import { getTenantIdForApi } from "@/lib/jwt-tenant";
import { presupuestoEstaActivo, type PartidaResponseDto } from "@/core/types/presupuesto-contract";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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

function flattenPartidas(
  nodes: Array<{ partida: PartidaResponseDto; children: unknown }>
): PartidaResponseDto[] {
  const out: PartidaResponseDto[] = [];
  const walk = (list: typeof nodes) => {
    for (const n of list) {
      out.push(n.partida);
      if (Array.isArray(n.children) && n.children.length) {
        walk(n.children as typeof nodes);
      }
    }
  };
  walk(nodes);
  return out;
}

export default function CronogramaPage() {
  const params = useParams();
  const proyectoId = params.id as string;
  const [actividades, setActividades] = useState<ActividadCronogramaDto[]>([]);
  const [partidas, setPartidas] = useState<PartidaResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [partidaId, setPartidaId] = useState("");
  const [fechaInicio, setFechaInicio] = useState("");
  const [fechaFin, setFechaFin] = useState("");
  const [saving, setSaving] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const crono = await CronogramaService.obtener(proyectoId).catch(() => ({
        actividades: [] as ActividadCronogramaDto[],
        proyectoId,
      }));
      setActividades(crono.actividades ?? []);

      const presupuestos = await PresupuestoApiService.listarTodosPorProyecto(
        getTenantIdForApi(),
        proyectoId
      );
      const activo =
        presupuestos.find((p) => p.estado === "CONGELADO") ??
        presupuestos.find((p) => presupuestoEstaActivo(p.estado));
      if (activo) {
        const tree = await PartidasWbsService.obtenerArbol(activo.id);
        const flat = flattenPartidas(tree);
        setPartidas(flat);
        if (!partidaId && flat[0]) setPartidaId(flat[0].id);
      }
    } catch (e) {
      if (BudgetProApiError.isInstance(e)) toast.error(`[${e.businessCode}] ${e.message}`);
      else toast.error("No se pudo cargar el cronograma.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [proyectoId]);

  const crear = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!partidaId || !fechaInicio || !fechaFin) {
      toast.error("Completa partida y fechas.");
      return;
    }
    setSaving(true);
    try {
      await CronogramaService.crearActividad(proyectoId, {
        partidaId,
        fechaInicio,
        fechaFin,
      });
      toast.success("Actividad creada.");
      await load();
    } catch (err) {
      if (BudgetProApiError.isInstance(err)) toast.error(`[${err.businessCode}] ${err.message}`);
      else toast.error("No se pudo crear la actividad.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Cronograma</h1>
        <p className="text-sm text-muted-foreground mt-1">Actividades del programa de obra.</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Nueva actividad</CardTitle>
          <CardDescription>`POST /proyectos/{"{id}"}/cronograma/actividades`</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={crear} className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4 items-end">
            <div className="space-y-1 sm:col-span-2">
              <Label>Partida</Label>
              <select
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 text-sm"
                value={partidaId}
                onChange={(e) => setPartidaId(e.target.value)}
              >
                {partidas.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.item} — {p.descripcion}
                  </option>
                ))}
              </select>
            </div>
            <div className="space-y-1">
              <Label>Inicio</Label>
              <Input type="date" value={fechaInicio} onChange={(e) => setFechaInicio(e.target.value)} />
            </div>
            <div className="space-y-1">
              <Label>Fin</Label>
              <Input type="date" value={fechaFin} onChange={(e) => setFechaFin(e.target.value)} />
            </div>
            <Button type="submit" disabled={saving || partidas.length === 0}>
              {saving ? "Guardando…" : "Agregar"}
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Actividades</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex items-center gap-2 text-muted-foreground py-8">
              <Loader2 className="h-5 w-5 animate-spin" /> Cargando…
            </div>
          ) : actividades.length === 0 ? (
            <p className="text-sm text-muted-foreground">Sin actividades programadas.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Partida</TableHead>
                  <TableHead>Inicio</TableHead>
                  <TableHead>Fin</TableHead>
                  <TableHead className="text-right">Días</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {actividades.map((a) => (
                  <TableRow key={a.id}>
                    <TableCell className="font-mono text-xs">{a.partidaId.slice(0, 8)}…</TableCell>
                    <TableCell>{a.fechaInicio}</TableCell>
                    <TableCell>{a.fechaFin}</TableCell>
                    <TableCell className="text-right">{a.duracionDias ?? "—"}</TableCell>
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

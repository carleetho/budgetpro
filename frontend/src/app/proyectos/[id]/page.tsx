"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { ProjectHeader } from "@/modules/proyectos/components/ProjectHeader";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { ProyectoService } from "@/services/proyecto.service";
import { PresupuestoApiService } from "@/services/presupuesto-api.service";
import type { Proyecto } from "@/core/types";
import type { PresupuestoResponseDto } from "@/core/types/presupuesto-contract";
import { presupuestoEstaActivo } from "@/core/types/presupuesto-contract";
import { getTenantIdForApi } from "@/lib/jwt-tenant";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";
import { ClipboardCheck, Loader2, Plus } from "lucide-react";
import { toast } from "sonner";

/**
 * Dashboard del proyecto: datos del proyecto + listado 1:N de presupuestos (histórico + activos).
 * [REGLA-110]: deshabilita “Crear presupuesto” si ya existe uno ACTIVO (BORRADOR o CONGELADO).
 */
export default function ProjectPage() {
  const params = useParams();
  const proyectoId = params.id as string;

  const [proyecto, setProyecto] = useState<Proyecto | null>(null);
  const [presupuestos, setPresupuestos] = useState<PresupuestoResponseDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [nuevoNombre, setNuevoNombre] = useState("");
  const [creating, setCreating] = useState(false);

  const loadPresupuestos = useCallback(async () => {
    const tenantId = getTenantIdForApi();
    const all = await PresupuestoApiService.listarTodosPorProyecto(tenantId, proyectoId);
    setPresupuestos(all);
  }, [proyectoId]);

  useEffect(() => {
    const load = async () => {
      if (!proyectoId) {
        setError("ID de proyecto no válido");
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError(null);

      try {
        const data = await ProyectoService.obtenerPorId(proyectoId);
        setProyecto(data);
        try {
          await loadPresupuestos();
        } catch (pe) {
          console.error(pe);
          if (BudgetProApiError.isInstance(pe)) {
            toast.error(`[${pe.businessCode}] ${pe.message}`);
          } else {
            toast.error("No se pudieron cargar los presupuestos del proyecto.");
          }
          setPresupuestos([]);
        }
      } catch (err) {
        console.error("Error al cargar proyecto:", err);
        const errorMessage =
          err instanceof Error
            ? err.message
            : "Error al cargar el proyecto. Por favor, intenta nuevamente.";
        setError(errorMessage);
        toast.error(errorMessage);
      } finally {
        setIsLoading(false);
      }
    };

    void load();
  }, [proyectoId, loadPresupuestos]);

  const handleEdit = () => {
    toast.info("Funcionalidad de edición próximamente");
  };

  const tienePresupuestoActivo = presupuestos.some((p) => presupuestoEstaActivo(p.estado));

  const handleCrearPresupuesto = async (e: React.FormEvent) => {
    e.preventDefault();
    const nombre = nuevoNombre.trim();
    if (!nombre) {
      toast.error("El nombre del presupuesto es obligatorio.");
      return;
    }
    setCreating(true);
    try {
      await PresupuestoApiService.crear({ proyectoId, nombre });
      toast.success("Presupuesto creado.");
      setCreateOpen(false);
      setNuevoNombre("");
      await loadPresupuestos();
    } catch (err) {
      if (BudgetProApiError.isInstance(err)) {
        toast.error(`[${err.businessCode}] ${err.message}`);
      } else {
        toast.error("No se pudo crear el presupuesto.");
      }
    } finally {
      setCreating(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        <span className="ml-2 text-muted-foreground">Cargando proyecto...</span>
      </div>
    );
  }

  if (error || !proyecto) {
    return (
      <div className="space-y-6">
        <Card>
          <CardContent className="pt-6">
            <div className="text-center py-12">
              <p className="text-destructive font-medium">Error al cargar el proyecto</p>
              <p className="text-muted-foreground mt-2 text-sm">
                {error || "El proyecto no existe o no se pudo cargar."}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <ProjectHeader proyecto={proyecto} onEdit={handleEdit} />

      <Card>
        <CardHeader>
          <CardTitle>Información General</CardTitle>
          <CardDescription>Vista de resumen del proyecto</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div>
              <p className="text-sm font-medium text-muted-foreground">ID del Proyecto</p>
              <p className="text-sm font-mono">{proyecto.id}</p>
            </div>
            {proyecto.createdAt && (
              <div>
                <p className="text-sm font-medium text-muted-foreground">Fecha de Creación</p>
                <p className="text-sm">
                  {new Date(proyecto.createdAt).toLocaleDateString("es-ES", {
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                  })}
                </p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      <Card id="presupuestos">
        <CardHeader className="flex flex-row flex-wrap items-start justify-between gap-4 space-y-0">
          <div>
            <CardTitle>Presupuestos</CardTitle>
            <CardDescription>
              Presupuestos vinculados a esta obra. Desde aquí puedes abrir cada uno para ver partidas,
              totales y parámetros.
            </CardDescription>
          </div>
          <Button type="button" onClick={() => setCreateOpen(true)} disabled={tienePresupuestoActivo}>
            <Plus className="h-4 w-4 mr-2" />
            Crear presupuesto
          </Button>
        </CardHeader>
        <CardContent className="space-y-3">
          {tienePresupuestoActivo && (
            <p className="text-sm text-muted-foreground">
              Ya hay un presupuesto en borrador o congelado para esta obra. Cierra o invalida el actual antes
              de crear otro, según las reglas de tu organización.
            </p>
          )}
          {presupuestos.length === 0 ? (
            <p className="text-sm text-muted-foreground py-4">No hay presupuestos registrados.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nombre</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead>Contractual</TableHead>
                  <TableHead className="text-right">Costo total</TableHead>
                  <TableHead className="w-[100px]"> </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {presupuestos.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-medium">{p.nombre}</TableCell>
                    <TableCell>
                      <Badge variant={p.estado === "CONGELADO" ? "secondary" : "outline"}>{p.estado}</Badge>
                    </TableCell>
                    <TableCell>{p.esContractual ? "Sí" : "No"}</TableCell>
                    <TableCell className="text-right font-mono text-xs">
                      {Number(p.costoTotal).toLocaleString("es-ES", {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                      })}
                    </TableCell>
                    <TableCell>
                      <Button variant="outline" size="sm" asChild>
                        <Link href={`/proyectos/${proyecto.id}/presupuestos/${p.id}/partidas`}>
                          Abrir
                        </Link>
                      </Button>
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
          <CardTitle>Acciones rápidas</CardTitle>
          <CardDescription>Accede a los flujos operativos principales del proyecto.</CardDescription>
        </CardHeader>
        <CardContent>
          <Link href={`/proyectos/${proyecto.id}/produccion/nuevo`}>
            <Button>
              <ClipboardCheck className="h-4 w-4 mr-2" />
              Reportar producción
            </Button>
          </Link>
        </CardContent>
      </Card>

      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent>
          <form onSubmit={handleCrearPresupuesto}>
            <DialogHeader>
              <DialogTitle>Nuevo presupuesto</DialogTitle>
              <DialogDescription>
                Asigna un nombre claro (por ejemplo versión o escenario). Podrás editar parámetros y partidas
                después.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-2 py-4">
              <Label htmlFor="nombre-pres">Nombre</Label>
              <Input
                id="nombre-pres"
                value={nuevoNombre}
                onChange={(e) => setNuevoNombre(e.target.value)}
                placeholder="Ej. Presupuesto contractual v1"
                required
              />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setCreateOpen(false)} disabled={creating}>
                Cancelar
              </Button>
              <Button type="submit" disabled={creating}>
                {creating ? <Loader2 className="h-4 w-4 animate-spin" /> : "Crear"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

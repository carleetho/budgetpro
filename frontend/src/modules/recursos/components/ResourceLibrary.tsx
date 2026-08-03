"use client";

import { useState, useEffect, useCallback } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Search, Package, Users, Wrench, FileText, Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { RecursosService } from "@/services/recursos.service";
import type { Recurso, TipoRecurso, TipoRecursoFiltroUi } from "@/core/types/recursos";
import { etiquetaTipoRecurso } from "@/core/types/recursos";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";

interface ResourceLibraryProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  tipoFiltro?: TipoRecursoFiltroUi;
  onSelect: (recurso: Recurso) => void;
}

const TIPOS_ALTA: TipoRecurso[] = [
  "MATERIAL",
  "MANO_OBRA",
  "EQUIPO_MAQUINA",
  "EQUIPO_HERRAMIENTA",
  "SUBCONTRATO",
];

/**
 * Catálogo de recursos: búsqueda, filtro por tipo y alta vía `POST /recursos`.
 */
export function ResourceLibrary({
  open,
  onOpenChange,
  tipoFiltro,
  onSelect,
}: ResourceLibraryProps) {
  const [recursos, setRecursos] = useState<Recurso[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [tipoSeleccionado, setTipoSeleccionado] = useState<TipoRecursoFiltroUi | "TODOS">(
    tipoFiltro || "TODOS"
  );
  const [showCreate, setShowCreate] = useState(false);
  const [creating, setCreating] = useState(false);
  const [nuevoNombre, setNuevoNombre] = useState("");
  const [nuevoTipo, setNuevoTipo] = useState<TipoRecurso>("MATERIAL");
  const [nuevaUnidad, setNuevaUnidad] = useState("");

  const loadRecursos = useCallback(async () => {
    setIsLoading(true);
    try {
      const tipo = tipoSeleccionado === "TODOS" ? undefined : tipoSeleccionado;
      const data = await RecursosService.buscar(searchTerm, tipo);
      setRecursos(data);
    } catch (error) {
      console.error("Error al cargar recursos:", error);
      if (BudgetProApiError.isInstance(error)) {
        toast.error(`[${error.businessCode}] ${error.message}`);
      } else {
        toast.error("No se pudo cargar el catálogo de recursos.");
      }
      setRecursos([]);
    } finally {
      setIsLoading(false);
    }
  }, [searchTerm, tipoSeleccionado]);

  useEffect(() => {
    if (open) {
      void loadRecursos();
    }
  }, [open, loadRecursos]);

  useEffect(() => {
    if (open && tipoFiltro) {
      setTipoSeleccionado(tipoFiltro);
    }
  }, [open, tipoFiltro]);

  const handleSelect = (recurso: Recurso) => {
    onSelect(recurso);
    onOpenChange(false);
    setSearchTerm("");
    setShowCreate(false);
  };

  const handleCrear = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!nuevoNombre.trim() || !nuevaUnidad.trim()) {
      toast.error("Nombre y unidad base son obligatorios.");
      return;
    }
    setCreating(true);
    try {
      const creado = await RecursosService.crear({
        nombre: nuevoNombre.trim(),
        tipo: nuevoTipo,
        unidadBase: nuevaUnidad.trim(),
      });
      toast.success("Recurso creado.");
      setShowCreate(false);
      setNuevoNombre("");
      setNuevaUnidad("");
      await loadRecursos();
      handleSelect(creado);
    } catch (error) {
      if (BudgetProApiError.isInstance(error)) {
        toast.error(`[${error.businessCode}] ${error.message}`);
      } else {
        toast.error("No se pudo crear el recurso.");
      }
    } finally {
      setCreating(false);
    }
  };

  const getTipoIcon = (tipo: TipoRecurso) => {
    if (tipo === "MATERIAL") return <Package className="h-4 w-4" />;
    if (tipo === "MANO_OBRA") return <Users className="h-4 w-4" />;
    if (tipo === "SUBCONTRATO") return <FileText className="h-4 w-4" />;
    return <Wrench className="h-4 w-4" />;
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[700px] max-h-[80vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>Catálogo de Recursos</DialogTitle>
          <DialogDescription>
            Selecciona un recurso o crea uno nuevo. El precio unitario se define al armar el APU.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 flex-1 overflow-hidden flex flex-col">
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Buscar por nombre..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            <Button
              type="button"
              variant={showCreate ? "secondary" : "outline"}
              onClick={() => setShowCreate((v) => !v)}
            >
              <Plus className="h-4 w-4 mr-1" />
              Nuevo
            </Button>
          </div>

          {showCreate && (
            <form onSubmit={handleCrear} className="border rounded-md p-3 space-y-3 bg-muted/30">
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="space-y-1 sm:col-span-1">
                  <Label htmlFor="rec-nombre">Nombre *</Label>
                  <Input
                    id="rec-nombre"
                    value={nuevoNombre}
                    onChange={(e) => setNuevoNombre(e.target.value)}
                    placeholder="Cemento Portland"
                  />
                </div>
                <div className="space-y-1">
                  <Label htmlFor="rec-tipo">Tipo *</Label>
                  <select
                    id="rec-tipo"
                    className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm"
                    value={nuevoTipo}
                    onChange={(e) => setNuevoTipo(e.target.value as TipoRecurso)}
                  >
                    {TIPOS_ALTA.map((t) => (
                      <option key={t} value={t}>
                        {etiquetaTipoRecurso(t)}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-1">
                  <Label htmlFor="rec-unidad">Unidad base *</Label>
                  <Input
                    id="rec-unidad"
                    value={nuevaUnidad}
                    onChange={(e) => setNuevaUnidad(e.target.value)}
                    placeholder="kg, m³, hh…"
                  />
                </div>
              </div>
              <Button type="submit" size="sm" disabled={creating}>
                {creating ? <Loader2 className="h-4 w-4 animate-spin" /> : "Crear y seleccionar"}
              </Button>
            </form>
          )}

          <Tabs
            value={tipoSeleccionado}
            onValueChange={(value) => setTipoSeleccionado(value as TipoRecursoFiltroUi | "TODOS")}
            className="flex-1 flex flex-col overflow-hidden"
          >
            <TabsList className="grid w-full grid-cols-5">
              <TabsTrigger value="TODOS">Todos</TabsTrigger>
              <TabsTrigger value="MATERIAL">Materiales</TabsTrigger>
              <TabsTrigger value="MANO_OBRA">Mano de Obra</TabsTrigger>
              <TabsTrigger value="EQUIPO">Equipos</TabsTrigger>
              <TabsTrigger value="SUBCONTRATO">Subcontratos</TabsTrigger>
            </TabsList>

            <div className="flex-1 overflow-auto mt-4">
              {isLoading ? (
                <div className="flex items-center justify-center py-12">
                  <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                </div>
              ) : recursos.length === 0 ? (
                <div className="text-center py-12 text-muted-foreground">
                  <p>No se encontraron recursos</p>
                  <p className="text-sm mt-2">Crea uno con el botón Nuevo o cambia el filtro.</p>
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-[60px]">Tipo</TableHead>
                      <TableHead>Nombre</TableHead>
                      <TableHead className="w-[80px]">Unidad</TableHead>
                      <TableHead className="w-[100px]">Estado</TableHead>
                      <TableHead className="w-[100px]">Acción</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {recursos.map((recurso) => (
                      <TableRow key={recurso.id}>
                        <TableCell>
                          <div className="flex items-center justify-center" title={etiquetaTipoRecurso(recurso.tipo)}>
                            {getTipoIcon(recurso.tipo)}
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="font-medium">{recurso.nombre}</div>
                          <div className="text-xs text-muted-foreground">
                            {etiquetaTipoRecurso(recurso.tipo)}
                          </div>
                        </TableCell>
                        <TableCell className="text-center text-sm">{recurso.unidad}</TableCell>
                        <TableCell className="text-xs text-muted-foreground">
                          {recurso.estado ?? "—"}
                        </TableCell>
                        <TableCell>
                          <Button variant="outline" size="sm" onClick={() => handleSelect(recurso)}>
                            Seleccionar
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </div>
          </Tabs>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

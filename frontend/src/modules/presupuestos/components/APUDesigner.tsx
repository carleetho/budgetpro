"use client";

import { useState, useEffect } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Plus, Trash2, Package, Users, Wrench, Calculator, FileText } from "lucide-react";
import { ResourceLibrary } from "@/modules/recursos/components/ResourceLibrary";
import { RecursosService } from "@/services/recursos.service";
import type { ItemPresupuesto } from "@/core/types/presupuesto";
import type { DetalleAPU, AnalisisUnitario } from "@/core/types/apu";
import type { Recurso, TipoRecurso } from "@/core/types/recursos";

interface APUDesignerProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  partida: ItemPresupuesto;
  onSave: (apu: AnalisisUnitario) => Promise<void>;
}

/**
 * Componente diseñador de Análisis de Precio Unitario (APU).
 * 
 * Permite descomponer el costo de una partida en sus insumos:
 * - Materiales
 * - Mano de Obra
 * - Equipos
 * 
 * Calcula el costo directo total en tiempo real.
 */
export function APUDesigner({
  open,
  onOpenChange,
  partida,
  onSave,
}: APUDesignerProps) {
  const [detalles, setDetalles] = useState<DetalleAPU[]>([]);
  const [rendimientoDiario, setRendimientoDiario] = useState<number | undefined>(undefined);
  const [isResourceLibraryOpen, setIsResourceLibraryOpen] = useState(false);
  const [tipoRecursoSeleccionado, setTipoRecursoSeleccionado] = useState<TipoRecurso>("MATERIAL");
  const [isSaving, setIsSaving] = useState(false);

  // Cargar APU existente si existe
  useEffect(() => {
    if (open && partida) {
      // TODO: Cargar APU desde backend cuando esté disponible
      // Por ahora, inicializar vacío
      setDetalles([]);
      setRendimientoDiario(undefined);
    }
  }, [open, partida]);

  // Calcular costo directo total
  const costoDirecto = detalles.reduce((sum, detalle) => sum + detalle.parcial, 0);

  // Agrupar detalles por tipo
  const detallesPorTipo = {
    MATERIAL: detalles.filter(d => d.recurso?.tipo === "MATERIAL"),
    MANO_DE_OBRA: detalles.filter(d => d.recurso?.tipo === "MANO_DE_OBRA"),
    EQUIPO: detalles.filter(d => d.recurso?.tipo === "EQUIPO"),
    SUBCONTRATO: detalles.filter(d => d.recurso?.tipo === "SUBCONTRATO"),
  };

  // Abrir biblioteca de recursos
  const handleAbrirBiblioteca = (tipo: TipoRecurso) => {
    setTipoRecursoSeleccionado(tipo);
    setIsResourceLibraryOpen(true);
  };

  // Agregar recurso seleccionado
  const handleSeleccionarRecurso = async (recurso: Recurso) => {
    const nuevoDetalle: DetalleAPU = {
      id: `det-${Date.now()}`,
      recursoId: recurso.id,
      recurso: recurso,
      rendimiento: 1.0,
      precio: recurso.precioBase,
      parcial: recurso.precioBase,
    };

    setDetalles([...detalles, nuevoDetalle]);
  };

  // Actualizar rendimiento de un detalle
  const handleActualizarRendimiento = (detalleId: string, nuevoRendimiento: number) => {
    setDetalles(detalles.map(d => {
      if (d.id === detalleId) {
        const nuevoParcial = nuevoRendimiento * d.precio;
        return { ...d, rendimiento: nuevoRendimiento, parcial: nuevoParcial };
      }
      return d;
    }));
  };

  // Actualizar precio de un detalle
  const handleActualizarPrecio = (detalleId: string, nuevoPrecio: number) => {
    setDetalles(detalles.map(d => {
      if (d.id === detalleId) {
        const nuevoParcial = d.rendimiento * nuevoPrecio;
        return { ...d, precio: nuevoPrecio, parcial: nuevoParcial };
      }
      return d;
    }));
  };

  // Eliminar detalle
  const handleEliminarDetalle = (detalleId: string) => {
    setDetalles(detalles.filter(d => d.id !== detalleId));
  };

  // Guardar APU
  const handleGuardar = async () => {
    setIsSaving(true);
    try {
      const apu: AnalisisUnitario = {
        id: `apu-${partida.id}`,
        partidaId: partida.id,
        rendimientoDiario,
        costoDirecto,
        detalles,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      await onSave(apu);
      onOpenChange(false);
    } catch (error) {
      console.error("Error al guardar APU:", error);
    } finally {
      setIsSaving(false);
    }
  };

  // Renderizar tabla de detalles por tipo
  const renderTablaDetalles = (tipo: TipoRecurso, icon: React.ReactNode) => {
    const detallesTipo = detallesPorTipo[tipo];

    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            {icon}
            <h3 className="font-semibold">
              {tipo === "MATERIAL" && "Materiales"}
              {tipo === "MANO_DE_OBRA" && "Mano de Obra"}
              {tipo === "EQUIPO" && "Equipos"}
              {tipo === "SUBCONTRATO" && "Subcontratos"}
            </h3>
            <Badge variant="secondary">{detallesTipo.length}</Badge>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => handleAbrirBiblioteca(tipo)}
          >
            <Plus className="h-4 w-4 mr-2" />
            Agregar
          </Button>
        </div>

        {detallesTipo.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground border rounded-lg">
            <p className="text-sm">No hay recursos agregados</p>
            <p className="text-xs mt-1">Haz clic en "Agregar" para incluir recursos</p>
          </div>
        ) : (
          <div className="border rounded-lg overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Recurso</TableHead>
                  <TableHead className="w-[120px]">Unidad</TableHead>
                  <TableHead className="w-[120px]">Rendimiento</TableHead>
                  <TableHead className="w-[140px]">Precio Unit.</TableHead>
                  <TableHead className="w-[140px] text-right">Parcial</TableHead>
                  <TableHead className="w-[80px]">Acción</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {detallesTipo.map((detalle) => (
                  <TableRow key={detalle.id}>
                    <TableCell>
                      <div>
                        <div className="font-medium">{detalle.recurso?.nombre || "N/A"}</div>
                        {detalle.recurso?.codigo && (
                          <div className="text-xs text-muted-foreground font-mono">
                            {detalle.recurso.codigo}
                          </div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="text-sm">
                      {detalle.recurso?.unidad || "-"}
                    </TableCell>
                    <TableCell>
                      <Input
                        type="number"
                        step="0.01"
                        min="0"
                        value={detalle.rendimiento}
                        onChange={(e) =>
                          handleActualizarRendimiento(
                            detalle.id,
                            parseFloat(e.target.value) || 0
                          )
                        }
                        className="w-full"
                      />
                    </TableCell>
                    <TableCell>
                      <Input
                        type="number"
                        step="0.01"
                        min="0"
                        value={detalle.precio}
                        onChange={(e) =>
                          handleActualizarPrecio(
                            detalle.id,
                            parseFloat(e.target.value) || 0
                          )
                        }
                        className="w-full font-mono text-sm"
                      />
                    </TableCell>
                    <TableCell className="text-right font-mono font-semibold">
                      {detalle.parcial.toLocaleString('es-ES', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                        style: 'currency',
                        currency: 'USD'
                      })}
                    </TableCell>
                    <TableCell>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleEliminarDetalle(detalle.id)}
                        className="text-destructive hover:text-destructive"
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}
      </div>
    );
  };

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="sm:max-w-4xl max-h-[90vh] flex flex-col">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Calculator className="h-5 w-5" />
              Análisis de Precio Unitario
            </DialogTitle>
            <DialogDescription>
              {partida.descripcion} ({partida.codigo})
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-6 flex-1 overflow-y-auto">
            {/* Rendimiento Diario */}
            <div className="space-y-2">
              <Label htmlFor="rendimientoDiario">Rendimiento Diario (Opcional)</Label>
              <Input
                id="rendimientoDiario"
                type="number"
                step="0.01"
                min="0"
                value={rendimientoDiario ?? ""}
                onChange={(e) =>
                  setRendimientoDiario(
                    e.target.value ? parseFloat(e.target.value) : undefined
                  )
                }
                placeholder="Ej: 10.5 m²/día"
              />
            </div>

            {/* Tabs por tipo de recurso */}
            <Tabs defaultValue="MATERIAL" className="w-full">
              <TabsList className="grid w-full grid-cols-4">
                <TabsTrigger value="MATERIAL">
                  <Package className="h-4 w-4 mr-2" />
                  Materiales
                </TabsTrigger>
                <TabsTrigger value="MANO_DE_OBRA">
                  <Users className="h-4 w-4 mr-2" />
                  Mano de Obra
                </TabsTrigger>
                <TabsTrigger value="EQUIPO">
                  <Wrench className="h-4 w-4 mr-2" />
                  Equipos
                </TabsTrigger>
                <TabsTrigger value="SUBCONTRATO">
                  <FileText className="h-4 w-4 mr-2" />
                  Subcontratos
                </TabsTrigger>
              </TabsList>

              <TabsContent value="MATERIAL" className="mt-4">
                {renderTablaDetalles("MATERIAL", <Package className="h-5 w-5" />)}
              </TabsContent>

              <TabsContent value="MANO_DE_OBRA" className="mt-4">
                {renderTablaDetalles("MANO_DE_OBRA", <Users className="h-5 w-5" />)}
              </TabsContent>

              <TabsContent value="EQUIPO" className="mt-4">
                {renderTablaDetalles("EQUIPO", <Wrench className="h-5 w-5" />)}
              </TabsContent>

              <TabsContent value="SUBCONTRATO" className="mt-4">
                {renderTablaDetalles("SUBCONTRATO", <FileText className="h-5 w-5" />)}
              </TabsContent>
            </Tabs>

            {/* Resumen de Costo */}
            <div className="border-t pt-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">
                    Costo Directo Total
                  </p>
                  <p className="text-2xl font-bold mt-1">
                    {costoDirecto.toLocaleString('es-ES', {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 2,
                      style: 'currency',
                      currency: 'USD'
                    })}
                  </p>
                </div>
                <Badge variant="outline" className="text-lg px-4 py-2">
                  {detalles.length} {detalles.length === 1 ? 'recurso' : 'recursos'}
                </Badge>
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => onOpenChange(false)} disabled={isSaving}>
              Cancelar
            </Button>
            <Button onClick={handleGuardar} disabled={isSaving}>
              {isSaving ? "Guardando..." : "Guardar APU"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Biblioteca de Recursos */}
      <ResourceLibrary
        open={isResourceLibraryOpen}
        onOpenChange={setIsResourceLibraryOpen}
        tipoFiltro={tipoRecursoSeleccionado}
        onSelect={handleSeleccionarRecurso}
      />
    </>
  );
}

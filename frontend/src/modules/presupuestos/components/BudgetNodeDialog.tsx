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
import type { ItemPresupuesto, NivelPresupuesto, CrearItemPresupuestoCommand } from "@/core/types/presupuesto";

interface BudgetNodeDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  proyectoId: string;
  nivel: NivelPresupuesto;
  padreId?: string | null;
  itemEditando?: ItemPresupuesto | null;
  onSubmit: (command: CrearItemPresupuestoCommand) => Promise<void>;
}

/**
 * Modal para crear o editar un nodo del presupuesto.
 * 
 * Los campos son dinámicos según el nivel:
 * - CAPITULO/SUBCAPITULO: Solo código y descripción
 * - PARTIDA: Código, descripción, unidad, metrado y precio unitario
 */
export function BudgetNodeDialog({
  open,
  onOpenChange,
  proyectoId,
  nivel,
  padreId = null,
  itemEditando = null,
  onSubmit,
}: BudgetNodeDialogProps) {
  const isPartida = nivel === 'PARTIDA';
  const isEditando = !!itemEditando;

  const [codigo, setCodigo] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [unidad, setUnidad] = useState("");
  const [metrado, setMetrado] = useState<number | undefined>(undefined);
  const [precioUnitario, setPrecioUnitario] = useState<number | undefined>(undefined);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Inicializar valores si estamos editando
  useEffect(() => {
    if (itemEditando) {
      setCodigo(itemEditando.codigo);
      setDescripcion(itemEditando.descripcion);
      setUnidad(itemEditando.unidad || "");
      setMetrado(itemEditando.metrado);
      setPrecioUnitario(itemEditando.precioUnitario);
    } else {
      // Resetear formulario
      setCodigo("");
      setDescripcion("");
      setUnidad("");
      setMetrado(undefined);
      setPrecioUnitario(undefined);
    }
  }, [itemEditando, open]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!codigo.trim() || !descripcion.trim()) {
      return;
    }

    if (isPartida && (!unidad.trim() || metrado === undefined || precioUnitario === undefined)) {
      return;
    }

    setIsSubmitting(true);

    try {
      const command: CrearItemPresupuestoCommand = {
        proyectoId,
        padreId,
        codigo: codigo.trim(),
        descripcion: descripcion.trim(),
        nivel,
        ...(isPartida && {
          unidad: unidad.trim(),
          metrado: metrado!,
          precioUnitario: precioUnitario!,
        }),
      };

      await onSubmit(command);
      onOpenChange(false);
    } catch (error) {
      console.error("Error al guardar item:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const getTitle = () => {
    if (isEditando) {
      return `Editar ${nivel}`;
    }
    return `Agregar ${nivel}`;
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{getTitle()}</DialogTitle>
          <DialogDescription>
            {isPartida
              ? "Completa los datos de la partida de obra."
              : "Completa los datos del capítulo o subcapítulo."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            {/* Código */}
            <div className="space-y-2">
              <Label htmlFor="codigo">Código *</Label>
              <Input
                id="codigo"
                value={codigo}
                onChange={(e) => setCodigo(e.target.value)}
                placeholder="Ej: 1.01, 2.03.05"
                required
              />
            </div>

            {/* Descripción */}
            <div className="space-y-2">
              <Label htmlFor="descripcion">Descripción *</Label>
              <Input
                id="descripcion"
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
                placeholder="Descripción del item"
                required
              />
            </div>

            {/* Campos específicos para PARTIDA */}
            {isPartida && (
              <>
                {/* Unidad */}
                <div className="space-y-2">
                  <Label htmlFor="unidad">Unidad *</Label>
                  <Input
                    id="unidad"
                    value={unidad}
                    onChange={(e) => setUnidad(e.target.value)}
                    placeholder="Ej: m², m³, kg"
                    required
                  />
                </div>

                {/* Metrado */}
                <div className="space-y-2">
                  <Label htmlFor="metrado">Metrado *</Label>
                  <Input
                    id="metrado"
                    type="number"
                    step="0.01"
                    min="0"
                    value={metrado ?? ""}
                    onChange={(e) => setMetrado(e.target.value ? parseFloat(e.target.value) : undefined)}
                    placeholder="0.00"
                    required
                  />
                </div>

                {/* Precio Unitario */}
                <div className="space-y-2">
                  <Label htmlFor="precioUnitario">Precio Unitario (USD) *</Label>
                  <Input
                    id="precioUnitario"
                    type="number"
                    step="0.01"
                    min="0"
                    value={precioUnitario ?? ""}
                    onChange={(e) => setPrecioUnitario(e.target.value ? parseFloat(e.target.value) : undefined)}
                    placeholder="0.00"
                    required
                  />
                </div>

                {/* Parcial calculado (solo lectura) */}
                {metrado !== undefined && precioUnitario !== undefined && (
                  <div className="space-y-2">
                    <Label>Parcial</Label>
                    <Input
                      value={(metrado * precioUnitario).toLocaleString('es-ES', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                        style: 'currency',
                        currency: 'USD'
                      })}
                      readOnly
                      className="font-semibold"
                    />
                  </div>
                )}
              </>
            )}
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Guardando..." : isEditando ? "Actualizar" : "Crear"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

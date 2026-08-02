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
  /** Nivel inicial sugerido (CAPITULO en raíz, PARTIDA al agregar hijo). */
  nivel: NivelPresupuesto;
  padreId?: string | null;
  itemEditando?: ItemPresupuesto | null;
  onSubmit: (command: CrearItemPresupuestoCommand) => Promise<void>;
}

/**
 * Modal para crear un nodo WBS.
 * `POST /partidas` no usa precio unitario: el PU nace del APU.
 */
export function BudgetNodeDialog({
  open,
  onOpenChange,
  proyectoId,
  nivel: nivelInicial,
  padreId = null,
  itemEditando = null,
  onSubmit,
}: BudgetNodeDialogProps) {
  const isEditando = !!itemEditando;

  const [nivel, setNivel] = useState<NivelPresupuesto>(nivelInicial);
  const [codigo, setCodigo] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [unidad, setUnidad] = useState("");
  const [metrado, setMetrado] = useState<number | undefined>(undefined);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const isPartida = nivel === "PARTIDA";

  useEffect(() => {
    if (itemEditando) {
      setCodigo(itemEditando.codigo);
      setDescripcion(itemEditando.descripcion);
      setUnidad(itemEditando.unidad || "");
      setMetrado(itemEditando.metrado);
      setNivel(itemEditando.nivel);
    } else {
      setCodigo("");
      setDescripcion("");
      setUnidad("");
      setMetrado(undefined);
      setNivel(nivelInicial);
    }
    setValidationError(null);
  }, [itemEditando, open, nivelInicial]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setValidationError(null);

    if (!codigo.trim() || !descripcion.trim()) {
      setValidationError("Código y descripción son obligatorios.");
      return;
    }

    if (isPartida && (!unidad.trim() || metrado === undefined || Number.isNaN(metrado))) {
      setValidationError("Para una partida indica unidad y metrado (≥ 0).");
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
    return padreId ? "Agregar nodo hijo" : "Agregar ítem raíz";
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{getTitle()}</DialogTitle>
          <DialogDescription>
            {isPartida
              ? "Partida de obra (hoja WBS). El precio unitario se define al guardar el APU."
              : "Capítulo o subcapítulo (puede tener hijos)."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            {!isEditando && (
              <div className="space-y-2">
                <Label htmlFor="nivel">Tipo de nodo *</Label>
                <select
                  id="nivel"
                  className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm"
                  value={nivel === "PARTIDA" ? "PARTIDA" : "CAPITULO"}
                  onChange={(e) =>
                    setNivel(e.target.value === "PARTIDA" ? "PARTIDA" : "CAPITULO")
                  }
                >
                  <option value="CAPITULO">Capítulo / título</option>
                  <option value="PARTIDA">Partida (hoja)</option>
                </select>
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="codigo">Código / ítem *</Label>
              <Input
                id="codigo"
                value={codigo}
                onChange={(e) => setCodigo(e.target.value)}
                placeholder="Ej: 1.01, 2.03.05"
                required
              />
            </div>

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

            {isPartida && (
              <>
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

                <div className="space-y-2">
                  <Label htmlFor="metrado">Metrado *</Label>
                  <Input
                    id="metrado"
                    type="number"
                    step="0.01"
                    min="0"
                    value={metrado ?? ""}
                    onChange={(e) =>
                      setMetrado(e.target.value ? parseFloat(e.target.value) : undefined)
                    }
                    placeholder="0.00"
                    required
                  />
                </div>
              </>
            )}

            {validationError && (
              <p className="text-sm text-destructive" role="alert">
                {validationError}
              </p>
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

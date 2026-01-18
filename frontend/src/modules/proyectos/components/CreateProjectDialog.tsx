"use client";

import { useState } from "react";
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
import { toast } from "sonner";

interface CreateProjectDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

/**
 * Componente de diálogo para crear un nuevo proyecto.
 */
export function CreateProjectDialog({
  open,
  onOpenChange,
  onSuccess,
}: CreateProjectDialogProps) {
  const [nombre, setNombre] = useState("");
  const [ubicacion, setUbicacion] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!nombre.trim()) {
      toast.error("El nombre del proyecto es requerido");
      return;
    }

    setIsSubmitting(true);

    try {
      const { ProyectoService } = await import("@/services/proyecto.service");
      
      await ProyectoService.crear({
        nombre: nombre.trim(),
        ubicacion: ubicacion.trim() || undefined,
      });

      toast.success("Proyecto creado exitosamente");
      
      // Resetear formulario
      setNombre("");
      setUbicacion("");
      onOpenChange(false);
      
      // Recargar lista
      onSuccess();
    } catch (error) {
      console.error("Error al crear proyecto:", error);
      
      // Extraer mensaje de error más detallado
      let errorMessage = "Error al crear el proyecto. Por favor, intenta nuevamente.";
      
      if (error instanceof Error) {
        errorMessage = error.message;
        
        // Si hay detalles adicionales, mostrarlos en consola
        if ((error as any).details) {
          console.error("Detalles del error del servidor:", JSON.stringify((error as any).details, null, 2));
          
          // Intentar extraer mensaje más específico de los detalles
          const details = (error as any).details;
          if (details.message) {
            errorMessage = details.message;
          } else if (details.error) {
            errorMessage = `Error: ${details.error}`;
          }
        }
        if ((error as any).status) {
          console.error("Status HTTP:", (error as any).status);
        }
      }
      
      toast.error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleOpenChange = (newOpen: boolean) => {
    if (!isSubmitting) {
      onOpenChange(newOpen);
      if (!newOpen) {
        // Resetear formulario al cerrar
        setNombre("");
        setUbicacion("");
      }
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Nuevo Proyecto</DialogTitle>
          <DialogDescription>
            Crea un nuevo proyecto para comenzar a gestionar presupuestos y costos.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="nombre">Nombre del Proyecto *</Label>
              <Input
                id="nombre"
                placeholder="Ej: Edificio Residencial Las Palmas"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                disabled={isSubmitting}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="ubicacion">Ubicación</Label>
              <Input
                id="ubicacion"
                placeholder="Ej: San Salvador, El Salvador"
                value={ubicacion}
                onChange={(e) => setUbicacion(e.target.value)}
                disabled={isSubmitting}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => handleOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Creando..." : "Crear Proyecto"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

"use client";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { MapPin, Edit } from "lucide-react";
import type { Proyecto, EstadoProyecto } from "@/core/types";
import { cn } from "@/lib/utils";

interface ProjectHeaderProps {
  proyecto: Proyecto;
  onEdit?: () => void;
}

/**
 * Componente de header para mostrar los detalles principales de un proyecto.
 */
export function ProjectHeader({ proyecto, onEdit }: ProjectHeaderProps) {
  const getEstadoBadgeVariant = (estado: EstadoProyecto) => {
    switch (estado) {
      case "ACTIVO":
        return "default";
      case "BORRADOR":
        return "secondary";
      case "SUSPENDIDO":
        return "outline";
      case "CERRADO":
        return "destructive";
      default:
        return "secondary";
    }
  };

  const formatEstado = (estado: EstadoProyecto) => {
    return estado.replace("_", " ");
  };

  return (
    <div className="flex items-start justify-between pb-6 border-b">
      <div className="space-y-2 flex-1">
        {/* Título */}
        <h1 className="text-3xl font-bold tracking-tight">{proyecto.nombre}</h1>
        
        {/* Ubicación */}
        {proyecto.ubicacion && (
          <div className="flex items-center text-muted-foreground">
            <MapPin className="h-4 w-4 mr-2" />
            <span>{proyecto.ubicacion}</span>
          </div>
        )}
        
        {/* Estado */}
        <div className="flex items-center gap-2">
          <Badge variant={getEstadoBadgeVariant(proyecto.estado)}>
            {formatEstado(proyecto.estado)}
          </Badge>
        </div>
      </div>
      
      {/* Botón Editar */}
      {onEdit && (
        <Button variant="outline" onClick={onEdit}>
          <Edit className="h-4 w-4 mr-2" />
          Editar
        </Button>
      )}
    </div>
  );
}

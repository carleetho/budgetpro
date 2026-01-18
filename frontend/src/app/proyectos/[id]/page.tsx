"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { ProjectHeader } from "@/modules/proyectos/components/ProjectHeader";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ProyectoService } from "@/services/proyecto.service";
import type { Proyecto } from "@/core/types";
import { ClipboardCheck, Loader2 } from "lucide-react";
import { toast } from "sonner";
import Link from "next/link";

/**
 * Página principal (General/Dashboard) de un proyecto individual.
 * Muestra un resumen del proyecto con datos reales.
 */
export default function ProjectPage() {
  const params = useParams();
  const proyectoId = params.id as string;
  
  const [proyecto, setProyecto] = useState<Proyecto | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadProyecto = async () => {
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
      } catch (err) {
        console.error("Error al cargar proyecto:", err);
        const errorMessage = err instanceof Error 
          ? err.message 
          : "Error al cargar el proyecto. Por favor, intenta nuevamente.";
        setError(errorMessage);
        toast.error(errorMessage);
      } finally {
        setIsLoading(false);
      }
    };

    loadProyecto();
  }, [proyectoId]);

  const handleEdit = () => {
    // TODO: Implementar edición de proyecto
    toast.info("Funcionalidad de edición próximamente");
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
      {/* Header del Proyecto */}
      <ProjectHeader proyecto={proyecto} onEdit={handleEdit} />

      {/* Información General */}
      <Card>
        <CardHeader>
          <CardTitle>Información General</CardTitle>
          <CardDescription>
            Vista de resumen y estadísticas del proyecto
          </CardDescription>
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
                  {new Date(proyecto.createdAt).toLocaleDateString('es-ES', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                  })}
                </p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Acciones rápidas</CardTitle>
          <CardDescription>
            Accede a los flujos operativos principales del proyecto.
          </CardDescription>
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
    </div>
  );
}

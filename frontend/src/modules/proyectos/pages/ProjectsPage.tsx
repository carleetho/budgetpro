"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ProjectListTable } from "../components/ProjectListTable";
import { CreateProjectDialog } from "../components/CreateProjectDialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ProyectoService } from "@/services/proyecto.service";
import type { Proyecto } from "@/core/types";
import { Plus, Loader2 } from "lucide-react";
import { toast } from "sonner";

/**
 * Página principal de gestión de proyectos.
 */
export default function ProjectsPage() {
  const router = useRouter();
  const [proyectos, setProyectos] = useState<Proyecto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const loadProyectos = async () => {
    setIsLoading(true);
    try {
      const data = await ProyectoService.listar();
      setProyectos(data);
    } catch (error) {
      console.error("Error al cargar proyectos:", error);
      toast.error(
        error instanceof Error
          ? error.message
          : "Error al cargar los proyectos. Por favor, intenta nuevamente."
      );
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadProyectos();
  }, []);

  const handleCreateSuccess = (created: Proyecto) => {
    void loadProyectos();
    toast.success("Obra creada", {
      description:
        "Siguiente paso: entra en la obra y crea un presupuesto cuando lo necesites.",
      action: {
        label: "Ir al proyecto",
        onClick: () => router.push(`/proyectos/${created.id}#presupuestos`),
      },
      duration: 10000,
    });
  };


  return (
    <div className="container mx-auto py-8 px-4">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Proyectos</CardTitle>
              <CardDescription>
                Gestiona tus proyectos de construcción y controla sus presupuestos.
              </CardDescription>
            </div>
            <Button onClick={() => setIsDialogOpen(true)}>
              <Plus className="h-4 w-4 mr-2" />
              Nuevo Proyecto
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
              <span className="ml-2 text-muted-foreground">Cargando proyectos...</span>
            </div>
          ) : (
            <ProjectListTable
              proyectos={proyectos}
            />
          )}
        </CardContent>
      </Card>

      <CreateProjectDialog
        open={isDialogOpen}
        onOpenChange={setIsDialogOpen}
        onSuccess={handleCreateSuccess}
      />
    </div>
  );
}

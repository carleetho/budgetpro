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
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
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
import { Search, Package, Users, Wrench, FileText } from "lucide-react";
import { RecursosService } from "@/services/recursos.service";
import type { Recurso, TipoRecurso } from "@/core/types/recursos";
import { Loader2 } from "lucide-react";

interface ResourceLibraryProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  tipoFiltro?: TipoRecurso;
  onSelect: (recurso: Recurso) => void;
}

/**
 * Componente de catálogo de recursos (Biblioteca).
 * 
 * Muestra un Dialog con lista de recursos buscable y filtrable por tipo.
 * Permite seleccionar un recurso para agregarlo a un APU.
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
  const [tipoSeleccionado, setTipoSeleccionado] = useState<TipoRecurso | "TODOS">(
    tipoFiltro || "TODOS"
  );

  // Cargar recursos
  useEffect(() => {
    if (open) {
      loadRecursos();
    }
  }, [open, tipoSeleccionado, searchTerm]);

  const loadRecursos = async () => {
    setIsLoading(true);
    try {
      const tipo = tipoSeleccionado === "TODOS" ? undefined : tipoSeleccionado;
      const data = await RecursosService.buscar(searchTerm, tipo);
      setRecursos(data);
    } catch (error) {
      console.error("Error al cargar recursos:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelect = (recurso: Recurso) => {
    onSelect(recurso);
    onOpenChange(false);
    setSearchTerm("");
  };

  const getTipoIcon = (tipo: TipoRecurso) => {
    switch (tipo) {
      case "MATERIAL":
        return <Package className="h-4 w-4" />;
      case "MANO_DE_OBRA":
        return <Users className="h-4 w-4" />;
      case "EQUIPO":
        return <Wrench className="h-4 w-4" />;
      case "SUBCONTRATO":
        return <FileText className="h-4 w-4" />;
    }
  };

  const getTipoBadgeVariant = (tipo: TipoRecurso) => {
    switch (tipo) {
      case "MATERIAL":
        return "default";
      case "MANO_DE_OBRA":
        return "secondary";
      case "EQUIPO":
        return "outline";
      case "SUBCONTRATO":
        return "destructive";
    }
  };

  const formatTipo = (tipo: TipoRecurso) => {
    return tipo.replace("_", " ");
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[700px] max-h-[80vh] flex flex-col">
        <DialogHeader>
          <DialogTitle>Catálogo de Recursos</DialogTitle>
          <DialogDescription>
            Selecciona un recurso para agregarlo al análisis de precio unitario
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 flex-1 overflow-hidden flex flex-col">
          {/* Búsqueda */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Buscar por nombre, código o descripción..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>

          {/* Tabs por tipo */}
          <Tabs
            value={tipoSeleccionado}
            onValueChange={(value) => setTipoSeleccionado(value as TipoRecurso | "TODOS")}
            className="flex-1 flex flex-col overflow-hidden"
          >
            <TabsList className="grid w-full grid-cols-5">
              <TabsTrigger value="TODOS">Todos</TabsTrigger>
              <TabsTrigger value="MATERIAL">Materiales</TabsTrigger>
              <TabsTrigger value="MANO_DE_OBRA">Mano de Obra</TabsTrigger>
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
                  <p className="text-sm mt-2">Intenta con otro término de búsqueda</p>
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-[60px]">Tipo</TableHead>
                      <TableHead>Código</TableHead>
                      <TableHead>Nombre</TableHead>
                      <TableHead className="w-[80px]">Unidad</TableHead>
                      <TableHead className="w-[120px] text-right">Precio Base</TableHead>
                      <TableHead className="w-[100px]">Acción</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {recursos.map((recurso) => (
                      <TableRow key={recurso.id}>
                        <TableCell>
                          <div className="flex items-center justify-center">
                            {getTipoIcon(recurso.tipo)}
                          </div>
                        </TableCell>
                        <TableCell className="font-mono text-sm">
                          {recurso.codigo || "-"}
                        </TableCell>
                        <TableCell>
                          <div>
                            <div className="font-medium">{recurso.nombre}</div>
                            {recurso.descripcion && (
                              <div className="text-sm text-muted-foreground">
                                {recurso.descripcion}
                              </div>
                            )}
                          </div>
                        </TableCell>
                        <TableCell className="text-center text-sm">
                          {recurso.unidad}
                        </TableCell>
                        <TableCell className="text-right font-mono text-sm">
                          {recurso.precioBase.toLocaleString('es-ES', {
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2,
                            style: 'currency',
                            currency: 'USD'
                          })}
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleSelect(recurso)}
                          >
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

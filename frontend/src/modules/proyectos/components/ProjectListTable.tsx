"use client";

import Link from "next/link";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { Proyecto, EstadoProyecto } from "@/core/types";

interface ProjectListTableProps {
  proyectos: Proyecto[];
}

/**
 * Componente de tabla para mostrar la lista de proyectos.
 */
export function ProjectListTable({ proyectos }: ProjectListTableProps) {
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

  if (proyectos.length === 0) {
    return (
      <div className="text-center py-12 text-muted-foreground">
        <p>No hay proyectos registrados.</p>
        <p className="text-sm mt-2">Crea tu primer proyecto para comenzar.</p>
      </div>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Nombre</TableHead>
          <TableHead>Ubicación</TableHead>
          <TableHead>Estado</TableHead>
          <TableHead className="text-right">Acciones</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {proyectos.map((proyecto) => (
          <TableRow key={proyecto.id}>
            <TableCell className="font-medium">{proyecto.nombre}</TableCell>
            <TableCell>{proyecto.ubicacion || "-"}</TableCell>
            <TableCell>
              <Badge variant={getEstadoBadgeVariant(proyecto.estado)}>
                {formatEstado(proyecto.estado)}
              </Badge>
            </TableCell>
            <TableCell className="text-right">
              <Button
                variant="outline"
                size="sm"
                asChild
              >
                <Link href={`/proyectos/${proyecto.id}`}>
                  Ver
                </Link>
              </Button>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}

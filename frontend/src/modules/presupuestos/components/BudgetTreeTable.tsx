"use client";

import React, { useMemo } from "react";
import { createColumnHelper, type ColumnDef } from "@tanstack/react-table";
import { TreeDataGrid } from "@/components/ui/data-grid/TreeDataGrid";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Folder, Hammer, Plus, Edit, Trash2, Calculator } from "lucide-react";
import type { ItemPresupuesto, NivelPresupuesto } from "@/core/types/presupuesto";
import { cn } from "@/lib/utils";

interface BudgetTreeTableProps {
  items: ItemPresupuesto[];
  onAgregarHijo?: (itemId: string) => void;
  onEditar?: (item: ItemPresupuesto) => void;
  onEliminar?: (itemId: string) => void;
  onAbrirAPU?: (item: ItemPresupuesto) => void;
}

const columnHelper = createColumnHelper<ItemPresupuesto>();

/**
 * Componente de tabla jerárquica para mostrar el presupuesto.
 * 
 * Utiliza TreeDataGrid con @tanstack/react-table para manejar
 * la expansión/colapso de nodos jerárquicos.
 */
export function BudgetTreeTable({
  items,
  onAgregarHijo,
  onEditar,
  onEliminar,
  onAbrirAPU,
}: BudgetTreeTableProps) {
  const getEstadoBadgeVariant = (estado: NivelPresupuesto) => {
    switch (estado) {
      case "CAPITULO":
        return "default";
      case "SUBCAPITULO":
        return "secondary";
      case "PARTIDA":
        return "outline";
      default:
        return "secondary";
    }
  };

  const formatEstado = (estado: NivelPresupuesto) => {
    return estado.replace("_", " ");
  };

  const columns = useMemo(
    () => [
      // Columna: Código
      columnHelper.accessor("codigo", {
        header: "Código",
        cell: (info) => {
          const item = info.row.original;
          const isCapitulo = item.nivel === "CAPITULO";
          const isSubcapitulo = item.nivel === "SUBCAPITULO";
          const isPartida = item.nivel === "PARTIDA";
          const Icon = isPartida ? Hammer : Folder;
          const iconSize = isCapitulo ? "h-4 w-4" : "h-3.5 w-3.5";

          return (
            <div className="flex items-center gap-2">
              <Icon className={cn(iconSize, "text-muted-foreground flex-shrink-0")} />
              <span className="font-mono text-xs">{item.codigo}</span>
            </div>
          );
        },
        size: 100,
      }),

      // Columna: Descripción
      columnHelper.accessor("descripcion", {
        header: "Descripción",
        cell: (info) => {
          const item = info.row.original;
          const isCapitulo = item.nivel === "CAPITULO";
          const isSubcapitulo = item.nivel === "SUBCAPITULO";

          return (
            <span
              className={cn(
                "text-sm",
                isCapitulo && "font-bold",
                isSubcapitulo && "font-semibold"
              )}
            >
              {item.descripcion}
            </span>
          );
        },
        size: 300,
      }),

      // Columna: Unidad
      columnHelper.accessor("unidad", {
        header: "Unidad",
        cell: (info) => {
          const unidad = info.getValue();
          return (
            <span className="text-xs text-center text-muted-foreground">
              {unidad || "-"}
            </span>
          );
        },
        size: 80,
      }),

      // Columna: Metrado
      columnHelper.accessor("metrado", {
        header: "Metrado",
        cell: (info) => {
          const metrado = info.getValue();
          return (
            <span className="font-mono text-xs text-right">
              {metrado !== undefined
                ? metrado.toLocaleString("es-ES", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                  })
                : "-"}
            </span>
          );
        },
        size: 120,
      }),

      // Columna: Precio Unitario
      columnHelper.accessor("precioUnitario", {
        header: "Precio Unit.",
        cell: (info) => {
          const precio = info.getValue();
          return (
            <span className="font-mono text-xs text-right">
              {precio !== undefined
                ? precio.toLocaleString("es-ES", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                    style: "currency",
                    currency: "USD",
                  })
                : "-"}
            </span>
          );
        },
        size: 140,
      }),

      // Columna: Parcial
      columnHelper.accessor("parcial", {
        header: "Parcial",
        cell: (info) => {
          const parcial = info.getValue();
          return (
            <span className="font-mono text-xs font-semibold text-right">
              {parcial !== undefined
                ? parcial.toLocaleString("es-ES", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2,
                    style: "currency",
                    currency: "USD",
                  })
                : "-"}
            </span>
          );
        },
        size: 140,
      }),

      // Columna: Acciones
      columnHelper.display({
        id: "acciones",
        header: "Acciones",
        cell: (info) => {
          const item = info.row.original;
          const isPartida = item.nivel === "PARTIDA";

          return (
            <div className="flex items-center gap-1">
              {isPartida && onAbrirAPU && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-7 w-7 p-0"
                  onClick={(e) => {
                    e.stopPropagation();
                    onAbrirAPU(item);
                  }}
                  title="Análisis de Precio Unitario"
                >
                  <Calculator className="h-3.5 w-3.5 text-primary" />
                </Button>
              )}
              {!isPartida && onAgregarHijo && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-7 w-7 p-0"
                  onClick={(e) => {
                    e.stopPropagation();
                    onAgregarHijo(item.id);
                  }}
                  title="Agregar hijo"
                >
                  <Plus className="h-3.5 w-3.5" />
                </Button>
              )}
              {onEditar && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-7 w-7 p-0"
                  onClick={(e) => {
                    e.stopPropagation();
                    onEditar(item);
                  }}
                  title="Editar"
                >
                  <Edit className="h-3.5 w-3.5" />
                </Button>
              )}
              {onEliminar && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-7 w-7 p-0 text-destructive hover:text-destructive"
                  onClick={(e) => {
                    e.stopPropagation();
                    onEliminar(item.id);
                  }}
                  title="Eliminar"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </Button>
              )}
            </div>
          );
        },
        size: 120,
      }),
    ],
    [onAgregarHijo, onEditar, onEliminar, onAbrirAPU]
  );

  // Función para obtener los hijos de un nodo
  const getSubRows = (row: ItemPresupuesto) => {
    return row.hijos && row.hijos.length > 0 ? row.hijos : undefined;
  };

  if (items.length === 0) {
    return (
      <div className="text-center py-12 text-muted-foreground">
        <p className="text-sm">No hay partidas registradas.</p>
        <p className="text-xs mt-2">Agrega un capítulo para comenzar.</p>
      </div>
    );
  }

  return (
    <div className="border rounded-lg overflow-hidden">
      <TreeDataGrid
        data={items}
        columns={columns as ColumnDef<ItemPresupuesto>[]}
        getSubRows={getSubRows}
        className="w-full"
      />
    </div>
  );
}

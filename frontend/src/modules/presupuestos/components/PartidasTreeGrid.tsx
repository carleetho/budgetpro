"use client";

import React, { useMemo } from "react";
import { createColumnHelper, type ColumnDef } from "@tanstack/react-table";
import { TreeDataGrid } from "@/components/ui/data-grid/TreeDataGrid";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Folder, Hammer, Plus, Calculator } from "lucide-react";
import type { PartidaResponseDto, WbsNodeResponseDto } from "@/core/types/presupuesto-contract";
import { isWbsLeafNode } from "@/core/types/presupuesto-contract";

type RowModel = WbsNodeResponseDto;

const columnHelper = createColumnHelper<RowModel>();

export interface PartidasTreeGridProps {
  nodes: WbsNodeResponseDto[];
  /** [P-01 / REGLA-046]: en CONGELADO toda la grilla es estrictamente solo lectura. */
  readOnly: boolean;
  onAssignApu?: (partida: PartidaResponseDto) => void;
  /**
   * Solo `POST /api/v1/partidas` existe en `PartidaController` (GF-02: sin PUT/PATCH/DELETE).
   * Botón “agregar hijo” solo si se provee callback (p. ej. diálogo de creación).
   */
  onAddChild?: (padrePartidaId: string, padreLevel: number) => void;
}

function formatMetrado(value: number | null | undefined): string {
  if (value == null || Number.isNaN(value)) return "";
  return String(value);
}

/**
 * TreeGrid de WBS devuelto por `GET /partidas/wbs`.
 * - [P-03]: “Asignar APU” solo en nodos hoja.
 * - [GF-02]: sin UI de borrado ni edición de metrado post-creación (no hay endpoints REST).
 * - [REGLA-1105]: metrado ≥ 0 se valida al crear partida (`BudgetNodeDialog`: `type="number"` + `min="0"`).
 */
export function PartidasTreeGrid({ nodes, readOnly, onAssignApu, onAddChild }: PartidasTreeGridProps) {
  const columns = useMemo(
    () => [
      columnHelper.accessor((row) => row.partida.item, {
        id: "item",
        header: "Item",
        cell: (info) => {
          const row = info.row.original;
          const leaf = isWbsLeafNode(row);
          return (
            <div className="flex items-center gap-2">
              {leaf ? (
                <Hammer className="h-3.5 w-3.5 text-muted-foreground flex-shrink-0" />
              ) : (
                <Folder className="h-4 w-4 text-muted-foreground flex-shrink-0" />
              )}
              <span className="font-mono text-xs">{row.partida.item}</span>
              <Badge variant="outline" className="text-[10px] px-1 py-0">
                N{row.partida.nivel}
              </Badge>
            </div>
          );
        },
        size: 120,
      }),
      columnHelper.accessor((row) => row.partida.descripcion, {
        id: "descripcion",
        header: "Descripción",
        cell: (info) => {
          const desc = info.row.original.partida.descripcion;
          return <span className="text-sm">{desc}</span>;
        },
        size: 280,
      }),
      columnHelper.accessor((row) => row.partida.unidad, {
        id: "unidad",
        header: "Und.",
        cell: (info) => (
          <span className="text-xs text-muted-foreground text-center block">
            {info.getValue() ?? "—"}
          </span>
        ),
        size: 72,
      }),
      columnHelper.display({
        id: "metradoBase",
        header: "Metrado (base)",
        cell: (info) => {
          const p = info.row.original.partida;
          const v = formatMetrado(p.metrado ?? undefined);
          return (
            <span className="font-mono text-xs text-right block w-full pr-1">
              {v || "—"}
            </span>
          );
        },
        size: 140,
      }),
      columnHelper.display({
        id: "acciones",
        header: "Acciones",
        cell: (info) => {
          const row = info.row.original;
          const p = row.partida;
          const leaf = isWbsLeafNode(row);
          const structureLocked = readOnly;

          return (
            <div className="flex items-center gap-1">
              {leaf && onAssignApu && (
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="h-7 w-7 p-0"
                  disabled={structureLocked}
                  title="Asignar APU"
                  onClick={(e) => {
                    e.stopPropagation();
                    onAssignApu(p);
                  }}
                >
                  <Calculator className="h-3.5 w-3.5 text-primary" />
                </Button>
              )}
              {!leaf && onAddChild && (
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  className="h-7 w-7 p-0"
                  disabled={structureLocked}
                  title="Agregar nodo hijo"
                  onClick={(e) => {
                    e.stopPropagation();
                    onAddChild(p.id, p.nivel);
                  }}
                >
                  <Plus className="h-3.5 w-3.5" />
                </Button>
              )}
            </div>
          );
        },
        size: 120,
      }),
    ],
    [readOnly, onAssignApu, onAddChild]
  );

  const getSubRows = (row: RowModel) => {
    const ch = row.children;
    return ch && ch.length > 0 ? ch : undefined;
  };

  if (!nodes.length) {
    return (
      <div className="text-center py-12 space-y-4">
        <p className="text-muted-foreground text-sm">
          No hay partidas en el WBS para este presupuesto.
        </p>
        {!readOnly && onAddChild && (
          <Button variant="outline" size="sm" onClick={() => onAddChild("", 0)}>
            <Plus className="h-4 w-4 mr-2" />
            Agregar primer capítulo o partida
          </Button>
        )}
      </div>
    );
  }

  return (
    <div className="border rounded-lg overflow-hidden">
      <TreeDataGrid
        data={nodes}
        columns={columns as ColumnDef<RowModel>[]}
        getSubRows={getSubRows}
        className="w-full"
      />
    </div>
  );
}

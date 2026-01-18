"use client";

import React, { useMemo, useState } from "react";
import {
  useReactTable,
  getCoreRowModel,
  getExpandedRowModel,
  flexRender,
  type ColumnDef,
  type Row,
  type ExpandedState,
} from "@tanstack/react-table";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { ChevronRight, ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";

export interface TreeDataGridProps<T> {
  data: T[];
  columns: ColumnDef<T>[];
  getSubRows?: (row: T) => T[] | undefined;
  onRowClick?: (row: Row<T>) => void;
  className?: string;
}

/**
 * Componente de tabla jerárquica (Tree Data Grid) reutilizable.
 * 
 * Utiliza @tanstack/react-table para manejar la expansión/colapso de nodos
 * y renderiza una tabla con estilo Enterprise compacto.
 */
export function TreeDataGrid<T extends Record<string, any>>({
  data,
  columns,
  getSubRows,
  onRowClick,
  className,
}: TreeDataGridProps<T>) {
  const [expanded, setExpanded] = useState<ExpandedState>({});

  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getExpandedRowModel: getExpandedRowModel(),
    getSubRows,
    state: {
      expanded,
    },
    onExpandedChange: setExpanded,
  });

  const rows = table.getRowModel().rows;

  return (
    <div className={cn("w-full", className)}>
      <Table>
        <TableHeader>
          {table.getHeaderGroups().map((headerGroup) => (
            <TableRow key={headerGroup.id}>
              {headerGroup.headers.map((header) => (
                <TableHead key={header.id} className="text-xs font-semibold">
                  {header.isPlaceholder
                    ? null
                    : flexRender(header.column.columnDef.header, header.getContext())}
                </TableHead>
              ))}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody>
          {rows.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={columns.length}
                className="h-24 text-center text-sm text-muted-foreground"
              >
                No hay datos para mostrar
              </TableCell>
            </TableRow>
          ) : (
            rows.map((row) => {
              const canExpand = row.getCanExpand();
              const isExpanded = row.getIsExpanded();

              return (
                <TableRow
                  key={row.id}
                  data-state={row.getIsSelected() && "selected"}
                  className={cn(
                    "cursor-pointer hover:bg-[#e3f2fd]/30",
                    row.getIsSelected() && "bg-[#e3f2fd]",
                    onRowClick && "hover:bg-[#e3f2fd]/30"
                  )}
                  onClick={() => onRowClick?.(row)}
                >
                  {row.getVisibleCells().map((cell, cellIndex) => {
                    const isFirstCell = cellIndex === 0;
                    const depth = row.depth;
                    const indentSize = depth * 20; // 20px por nivel

                    return (
                      <TableCell
                        key={cell.id}
                        className={cn(
                          "text-xs",
                          isFirstCell && "relative"
                        )}
                        style={isFirstCell ? { paddingLeft: `${8 + indentSize}px` } : undefined}
                      >
                        {isFirstCell && canExpand && (
                          <Button
                            variant="ghost"
                            size="sm"
                            className="absolute left-0 h-6 w-6 p-0 hover:bg-transparent"
                            onClick={(e) => {
                              e.stopPropagation();
                              row.toggleExpanded();
                            }}
                          >
                            {isExpanded ? (
                              <ChevronDown className="h-4 w-4" />
                            ) : (
                              <ChevronRight className="h-4 w-4" />
                            )}
                          </Button>
                        )}
                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                      </TableCell>
                    );
                  })}
                </TableRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </div>
  );
}

"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Plus, DollarSign, Loader2 } from "lucide-react";
import { BudgetTreeTable } from "@/modules/presupuestos/components/BudgetTreeTable";
import { BudgetNodeDialog } from "@/modules/presupuestos/components/BudgetNodeDialog";
import { APUDesigner } from "@/modules/presupuestos/components/APUDesigner";
import { PresupuestoService } from "@/services/presupuesto.service";
import type { ItemPresupuesto, CrearItemPresupuestoCommand } from "@/core/types/presupuesto";
import type { AnalisisUnitario } from "@/core/types/apu";
import { toast } from "sonner";

/**
 * Página de gestión del Presupuesto de Obra.
 * 
 * Muestra la estructura jerárquica de Partidas (WBS) y permite
 * agregar, editar y eliminar items.
 */
export default function PresupuestoPage() {
  const params = useParams();
  const proyectoId = params.id as string;

  const [items, setItems] = useState<ItemPresupuesto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [dialogNivel, setDialogNivel] = useState<'CAPITULO' | 'SUBCAPITULO' | 'PARTIDA'>('CAPITULO');
  const [dialogPadreId, setDialogPadreId] = useState<string | null>(null);
  const [itemEditando, setItemEditando] = useState<ItemPresupuesto | null>(null);
  const [isAPUOpen, setIsAPUOpen] = useState(false);
  const [partidaAPU, setPartidaAPU] = useState<ItemPresupuesto | null>(null);

  // Calcular costo directo total
  const costoDirectoTotal = items.reduce((sum, item) => {
    return sum + (item.parcial || 0);
  }, 0);

  // Cargar árbol de partidas
  useEffect(() => {
    const loadArbol = async () => {
      if (!proyectoId) return;

      setIsLoading(true);
      try {
        const data = await PresupuestoService.obtenerArbol(proyectoId);
        setItems(data);
      } catch (error) {
        console.error("Error al cargar presupuesto:", error);
        toast.error("Error al cargar el presupuesto");
      } finally {
        setIsLoading(false);
      }
    };

    loadArbol();
  }, [proyectoId]);

  // Abrir dialog para agregar capítulo
  const handleAgregarCapitulo = () => {
    setDialogNivel('CAPITULO');
    setDialogPadreId(null);
    setItemEditando(null);
    setIsDialogOpen(true);
  };

  // Abrir dialog para agregar hijo
  const handleAgregarHijo = (padreId: string) => {
    // Buscar el item padre para determinar el nivel del hijo
    const encontrarItem = (items: ItemPresupuesto[], id: string): ItemPresupuesto | null => {
      for (const item of items) {
        if (item.id === id) return item;
        if (item.hijos) {
          const found = encontrarItem(item.hijos, id);
          if (found) return found;
        }
      }
      return null;
    };

    const padre = encontrarItem(items, padreId);
    if (!padre) return;

    // Determinar nivel del hijo
    let nivelHijo: 'CAPITULO' | 'SUBCAPITULO' | 'PARTIDA' = 'PARTIDA';
    if (padre.nivel === 'CAPITULO') {
      nivelHijo = 'SUBCAPITULO';
    } else if (padre.nivel === 'SUBCAPITULO') {
      nivelHijo = 'PARTIDA';
    }

    setDialogNivel(nivelHijo);
    setDialogPadreId(padreId);
    setItemEditando(null);
    setIsDialogOpen(true);
  };

  // Abrir dialog para editar
  const handleEditar = (item: ItemPresupuesto) => {
    setDialogNivel(item.nivel);
    setDialogPadreId(item.padreId || null);
    setItemEditando(item);
    setIsDialogOpen(true);
  };

  // Abrir diseñador de APU
  const handleAbrirAPU = (item: ItemPresupuesto) => {
    setPartidaAPU(item);
    setIsAPUOpen(true);
  };

  // Guardar APU
  const handleGuardarAPU = async (apu: AnalisisUnitario) => {
    try {
      // TODO: Guardar APU en backend cuando esté disponible
      console.log("APU guardado:", apu);
      
      // Actualizar precio unitario de la partida en el árbol
      const actualizarPrecioPartida = (items: ItemPresupuesto[]): ItemPresupuesto[] => {
        return items.map(item => {
          if (item.id === apu.partidaId) {
            return {
              ...item,
              precioUnitario: apu.costoDirecto,
              parcial: (item.metrado || 0) * apu.costoDirecto,
            };
          }
          return {
            ...item,
            hijos: item.hijos ? actualizarPrecioPartida(item.hijos) : []
          };
        });
      };
      
      setItems(actualizarPrecioPartida(items));
      toast.success("APU guardado correctamente. Precio unitario actualizado.");
    } catch (error) {
      console.error("Error al guardar APU:", error);
      toast.error("Error al guardar el APU");
      throw error;
    }
  };

  // Eliminar item
  const handleEliminar = async (itemId: string) => {
    if (!confirm("¿Estás seguro de eliminar este item?")) {
      return;
    }

    try {
      await PresupuestoService.eliminarItem(itemId);
      
      // Remover del árbol local
      const removerItem = (items: ItemPresupuesto[]): ItemPresupuesto[] => {
        return items
          .filter(item => item.id !== itemId)
          .map(item => ({
            ...item,
            hijos: item.hijos ? removerItem(item.hijos) : []
          }));
      };

      setItems(removerItem(items));
      toast.success("Item eliminado correctamente");
    } catch (error) {
      console.error("Error al eliminar item:", error);
      toast.error("Error al eliminar el item");
    }
  };

  // Guardar item (crear o actualizar)
  const handleSubmitItem = async (command: CrearItemPresupuestoCommand) => {
    try {
      const nuevoItem = await PresupuestoService.crearItem(proyectoId, command);

      if (itemEditando) {
        // Actualizar item existente
        const actualizarItem = (items: ItemPresupuesto[]): ItemPresupuesto[] => {
          return items.map(item => {
            if (item.id === itemEditando.id) {
              return { ...nuevoItem, hijos: item.hijos };
            }
            return {
              ...item,
              hijos: item.hijos ? actualizarItem(item.hijos) : []
            };
          });
        };
        setItems(actualizarItem(items));
        toast.success("Item actualizado correctamente");
      } else {
        // Agregar nuevo item
        if (command.padreId) {
          // Agregar como hijo
          const agregarHijo = (items: ItemPresupuesto[]): ItemPresupuesto[] => {
            return items.map(item => {
              if (item.id === command.padreId) {
                return {
                  ...item,
                  hijos: [...(item.hijos || []), nuevoItem]
                };
              }
              return {
                ...item,
                hijos: item.hijos ? agregarHijo(item.hijos) : []
              };
            });
          };
          setItems(agregarHijo(items));
        } else {
          // Agregar como raíz
          setItems([...items, nuevoItem]);
        }
        toast.success("Item creado correctamente");
      }

      setIsDialogOpen(false);
    } catch (error) {
      console.error("Error al guardar item:", error);
      toast.error("Error al guardar el item");
      throw error;
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        <span className="ml-2 text-muted-foreground">Cargando presupuesto...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Presupuesto de Obra</h1>
          <p className="text-muted-foreground mt-2">
            Gestión del desglose de trabajo (WBS) y estructura de costos
          </p>
        </div>
        <Button onClick={handleAgregarCapitulo}>
          <Plus className="h-4 w-4 mr-2" />
          Agregar Capítulo
        </Button>
      </div>

      {/* Resumen Financiero */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <DollarSign className="h-5 w-5" />
            Resumen Financiero
          </CardTitle>
          <CardDescription>
            Costo directo total del presupuesto
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-3xl font-bold">
            {costoDirectoTotal.toLocaleString('es-ES', {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2,
              style: 'currency',
              currency: 'USD'
            })}
          </div>
        </CardContent>
      </Card>

      {/* Tabla Jerárquica */}
      <Card>
        <CardHeader>
          <CardTitle>Estructura de Partidas</CardTitle>
          <CardDescription>
            Desglose jerárquico del presupuesto (Capítulos, Subcapítulos y Partidas)
          </CardDescription>
        </CardHeader>
        <CardContent>
          <BudgetTreeTable
            items={items}
            onAgregarHijo={handleAgregarHijo}
            onEditar={handleEditar}
            onEliminar={handleEliminar}
            onAbrirAPU={handleAbrirAPU}
          />
        </CardContent>
      </Card>

      {/* Dialog de Creación/Edición */}
      <BudgetNodeDialog
        open={isDialogOpen}
        onOpenChange={setIsDialogOpen}
        proyectoId={proyectoId}
        nivel={dialogNivel}
        padreId={dialogPadreId}
        itemEditando={itemEditando}
        onSubmit={handleSubmitItem}
      />

      {/* Diseñador de APU */}
      {partidaAPU && (
        <APUDesigner
          open={isAPUOpen}
          onOpenChange={setIsAPUOpen}
          partida={partidaAPU}
          onSave={handleGuardarAPU}
        />
      )}
    </div>
  );
}

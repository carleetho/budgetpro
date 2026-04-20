import type { ItemPresupuesto } from "@/core/types/presupuesto";
import type { PartidaResponseDto } from "@/core/types/presupuesto-contract";

/** Adapta la partida del WBS REST al modelo de ítem usado por el diseñador de APU. */
export function partidaResponseToItemPresupuesto(p: PartidaResponseDto): ItemPresupuesto {
  return {
    id: p.id,
    codigo: p.item,
    descripcion: p.descripcion,
    nivel: "PARTIDA",
    unidad: p.unidad ?? undefined,
    metrado: p.metrado ?? undefined,
    padreId: p.padreId ?? undefined,
  };
}

import type { WbsNodeResponseDto, PartidaResponseDto } from "@/core/types/presupuesto-contract";
import { isWbsLeafNode } from "@/core/types/presupuesto-contract";

/** Recorre el WBS y devuelve solo nodos hoja (candidatos a APU). */
export function collectWbsLeafPartidas(nodes: WbsNodeResponseDto[]): PartidaResponseDto[] {
  const leaves: PartidaResponseDto[] = [];
  const walk = (list: WbsNodeResponseDto[]) => {
    for (const n of list) {
      if (isWbsLeafNode(n)) {
        leaves.push(n.partida);
      } else if (n.children?.length) {
        walk(n.children);
      }
    }
  };
  walk(nodes);
  return leaves;
}

"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { useParams, usePathname } from "next/navigation";
import Link from "next/link";
import { Loader2, ShieldCheck } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { BudgetNodeDialog } from "@/modules/presupuestos/components/BudgetNodeDialog";
import { apiClient } from "@/services/api-client";
import { PresupuestoApiService } from "@/services/presupuesto-api.service";
import { PartidasWbsService } from "@/services/partidas-wbs.service";
import { ProyectoService } from "@/services/proyecto.service";
import type { Proyecto } from "@/core/types";
import type { CrearItemPresupuestoCommand } from "@/core/types/presupuesto";
import type { PresupuestoResponseDto, WbsNodeResponseDto } from "@/core/types/presupuesto-contract";
import { isPresupuestoWbsReadOnly } from "@/core/types/presupuesto-contract";
import { BudgetProApiError } from "@/lib/budget-pro-api-error";
import { cn } from "@/lib/utils";

function badgeVariant(
  estado: PresupuestoResponseDto["estado"]
): "default" | "secondary" | "destructive" | "outline" {
  switch (estado) {
    case "CONGELADO":
      return "secondary";
    case "INVALIDADO":
      return "destructive";
    default:
      return "outline";
  }
}

export interface PresupuestoWorkspaceContextValue {
  presupuestoId: string;
  proyectoId: string;
  budget: PresupuestoResponseDto | null;
  proyecto: Proyecto | null;
  proyectoLoading: boolean;
  wbs: WbsNodeResponseDto[];
  loading: boolean;
  readOnly: boolean;
  reload: () => Promise<void>;
  approving: boolean;
  aprobar: () => Promise<void>;
  isDialogOpen: boolean;
  setDialogOpen: (open: boolean) => void;
  selectedPadreId: string | null;
  openAddChild: (padreId: string | null, padreLevel: number) => void;
  handleCreatePartida: (command: CrearItemPresupuestoCommand) => Promise<void>;
}

const PresupuestoWorkspaceContext = createContext<PresupuestoWorkspaceContextValue | null>(null);

export function usePresupuestoWorkspace(): PresupuestoWorkspaceContextValue {
  const ctx = useContext(PresupuestoWorkspaceContext);
  if (!ctx) {
    throw new Error("usePresupuestoWorkspace debe usarse dentro de PresupuestoWorkspaceProvider");
  }
  return ctx;
}

export function PresupuestoWorkspaceProvider({ children }: { children: ReactNode }) {
  const params = useParams();
  const presupuestoId = params.presupuestoId as string;

  const [budget, setBudget] = useState<PresupuestoResponseDto | null>(null);
  const [proyecto, setProyecto] = useState<Proyecto | null>(null);
  const [proyectoLoading, setProyectoLoading] = useState(false);
  const [wbs, setWbs] = useState<WbsNodeResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [approving, setApproving] = useState(false);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [selectedPadreId, setSelectedPadreId] = useState<string | null>(null);
  const [selectedPadreLevel, setSelectedPadreLevel] = useState(0);

  const proyectoId = budget?.proyectoId ?? "";

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const [b, tree] = await Promise.all([
        PresupuestoApiService.obtenerPorId(presupuestoId),
        PartidasWbsService.obtenerArbol(presupuestoId),
      ]);
      setBudget(b);
      setWbs(tree);
      setProyectoLoading(true);
      try {
        const p = await ProyectoService.obtenerPorId(b.proyectoId);
        setProyecto(p);
      } catch (pe) {
        console.error(pe);
        setProyecto(null);
      } finally {
        setProyectoLoading(false);
      }
    } catch (e) {
      console.error(e);
      if (BudgetProApiError.isInstance(e)) {
        toast.error(`[${e.businessCode}] ${e.message}`);
      } else {
        toast.error("No se pudo cargar el presupuesto.");
      }
    } finally {
      setLoading(false);
    }
  }, [presupuestoId]);

  useEffect(() => {
    void loadAll();
  }, [loadAll]);

  const readOnly = budget ? isPresupuestoWbsReadOnly(budget.estado) : true;

  const aprobar = useCallback(async () => {
    setApproving(true);
    try {
      await PresupuestoApiService.aprobar(presupuestoId);
      toast.success("Presupuesto aprobado y congelado.");
      await loadAll();
    } catch (e) {
      if (BudgetProApiError.isInstance(e)) {
        toast.error(`[${e.businessCode}] ${e.message}`);
      } else {
        toast.error("No se pudo aprobar el presupuesto.");
      }
    } finally {
      setApproving(false);
    }
  }, [loadAll, presupuestoId]);

  const openAddChild = useCallback((padreId: string | null, padreLevel: number) => {
    setSelectedPadreId(padreId || null);
    setSelectedPadreLevel(padreLevel);
    setIsDialogOpen(true);
  }, []);

  const handleCreatePartida = useCallback(
    async (command: CrearItemPresupuestoCommand) => {
      try {
        const payload = {
          presupuestoId,
          padreId: command.padreId,
          item: command.codigo,
          descripcion: command.descripcion,
          unidad: command.unidad || null,
          metrado: command.metrado ?? 0,
          nivel: selectedPadreLevel + 1,
        };
        await apiClient.post("/partidas", payload);
        toast.success("Ítem creado correctamente.");
        await loadAll();
      } catch (e) {
        console.error(e);
        toast.error("No se pudo crear el ítem.");
        throw e;
      }
    },
    [loadAll, presupuestoId, selectedPadreLevel]
  );

  const value = useMemo(
    (): PresupuestoWorkspaceContextValue => ({
      presupuestoId,
      proyectoId,
      budget,
      proyecto,
      proyectoLoading,
      wbs,
      loading,
      readOnly,
      reload: loadAll,
      approving,
      aprobar,
      isDialogOpen,
      setDialogOpen: setIsDialogOpen,
      selectedPadreId,
      openAddChild,
      handleCreatePartida,
    }),
    [
      presupuestoId,
      proyectoId,
      budget,
      proyecto,
      proyectoLoading,
      wbs,
      loading,
      readOnly,
      loadAll,
      approving,
      aprobar,
      isDialogOpen,
      selectedPadreId,
      openAddChild,
      handleCreatePartida,
    ]
  );

  return (
    <PresupuestoWorkspaceContext.Provider value={value}>{children}</PresupuestoWorkspaceContext.Provider>
  );
}

/** Cabecera + subnavegación del workspace de presupuesto */
export function PresupuestoWorkspaceShell({ children }: { children: ReactNode }) {
  const params = useParams();
  const proyectoRouteId = params.id as string;
  const presupuestoId = params.presupuestoId as string;

  const {
    budget,
    proyecto,
    proyectoLoading,
    loading,
    approving,
    aprobar,
    isDialogOpen,
    setDialogOpen,
    selectedPadreId,
    handleCreatePartida,
    proyectoId: pid,
  } = usePresupuestoWorkspace();

  const base = `/proyectos/${proyectoRouteId}/presupuestos/${presupuestoId}`;

  if (loading && !budget) {
    return (
      <div className="flex items-center justify-center py-12 text-muted-foreground">
        <Loader2 className="h-8 w-8 animate-spin" />
        <span className="ml-2">Cargando presupuesto…</span>
      </div>
    );
  }

  if (!budget) {
    return (
      <p className="text-sm text-muted-foreground py-8 text-center">Presupuesto no disponible.</p>
    );
  }

  return (
    <div className="space-y-6">
      <Button variant="outline" size="sm" asChild>
        <Link href={`/proyectos/${proyectoRouteId}#presupuestos`}>← Volver a presupuestos del proyecto</Link>
      </Button>

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">{budget.nombre}</h1>
          <p className="text-muted-foreground text-sm mt-1">
            {proyectoLoading ? (
              "Cargando obra…"
            ) : proyecto ? (
              <>
                Obra: <span className="font-medium text-foreground">{proyecto.nombre}</span>
                {proyecto.ubicacion ? (
                  <span className="text-muted-foreground"> — {proyecto.ubicacion}</span>
                ) : null}
              </>
            ) : (
              "Obra vinculada"
            )}
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <Badge variant={badgeVariant(budget.estado)}>{budget.estado}</Badge>
          {budget.esContractual && (
            <Badge variant="default" className="gap-1">
              <ShieldCheck className="h-3 w-3" />
              Contractual
            </Badge>
          )}
          <Button
            type="button"
            onClick={() => void aprobar()}
            disabled={budget.estado !== "BORRADOR" || approving}
          >
            {approving ? <Loader2 className="h-4 w-4 animate-spin" /> : "Aprobar presupuesto"}
          </Button>
        </div>
      </div>

      <nav className="flex flex-wrap gap-2 border-b pb-3" aria-label="Secciones del presupuesto">
        <WorkspaceNavLink href={`${base}/resumen`}>Resumen</WorkspaceNavLink>
        <WorkspaceNavLink href={`${base}/partidas`}>Partidas</WorkspaceNavLink>
        <WorkspaceNavLink href={`${base}/parametros`}>Parámetros</WorkspaceNavLink>
      </nav>

      <div>{children}</div>

      <BudgetNodeDialog
        open={isDialogOpen}
        onOpenChange={setDialogOpen}
        proyectoId={pid}
        nivel={selectedPadreId ? "PARTIDA" : "CAPITULO"}
        padreId={selectedPadreId}
        onSubmit={handleCreatePartida}
      />
    </div>
  );
}

function WorkspaceNavLink({ href, children }: { href: string; children: React.ReactNode }) {
  const pathname = usePathname();
  const active = pathname === href;

  return (
    <Button
      variant={active ? "secondary" : "ghost"}
      size="sm"
      className={cn(active && "font-semibold")}
      asChild
    >
      <Link href={href}>{children}</Link>
    </Button>
  );
}

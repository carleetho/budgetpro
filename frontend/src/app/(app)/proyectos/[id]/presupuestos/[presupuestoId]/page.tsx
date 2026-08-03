import { redirect } from "next/navigation";

type Props = {
  params: Promise<{ id: string; presupuestoId: string }>;
};

/** Entrada del workspace: por defecto abre Partidas (trabajo principal del presupuesto). */
export default async function PresupuestoWorkspaceRootPage({ params }: Props) {
  const { id, presupuestoId } = await params;
  redirect(`/proyectos/${id}/presupuestos/${presupuestoId}/partidas`);
}

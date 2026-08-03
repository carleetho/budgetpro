import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { ProjectSubnav } from "@/components/layout/ProjectSubnav";

interface ProjectLayoutProps {
  children: React.ReactNode;
  params: Promise<{ id: string }>;
}

/**
 * Layout de detalle de proyecto.
 * Shell global: `app/(app)/layout.tsx`. Subnav de módulos: ProjectSubnav.
 */
export default async function ProjectLayout({ children, params }: ProjectLayoutProps) {
  const { id } = await params;

  return (
    <div className="mx-auto w-full max-w-7xl px-6 py-8">
      <Link
        href="/"
        className="mb-4 inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Volver a Proyectos
      </Link>
      <ProjectSubnav proyectoId={id} />
      {children}
    </div>
  );
}

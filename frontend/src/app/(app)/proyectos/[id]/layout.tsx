import Link from "next/link";
import { ArrowLeft } from "lucide-react";

interface ProjectLayoutProps {
  children: React.ReactNode;
  params: Promise<{ id: string }>;
}

/**
 * Layout de detalle de proyecto.
 * El shell global (sidebar) lo aporta `app/(app)/layout.tsx`.
 */
export default async function ProjectLayout({ children }: ProjectLayoutProps) {
  return (
    <div className="mx-auto w-full max-w-7xl px-6 py-8">
      <Link
        href="/"
        className="mb-4 inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Volver a Proyectos
      </Link>
      {children}
    </div>
  );
}

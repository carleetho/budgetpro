import { ProjectSidebar } from "@/components/layout/ProjectSidebar";
import AuthGuard from "@/components/auth/AuthGuard";

interface ProjectLayoutProps {
  children: React.ReactNode;
  params: Promise<{ id: string }>;
}

/**
 * Layout específico para la gestión de un proyecto individual.
 * Incluye un Sidebar lateral con navegación entre secciones.
 */
export default async function ProjectLayout({ children, params }: ProjectLayoutProps) {
  const { id } = await params;

  return (
    <AuthGuard>
      <div className="flex min-h-screen">
        {/* Sidebar Lateral */}
        <ProjectSidebar proyectoId={id} />

        {/* Contenido Principal */}
        <main className="flex-1 ml-64 max-w-7xl">
          <div className="py-8 px-6">
            {children}
          </div>
        </main>
      </div>
    </AuthGuard>
  );
}

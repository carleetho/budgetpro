"use client";

import Link from "next/link";
import AuthGuard from "@/components/auth/AuthGuard";
import { HomeSidebar } from "@/components/layout/HomeSidebar";
import { Button } from "@/components/ui/button";
import ProjectsPage from "./ProjectsPage";

/**
 * Vista principal autenticada: lista de proyectos con barra lateral (paridad con el layout de detalle).
 */
export default function ProjectsHomeLayout() {
  return (
    <AuthGuard>
      <div className="flex min-h-screen">
        <HomeSidebar />
        <main className="flex-1 ml-64 max-w-7xl w-full">
          <div className="flex justify-end px-6 pt-6">
            <Link href="/landing">
              <Button variant="outline">Ver Landing</Button>
            </Link>
          </div>
          <ProjectsPage />
        </main>
      </div>
    </AuthGuard>
  );
}

"use client";

import Link from "next/link";
import ProjectsPage from "./ProjectsPage";
import { Button } from "@/components/ui/button";

/**
 * Contenido de la home autenticada (lista de proyectos).
 * El shell global (sidebar) lo aporta `app/(app)/layout.tsx`.
 */
export default function ProjectsHomeLayout() {
  return (
    <div className="mx-auto w-full max-w-7xl">
      <div className="flex justify-end px-6 pt-6">
        <Link href="/landing">
          <Button variant="outline">Ver Landing</Button>
        </Link>
      </div>
      <ProjectsPage />
    </div>
  );
}

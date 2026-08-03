"use client";

import { AppSidebar } from "@/components/layout/AppSidebar";
import { useSidebarState } from "@/components/layout/SidebarStateProvider";
import { cn } from "@/lib/utils";

/**
 * Shell autenticado: sidebar global + área de contenido (`flex-1`, sin padding propio).
 */
export function AppShell({ children }: { children: React.ReactNode }) {
  const { collapsed } = useSidebarState();

  return (
    <div className="flex min-h-screen bg-background">
      <AppSidebar />
      <main
        className={cn(
          "min-w-0 flex-1 transition-[margin] duration-200 ease-in-out",
          collapsed ? "ml-16" : "ml-60"
        )}
      >
        {children}
      </main>
    </div>
  );
}

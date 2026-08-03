"use client";

import AuthGuard from "@/components/auth/AuthGuard";
import { AppShell } from "@/components/layout/AppShell";
import { SidebarStateProvider } from "@/components/layout/SidebarStateProvider";

/**
 * Layout de rutas autenticadas: AuthGuard + sidebar global (REQ-70).
 * Rutas públicas (`/login`, `/demo`, `/landing`, `/register`) quedan fuera de este group.
 */
export default function AuthenticatedAppLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthGuard>
      <SidebarStateProvider>
        <AppShell>{children}</AppShell>
      </SidebarStateProvider>
    </AuthGuard>
  );
}

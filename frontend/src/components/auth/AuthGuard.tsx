"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Loader2 } from "lucide-react";
import { AuthService } from "@/services/auth.service";

interface AuthGuardProps {
  children: React.ReactNode;
}

/**
 * Valida token local y confirma sesión con `GET /auth/me`.
 * Ante 401, `api-client` ya redirige a login.
 */
export default function AuthGuard({ children }: AuthGuardProps) {
  const router = useRouter();
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    let cancelled = false;
    void (async () => {
      const token = localStorage.getItem("auth_token");
      if (!token) {
        router.replace("/login");
        return;
      }
      try {
        const me = await AuthService.me();
        if (cancelled) return;
        const prevRaw = localStorage.getItem("auth_user");
        let prev: Record<string, unknown> = {};
        try {
          prev = prevRaw ? (JSON.parse(prevRaw) as Record<string, unknown>) : {};
        } catch {
          prev = {};
        }
        localStorage.setItem(
          "auth_user",
          JSON.stringify({
            ...prev,
            ...me,
            email: me.email ?? prev.email,
            rol: me.rol ?? prev.rol,
            usuarioId: me.usuarioId ?? me.id ?? prev.usuarioId,
          })
        );
        setChecked(true);
      } catch {
        if (cancelled) return;
        AuthService.logout();
        router.replace("/login");
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [router]);

  if (!checked) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-2 text-muted-foreground">
        <Loader2 className="h-8 w-8 animate-spin" aria-hidden />
        <span className="text-sm">Comprobando sesión…</span>
      </div>
    );
  }

  return <>{children}</>;
}

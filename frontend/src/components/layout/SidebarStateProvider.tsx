"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import {
  SIDEBAR_COLLAPSED_KEY,
  SIDEBAR_SECTIONS_KEY,
} from "@/components/layout/nav-config";

type SidebarState = {
  collapsed: boolean;
  hydrated: boolean;
  sectionsOpen: Record<string, boolean>;
  setCollapsed: (next: boolean) => void;
  toggleSection: (id: string) => void;
};

const SidebarContext = createContext<SidebarState | null>(null);

function readCollapsed(): boolean {
  try {
    return localStorage.getItem(SIDEBAR_COLLAPSED_KEY) === "true";
  } catch {
    return false;
  }
}

function readSections(): Record<string, boolean> {
  try {
    const raw = localStorage.getItem(SIDEBAR_SECTIONS_KEY);
    if (!raw) return { dashboard: true, reports: false };
    return JSON.parse(raw) as Record<string, boolean>;
  } catch {
    return { dashboard: true, reports: false };
  }
}

export function SidebarStateProvider({ children }: { children: React.ReactNode }) {
  const [collapsed, setCollapsedState] = useState(false);
  const [hydrated, setHydrated] = useState(false);
  const [sectionsOpen, setSectionsOpen] = useState<Record<string, boolean>>({
    dashboard: true,
    reports: false,
  });

  useEffect(() => {
    setCollapsedState(readCollapsed());
    setSectionsOpen(readSections());
    setHydrated(true);
  }, []);

  const setCollapsed = useCallback((next: boolean) => {
    setCollapsedState(next);
    try {
      localStorage.setItem(SIDEBAR_COLLAPSED_KEY, String(next));
    } catch {
      /* localStorage no disponible */
    }
  }, []);

  const toggleSection = useCallback((id: string) => {
    setSectionsOpen((prev) => {
      const next = { ...prev, [id]: !prev[id] };
      try {
        localStorage.setItem(SIDEBAR_SECTIONS_KEY, JSON.stringify(next));
      } catch {
        /* ignore */
      }
      return next;
    });
  }, []);

  const value = useMemo(
    () => ({ collapsed, hydrated, sectionsOpen, setCollapsed, toggleSection }),
    [collapsed, hydrated, sectionsOpen, setCollapsed, toggleSection]
  );

  return <SidebarContext.Provider value={value}>{children}</SidebarContext.Provider>;
}

export function useSidebarState(): SidebarState {
  const ctx = useContext(SidebarContext);
  if (!ctx) {
    throw new Error("useSidebarState debe usarse dentro de SidebarStateProvider");
  }
  return ctx;
}

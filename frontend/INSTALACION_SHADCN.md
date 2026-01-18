# 📦 Instalación de Componentes Shadcn UI

## Componentes Core Requeridos

Ejecuta los siguientes comandos en el directorio `frontend/`:

```bash
cd frontend

# Instalar componentes esenciales
npx shadcn@latest add button
npx shadcn@latest add card
npx shadcn@latest add input
npx shadcn@latest add table
npx shadcn@latest add dialog
npx shadcn@latest add dropdown-menu
npx shadcn@latest add toast
```

## Comando Único (Instalación en Lote)

Si prefieres instalar todos a la vez:

```bash
cd frontend

npx shadcn@latest add button card input table dialog dropdown-menu toast
```

## Verificación

Después de la instalación, verifica que los componentes se crearon en:
- `src/components/ui/button.tsx`
- `src/components/ui/card.tsx`
- `src/components/ui/input.tsx`
- `src/components/ui/table.tsx`
- `src/components/ui/dialog.tsx`
- `src/components/ui/dropdown-menu.tsx`
- `src/components/ui/toast.tsx`
- `src/components/ui/toaster.tsx` (para toast)

## Notas

- Los componentes se instalarán automáticamente en `src/components/ui/`
- No modifiques estos archivos directamente (son generados por Shadcn)
- Puedes crear variantes personalizadas en `src/components/common/`

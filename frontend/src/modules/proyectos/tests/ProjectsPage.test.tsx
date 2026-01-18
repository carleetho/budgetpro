import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProjectsPage from '../pages/ProjectsPage';
import { ProyectoService } from '@/services/proyecto.service';
import type { Proyecto } from '@/core/types';

// Mock del servicio ProyectoService
vi.mock('@/services/proyecto.service', () => ({
  ProyectoService: {
    listar: vi.fn(),
    crear: vi.fn(),
    obtenerPorId: vi.fn(),
  },
}));

// Mock de sonner (toast)
vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
  },
}));

describe('ProjectsPage', () => {
  const mockProyectos: Proyecto[] = [
    {
      id: '1',
      nombre: 'Proyecto Test 1',
      ubicacion: 'San Salvador',
      estado: 'ACTIVO',
    },
    {
      id: '2',
      nombre: 'Proyecto Test 2',
      ubicacion: 'Santa Ana',
      estado: 'BORRADOR',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('debe renderizar la página correctamente', async () => {
    vi.mocked(ProyectoService.listar).mockResolvedValue(mockProyectos);

    render(<ProjectsPage />);

    // Verificar que el título "Proyectos" esté presente
    await waitFor(() => {
      expect(screen.getByText('Proyectos')).toBeInTheDocument();
    });
  });

  it('debe mostrar la tabla con los proyectos mockeados', async () => {
    vi.mocked(ProyectoService.listar).mockResolvedValue(mockProyectos);

    render(<ProjectsPage />);

    // Esperar a que se carguen los proyectos
    await waitFor(() => {
      expect(screen.getByText('Proyecto Test 1')).toBeInTheDocument();
    });

    // Verificar que los datos se muestran en la tabla
    expect(screen.getByText('Proyecto Test 1')).toBeInTheDocument();
    expect(screen.getByText('San Salvador')).toBeInTheDocument();
    expect(screen.getByText('Proyecto Test 2')).toBeInTheDocument();
    expect(screen.getByText('Santa Ana')).toBeInTheDocument();
  });

  it('debe abrir el Dialog al hacer clic en "Nuevo Proyecto"', async () => {
    vi.mocked(ProyectoService.listar).mockResolvedValue([]);

    const user = userEvent.setup();
    render(<ProjectsPage />);

    // Esperar a que termine la carga
    await waitFor(() => {
      expect(screen.queryByText('Cargando proyectos...')).not.toBeInTheDocument();
    });

    // Buscar todos los botones y encontrar el que contiene "Nuevo Proyecto"
    const buttons = screen.getAllByRole('button');
    const nuevoProyectoButton = buttons.find(button => 
      button.textContent?.includes('Nuevo Proyecto')
    );
    
    expect(nuevoProyectoButton).toBeDefined();
    if (nuevoProyectoButton) {
      await user.click(nuevoProyectoButton);
    }

    // Verificar que el Dialog se abre (debe aparecer la descripción del dialog)
    await waitFor(() => {
      expect(screen.getByText(/Crea un nuevo proyecto para comenzar/i)).toBeInTheDocument();
    });
  });

  it('debe mostrar el estado de carga mientras se cargan los proyectos', () => {
    // Mock que nunca resuelve para mantener el estado de carga
    vi.mocked(ProyectoService.listar).mockImplementation(
      () => new Promise(() => {}) // Promise que nunca se resuelve
    );

    render(<ProjectsPage />);

    // Verificar que se muestra el estado de carga
    expect(screen.getByText('Cargando proyectos...')).toBeInTheDocument();
  });

  it('debe mostrar mensaje cuando no hay proyectos', async () => {
    vi.mocked(ProyectoService.listar).mockResolvedValue([]);

    render(<ProjectsPage />);

    // Esperar a que termine la carga
    await waitFor(() => {
      expect(screen.queryByText('Cargando proyectos...')).not.toBeInTheDocument();
    });

    // Verificar el mensaje de estado vacío
    expect(screen.getByText('No hay proyectos registrados.')).toBeInTheDocument();
  });
});

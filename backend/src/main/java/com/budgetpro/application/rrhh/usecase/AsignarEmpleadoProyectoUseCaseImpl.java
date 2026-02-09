package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.recurso.port.out.RecursoRepository;
import com.budgetpro.application.rrhh.dto.AsignacionProyectoResponse;
import com.budgetpro.application.rrhh.dto.AsignarEmpleadoProyectoCommand;
import com.budgetpro.application.rrhh.exception.AsignacionProyectoConflictoException;
import com.budgetpro.application.rrhh.port.in.AsignarEmpleadoProyectoUseCase;
import com.budgetpro.application.rrhh.port.out.AsignacionProyectoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.catalogo.model.RecursoProxy;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.catalogo.port.RecursoProxyRepository;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.domain.finanzas.recurso.model.Recurso;
import com.budgetpro.domain.shared.model.TipoRecurso;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.AsignacionProyectoId;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.domain.rrhh.model.HistorialLaboral;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AsignarEmpleadoProyectoUseCaseImpl implements AsignarEmpleadoProyectoUseCase {

        private final EmpleadoRepositoryPort empleadoRepository;
        private final ProyectoRepository proyectoRepository;
        private final RecursoRepository recursoCatalogRepository;
        private final RecursoProxyRepository recursoProxyRepository;
        private final AsignacionProyectoRepositoryPort asignacionRepository;

        @Override
        @Transactional
        public AsignacionProyectoResponse asignar(AsignarEmpleadoProyectoCommand command) {
                // 1. Validate employee exists and is ACTIVO
                Empleado empleado = empleadoRepository.findById(EmpleadoId.of(command.empleadoId())).orElseThrow(
                                () -> new IllegalArgumentException("Empleado no encontrado: " + command.empleadoId()));

                if (empleado.getEstado() != EstadoEmpleado.ACTIVO) {
                        throw new IllegalStateException("El empleado no está activo: " + command.empleadoId());
                }

                // 2. Validate project exists
                Proyecto proyecto = proyectoRepository.findById(ProyectoId.from(command.proyectoId())).orElseThrow(
                                () -> new IllegalArgumentException("Proyecto no encontrado: " + command.proyectoId()));

                // 3. Check for overlapping assignments
                if (asignacionRepository.existsOverlap(empleado.getId(), command.fechaInicio(), command.fechaFin())) {
                        throw new AsignacionProyectoConflictoException(empleado.getId(), command.fechaInicio(),
                                        command.fechaFin());
                }

                // 4. Get employee's current position (cargo)
                HistorialLaboral currentHistory = empleado.getSalarioActual().orElseThrow(
                                () -> new IllegalStateException("El empleado no tiene un historial laboral activo"));

                String cargo = currentHistory.getCargo();

                // 5. Query Recurso catalog for matching position (TipoRecurso.MANO_OBRA)
                Recurso catalogRecurso = recursoCatalogRepository.findByNombre(cargo.toUpperCase().trim())
                                .orElseThrow(() -> new IllegalStateException(
                                                "No se encontró el recurso en el catálogo para el cargo: " + cargo));

                if (catalogRecurso.getTipo() != TipoRecurso.MANO_OBRA) {
                        throw new IllegalStateException("El recurso encontrado no es de tipo MANO_OBRA: " + cargo);
                }

                // 6. Create RecursoProxy with costoEstimado from catalog
                RecursoProxy proxy = RecursoProxy.crear(RecursoProxyId.generate(), catalogRecurso.getId().toString(),
                                "CATALOGO_GLOBAL", catalogRecurso.getNombre(), catalogRecurso.getTipo(),
                                catalogRecurso.getUnidadBase(),
                                command.tarifaHora() != null ? command.tarifaHora() : currentHistory.getSalarioBase(),
                                LocalDateTime.now());

                // Save RecursoProxy
                recursoProxyRepository.save(proxy);

                // 7. Create AsignacionProyecto entity
                AsignacionProyecto assignment = AsignacionProyecto.crear(AsignacionProyectoId.generate(),
                                empleado.getId(), proyecto.getId(), proxy.getId(), command.fechaInicio(),
                                command.fechaFin(), command.tarifaHora(), command.rolProyecto());

                // 8. Save AsignacionProyecto
                asignacionRepository.save(assignment);

                // 9. Return response
                return new AsignacionProyectoResponse(assignment.getId().getValue(),
                                assignment.getEmpleadoId().getValue(), assignment.getProyectoId().getValue(),
                                assignment.getRecursoProxyId().getValue(), assignment.getFechaInicio(),
                                assignment.getFechaFin(), assignment.getTarifaHora(), assignment.getRolProyecto());
        }
}

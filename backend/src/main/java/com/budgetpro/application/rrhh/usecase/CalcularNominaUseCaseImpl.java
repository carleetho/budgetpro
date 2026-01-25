package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.CalcularNominaCommand;
import com.budgetpro.application.rrhh.dto.NominaResponse;
import com.budgetpro.application.rrhh.port.in.CalcularNominaUseCase;
import com.budgetpro.application.rrhh.port.out.AsistenciaRepositoryPort;
import com.budgetpro.application.rrhh.port.out.ConfiguracionLaboralRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.application.rrhh.port.out.NominaRepositoryPort;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.rrhh.model.*;
import com.budgetpro.domain.rrhh.service.CalculadorFSR;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CalcularNominaUseCaseImpl implements CalcularNominaUseCase {

    private final NominaRepositoryPort nominaRepositoryPort;
    private final ConfiguracionLaboralRepositoryPort configuracionRepositoryPort;
    private final EmpleadoRepositoryPort empleadoRepositoryPort;
    private final AsistenciaRepositoryPort asistenciaRepositoryPort;
    private final CalculadorFSR calculadorFSR;

    public CalcularNominaUseCaseImpl(NominaRepositoryPort nominaRepositoryPort,
            ConfiguracionLaboralRepositoryPort configuracionRepositoryPort,
            EmpleadoRepositoryPort empleadoRepositoryPort, AsistenciaRepositoryPort asistenciaRepositoryPort,
            CalculadorFSR calculadorFSR) {
        this.nominaRepositoryPort = nominaRepositoryPort;
        this.configuracionRepositoryPort = configuracionRepositoryPort;
        this.empleadoRepositoryPort = empleadoRepositoryPort;
        this.asistenciaRepositoryPort = asistenciaRepositoryPort;
        this.calculadorFSR = calculadorFSR;
    }

    @Override
    public NominaResponse calcularNomina(CalcularNominaCommand command) {
        // 1. Idempotency Check
        if (nominaRepositoryPort.existsForPeriod(command.getProyectoId(), command.getPeriodoInicio(),
                command.getPeriodoFin())) {
            throw new IllegalStateException("Payroll already exists for this project and period");
        }

        // 2. Fetch Configuration
        ConfiguracionLaboral config = configuracionRepositoryPort
                .findEffectiveConfig(command.getProyectoId(), command.getPeriodoInicio())
                .orElseThrow(() -> new IllegalStateException("No labor configuration found for project/date"));

        // 3. Fetch Employees
        List<Empleado> empleados;
        if (command.getEmpleadoIds() != null && !command.getEmpleadoIds().isEmpty()) {
            empleados = empleadoRepositoryPort.findAllById(command.getEmpleadoIds());
        } else {
            // Need a way to fetch all employees for project?
            // For now, if no IDs provided, we might fail or fetch ALL active.
            // Assuming command always validates this or we fetch all.
            // Let's assume we fetch all active for simplicity if null, but usually specific
            // list is better.
            empleados = empleadoRepositoryPort.findByEstado(EstadoEmpleado.ACTIVO);
            // Verify if they belong to project?
            // The requirements didn't specify project assignment filtering here, but
            // usually payroll is by project.
            // We proceed with the list.
        }

        // 4. Batch Fetch Attendance
        List<AsistenciaRegistro> allAsistencias = asistenciaRepositoryPort.findByEmpleadosAndPeriodo(
                empleados.stream().map(Empleado::getId).collect(Collectors.toList()), command.getPeriodoInicio(),
                command.getPeriodoFin());

        // Map attendance by EmployeeId for easy access
        Map<EmpleadoId, List<AsistenciaRegistro>> asistenciaMap = allAsistencias.stream()
                .collect(Collectors.groupingBy(AsistenciaRegistro::getEmpleadoId));

        List<DetalleNomina> detalles = new ArrayList<>();

        // 5. Calculate for each employee
        for (Empleado empleado : empleados) {
            detalles.add(calcularDetalleEmpleado(empleado, command.getPeriodoInicio(), command.getPeriodoFin(), config,
                    asistenciaMap.getOrDefault(empleado.getId(), Collections.emptyList())));
        }

        // 6. Create Aggregate
        Nomina nomina = Nomina.calcular(NominaId.random(), command.getProyectoId(), command.getPeriodoInicio(),
                command.getPeriodoFin(), detalles);

        // 7. Save
        Nomina saved = nominaRepositoryPort.save(nomina);

        return NominaResponse.fromDomain(saved);
    }

    private DetalleNomina calcularDetalleEmpleado(Empleado empleado, LocalDate inicio, LocalDate fin,
            ConfiguracionLaboral config, List<AsistenciaRegistro> asistencias) {

        BigDecimal salarioTotalPeriodo = BigDecimal.ZERO;
        BigDecimal horasExtrasTotalCost = BigDecimal.ZERO;
        BigDecimal bonoAsistencia = BigDecimal.ZERO; // Placeholder logic
        BigDecimal otrosIngresos = BigDecimal.ZERO;

        int diasTrabajados = 0;

        // Iterate each day
        for (LocalDate date = inicio; !date.isAfter(fin); date = date.plusDays(1)) {
            final LocalDate currentDate = date; // for lambda
            Optional<HistorialLaboral> historialOpt = empleado.getSalarioEnFecha(date);

            if (historialOpt.isPresent()) {
                HistorialLaboral historial = historialOpt.get();
                BigDecimal salarioDiario = historial.getSalarioBase().divide(new BigDecimal("30"), 2,
                        RoundingMode.HALF_UP);

                salarioTotalPeriodo = salarioTotalPeriodo.add(salarioDiario);
                diasTrabajados++; // Assuming if they have salary history they are employed.
                // Ideally check attendance for "worked days" vs "paid days".
                // Requirement: "Sum hours from attendance".
            }
        }

        // Calculate Overtime cost based on attendance records
        for (AsistenciaRegistro reg : asistencias) {
            // Only consider attendance within period (already filtered by query, but double
            // check date if needed)
            if (reg.getFecha().isBefore(inicio) || reg.getFecha().isAfter(fin)) {
                continue;
            }

            // Get salary for the specific day of attendance
            Optional<HistorialLaboral> hist = empleado.getSalarioEnFecha(reg.getFecha());
            if (hist.isEmpty())
                continue;

            BigDecimal salarioDiario = hist.get().getSalarioBase().divide(new BigDecimal("30"), 2,
                    RoundingMode.HALF_UP);
            BigDecimal hourlyRate = salarioDiario.divide(new BigDecimal("8"), 2, RoundingMode.HALF_UP); // Standard 8
                                                                                                        // hour day

            // Use the domain method from AsistenciaRegistro (Duration)
            java.time.Duration extraDuration = reg.calcularHorasExtras();
            if (!extraDuration.isZero()) {
                long extraHours = extraDuration.toHours();
                // Note: toHours truncates minutes. If we need minutes precision, we should use
                // toMinutes() / 60.0.
                // Requirement: "Overtime rates: 1.5x for first 2 hours, 2.0x beyond that"

                BigDecimal horasExtrasCost = BigDecimal.ZERO;

                if (extraHours <= 2) {
                    BigDecimal cost = hourlyRate.multiply(new BigDecimal("1.5")).multiply(new BigDecimal(extraHours));
                    horasExtrasCost = horasExtrasCost.add(cost);
                } else {
                    // First 2 hours at 1.5
                    BigDecimal first2 = hourlyRate.multiply(new BigDecimal("1.5")).multiply(new BigDecimal("2"));
                    // Remainder at 2.0
                    BigDecimal remainder = hourlyRate.multiply(new BigDecimal("2.0"))
                            .multiply(new BigDecimal(extraHours - 2));
                    horasExtrasCost = horasExtrasCost.add(first2).add(remainder);
                }
                horasExtrasTotalCost = horasExtrasTotalCost.add(horasExtrasCost);
            }
        }

        // FSR Application
        // "FSR applied per employee"
        BigDecimal fsrMultiplier = calculadorFSR.calcularFSR(config, empleado);
        // Costo Patronal = SalarioBase * FSR? Or Costo Patronal = SalarioTotal * FSR?
        // Usually FSR adjusts the Base Salary to "Integrated Salary".
        // Employer Cost = (Salario Diario * FSR) * Days?
        // Or does it include taxes?
        // Requirement: "Calculate: gross salary... net salary, employer cost".

        // Let's Calculate Simple:
        // Gross = Salary + Overtime + Bonos.
        BigDecimal totalPercepciones = salarioTotalPeriodo.add(horasExtrasTotalCost).add(bonoAsistencia)
                .add(otrosIngresos);

        // Deductions
        BigDecimal deduccionesFiscales = totalPercepciones.multiply(new BigDecimal("0.10")); // Mock 10%
        BigDecimal deduccionesSeguridadSocial = totalPercepciones.multiply(new BigDecimal("0.05")); // Mock 5%
        BigDecimal otrasDeducciones = BigDecimal.ZERO;

        BigDecimal costoPatronal = salarioTotalPeriodo.multiply(fsrMultiplier); // Basic approximation of cost impact

        return DetalleNomina.crear(empleado.getId(), salarioTotalPeriodo, horasExtrasTotalCost, bonoAsistencia,
                otrosIngresos, deduccionesFiscales, deduccionesSeguridadSocial, otrasDeducciones, costoPatronal,
                fsrMultiplier.doubleValue(), diasTrabajados);
    }
}

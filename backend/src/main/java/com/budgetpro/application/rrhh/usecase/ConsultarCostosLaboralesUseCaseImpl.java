package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.ConsultarCostosLaboralesQuery;
import com.budgetpro.application.rrhh.dto.CostosLaboralesResponse;
import com.budgetpro.application.rrhh.dto.DesgloseCostoLaboral;
import com.budgetpro.application.rrhh.dto.VarianzaCostoLaboral;
import com.budgetpro.application.rrhh.exception.ConfiguracionLaboralNotFoundException;
import com.budgetpro.application.rrhh.port.in.ConsultarCostosLaboralesUseCase;
import com.budgetpro.application.rrhh.port.out.AsistenciaRepositoryPort;
import com.budgetpro.application.rrhh.port.out.ConfiguracionLaboralRepositoryPort;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.service.CalculadorFSR;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsultarCostosLaboralesUseCaseImpl implements ConsultarCostosLaboralesUseCase {

    private final AsistenciaRepositoryPort asistenciaRepository;
    private final EmpleadoRepositoryPort empleadoRepository;
    private final ConfiguracionLaboralRepositoryPort configuracionLaboralRepository;
    private final CalculadorFSR calculadorFSR;

    public ConsultarCostosLaboralesUseCaseImpl(AsistenciaRepositoryPort asistenciaRepository,
            EmpleadoRepositoryPort empleadoRepository,
            ConfiguracionLaboralRepositoryPort configuracionLaboralRepository, CalculadorFSR calculadorFSR) {
        this.asistenciaRepository = asistenciaRepository;
        this.empleadoRepository = empleadoRepository;
        this.configuracionLaboralRepository = configuracionLaboralRepository;
        this.calculadorFSR = calculadorFSR;
    }

    @Override
    public CostosLaboralesResponse consultarCostos(ConsultarCostosLaboralesQuery query) {
        ConfiguracionLaboral configLaboral = configuracionLaboralRepository
                .findEffectiveConfig(query.getProyectoId(), query.getFechaInicio())
                .orElseThrow(() -> new ConfiguracionLaboralNotFoundException(String.format(
                        "No se encontró configuración laboral para proyecto %s en fecha %s",
                        query.getProyectoId().getValue(), query.getFechaInicio())));

        List<AsistenciaRegistro> asistencias = asistenciaRepository.findByProyectoAndPeriodo(query.getProyectoId(),
                query.getFechaInicio(), query.getFechaFin());

        List<EmpleadoId> empleadoIds = asistencias.stream().map(AsistenciaRegistro::getEmpleadoId).distinct()
                .collect(Collectors.toList());

        Map<EmpleadoId, Empleado> empleados = empleadoRepository.findAllById(empleadoIds).stream()
                .collect(Collectors.toMap(Empleado::getId, e -> e));

        Map<String, List<AsistenciaRegistro>> groupedAsistencias;
        switch (query.getAgruparPor()) {
        case EMPLEADO:
            groupedAsistencias = asistencias.stream().collect(Collectors.groupingBy(a -> a.getEmpleadoId().toString()));
            break;
        case CUADRILLA:
            groupedAsistencias = asistencias.stream().collect(Collectors.groupingBy(a -> "N/A"));
            break;
        case PARTIDA:
            groupedAsistencias = asistencias.stream().collect(Collectors.groupingBy(a -> "N/A"));
            break;
        default:
            groupedAsistencias = asistencias.stream().collect(Collectors.groupingBy(a -> a.getEmpleadoId().toString()));
        }

        List<DesgloseCostoLaboral> desglose = new ArrayList<>();
        BigDecimal totalCostoGlobal = BigDecimal.ZERO;

        for (Map.Entry<String, List<AsistenciaRegistro>> entry : groupedAsistencias.entrySet()) {
            String grupoId = entry.getKey();
            List<AsistenciaRegistro> registros = entry.getValue();

            Duration totalHorasNormales = Duration.ZERO;
            Duration totalHorasExtras = Duration.ZERO;
            BigDecimal costoGrupo = BigDecimal.ZERO;

            for (AsistenciaRegistro registro : registros) {
                Empleado empleado = empleados.get(registro.getEmpleadoId());
                if (empleado == null) {
                    continue;
                }

                BigDecimal salarioDiario = empleado.getSalarioEnFecha(registro.getFecha())
                        .map(com.budgetpro.domain.rrhh.model.HistorialLaboral::getSalarioBase).orElse(BigDecimal.ZERO);
                BigDecimal salarioHora = salarioDiario.divide(BigDecimal.valueOf(8), MathContext.DECIMAL128);

                BigDecimal fsrMultiplier = calculadorFSR.calcularFSR(configLaboral, empleado);

                Duration horas = registro.calcularHoras();
                Duration extras = registro.calcularHorasExtras();
                Duration normales = horas.minus(extras);

                totalHorasNormales = totalHorasNormales.plus(normales);
                totalHorasExtras = totalHorasExtras.plus(extras);

                BigDecimal horasDecimal = BigDecimal.valueOf(horas.toMinutes()).divide(BigDecimal.valueOf(60),
                        MathContext.DECIMAL128);
                BigDecimal costoRegistro = horasDecimal.multiply(salarioHora).multiply(fsrMultiplier);

                costoGrupo = costoGrupo.add(costoRegistro);
            }

            totalCostoGlobal = totalCostoGlobal.add(costoGrupo);

            String nombreGrupo = grupoId;
            if (query.getAgruparPor() == ConsultarCostosLaboralesQuery.Agrupacion.EMPLEADO
                    && empleados.containsKey(EmpleadoId.fromString(grupoId))) {
                Empleado e = empleados.get(EmpleadoId.fromString(grupoId));
                nombreGrupo = e.getNombre() + " " + e.getApellido();
            }

            BigDecimal promedioHora = BigDecimal.ZERO;
            long totalMinutes = totalHorasNormales.plus(totalHorasExtras).toMinutes();
            if (totalMinutes > 0) {
                promedioHora = costoGrupo.divide(
                        BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), MathContext.DECIMAL128),
                        RoundingMode.HALF_UP);
            }

            desglose.add(new DesgloseCostoLaboral(grupoId, nombreGrupo, totalHorasNormales, totalHorasExtras,
                    costoGrupo, promedioHora));
        }

        Optional<VarianzaCostoLaboral> varianza = Optional.empty();
        if (query.isIncluirVarianza()) {
            BigDecimal costoEstimado = BigDecimal.valueOf(100000);
            BigDecimal diferencia = totalCostoGlobal.subtract(costoEstimado);
            BigDecimal porcentaje = BigDecimal.ZERO;
            if (costoEstimado.compareTo(BigDecimal.ZERO) > 0) {
                porcentaje = diferencia.divide(costoEstimado, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            }
            varianza = Optional.of(new VarianzaCostoLaboral(totalCostoGlobal, costoEstimado, diferencia, porcentaje));
        }

        return new CostosLaboralesResponse(totalCostoGlobal, "MXN", desglose, varianza);
    }
}

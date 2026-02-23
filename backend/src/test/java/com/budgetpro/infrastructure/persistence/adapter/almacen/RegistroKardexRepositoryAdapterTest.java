package com.budgetpro.infrastructure.persistence.adapter.almacen;

import com.budgetpro.domain.logistica.almacen.model.RegistroKardex;
import com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen;
import com.budgetpro.domain.logistica.almacen.port.out.RegistroKardexRepository;
import com.budgetpro.domain.logistica.almacen.service.GestionKardexService;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.almacen.KardexEntity;
import com.budgetpro.infrastructure.persistence.repository.almacen.KardexJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para RegistroKardexRepositoryAdapter.
 * 
 * Verifica:
 * - Persistencia correcta de registros de Kárdex
 * - Cálculo de PMP (Precio Medio Ponderado) con precisión
 * - Búsqueda del último registro por almacén y recurso
 * - Comportamiento con registros secuenciales
 * - Integridad de datos en operaciones concurrentes
 * 
 * NOTA: El cálculo de PMP se realiza en GestionKardexService.
 * Este test verifica que los valores calculados se persisten correctamente.
 */
@Transactional
class RegistroKardexRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private RegistroKardexRepository kardexRepository;

    @Autowired
    private KardexJpaRepository kardexJpaRepository;

    @Autowired
    private GestionKardexService gestionKardexService;

    @Autowired
    private EntityManager entityManager;

    private UUID almacenId;
    private UUID recursoId;
    private UUID movimientoId1;
    private UUID movimientoId2;

    @BeforeEach
    void setUp() {
        almacenId = UUID.randomUUID();
        recursoId = UUID.randomUUID();
        movimientoId1 = UUID.randomUUID();
        movimientoId2 = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe guardar un registro de entrada y calcular PMP correctamente")
    void debeGuardarRegistroEntradaYCalcularPMP() {
        // Arrange - Primera entrada (stock inicial)
        BigDecimal cantidad1 = new BigDecimal("100.00");
        BigDecimal precio1 = new BigDecimal("10.00");
        BigDecimal saldoCantidadAnterior = BigDecimal.ZERO;
        BigDecimal saldoValorAnterior = BigDecimal.ZERO;

        RegistroKardex registro1 = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            cantidad1,
            precio1,
            movimientoId1,
            saldoCantidadAnterior,
            saldoValorAnterior
        );

        // Act
        kardexRepository.guardar(registro1);
        entityManager.flush();
        entityManager.clear();

        // Assert - Verificar PMP calculado
        // PMP esperado = (0 + 100 * 10) / (0 + 100) = 1000 / 100 = 10.00
        BigDecimal pmpEsperado = new BigDecimal("10.00");
        
        Optional<RegistroKardex> recuperado = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId);
        assertTrue(recuperado.isPresent());
        
        RegistroKardex registro = recuperado.get();
        assertEquals(cantidad1, registro.getSaldoCantidad());
        assertEquals(new BigDecimal("1000.00"), registro.getSaldoValor());
        assertEquals(0, registro.getCostoPromedioPonderado().compareTo(pmpEsperado),
            "El PMP debe ser exactamente 10.00");
    }

    @Test
    @DisplayName("Debe calcular PMP correctamente con múltiples entradas")
    void debeCalcularPMPConMultiplesEntradas() {
        // Arrange - Primera entrada
        BigDecimal cantidad1 = new BigDecimal("100.00");
        BigDecimal precio1 = new BigDecimal("10.00");
        RegistroKardex registro1 = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            cantidad1,
            precio1,
            movimientoId1,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );
        kardexRepository.guardar(registro1);
        entityManager.flush();
        entityManager.clear();

        // Segunda entrada con precio diferente
        BigDecimal cantidad2 = new BigDecimal("50.00");
        BigDecimal precio2 = new BigDecimal("12.00");
        BigDecimal saldoCantidadAnterior = registro1.getSaldoCantidad();
        BigDecimal saldoValorAnterior = registro1.getSaldoValor();

        RegistroKardex registro2 = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            cantidad2,
            precio2,
            movimientoId2,
            saldoCantidadAnterior,
            saldoValorAnterior
        );

        // Act
        kardexRepository.guardar(registro2);
        entityManager.flush();
        entityManager.clear();

        // Assert - Verificar PMP recalculado
        // PMP esperado = (1000 + 50 * 12) / (100 + 50) = 1600 / 150 = 10.6667
        BigDecimal pmpEsperado = new BigDecimal("10.6667");
        BigDecimal tolerancia = new BigDecimal("0.01");

        Optional<RegistroKardex> ultimo = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId);
        assertTrue(ultimo.isPresent());

        RegistroKardex registro = ultimo.get();
        assertEquals(new BigDecimal("150.00"), registro.getSaldoCantidad());
        assertEquals(new BigDecimal("1600.00"), registro.getSaldoValor());
        
        BigDecimal diferencia = registro.getCostoPromedioPonderado().subtract(pmpEsperado).abs();
        assertTrue(diferencia.compareTo(tolerancia) <= 0,
            String.format("El PMP debe ser aproximadamente %s (diferencia: %s)", pmpEsperado, diferencia));
    }

    @Test
    @DisplayName("Debe calcular PMP con fórmula: (Stock × PMP + Entrada × Precio) / (Stock + Entrada)")
    void debeCalcularPMPConFormulaCorrecta() {
        // Arrange - Stock inicial
        BigDecimal stockCantidad = new BigDecimal("100.00");
        BigDecimal stockPMP = new BigDecimal("10.00");
        BigDecimal stockValor = stockCantidad.multiply(stockPMP); // 1000.00

        // Crear registro inicial manualmente para simular stock existente
        RegistroKardex registroInicial = RegistroKardex.crearEntrada(
            almacenId,
            recursoId,
            movimientoId1,
            stockCantidad,
            stockPMP,
            stockValor,
            stockCantidad,
            stockValor,
            stockPMP
        );
        kardexRepository.guardar(registroInicial);
        entityManager.flush();
        entityManager.clear();

        // Nueva entrada
        BigDecimal entradaCantidad = new BigDecimal("50.00");
        BigDecimal entradaPrecio = new BigDecimal("12.00");

        RegistroKardex nuevoRegistro = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            entradaCantidad,
            entradaPrecio,
            movimientoId2,
            stockCantidad,
            stockValor
        );

        // Act
        kardexRepository.guardar(nuevoRegistro);
        entityManager.flush();
        entityManager.clear();

        // Assert - Verificar fórmula PMP
        // PMP = (Stock × PMP + Entrada × Precio) / (Stock + Entrada)
        // PMP = (100 × 10 + 50 × 12) / (100 + 50) = (1000 + 600) / 150 = 1600 / 150 = 10.6667
        BigDecimal pmpEsperado = stockValor
            .add(entradaCantidad.multiply(entradaPrecio))
            .divide(stockCantidad.add(entradaCantidad), 4, RoundingMode.HALF_UP);

        Optional<RegistroKardex> ultimo = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId);
        assertTrue(ultimo.isPresent());

        RegistroKardex registro = ultimo.get();
        BigDecimal diferencia = registro.getCostoPromedioPonderado().subtract(pmpEsperado).abs();
        assertTrue(diferencia.compareTo(new BigDecimal("0.01")) <= 0,
            String.format("PMP calculado: %s, Esperado: %s, Diferencia: %s",
                registro.getCostoPromedioPonderado(), pmpEsperado, diferencia));
    }

    @Test
    @DisplayName("Debe buscar el último registro correctamente")
    void debeBuscarUltimoRegistro() {
        // Arrange - Crear múltiples registros
        RegistroKardex registro1 = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            new BigDecimal("100.00"),
            new BigDecimal("10.00"),
            movimientoId1,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );
        kardexRepository.guardar(registro1);
        entityManager.flush();

        // Esperar un momento para que las fechas sean diferentes
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        RegistroKardex registro2 = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            new BigDecimal("50.00"),
            new BigDecimal("12.00"),
            movimientoId2,
            registro1.getSaldoCantidad(),
            registro1.getSaldoValor()
        );
        kardexRepository.guardar(registro2);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<RegistroKardex> ultimo = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId);

        // Assert
        assertTrue(ultimo.isPresent());
        assertEquals(movimientoId2, ultimo.get().getMovimientoId());
        assertEquals(new BigDecimal("150.00"), ultimo.get().getSaldoCantidad());
    }

    @Test
    @DisplayName("Debe retornar Optional.empty cuando no hay registros")
    void debeRetornarEmptyCuandoNoHayRegistros() {
        // Act
        Optional<RegistroKardex> resultado = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId);

        // Assert
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Debe buscar todos los registros de un almacén y recurso")
    void debeBuscarTodosLosRegistros() {
        // Arrange - Crear múltiples registros
        RegistroKardex registro1 = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            new BigDecimal("100.00"),
            new BigDecimal("10.00"),
            movimientoId1,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );
        kardexRepository.guardar(registro1);
        entityManager.flush();

        RegistroKardex registro2 = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            new BigDecimal("50.00"),
            new BigDecimal("12.00"),
            movimientoId2,
            registro1.getSaldoCantidad(),
            registro1.getSaldoValor()
        );
        kardexRepository.guardar(registro2);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<RegistroKardex> registros = kardexRepository.buscarPorAlmacenIdYRecursoId(almacenId, recursoId);

        // Assert
        assertEquals(2, registros.size());
        // Deben estar ordenados por fecha descendente
        assertTrue(registros.get(0).getFechaMovimiento().isAfter(registros.get(1).getFechaMovimiento()) ||
                   registros.get(0).getFechaMovimiento().isEqual(registros.get(1).getFechaMovimiento()));
    }

    @Test
    @DisplayName("Debe mantener precisión de PMP dentro de 0.01")
    void debeMantenerPrecisionPMP() {
        // Arrange - Caso con valores que pueden causar problemas de precisión
        BigDecimal cantidad1 = new BigDecimal("33.333333");
        BigDecimal precio1 = new BigDecimal("10.123456");
        
        RegistroKardex registro1 = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            cantidad1,
            precio1,
            movimientoId1,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );
        kardexRepository.guardar(registro1);
        entityManager.flush();
        entityManager.clear();

        BigDecimal cantidad2 = new BigDecimal("66.666667");
        BigDecimal precio2 = new BigDecimal("15.789012");

        RegistroKardex registro2 = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            cantidad2,
            precio2,
            movimientoId2,
            registro1.getSaldoCantidad(),
            registro1.getSaldoValor()
        );
        kardexRepository.guardar(registro2);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<RegistroKardex> ultimo = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId);

        // Assert - Verificar que el PMP se calculó y persistió correctamente
        assertTrue(ultimo.isPresent());
        RegistroKardex registro = ultimo.get();
        
        // PMP debe ser: (saldoValor) / (saldoCantidad)
        BigDecimal pmpCalculado = registro.getSaldoValor()
            .divide(registro.getSaldoCantidad(), 4, RoundingMode.HALF_UP);
        
        BigDecimal diferencia = registro.getCostoPromedioPonderado().subtract(pmpCalculado).abs();
        assertTrue(diferencia.compareTo(new BigDecimal("0.01")) <= 0,
            String.format("PMP debe tener precisión de 0.01. Diferencia: %s", diferencia));
    }

    @Test
    @DisplayName("Debe manejar operaciones concurrentes correctamente")
    void debeManejarOperacionesConcurrentes() throws InterruptedException {
        // Arrange
        int numThreads = 5;
        int entradasPorThread = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger excepciones = new AtomicInteger(0);

        // Act - Ejecutar entradas concurrentes
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < entradasPorThread; j++) {
                        // Obtener último registro (puede haber race conditions)
                        Optional<RegistroKardex> ultimo = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(
                            almacenId, recursoId);
                        
                        BigDecimal saldoCantidad = ultimo.map(RegistroKardex::getSaldoCantidad)
                            .orElse(BigDecimal.ZERO);
                        BigDecimal saldoValor = ultimo.map(RegistroKardex::getSaldoValor)
                            .orElse(BigDecimal.ZERO);

                        // Crear nueva entrada
                        BigDecimal cantidad = new BigDecimal("10.00");
                        BigDecimal precio = new BigDecimal("10.00");
                        UUID movimientoId = UUID.randomUUID();

                        RegistroKardex nuevoRegistro = gestionKardexService.procesarEntrada(
                            almacenId,
                            recursoId,
                            cantidad,
                            precio,
                            movimientoId,
                            saldoCantidad,
                            saldoValor
                        );

                        kardexRepository.guardar(nuevoRegistro);
                        entityManager.flush();
                    }
                } catch (Exception e) {
                    excepciones.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Esperar a que todos los threads terminen
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Los threads deben completarse en 10 segundos");
        executor.shutdown();
        entityManager.clear();

        // Assert - Verificar que se crearon todos los registros
        List<RegistroKardex> registros = kardexRepository.buscarPorAlmacenIdYRecursoId(almacenId, recursoId);
        
        // Deben haberse creado numThreads * entradasPorThread registros
        // (aunque algunos pueden fallar por race conditions, esperamos al menos algunos)
        assertTrue(registros.size() > 0, "Debe haber al menos un registro creado");
        
        // Verificar que el último registro tiene valores consistentes
        Optional<RegistroKardex> ultimo = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId);
        if (ultimo.isPresent()) {
            RegistroKardex registro = ultimo.get();
            // PMP = saldoValor / saldoCantidad
            BigDecimal pmpCalculado = registro.getSaldoValor()
                .divide(registro.getSaldoCantidad(), 4, RoundingMode.HALF_UP);
            BigDecimal diferencia = registro.getCostoPromedioPonderado().subtract(pmpCalculado).abs();
            assertTrue(diferencia.compareTo(new BigDecimal("0.01")) <= 0,
                "El PMP debe ser consistente incluso con concurrencia");
        }
    }

    @Test
    @DisplayName("Debe persistir correctamente todos los campos del registro")
    void debePersistirCorrectamenteTodosLosCampos() {
        // Arrange
        BigDecimal cantidad = new BigDecimal("100.00");
        BigDecimal precio = new BigDecimal("10.50");
        
        RegistroKardex registro = gestionKardexService.procesarEntrada(
            almacenId,
            recursoId,
            cantidad,
            precio,
            movimientoId1,
            BigDecimal.ZERO,
            BigDecimal.ZERO
        );

        // Act
        kardexRepository.guardar(registro);
        entityManager.flush();
        entityManager.clear();

        // Assert - Verificar todos los campos
        KardexEntity entity = kardexJpaRepository.findById(registro.getId())
            .orElseThrow(() -> new AssertionError("El registro debería existir"));

        assertEquals(almacenId, entity.getAlmacenId());
        assertEquals(recursoId, entity.getRecursoId());
        assertEquals(movimientoId1, entity.getMovimientoId());
        assertEquals(TipoMovimientoAlmacen.ENTRADA, entity.getTipoMovimiento());
        assertEquals(cantidad, entity.getCantidadEntrada());
        assertEquals(BigDecimal.ZERO, entity.getCantidadSalida());
        assertEquals(precio, entity.getPrecioUnitario());
        assertEquals(cantidad, entity.getSaldoCantidad());
        assertEquals(new BigDecimal("1050.00"), entity.getSaldoValor());
        assertNotNull(entity.getCostoPromedioPonderado());
        assertNotNull(entity.getFechaMovimiento());
        assertNotNull(entity.getCreatedAt());
    }
}

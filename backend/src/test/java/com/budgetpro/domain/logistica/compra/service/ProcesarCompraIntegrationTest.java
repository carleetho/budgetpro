package com.budgetpro.domain.logistica.compra.service;

import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraDetalleId;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.NaturalezaGasto;
import com.budgetpro.domain.logistica.compra.model.RelacionContractual;
import com.budgetpro.domain.logistica.compra.model.RubroInsumo;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcesarCompraIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProcesarCompraService procesarCompraService;

    @Autowired
    private PartidaJpaRepository partidaJpaRepository;

    @Autowired
    private PresupuestoJpaRepository presupuestoJpaRepository;

    @Autowired
    private ProyectoJpaRepository proyectoJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockBean
    private GestionInventarioService gestionInventarioService;

    @Test
    void procesar_compraActualizaCompromisosEnBaseDeDatos() {
        UUID proyectoId = UUID.randomUUID();
        PresupuestoEntity presupuesto = crearPresupuesto(proyectoId);
        PartidaEntity partida = crearPartida(presupuesto, new BigDecimal("10.00"), new BigDecimal("100.00"));
        Compra compra = crearCompra(partida.getId(), new BigDecimal("3"), new BigDecimal("100.00"));

        TransactionTemplate tx = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        tx.executeWithoutResult(status -> procesarCompraService.procesar(compra, crearBilleteraMock()));

        entityManager.clear();
        UUID partidaId = Objects.requireNonNull(partida.getId(), "Partida ID no puede ser nulo");
        PartidaEntity actualizada = partidaJpaRepository.findById(partidaId).orElseThrow();
        assertEquals(new BigDecimal("300.00"), actualizada.getCompromisosPendientes());
        assertEquals(true, compra.isAprobada());
    }

    @Test
    void procesar_saldoInsuficiente_debeRevertirTransaccion() {
        UUID proyectoId = UUID.randomUUID();
        PresupuestoEntity presupuesto = crearPresupuesto(proyectoId);
        PartidaEntity partida = crearPartida(presupuesto, new BigDecimal("1.00"), new BigDecimal("50.00"));
        Compra compra = crearCompra(partida.getId(), new BigDecimal("2"), new BigDecimal("100.00"));

        TransactionTemplate tx = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        assertThrows(SaldoInsuficienteException.class,
                () -> tx.executeWithoutResult(status -> procesarCompraService.procesar(compra, crearBilleteraMock())));

        entityManager.clear();
        UUID partidaId = Objects.requireNonNull(partida.getId(), "Partida ID no puede ser nulo");
        PartidaEntity actualizada = partidaJpaRepository.findById(partidaId).orElseThrow();
        assertEquals(BigDecimal.ZERO, actualizada.getCompromisosPendientes());
    }

    @Test
    void partidaEntity_concurrenteDebeLanzarOptimisticLock() {
        UUID proyectoId = UUID.randomUUID();
        PresupuestoEntity presupuesto = crearPresupuesto(proyectoId);
        PartidaEntity partida = crearPartida(presupuesto, new BigDecimal("5.00"), new BigDecimal("100.00"));

        TransactionTemplate tx = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        UUID partidaId = Objects.requireNonNull(partida.getId(), "Partida ID no puede ser nulo");
        PartidaEntity instanciaB = Objects.requireNonNull(
                tx.execute(status -> partidaJpaRepository.findById(partidaId).orElseThrow()),
                "Instancia B no puede ser nula"
        );

        tx.executeWithoutResult(status -> {
            PartidaEntity entidad = partidaJpaRepository.findById(partidaId).orElseThrow();
            entidad.setDescripcion("Actualizacion 1");
            partidaJpaRepository.saveAndFlush(entidad);
        });

        RuntimeException ex = assertThrows(RuntimeException.class, () -> tx.executeWithoutResult(status -> {
            instanciaB.setDescripcion("Actualizacion 2");
            entityManager.merge(instanciaB);
            entityManager.flush();
        }));
        assertTrue(ex instanceof OptimisticLockException
                || ex instanceof ObjectOptimisticLockingFailureException);
    }

    private PresupuestoEntity crearPresupuesto(UUID proyectoId) {
        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(proyectoId);
        proyecto.setNombre("Proyecto IT");
        proyecto.setUbicacion("Lima");
        proyecto.setEstado(com.budgetpro.domain.proyecto.model.EstadoProyecto.ACTIVO);
        proyecto.setMoneda("USD");
        proyecto.setPresupuestoTotal(new BigDecimal("100000.00"));
        proyectoJpaRepository.save(proyecto);

        PresupuestoEntity presupuesto = new PresupuestoEntity();
        presupuesto.setId(UUID.randomUUID());
        presupuesto.setProyectoId(proyectoId);
        presupuesto.setProyecto(proyecto);
        presupuesto.setNombre("Presupuesto Base");
        presupuesto.setEstado(com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.BORRADOR);
        presupuesto.setEsLineaBase(Boolean.TRUE);
        presupuesto.setEsContractual(Boolean.TRUE);
        return presupuestoJpaRepository.save(presupuesto);
    }

    private PartidaEntity crearPartida(PresupuestoEntity presupuesto, BigDecimal metrado, BigDecimal precioUnitario) {
        PartidaEntity partida = new PartidaEntity();
        partida.setId(UUID.randomUUID());
        partida.setPresupuesto(presupuesto);
        partida.setPadre(null);
        partida.setCodigo("01.01");
        partida.setItem("01.01");
        partida.setDescripcion("Partida IT");
        partida.setUnidad("UND");
        partida.setMetradoOriginal(metrado);
        partida.setMetradoVigente(metrado);
        partida.setPrecioUnitario(precioUnitario);
        partida.setGastosReales(BigDecimal.ZERO);
        partida.setCompromisosPendientes(BigDecimal.ZERO);
        partida.setNivel(1);
        return partidaJpaRepository.save(partida);
    }

    private Compra crearCompra(UUID partidaId, BigDecimal cantidad, BigDecimal precioUnitario) {
        CompraDetalle detalle = CompraDetalle.crear(
                CompraDetalleId.nuevo(),
                UUID.randomUUID(),
                partidaId,
                NaturalezaGasto.DIRECTO_PARTIDA,
                RelacionContractual.CONTRACTUAL,
                RubroInsumo.MATERIAL_CONSTRUCCION,
                cantidad,
                precioUnitario
        );
        return Compra.crear(
                CompraId.nuevo(),
                UUID.randomUUID(),
                LocalDate.now(),
                "Proveedor IT",
                List.of(detalle)
        );
    }

    private com.budgetpro.domain.finanzas.model.Billetera crearBilleteraMock() {
        com.budgetpro.domain.finanzas.model.Billetera billetera = com.budgetpro.domain.finanzas.model.Billetera.crear(
                com.budgetpro.domain.finanzas.model.BilleteraId.generate(),
                UUID.randomUUID()
        );
        billetera.ingresar(new BigDecimal("10000.00"), "Ingreso IT", "http://evidencia/ok");
        return billetera;
    }
}

package com.budgetpro.domain.catalogo.service;

import com.budgetpro.domain.catalogo.exception.CatalogNotFoundException;
import com.budgetpro.domain.catalogo.exception.CatalogServiceException;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import com.budgetpro.infrastructure.catalogo.observability.CatalogEventLogger;
import com.budgetpro.infrastructure.catalogo.observability.CatalogMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock
    private CatalogPort catalogPort;

    @Mock
    private CatalogMetrics catalogMetrics;

    @Mock
    private CatalogEventLogger catalogEventLogger;

    @Test
    void createAPUSnapshot_debeConstruirSnapshotCompleto() {
        SnapshotService service = new SnapshotService(catalogPort, catalogMetrics, catalogEventLogger);
        UUID partidaId = UUID.randomUUID();

        APUSnapshot apuData = APUSnapshot.crear(
                APUSnapshotId.generate(),
                partidaId,
                "APU-EXT",
                "CAPECO",
                new BigDecimal("1.5"),
                "UND",
                LocalDateTime.now()
        );
        apuData.agregarInsumo(APUInsumoSnapshot.crear(
                APUInsumoSnapshotId.generate(),
                "MAT-001",
                "CEMENTO",
                new BigDecimal("2.0"),
                new BigDecimal("10.00")
        ));

        when(catalogPort.fetchAPU("APU-EXT", "CAPECO")).thenReturn(apuData);
        when(catalogPort.isRecursoActive("MAT-001", "CAPECO")).thenReturn(true);
        when(catalogPort.fetchRecurso("MAT-001", "CAPECO")).thenReturn(new RecursoSnapshot(
                "MAT-001",
                "CAPECO",
                "CEMENTO PORTLAND",
                TipoRecurso.MATERIAL,
                "BOL",
                new BigDecimal("25.50"),
                LocalDateTime.now()
        ));

        APUSnapshot snapshot = service.createAPUSnapshot("APU-EXT", "CAPECO");

        assertEquals("APU-EXT", snapshot.getExternalApuId());
        assertEquals("CAPECO", snapshot.getCatalogSource());
        assertEquals(apuData.getRendimientoOriginal(), snapshot.getRendimientoOriginal());
        assertEquals(apuData.getRendimientoOriginal(), snapshot.getRendimientoVigente());
        assertEquals(1, snapshot.getInsumos().size());
    }

    @Test
    void createAPUSnapshot_recursoInactivo_debeFallar() {
        SnapshotService service = new SnapshotService(catalogPort, catalogMetrics, catalogEventLogger);
        UUID partidaId = UUID.randomUUID();

        APUSnapshot apuData = APUSnapshot.crear(
                APUSnapshotId.generate(),
                partidaId,
                "APU-EXT",
                "CAPECO",
                new BigDecimal("1.0"),
                "UND",
                LocalDateTime.now()
        );
        apuData.agregarInsumo(APUInsumoSnapshot.crear(
                APUInsumoSnapshotId.generate(),
                "MAT-404",
                "NO EXISTE",
                new BigDecimal("1.0"),
                new BigDecimal("1.0")
        ));

        when(catalogPort.fetchAPU("APU-EXT", "CAPECO")).thenReturn(apuData);
        when(catalogPort.isRecursoActive("MAT-404", "CAPECO")).thenReturn(false);

        assertThrows(CatalogNotFoundException.class,
                () -> service.createAPUSnapshot("APU-EXT", "CAPECO"));
    }

    @Test
    void actualizarRendimiento_debeRegistrarAuditoria() {
        SnapshotService service = new SnapshotService(catalogPort, catalogMetrics, catalogEventLogger);
        APUSnapshot snapshot = APUSnapshot.crear(
                APUSnapshotId.generate(),
                UUID.randomUUID(),
                "APU-EXT",
                "CAPECO",
                new BigDecimal("1.0"),
                "UND",
                LocalDateTime.now()
        );

        UUID usuarioId = UUID.randomUUID();
        service.actualizarRendimiento(snapshot, new BigDecimal("2.0"), usuarioId);

        assertTrue(snapshot.isRendimientoModificado());
        assertEquals(usuarioId, snapshot.getRendimientoModificadoPor());
        assertNotNull(snapshot.getRendimientoModificadoEn());
    }

    @Test
    void validateRecursoProxy_debeDelegar() {
        SnapshotService service = new SnapshotService(catalogPort, catalogMetrics, catalogEventLogger);
        when(catalogPort.isRecursoActive("MAT-001", "CAPECO")).thenReturn(true);

        assertTrue(service.validateRecursoProxy("MAT-001", "CAPECO"));
    }

    @Test
    void createAPUSnapshot_errorCatalogo_debeEnvolver() {
        SnapshotService service = new SnapshotService(catalogPort, catalogMetrics, catalogEventLogger);
        when(catalogPort.fetchAPU("APU-ERR", "CAPECO"))
                .thenThrow(new RuntimeException("down"));

        assertThrows(CatalogServiceException.class,
                () -> service.createAPUSnapshot("APU-ERR", "CAPECO"));
    }
}

package com.budgetpro.infrastructure.persistence.entity;

import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Version;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartidaEntityVersionTest {

    @Test
    void partidaEntity_debeTenerVersionParaOptimisticLocking() throws Exception {
        assertNotNull(
                PartidaEntity.class.getDeclaredField("version").getAnnotation(Version.class),
                "La entidad Partida debe tener @Version para locking optimista"
        );
    }

    @Test
    void partidaRepository_debeExponerMetodosMinimos() {
        Method[] methods = PartidaRepository.class.getDeclaredMethods();
        boolean tieneFindByIdUuid = Arrays.stream(methods)
                .anyMatch(method -> method.getName().equals("findById")
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].equals(UUID.class));
        boolean tieneSave = Arrays.stream(methods)
                .anyMatch(method -> method.getName().equals("save") && method.getParameterCount() == 1);

        assertTrue(tieneFindByIdUuid, "PartidaRepository debe tener findById(UUID)");
        assertTrue(tieneSave, "PartidaRepository debe tener save(Partida)");
    }
}

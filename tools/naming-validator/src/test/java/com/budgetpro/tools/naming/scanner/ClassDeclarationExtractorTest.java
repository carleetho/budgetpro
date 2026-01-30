package com.budgetpro.tools.naming.scanner;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class ClassDeclarationExtractorTest {

    private final ClassDeclarationExtractor extractor = new ClassDeclarationExtractor();

    @Test
    void testExtractClass() {
        String content = "package com.example;\n\npublic class Presupuesto {\n}";
        assertEquals(Optional.of("Presupuesto"), extractor.extractClassName(content));
    }

    @Test
    void testExtractInterface() {
        String content = "public interface PresupuestoRepository { }";
        assertEquals(Optional.of("PresupuestoRepository"), extractor.extractClassName(content));
    }

    @Test
    void testExtractEnum() {
        String content = "public enum EstadoPresupuesto { }";
        assertEquals(Optional.of("EstadoPresupuesto"), extractor.extractClassName(content));
    }

    @Test
    void testExtractRecord() {
        String content = "public record PresupuestoId(UUID value) { }";
        assertEquals(Optional.of("PresupuestoId"), extractor.extractClassName(content));
    }

    @Test
    void testExtractMultipleClasses() {
        String content = "class Helper {}\npublic class Main { }\nclass Another { }";
        assertEquals(Optional.of("Main"), extractor.extractClassName(content));
    }

    @Test
    void testMalformedJava() {
        String content = "public clas Broken { }";
        assertEquals(Optional.empty(), extractor.extractClassName(content));
    }

    @Test
    void testEmptyFile() {
        assertEquals(Optional.empty(), extractor.extractClassName(""));
        assertEquals(Optional.empty(), extractor.extractClassName(null));
    }

    @Test
    void testNoPublicClass() {
        String content = "class Private { }";
        assertEquals(Optional.empty(), extractor.extractClassName(content));
    }
}

package com.budgetpro.tools.naming.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Escanea directorios recursivamente en busca de archivos Java.
 */
public class JavaFileScanner {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(JavaFileScanner.class.getName());

    /**
     * Busca todos los archivos .java dentro de un directorio raíz de forma
     * recursiva.
     * 
     * @param rootPath Ruta raíz para iniciar el escaneo.
     * @return Lista de rutas a archivos .java encontrados.
     * @throws IOException Si ocurre un error al acceder al sistema de archivos.
     */
    public List<Path> scanJavaFiles(Path rootPath) throws IOException {
        java.util.Objects.requireNonNull(rootPath, "Root path cannot be null");
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("La ruta especificada no existe o no es un directorio: " + rootPath);
        }

        try (Stream<Path> walk = Files.walk(rootPath)) {
            return walk.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.severe("Error escaneando archivos en " + rootPath + ": " + e.getMessage());
            throw e;
        }
    }
}

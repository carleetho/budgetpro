package com.budgetpro.validator.boundary.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Escanea archivos Java en busca de declaraciones de import.
 */
public class DomainScanner {

    private static final Pattern IMPORT_PATTERN = Pattern.compile("^import\\s+([\\w.]+);");

    /**
     * Escanea un directorio de forma recursiva buscando archivos .java.
     * 
     * @param rootDir Directorio raíz a escanear.
     * @return Lista de archivos Java encontrados.
     * @throws IOException Si hay error accediendo al sistema de archivos.
     */
    public List<Path> scanJavaFiles(Path rootDir) throws IOException {
        if (!Files.exists(rootDir) || !Files.isDirectory(rootDir)) {
            return List.of();
        }

        try (Stream<Path> walk = Files.walk(rootDir)) {
            return walk.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Extrae los imports de un archivo Java.
     * 
     * @param javaFile Path al archivo Java.
     * @return Lista de nombres completos de las clases importadas.
     * @throws IOException Si hay error leyendo el archivo.
     */
    public List<String> extractImports(Path javaFile) throws IOException {
        List<String> imports = new ArrayList<>();
        List<String> lines = Files.readAllLines(javaFile);

        for (String line : lines) {
            String trimmedLine = line.trim();
            // Optimización: si ya pasamos la sección de imports, podemos parar
            // (suponiendo que no hay clases anidadas con imports raros, pero es Java
            // estándar)
            if (trimmedLine.startsWith("public ") || trimmedLine.startsWith("class ")
                    || trimmedLine.startsWith("interface ") || trimmedLine.startsWith("record ")
                    || trimmedLine.startsWith("enum ")) {
                break;
            }

            Matcher matcher = IMPORT_PATTERN.matcher(trimmedLine);
            if (matcher.find()) {
                imports.add(matcher.group(1));
            }
        }

        return imports;
    }
}

package com.budgetpro.tools.naming.scanner;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extrae nombres de clases de contenido de archivos Java usando expresiones
 * regulares.
 */
public class ClassDeclarationExtractor {

    private static final Pattern CLASS_PATTERN = Pattern.compile("public\\s+(?:class|interface|enum|record)\\s+(\\w+)",
            Pattern.MULTILINE);

    /**
     * Extrae el nombre de la primera clase/interfaz/enum/record pública encontrada.
     * 
     * @param fileContent Contenido del archivo Java.
     * @return Optional con el nombre encontrado, o vacío si no hay coincidencia.
     */
    public Optional<String> extractClassName(String fileContent) {
        if (fileContent == null || fileContent.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = CLASS_PATTERN.matcher(fileContent);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }

        return Optional.empty();
    }
}

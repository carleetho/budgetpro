package com.budgetpro.tools.naming.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.Objects;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Encargado de cargar la configuración desde archivos YAML.
 */
public class ConfigLoader {
    private final ObjectMapper mapper;

    public ConfigLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * Carga la configuración desde el archivo especificado.
     * 
     * @param configPath Ruta al archivo YAML.
     * @return {@link ValidationConfig} cargado.
     * @throws IOException Si hay problemas leyendo el archivo.
     */
    public ValidationConfig load(Path configPath) throws IOException {
        Objects.requireNonNull(configPath, "Config path cannot be null");
        File configFile = configPath.toFile();
        if (!configFile.exists()) {
            throw new IOException("Archivo de configuración no encontrado: " + configPath.toAbsolutePath());
        }
        return mapper.readValue(configFile, ValidationConfig.class);
    }
}

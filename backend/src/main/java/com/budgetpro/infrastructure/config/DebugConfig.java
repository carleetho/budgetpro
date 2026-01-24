package com.budgetpro.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class DebugConfig implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(DebugConfig.class);
    private static final String LOG_PATH = "/home/wazoox/Desktop/budgetpro-backend/.cursor/debug.log";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired(required = false)
    private DataSourceProperties dataSourceProperties;
    
    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private ApplicationContext applicationContext;

    // #region agent log
    @PostConstruct
    public void logStartupInfo() {
        String sessionId = "debug-session";
        String runId = "run1";
        
        try {
            // Hipótesis B: Verificar variables de entorno
            Map<String, Object> envData = new HashMap<>();
            envData.put("DB_URL", environment.getProperty("DB_URL", "NOT_SET"));
            envData.put("DB_USERNAME", environment.getProperty("DB_USERNAME", "NOT_SET"));
            envData.put("DB_PASSWORD", environment.getProperty("DB_PASSWORD", "NOT_SET") != null ? "***" : "NOT_SET");
            envData.put("spring.datasource.url", environment.getProperty("spring.datasource.url", "NOT_SET"));
            envData.put("spring.datasource.username", environment.getProperty("spring.datasource.username", "NOT_SET"));
            
            writeLog(sessionId, runId, "B", "DebugConfig.logStartupInfo:env", "Environment variables check", envData);
            
            // Hipótesis B: Verificar DataSourceProperties
            if (dataSourceProperties != null) {
                Map<String, Object> dsData = new HashMap<>();
                dsData.put("url", dataSourceProperties.getUrl());
                dsData.put("username", dataSourceProperties.getUsername());
                dsData.put("password", dataSourceProperties.getPassword() != null ? "***" : null);
                writeLog(sessionId, runId, "B", "DebugConfig.logStartupInfo:datasource", "DataSource configuration", dsData);
            } else {
                writeLog(sessionId, runId, "B", "DebugConfig.logStartupInfo:datasource", "DataSourceProperties is null", Map.of());
            }
            
            // Hipótesis C/D: Verificar Redis
            if (redisConnectionFactory != null) {
                Map<String, Object> redisData = new HashMap<>();
                redisData.put("redisEnabled", true);
                redisData.put("connectionFactoryClass", redisConnectionFactory.getClass().getName());
                writeLog(sessionId, runId, "C", "DebugConfig.logStartupInfo:redis", "Redis configuration detected", redisData);
            } else {
                writeLog(sessionId, runId, "C", "DebugConfig.logStartupInfo:redis", "RedisConnectionFactory is null", Map.of("redisEnabled", false));
            }
            
            // Verificar si hay repositorios JPA y Redis configurados
            try {
                String[] jpaRepos = applicationContext.getBeanNamesForType(org.springframework.data.repository.Repository.class);
                Map<String, Object> repoData = new HashMap<>();
                repoData.put("totalRepositories", jpaRepos.length);
                writeLog(sessionId, runId, "D", "DebugConfig.logStartupInfo:repositories", "Repository count", repoData);
            } catch (Exception e) {
                writeLog(sessionId, runId, "D", "DebugConfig.logStartupInfo:repositories", "Error checking repositories", Map.of("error", e.getMessage()));
            }
            
        } catch (Exception e) {
            log.error("Error in debug logging", e);
        }
    }
    
    // #region agent log
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        String sessionId = "debug-session";
        String runId = "run1";
        
        try {
            // Hipótesis A: Verificar si PostgreSQL está accesible
            String dbUrl = environment.getProperty("spring.datasource.url", 
                environment.getProperty("DB_URL", "jdbc:postgresql://localhost:5432/budgetpro"));
            Map<String, Object> connectionData = new HashMap<>();
            connectionData.put("dbUrl", dbUrl);
            connectionData.put("attemptingConnection", true);
            writeLog(sessionId, runId, "A", "DebugConfig.onApplicationEvent:connection", "Attempting PostgreSQL connection", connectionData);
            
            // Hipótesis E: Verificar Flyway
            boolean flywayEnabled = environment.getProperty("spring.flyway.enabled", Boolean.class, true);
            Map<String, Object> flywayData = new HashMap<>();
            flywayData.put("flywayEnabled", flywayEnabled);
            writeLog(sessionId, runId, "E", "DebugConfig.onApplicationEvent:flyway", "Flyway configuration", flywayData);
            
        } catch (Exception e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            errorData.put("errorClass", e.getClass().getName());
            writeLog(sessionId, runId, "A", "DebugConfig.onApplicationEvent:error", "Error during context refresh", errorData);
        }
    }
    // #endregion
    
    private void writeLog(String sessionId, String runId, String hypothesisId, String location, String message, Map<String, Object> data) {
        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("sessionId", sessionId);
            logEntry.put("runId", runId);
            logEntry.put("hypothesisId", hypothesisId);
            logEntry.put("location", location);
            logEntry.put("message", message);
            logEntry.put("data", data);
            logEntry.put("timestamp", Instant.now().toEpochMilli());
            
            String jsonLine = objectMapper.writeValueAsString(logEntry);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_PATH, true))) {
                writer.println(jsonLine);
            }
        } catch (IOException e) {
            log.error("Failed to write debug log", e);
        }
    }
    // #endregion
}

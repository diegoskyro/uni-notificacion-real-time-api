package co.edu.unisimon.corenotificacion.service;

import co.edu.unisimon.corenotificacion.dto.response.TablaGestionableResponseDTO;
import co.edu.unisimon.corenotificacion.entity.TablaGestionable;
import co.edu.unisimon.corenotificacion.exception.BusinessException;
import co.edu.unisimon.corenotificacion.exception.ResourceNotFoundException;
import co.edu.unisimon.corenotificacion.repository.TablaGestionableRepository;
import co.edu.unisimon.corenotificacion.security.AuditorAwareImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DominioDinamicoService {

    private final TablaGestionableRepository configRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final AuditorAwareImpl auditorAware;

    @Transactional(readOnly = true)
    public List<TablaGestionableResponseDTO> listarConfiguraciones(Integer sistemaId) {
        log.info("[DominioDinamicoService] Listando configuraciones para sistemaId: {}", sistemaId);
        List<TablaGestionable> tablas;
        if (sistemaId != null) {
            tablas = new ArrayList<>(configRepository.findBySistemaIdAndEsActivoTrue(sistemaId));
            // Incluir por defecto la tabla de autogestión (soporte.tabla_gestionable)
            boolean contieneAutoGestion = tablas.stream()
                    .anyMatch(t -> "soporte".equalsIgnoreCase(t.getEsquema()) && "tabla_gestionable".equalsIgnoreCase(t.getNombreTabla()));
            if (!contieneAutoGestion) {
                configRepository.findByEsquemaAndNombreTablaAndEsActivoTrue("soporte", "tabla_gestionable")
                        .ifPresent(tablas::add);
            }
        } else {
            tablas = configRepository.findByEsActivoTrue();
        }

        log.info("[DominioDinamicoService] Se encontraron {} tablas gestionables activas", tablas.size());
        return tablas.stream()
                .map(TablaGestionableResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TablaGestionableResponseDTO> listarConfiguracionesPorHeader(UUID sistemaUuid) {
        log.info("[DominioDinamicoService] Petición recibida con X-Sistema-Uuid: {}", sistemaUuid);

        if (sistemaUuid != null) {
            log.info("[DominioDinamicoService] Buscando sistemaId en auth.sistema para UUID: {}", sistemaUuid);
            try {
                String sql = "SELECT id FROM auth.sistema WHERE uuid = :uuid AND es_activo = 1";
                List<Integer> ids = jdbcTemplate.queryForList(sql, Map.of("uuid", sistemaUuid), Integer.class);
                if (!ids.isEmpty()) {
                    log.info("[DominioDinamicoService] Encontrado sistemaId: {} en auth.sistema para UUID: {}", ids.get(0), sistemaUuid);
                    return listarConfiguraciones(ids.get(0));
                }
            } catch (Exception e) {
                log.warn("[DominioDinamicoService] Falló búsqueda con es_activo=1 en auth.sistema: {}", e.getMessage());
                try {
                    String sql = "SELECT id FROM auth.sistema WHERE uuid = :uuid";
                    List<Integer> ids = jdbcTemplate.queryForList(sql, Map.of("uuid", sistemaUuid), Integer.class);
                    if (!ids.isEmpty()) {
                        log.info("[DominioDinamicoService] Encontrado sistemaId: {} en auth.sistema para UUID: {}", ids.get(0), sistemaUuid);
                        return listarConfiguraciones(ids.get(0));
                    }
                } catch (Exception e2) {
                    log.warn("[DominioDinamicoService] Falló búsqueda en auth.sistema: {}", e2.getMessage());
                }
            }
        }

        log.warn("[DominioDinamicoService] No se pudo resolver sistemaId por UUID ni Header. Listando todas las tablas activas.");
        return listarConfiguraciones(null);
    }

    @Transactional(readOnly = true)
    public TablaGestionableResponseDTO obtenerConfiguracionPorUuid(UUID uuid) {
        TablaGestionable config = obtenerConfig(uuid);
        return new TablaGestionableResponseDTO(config);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerColumnas(UUID uuid) {
        TablaGestionable config = obtenerConfig(uuid);
        String sql = "SELECT COLUMN_NAME as columnName, DATA_TYPE as dataType, " +
                "CHARACTER_MAXIMUM_LENGTH as maxLength, IS_NULLABLE as isNullable " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = :esquema AND TABLE_NAME = :tabla";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("esquema", config.getEsquema())
                .addValue("tabla", config.getNombreTabla());

        return jdbcTemplate.queryForList(sql, params);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarDatos(UUID uuid) {
        TablaGestionable config = obtenerConfig(uuid);

        String sql = String.format("SELECT * FROM %s.%s", 
                escapeIdentifier(config.getEsquema()), 
                escapeIdentifier(config.getNombreTabla()));

        return jdbcTemplate.queryForList(sql, new MapSqlParameterSource());
    }

    public Map<String, Object> guardar(UUID uuid, Map<String, Object> datos) {
        TablaGestionable config = obtenerConfig(uuid);
        Set<String> columnasValidas = obtenerNombresColumnas(config);

        // Remover columnas autogeneradas o de auditoría manuales del body para prevenirlas
        datos.remove("id");
        datos.remove("fecha_creacion");
        datos.remove("usuario_creacion");
        datos.remove("fecha_actualizacion");
        datos.remove("usuario_actualizacion");

        // Inyectar auditoría y UUID público
        String usuarioActual = auditorAware.getCurrentAuditor().orElse("SYSTEM");
        LocalDateTime ahora = LocalDateTime.now();
        UUID recordUuid = UUID.randomUUID();

        datos.put("uuid", recordUuid);
        datos.put("usuario_creacion", usuarioActual);
        datos.put("fecha_creacion", ahora);

        if (columnasValidas.contains("es_activo")) {
            datos.put("es_activo", true);
        } else if (columnasValidas.contains("estado")) {
            datos.put("estado", 1);
        }

        // Filtrar datos para que solo contengan columnas reales de la tabla
        Map<String, Object> datosFiltrados = new HashMap<>();
        datos.forEach((key, val) -> {
            if (columnasValidas.contains(key)) {
                if (val instanceof String && ((String) val).trim().isEmpty()) {
                    datosFiltrados.put(key, null);
                } else {
                    datosFiltrados.put(key, val);
                }
            }
        });

        if (datosFiltrados.isEmpty()) {
            throw new BusinessException("No hay campos válidos para insertar");
        }

        String columnas = String.join(", ", datosFiltrados.keySet());
        String placeholders = datosFiltrados.keySet().stream()
                .map(col -> ":" + col)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s.%s (%s) VALUES (%s)",
                escapeIdentifier(config.getEsquema()),
                escapeIdentifier(config.getNombreTabla()),
                columnas,
                placeholders);

        jdbcTemplate.update(sql, new MapSqlParameterSource(datosFiltrados));

        return obtenerRegistroPorUuid(config, recordUuid);
    }

    public Map<String, Object> actualizar(UUID uuid, UUID registroUuid, Map<String, Object> datos) {
        TablaGestionable config = obtenerConfig(uuid);
        Set<String> columnasValidas = obtenerNombresColumnas(config);

        // Validar que el registro exista
        obtenerRegistroPorUuid(config, registroUuid);

        // Limpiar campos inmutables
        datos.remove("id");
        datos.remove("uuid");
        datos.remove("fecha_creacion");
        datos.remove("usuario_creacion");
        datos.remove("fecha_actualizacion");
        datos.remove("usuario_actualizacion");

        String usuarioActual = auditorAware.getCurrentAuditor().orElse("SYSTEM");
        LocalDateTime ahora = LocalDateTime.now();

        datos.put("usuario_actualizacion", usuarioActual);
        datos.put("fecha_actualizacion", ahora);

        // Filtrar datos para que solo contengan columnas reales de la tabla
        Map<String, Object> datosFiltrados = new HashMap<>();
        datos.forEach((key, val) -> {
            if (columnasValidas.contains(key)) {
                if (val instanceof String && ((String) val).trim().isEmpty()) {
                    datosFiltrados.put(key, null);
                } else {
                    datosFiltrados.put(key, val);
                }
            }
        });

        if (datosFiltrados.isEmpty()) {
            throw new BusinessException("No hay campos válidos para actualizar");
        }

        String setClause = datosFiltrados.keySet().stream()
                .map(col -> col + " = :" + col)
                .collect(Collectors.joining(", "));

        String sql = String.format("UPDATE %s.%s SET %s WHERE uuid = :registroUuid",
                escapeIdentifier(config.getEsquema()),
                escapeIdentifier(config.getNombreTabla()),
                setClause);

        MapSqlParameterSource params = new MapSqlParameterSource(datosFiltrados)
                .addValue("registroUuid", registroUuid);

        jdbcTemplate.update(sql, params);

        return obtenerRegistroPorUuid(config, registroUuid);
    }

    public void eliminar(UUID uuid, UUID registroUuid, boolean borradoFisico) {
        TablaGestionable config = obtenerConfig(uuid);
        Set<String> columnasValidas = obtenerNombresColumnas(config);

        // Validar que exista
        obtenerRegistroPorUuid(config, registroUuid);

        String sql;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("registroUuid", registroUuid);

        if (!borradoFisico && columnasValidas.contains("es_activo")) {
            String usuarioActual = auditorAware.getCurrentAuditor().orElse("SYSTEM");
            LocalDateTime ahora = LocalDateTime.now();
            sql = String.format("UPDATE %s.%s SET es_activo = 0, usuario_actualizacion = :user, fecha_actualizacion = :fecha WHERE uuid = :registroUuid",
                    escapeIdentifier(config.getEsquema()),
                    escapeIdentifier(config.getNombreTabla()));
            params.addValue("user", usuarioActual).addValue("fecha", ahora);
        } else if (!borradoFisico && columnasValidas.contains("estado")) {
            String usuarioActual = auditorAware.getCurrentAuditor().orElse("SYSTEM");
            LocalDateTime ahora = LocalDateTime.now();
            sql = String.format("UPDATE %s.%s SET estado = 0, usuario_actualizacion = :user, fecha_actualizacion = :fecha WHERE uuid = :registroUuid",
                    escapeIdentifier(config.getEsquema()),
                    escapeIdentifier(config.getNombreTabla()));
            params.addValue("user", usuarioActual).addValue("fecha", ahora);
        } else {
            sql = String.format("DELETE FROM %s.%s WHERE uuid = :registroUuid",
                    escapeIdentifier(config.getEsquema()),
                    escapeIdentifier(config.getNombreTabla()));
        }

        try {
            jdbcTemplate.update(sql, params);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (e.getCause() != null) {
                errorMsg = e.getCause().getMessage();
            }
            throw new BusinessException("Error de base de datos al eliminar: " + errorMsg);
        }
    }

    private TablaGestionable obtenerConfig(UUID uuid) {
        return configRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de tabla", uuid));
    }

    private Set<String> obtenerNombresColumnas(TablaGestionable config) {
        List<Map<String, Object>> columnas = obtenerColumnas(config.getUuid());
        return columnas.stream()
                .map(col -> (String) col.get("columnName"))
                .collect(Collectors.toSet());
    }

    private Map<String, Object> obtenerRegistroPorUuid(TablaGestionable config, UUID registroUuid) {
        String sql = String.format("SELECT * FROM %s.%s WHERE uuid = :registroUuid",
                escapeIdentifier(config.getEsquema()),
                escapeIdentifier(config.getNombreTabla()));

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("registroUuid", registroUuid);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params);

        if (result.isEmpty()) {
            throw new ResourceNotFoundException(config.getNombreMostrar(), registroUuid);
        }

        return result.get(0);
    }

    public String sincronizar(UUID uuid) {
        TablaGestionable config = obtenerConfig(uuid);
        if (config.getUrlSincronizacion() == null || config.getUrlSincronizacion().isBlank()) {
            throw new BusinessException("El catálogo '" + config.getNombreMostrar() + "' no tiene configurada una URL de sincronización externa");
        }

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.ResponseEntity<String> response;

            if ("GET".equalsIgnoreCase(config.getMetodoSincronizacion())) {
                response = restTemplate.getForEntity(config.getUrlSincronizacion(), String.class);
            } else {
                response = restTemplate.postForEntity(config.getUrlSincronizacion(), null, String.class);
            }
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return "Sincronización exitosa: " + response.getBody();
            } else {
                throw new BusinessException("El servicio externo respondió con código: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new BusinessException("Error al comunicarse con el servicio de sincronización externo: " + e.getMessage());
        }
    }

    private String escapeIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Identificador inválido: " + identifier);
        }
        return "[" + identifier + "]";
    }
}

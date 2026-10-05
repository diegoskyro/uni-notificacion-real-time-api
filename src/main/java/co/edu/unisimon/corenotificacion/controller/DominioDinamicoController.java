package co.edu.unisimon.corenotificacion.controller;

import co.edu.unisimon.corenotificacion.dto.response.TablaGestionableResponseDTO;
import co.edu.unisimon.corenotificacion.response.ResponseApi;
import co.edu.unisimon.corenotificacion.service.DominioDinamicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("dominios")
@RequiredArgsConstructor
@Tag(name = "Dominios Dinámicos", description = "Endpoints para la gestión dinámica de catálogos y tablas de dominios.")
@Slf4j
public class DominioDinamicoController {

    private final DominioDinamicoService dominioService;

    @Operation(summary = "Listar configuraciones de tablas gestionables", description = "Obtiene la lista de todas las tablas registradas en el sistema para gestión dinámica.")
    @GetMapping
    // @PreAuthorize("@authz.tienePermisoSede('DOMINIO_LISTAR')")
    public ResponseEntity<ResponseApi<List<TablaGestionableResponseDTO>>> listarConfiguraciones(
            @RequestHeader(value = "X-Sistema-Uuid", required = false) UUID sistemaUuid) {
        log.info("[DominioDinamicoController] GET /dominios - Header X-Sistema-Uuid={}", sistemaUuid);
        return ResponseEntity.ok(new ResponseApi<>(
                "Listado de tablas gestionables obtenido correctamente",
                HttpStatus.OK.value(),
                dominioService.listarConfiguracionesPorHeader(sistemaUuid)));
    }

    @Operation(summary = "Obtener configuración por UUID", description = "Obtiene los detalles de configuración de una tabla por su UUID.")
    @GetMapping("/{uuid}")
    // @PreAuthorize("@authz.tienePermisoSede('DOMINIO_VER')")
    public ResponseEntity<ResponseApi<TablaGestionableResponseDTO>> obtenerConfiguracionPorUuid(@PathVariable UUID uuid) {
        return ResponseEntity.ok(new ResponseApi<>(
                "Configuración de tabla obtenida correctamente",
                HttpStatus.OK.value(),
                dominioService.obtenerConfiguracionPorUuid(uuid)));
    }

    @Operation(summary = "Obtener columnas de una tabla", description = "Obtiene los nombres y tipos de columnas físicas de la tabla especificada.")
    @GetMapping("/{uuid}/columnas")
    // @PreAuthorize("@authz.tienePermisoSede('DOMINIO_LISTAR')")
    public ResponseEntity<ResponseApi<List<Map<String, Object>>>> obtenerColumnas(@PathVariable UUID uuid) {
        return ResponseEntity.ok(new ResponseApi<>(
                "Columnas obtenidas correctamente",
                HttpStatus.OK.value(),
                dominioService.obtenerColumnas(uuid)));
    }

    @Operation(summary = "Listar registros de la tabla dinámica", description = "Obtiene todos los registros activos de la tabla correspondiente al UUID.")
    @GetMapping("/{uuid}/datos")
    // @PreAuthorize("@authz.tienePermisoSede('DOMINIO_LISTAR_DATOS')")
    public ResponseEntity<ResponseApi<List<Map<String, Object>>>> listarDatos(@PathVariable UUID uuid) {
        return ResponseEntity.ok(new ResponseApi<>(
                "Datos obtenidos correctamente",
                HttpStatus.OK.value(),
                dominioService.listarDatos(uuid)));
    }

    @Operation(summary = "Crear registro dinámico", description = "Inserta un nuevo registro en la tabla parametrizable con los datos enviados.")
    @PostMapping("/{uuid}/datos")
    // @PreAuthorize("@authz.tienePermisoSede('DOMINIO_CREAR_DATOS')")
    public ResponseEntity<ResponseApi<Map<String, Object>>> guardar(
            @PathVariable UUID uuid,
            @RequestBody Map<String, Object> datos) {
        return ResponseEntity.ok(new ResponseApi<>(
                "Registro creado correctamente",
                HttpStatus.OK.value(),
                dominioService.guardar(uuid, datos)));
    }

    @Operation(summary = "Actualizar registro dinámico", description = "Actualiza las columnas del registro identificado por UUID en la tabla parametrizable.")
    @PutMapping("/{uuid}/datos/{registroUuid}")
    // @PreAuthorize("@authz.tienePermisoSede('DOMINIO_ACTUALIZAR_DATOS')")
    public ResponseEntity<ResponseApi<Map<String, Object>>> actualizar(
            @PathVariable UUID uuid,
            @PathVariable UUID registroUuid,
            @RequestBody Map<String, Object> datos) {
        return ResponseEntity.ok(new ResponseApi<>(
                "Registro actualizado correctamente",
                HttpStatus.OK.value(),
                dominioService.actualizar(uuid, registroUuid, datos)));
    }

    @Operation(summary = "Eliminar/Desactivar registro dinámico", description = "Elimina físicamente o realiza borrado lógico (si existe columna de estado) del registro.")
    @DeleteMapping("/{uuid}/datos/{registroUuid}")
    // @PreAuthorize("@authz.tienePermisoSede('DOMINIO_ELIMINAR_DATOS')")
    public ResponseEntity<ResponseApi<Void>> eliminar(
            @PathVariable UUID uuid,
            @PathVariable UUID registroUuid,
            @RequestParam(required = false, defaultValue = "false") boolean fisico) {
        dominioService.eliminar(uuid, registroUuid, fisico);
        return ResponseEntity.ok(new ResponseApi<>(
                "Registro procesado correctamente",
                HttpStatus.OK.value(),
                null));
    }

    @Operation(summary = "Sincronizar catálogo con fuente externa", description = "Ejecuta el servicio de sincronización configurado para este catálogo de forma proxy.")
    @PostMapping("/{uuid}/sincronizar")
    // @PreAuthorize("@authz.tienePermisoSede('DOMINIO_SINCRONIZAR')")
    public ResponseEntity<ResponseApi<String>> sincronizar(@PathVariable UUID uuid) {
        String resultado = dominioService.sincronizar(uuid);
        return ResponseEntity.ok(new ResponseApi<>(
                "Sincronización procesada",
                HttpStatus.OK.value(),
                resultado));
    }
}

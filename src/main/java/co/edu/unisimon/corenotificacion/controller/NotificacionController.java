package co.edu.unisimon.corenotificacion.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import co.edu.unisimon.corenotificacion.dto.response.NotificacionResponseDTO;
import co.edu.unisimon.corenotificacion.response.ResponseApi;
import co.edu.unisimon.corenotificacion.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Slf4j
public class NotificacionController {

    private final NotificacionService notificacionService;

    public record UserPrincipalData(String usuarioId, List<String> roles) {
    }

    private List<String> consultarRolesDesdeAuth(String jwtToken) {
        if (jwtToken == null || jwtToken.isBlank()) {
            log.warn("[NotificacionController] Token JWT ausente. No se puede consultar auth/getRole");
            return List.of();
        }
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", jwtToken.startsWith("Bearer ") ? jwtToken : "Bearer " + jwtToken);
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

            log.info("[NotificacionController] Invocando http://192.168.3.83/auth/getRole con el JWT recibido");
            org.springframework.http.ResponseEntity<java.util.Map> response = restTemplate.exchange(
                    "http://192.168.3.83/auth/getRole",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    java.util.Map.class);

            log.info("[NotificacionController] Respuesta HTTP getRole: status={}, body={}", response.getStatusCode(),
                    response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object dataObj = response.getBody().get("data");
                if (dataObj instanceof List<?> list) {
                    List<String> res = new java.util.ArrayList<>();
                    for (Object item : list) {
                        if (item != null && !item.toString().isBlank()) {
                            res.add(item.toString());
                        }
                    }
                    log.info("[NotificacionController] Roles obtenidos desde auth/getRole: {}", res);
                    return res;
                }
            }
        } catch (Exception e) {
            log.error("[NotificacionController] Error al consultar http://192.168.3.83/auth/getRole: {}",
                    e.getMessage());
        }
        return List.of();
    }

    private UserPrincipalData extraerUsuarioYRoles(
            Authentication authentication,
            java.util.Map<String, String> headersMap,
            String tokenParam) {

        String usuarioId = null;
        Set<String> roles = new java.util.LinkedHashSet<>();

        // Normalizar claves de headers a minúsculas
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        if (headersMap != null) {
            for (java.util.Map.Entry<String, String> entry : headersMap.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    headers.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
        }

        // 1. Extraer token JWT recibido (header Authorization o param token)
        String jwt = tokenParam;
        String authHeader = headers.get("authorization");
        if ((jwt == null || jwt.isBlank()) && authHeader != null) {
            jwt = authHeader.trim();
        }
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7).trim();
        }

        // 2. Consultar servicio Auth GET http://192.168.3.83/auth/getRole pasando el JWT
        if (jwt != null && !jwt.isBlank()) {
            List<String> rolesAuthService = consultarRolesDesdeAuth(jwt);
            for (String r : rolesAuthService) {
                if (r != null && !r.isBlank()) {
                    String clean = r.replace("ROLE_", "").trim();
                    if (!clean.isBlank()) {
                        roles.add(clean);
                    }
                }
            }
        }

        // 3. Extraer usuarioId desde el payload del token JWT (sub, email, username)
        if (jwt != null && !jwt.isBlank()) {
            try {
                String[] parts = jwt.split("\\.");
                if (parts.length >= 2) {
                    String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]),
                            java.nio.charset.StandardCharsets.UTF_8);
                    com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper()
                            .readTree(payloadJson);

                    if (rootNode.has("sub") && !rootNode.get("sub").isNull()) {
                        usuarioId = rootNode.get("sub").asText();
                    } else if (rootNode.has("email") && !rootNode.get("email").isNull()) {
                        usuarioId = rootNode.get("email").asText();
                    } else if (rootNode.has("username") && !rootNode.get("username").isNull()) {
                        usuarioId = rootNode.get("username").asText();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback de usuarioId desde SecurityContext o header x-user-id si no vino en el token
        if (usuarioId == null || usuarioId.isBlank()) {
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                usuarioId = authentication.getName();
            } else if (headers.containsKey("x-user-id")) {
                usuarioId = headers.get("x-user-id");
            }
        }

        List<String> rolesFinales = new java.util.ArrayList<>(roles);
        log.info("[NotificacionController] UserPrincipalData obtenido: usuarioId='{}', roles={}", usuarioId,
                rolesFinales);
        return new UserPrincipalData(usuarioId, rolesFinales);
    }

    @GetMapping("/usuario")
    public ResponseEntity<ResponseApi<List<NotificacionResponseDTO>>> listarPorUsuario(
            @RequestHeader(required = false) java.util.Map<String, String> headers,
            @RequestParam(required = false) String token,
            Authentication authentication) {
        UserPrincipalData principal = extraerUsuarioYRoles(authentication, headers, token);
        List<NotificacionResponseDTO> lista = notificacionService.listarPorUsuario(principal.usuarioId(),
                principal.roles());
        return ResponseEntity.ok(new ResponseApi<>("Notificaciones obtenidas", HttpStatus.OK.value(), lista));
    }

    @GetMapping("/usuario/no-leidas")
    public ResponseEntity<ResponseApi<List<NotificacionResponseDTO>>> listarNoLeidas(
            @RequestHeader(required = false) java.util.Map<String, String> headers,
            @RequestParam(required = false) String token,
            Authentication authentication) {
        UserPrincipalData principal = extraerUsuarioYRoles(authentication, headers, token);
        List<NotificacionResponseDTO> lista = notificacionService.listarNoLeidas(principal.usuarioId(),
                principal.roles());
        return ResponseEntity
                .ok(new ResponseApi<>("Notificaciones no leídas obtenidas", HttpStatus.OK.value(), lista));
    }

    @GetMapping("/usuario/conteo-no-leidas")
    public ResponseEntity<ResponseApi<Long>> contarNoLeidas(
            @RequestHeader(required = false) java.util.Map<String, String> headers,
            @RequestParam(required = false) String token,
            Authentication authentication) {
        UserPrincipalData principal = extraerUsuarioYRoles(authentication, headers, token);
        long conteo = notificacionService.contarNoLeidas(principal.usuarioId(), principal.roles());
        return ResponseEntity.ok(new ResponseApi<>("Conteo de no leídas obtenido", HttpStatus.OK.value(), conteo));
    }

    @PutMapping("/{uuid}/marcar-leida")
    public ResponseEntity<ResponseApi<NotificacionResponseDTO>> marcarComoLeida(
            @PathVariable UUID uuid,
            @RequestHeader(required = false) java.util.Map<String, String> headers,
            Authentication authentication) {
        UserPrincipalData principal = extraerUsuarioYRoles(authentication, headers, null);
        NotificacionResponseDTO dto = notificacionService.marcarComoLeida(uuid, principal.usuarioId());
        return ResponseEntity.ok(new ResponseApi<>("Notificación marcada como leída", HttpStatus.OK.value(), dto));
    }

    @PutMapping("/usuario/marcar-todas-leidas")
    public ResponseEntity<ResponseApi<Void>> marcarTodasComoLeidas(
            @RequestHeader(required = false) java.util.Map<String, String> headers,
            @RequestParam(required = false) String token,
            Authentication authentication) {
        UserPrincipalData principal = extraerUsuarioYRoles(authentication, headers, token);
        notificacionService.marcarTodasComoLeidas(principal.usuarioId(), principal.roles());
        return ResponseEntity
                .ok(new ResponseApi<>("Todas las notificaciones marcadas como leídas", HttpStatus.OK.value(), null));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ResponseApi<Void>> eliminar(
            @PathVariable UUID uuid,
            @RequestHeader(required = false) java.util.Map<String, String> headers,
            Authentication authentication) {
        UserPrincipalData principal = extraerUsuarioYRoles(authentication, headers, null);
        notificacionService.eliminarParaUsuario(uuid, principal.usuarioId());
        return ResponseEntity.ok(new ResponseApi<>("Notificación eliminada correctamente", HttpStatus.OK.value(), null));
    }
}

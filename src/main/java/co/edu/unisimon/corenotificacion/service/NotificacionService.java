package co.edu.unisimon.corenotificacion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unisimon.corenotificacion.dto.response.NotificacionResponseDTO;
import co.edu.unisimon.corenotificacion.entity.Notificacion;
import co.edu.unisimon.corenotificacion.entity.NotificacionLectura;
import co.edu.unisimon.corenotificacion.exception.ResourceNotFoundException;
import co.edu.unisimon.corenotificacion.repository.NotificacionLecturaRepository;
import co.edu.unisimon.corenotificacion.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final NotificacionLecturaRepository notificacionLecturaRepository;

    private List<String> normalizarRolesLimpios(List<String> roles) {
        if (roles == null || roles.isEmpty()) return List.of();
        return roles.stream()
                .flatMap(r -> java.util.Arrays.stream(r.split(",")))
                .map(r -> r.replace("ROLE_", "").trim().toUpperCase())
                .filter(r -> !r.isBlank())
                .distinct()
                .toList();
    }

    private List<String> normalizarRolesPrefijados(List<String> rolesLimpios) {
        if (rolesLimpios == null || rolesLimpios.isEmpty()) return List.of();
        return rolesLimpios.stream()
                .map(r -> "ROLE_" + r)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarPorUsuario(String usuarioId, List<String> roles) {
        List<String> cleanRoles = normalizarRolesLimpios(roles);
        List<String> prefRoles = normalizarRolesPrefijados(cleanRoles);
        boolean hasRoles = !cleanRoles.isEmpty();
        String r1 = cleanRoles.size() > 0 ? cleanRoles.get(0) : "__NONE__";
        String r2 = cleanRoles.size() > 1 ? cleanRoles.get(1) : "__NONE__";
        String r3 = cleanRoles.size() > 2 ? cleanRoles.get(2) : "__NONE__";
        LocalDateTime fechaDesde = LocalDateTime.now().minusHours(24);

        List<Notificacion> notificaciones = notificacionRepository.findNotificacionesParaUsuario(
                usuarioId != null ? usuarioId : "",
                hasRoles ? cleanRoles : List.of("__NONE__"),
                hasRoles ? prefRoles : List.of("__NONE__"),
                hasRoles,
                r1, r2, r3,
                fechaDesde
        );
        return notificaciones.stream().map(n -> {
            NotificacionResponseDTO dto = new NotificacionResponseDTO(n);
            boolean yaLeida = usuarioId != null && notificacionLecturaRepository.existsByNotificacionIdAndUsuarioId(n.getId(), usuarioId);
            dto.setLeida(yaLeida);
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarNoLeidas(String usuarioId, List<String> roles) {
        List<String> cleanRoles = normalizarRolesLimpios(roles);
        List<String> prefRoles = normalizarRolesPrefijados(cleanRoles);
        boolean hasRoles = !cleanRoles.isEmpty();
        String r1 = cleanRoles.size() > 0 ? cleanRoles.get(0) : "__NONE__";
        String r2 = cleanRoles.size() > 1 ? cleanRoles.get(1) : "__NONE__";
        String r3 = cleanRoles.size() > 2 ? cleanRoles.get(2) : "__NONE__";
        LocalDateTime fechaDesde = LocalDateTime.now().minusHours(24);

        List<Notificacion> noLeidas = notificacionRepository.findNotificacionesNoLeidasParaUsuario(
                usuarioId != null ? usuarioId : "",
                hasRoles ? cleanRoles : List.of("__NONE__"),
                hasRoles ? prefRoles : List.of("__NONE__"),
                hasRoles,
                r1, r2, r3,
                fechaDesde
        );
        return noLeidas.stream().map(n -> {
            NotificacionResponseDTO dto = new NotificacionResponseDTO(n);
            dto.setLeida(false);
            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String usuarioId, List<String> roles) {
        List<String> cleanRoles = normalizarRolesLimpios(roles);
        List<String> prefRoles = normalizarRolesPrefijados(cleanRoles);
        boolean hasRoles = !cleanRoles.isEmpty();
        String r1 = cleanRoles.size() > 0 ? cleanRoles.get(0) : "__NONE__";
        String r2 = cleanRoles.size() > 1 ? cleanRoles.get(1) : "__NONE__";
        String r3 = cleanRoles.size() > 2 ? cleanRoles.get(2) : "__NONE__";
        LocalDateTime fechaDesde = LocalDateTime.now().minusHours(24);

        return notificacionRepository.countNotificacionesNoLeidasParaUsuario(
                usuarioId != null ? usuarioId : "",
                hasRoles ? cleanRoles : List.of("__NONE__"),
                hasRoles ? prefRoles : List.of("__NONE__"),
                hasRoles,
                r1, r2, r3,
                fechaDesde
        );
    }

    public NotificacionResponseDTO marcarComoLeida(UUID uuid, String usuarioId) {
        Notificacion notificacion = notificacionRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", uuid));

        if (usuarioId != null && !usuarioId.isBlank()) {
            boolean yaRegistrada = notificacionLecturaRepository.existsByNotificacionIdAndUsuarioId(notificacion.getId(), usuarioId);
            if (!yaRegistrada) {
                NotificacionLectura lectura = new NotificacionLectura();
                lectura.setNotificacion(notificacion);
                lectura.setUsuarioId(usuarioId);
                lectura.setFechaLectura(LocalDateTime.now());
                lectura.setEsActivo(true);
                notificacionLecturaRepository.save(lectura);
            }
        }

        NotificacionResponseDTO dto = new NotificacionResponseDTO(notificacion);
        dto.setLeida(true);
        return dto;
    }

    public void marcarTodasComoLeidas(String usuarioId, List<String> roles) {
        if (usuarioId == null || usuarioId.isBlank()) return;

        List<String> cleanRoles = normalizarRolesLimpios(roles);
        List<String> prefRoles = normalizarRolesPrefijados(cleanRoles);
        boolean hasRoles = !cleanRoles.isEmpty();
        String r1 = cleanRoles.size() > 0 ? cleanRoles.get(0) : "__NONE__";
        String r2 = cleanRoles.size() > 1 ? cleanRoles.get(1) : "__NONE__";
        String r3 = cleanRoles.size() > 2 ? cleanRoles.get(2) : "__NONE__";
        LocalDateTime fechaDesde = LocalDateTime.now().minusHours(24);

        List<Notificacion> noLeidas = notificacionRepository.findNotificacionesNoLeidasParaUsuario(
                usuarioId,
                hasRoles ? cleanRoles : List.of("__NONE__"),
                hasRoles ? prefRoles : List.of("__NONE__"),
                hasRoles,
                r1, r2, r3,
                fechaDesde
        );
        List<NotificacionLectura> nuevasLecturas = noLeidas.stream().map(n -> {
            NotificacionLectura nl = new NotificacionLectura();
            nl.setNotificacion(n);
            nl.setUsuarioId(usuarioId);
            nl.setFechaLectura(LocalDateTime.now());
            nl.setEsActivo(true);
            return nl;
        }).toList();

        if (!nuevasLecturas.isEmpty()) {
            notificacionLecturaRepository.saveAll(nuevasLecturas);
        }
    }

    public void eliminarParaUsuario(UUID uuid, String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) return;

        Notificacion notificacion = notificacionRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", uuid));

        NotificacionLectura lectura = notificacionLecturaRepository
                .findByNotificacionIdAndUsuarioId(notificacion.getId(), usuarioId)
                .orElseGet(() -> {
                    NotificacionLectura nl = new NotificacionLectura();
                    nl.setNotificacion(notificacion);
                    nl.setUsuarioId(usuarioId);
                    nl.setFechaLectura(LocalDateTime.now());
                    return nl;
                });

        lectura.setEsActivo(false);
        notificacionLecturaRepository.save(lectura);
    }
}

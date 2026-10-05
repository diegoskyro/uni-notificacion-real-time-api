package co.edu.unisimon.corenotificacion.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unisimon.corenotificacion.entity.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    Optional<Notificacion> findByUuid(UUID uuid);

    /**
     * Lista todas las notificaciones activas aplicables a un usuario (individuales,
     * globales o por rol) de las últimas 24 horas
     */
    @Query("SELECT n FROM Notificacion n WHERE n.esActivo = true AND n.fechaCreacion >= :fechaDesde AND ("
            + "n.usuarioId = :usuarioId "
            + "OR n.targetAudiencia = :usuarioId "
            + "OR UPPER(n.tipoAudiencia) = 'ALL' "
            + "OR UPPER(n.usuarioId) = 'ALL' "
            + "OR UPPER(n.usuarioId) = 'SISTEMA' "
            + "OR TRIM(UPPER(n.targetAudiencia)) IN (:roles) "
            + "OR TRIM(UPPER(n.targetAudiencia)) IN (:rolesPrefixed) "
            + "OR TRIM(UPPER(n.usuarioId)) IN (:roles) "
            + "OR TRIM(UPPER(n.usuarioId)) IN (:rolesPrefixed) "
            + "OR (n.targetAudiencia IS NOT NULL AND ("
            + "   (:r1 != '__NONE__' AND UPPER(n.targetAudiencia) LIKE CONCAT('%', :r1, '%')) OR "
            + "   (:r2 != '__NONE__' AND UPPER(n.targetAudiencia) LIKE CONCAT('%', :r2, '%')) OR "
            + "   (:r3 != '__NONE__' AND UPPER(n.targetAudiencia) LIKE CONCAT('%', :r3, '%'))"
            + "))"
            + ") AND NOT EXISTS (SELECT 1 FROM NotificacionLectura nl WHERE nl.notificacion.id = n.id AND nl.usuarioId = :usuarioId AND nl.esActivo = false) ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findNotificacionesParaUsuario(
            @Param("usuarioId") String usuarioId,
            @Param("roles") List<String> roles,
            @Param("rolesPrefixed") List<String> rolesPrefixed,
            @Param("hasRoles") boolean hasRoles,
            @Param("r1") String r1,
            @Param("r2") String r2,
            @Param("r3") String r3,
            @Param("fechaDesde") LocalDateTime fechaDesde);

    /**
     * Lista las notificaciones no leídas por un usuario específico de las últimas 24 horas
     */
    @Query("SELECT n FROM Notificacion n WHERE n.esActivo = true AND n.fechaCreacion >= :fechaDesde AND ("
            + "n.usuarioId = :usuarioId "
            + "OR n.targetAudiencia = :usuarioId "
            + "OR UPPER(n.tipoAudiencia) = 'ALL' "
            + "OR UPPER(n.usuarioId) = 'ALL' "
            + "OR UPPER(n.usuarioId) = 'SISTEMA' "
            + "OR TRIM(UPPER(n.targetAudiencia)) IN (:roles) "
            + "OR TRIM(UPPER(n.targetAudiencia)) IN (:rolesPrefixed) "
            + "OR TRIM(UPPER(n.usuarioId)) IN (:roles) "
            + "OR TRIM(UPPER(n.usuarioId)) IN (:rolesPrefixed) "
            + "OR (n.targetAudiencia IS NOT NULL AND ("
            + "   (:r1 != '__NONE__' AND UPPER(n.targetAudiencia) LIKE CONCAT('%', :r1, '%')) OR "
            + "   (:r2 != '__NONE__' AND UPPER(n.targetAudiencia) LIKE CONCAT('%', :r2, '%')) OR "
            + "   (:r3 != '__NONE__' AND UPPER(n.targetAudiencia) LIKE CONCAT('%', :r3, '%'))"
            + "))"
            + ") AND NOT EXISTS (SELECT 1 FROM NotificacionLectura nl WHERE nl.notificacion.id = n.id AND nl.usuarioId = :usuarioId) ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findNotificacionesNoLeidasParaUsuario(
            @Param("usuarioId") String usuarioId,
            @Param("roles") List<String> roles,
            @Param("rolesPrefixed") List<String> rolesPrefixed,
            @Param("hasRoles") boolean hasRoles,
            @Param("r1") String r1,
            @Param("r2") String r2,
            @Param("r3") String r3,
            @Param("fechaDesde") LocalDateTime fechaDesde);

    /**
     * Cuenta el número de notificaciones no leídas para un usuario de las últimas 24 horas
     */
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.esActivo = true AND n.fechaCreacion >= :fechaDesde AND ("
            + "n.usuarioId = :usuarioId "
            + "OR n.targetAudiencia = :usuarioId "
            + "OR UPPER(n.tipoAudiencia) = 'ALL' "
            + "OR UPPER(n.usuarioId) = 'ALL' "
            + "OR UPPER(n.usuarioId) = 'SISTEMA' "
            + "OR TRIM(UPPER(n.targetAudiencia)) IN (:roles) "
            + "OR TRIM(UPPER(n.targetAudiencia)) IN (:rolesPrefixed) "
            + "OR TRIM(UPPER(n.usuarioId)) IN (:roles) "
            + "OR TRIM(UPPER(n.usuarioId)) IN (:rolesPrefixed) "
            + "OR (n.targetAudiencia IS NOT NULL AND ("
            + "   (:r1 != '__NONE__' AND UPPER(n.targetAudiencia) LIKE CONCAT('%', :r1, '%')) OR "
            + "   (:r2 != '__NONE__' AND UPPER(n.targetAudiencia) LIKE CONCAT('%', :r2, '%')) OR "
            + "   (:r3 != '__NONE__' AND UPPER(n.targetAudiencia) LIKE CONCAT('%', :r3, '%'))"
            + "))"
            + ") AND NOT EXISTS (SELECT 1 FROM NotificacionLectura nl WHERE nl.notificacion.id = n.id AND nl.usuarioId = :usuarioId)")
    long countNotificacionesNoLeidasParaUsuario(
            @Param("usuarioId") String usuarioId,
            @Param("roles") List<String> roles,
            @Param("rolesPrefixed") List<String> rolesPrefixed,
            @Param("hasRoles") boolean hasRoles,
            @Param("r1") String r1,
            @Param("r2") String r2,
            @Param("r3") String r3,
            @Param("fechaDesde") LocalDateTime fechaDesde);
}

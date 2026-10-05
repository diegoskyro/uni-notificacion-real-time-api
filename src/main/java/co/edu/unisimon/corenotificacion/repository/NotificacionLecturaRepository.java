package co.edu.unisimon.corenotificacion.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unisimon.corenotificacion.entity.NotificacionLectura;

@Repository
public interface NotificacionLecturaRepository extends JpaRepository<NotificacionLectura, Integer> {

    Optional<NotificacionLectura> findByUuid(UUID uuid);

    Optional<NotificacionLectura> findByNotificacionIdAndUsuarioId(Integer notificacionId, String usuarioId);

    boolean existsByNotificacionIdAndUsuarioId(Integer notificacionId, String usuarioId);

    List<NotificacionLectura> findByNotificacionId(Integer notificacionId);

    long countByNotificacionId(Integer notificacionId);
}

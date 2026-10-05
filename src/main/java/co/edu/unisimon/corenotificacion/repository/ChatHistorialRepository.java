package co.edu.unisimon.corenotificacion.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unisimon.corenotificacion.entity.ChatHistorial;

@Repository
public interface ChatHistorialRepository extends JpaRepository<ChatHistorial, Integer> {

    Page<ChatHistorial> findByConversacionUuidAndEsActivoTrueOrderByFechaCreacionAsc(UUID conversacionUuid, Pageable pageable);
}

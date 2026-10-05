package co.edu.unisimon.corenotificacion.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import co.edu.unisimon.corenotificacion.entity.Sede;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Integer> {
	Optional<Sede> findByUuid(UUID uuid);
	boolean existsByNombreIgnoreCase(String nombre);
}

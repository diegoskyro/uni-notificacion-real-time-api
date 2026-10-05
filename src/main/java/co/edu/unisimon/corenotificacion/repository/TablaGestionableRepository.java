package co.edu.unisimon.corenotificacion.repository;

import co.edu.unisimon.corenotificacion.entity.TablaGestionable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TablaGestionableRepository extends JpaRepository<TablaGestionable, Integer> {
    Optional<TablaGestionable> findByUuid(UUID uuid);
    List<TablaGestionable> findByEsActivoTrue();
    List<TablaGestionable> findBySistemaIdAndEsActivoTrue(Integer sistemaId);
    Optional<TablaGestionable> findByEsquemaAndNombreTablaAndEsActivoTrue(String esquema, String nombreTabla);
}

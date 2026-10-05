package co.edu.unisimon.corenotificacion.dto.response;

import co.edu.unisimon.corenotificacion.entity.TablaGestionable;
import lombok.Data;
import java.util.UUID;

@Data
public class TablaGestionableResponseDTO {
    private Integer id;
    private UUID uuid;
    private String esquema;
    private String nombreTabla;
    private String nombreMostrar;
    private Integer sistemaId;
    private Integer permisoId;
    private String camposConfig;
    private String urlSincronizacion;
    private String origenSincronizacion;
    private String metodoSincronizacion;

    public TablaGestionableResponseDTO(TablaGestionable entity) {
        this.id = entity.getId();
        this.uuid = entity.getUuid();
        this.esquema = entity.getEsquema();
        this.nombreTabla = entity.getNombreTabla();
        this.nombreMostrar = entity.getNombreMostrar();
        this.sistemaId = entity.getSistemaId();
        this.permisoId = entity.getPermisoId();
        this.camposConfig = entity.getCamposConfig();
        this.urlSincronizacion = entity.getUrlSincronizacion();
        this.origenSincronizacion = entity.getOrigenSincronizacion();
        this.metodoSincronizacion = entity.getMetodoSincronizacion();
    }
}

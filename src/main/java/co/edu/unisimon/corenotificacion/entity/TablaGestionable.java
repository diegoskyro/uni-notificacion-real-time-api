package co.edu.unisimon.corenotificacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(schema = "soporte", name = "tabla_gestionable")
@Data
@EqualsAndHashCode(callSuper = false)
public class TablaGestionable extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "esquema", nullable = false, length = 50)
    private String esquema;

    @Column(name = "nombre_tabla", nullable = false, length = 100)
    private String nombreTabla;

    @Column(name = "nombre_mostrar", nullable = false, length = 150)
    private String nombreMostrar;

    @Column(name = "sistema_id", nullable = false)
    private Integer sistemaId;

    @Column(name = "permiso_id")
    private Integer permisoId;

    @Column(name = "campos_config", columnDefinition = "varchar(max)")
    private String camposConfig;

    @Column(name = "url_sincronizacion", length = 500)
    private String urlSincronizacion;

    @Column(name = "origen_sincronizacion", length = 100)
    private String origenSincronizacion;

    @Column(name = "metodo_sincronizacion", length = 10, nullable = false)
    private String metodoSincronizacion = "POST";

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo = true;
}

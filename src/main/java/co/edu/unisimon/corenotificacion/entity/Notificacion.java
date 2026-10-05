package co.edu.unisimon.corenotificacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(schema = "themis_chat", name = "notificacion")
@Data
@EqualsAndHashCode(callSuper = false)
public class Notificacion extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 100, message = "El usuarioId no debe exceder 100 caracteres")
    @Column(name = "usuario_id", nullable = true, length = 100)
    private String usuarioId;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no debe exceder 255 caracteres")
    @Column(nullable = false, length = 255)
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 1000, message = "El mensaje no debe exceder 1000 caracteres")
    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Size(max = 50, message = "El tipo no debe exceder 50 caracteres")
    @Column(length = 50)
    private String tipo;

    @Size(max = 50, message = "El tipo de audiencia no debe exceder 50 caracteres")
    @Column(name = "tipo_audiencia", length = 50)
    private String tipoAudiencia;

    @Size(max = 500, message = "El target de audiencia no debe exceder 500 caracteres")
    @Column(name = "target_audiencia", length = 500)
    private String targetAudiencia;

    @Size(max = 100, message = "El origen no debe exceder 100 caracteres")
    @Column(length = 100)
    private String origen;

    @Size(max = 255, message = "El target/url no debe exceder 255 caracteres")
    @Column(length = 255)
    private String target;

    @Column(name = "datos_json", columnDefinition = "varchar(max)")
    private String datosJson;

    @Column(name = "es_leida", nullable = false)
    private Boolean leida = false;

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo = true;
}

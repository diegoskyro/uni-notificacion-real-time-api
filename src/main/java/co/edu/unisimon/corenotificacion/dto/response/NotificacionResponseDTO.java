package co.edu.unisimon.corenotificacion.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import co.edu.unisimon.corenotificacion.entity.Notificacion;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificacionResponseDTO {
    private Integer id;
    private UUID uuid;
    private String usuarioId;
    private String titulo;
    private String mensaje;
    private String tipo;
    private String tipoAudiencia;
    private String targetAudiencia;
    private String origen;
    private String target;
    private String datosJson;
    private Boolean leida = false;
    private LocalDateTime fechaCreacion;

    public NotificacionResponseDTO(Notificacion entidad) {
        if (entidad != null) {
            this.id = entidad.getId();
            this.uuid = entidad.getUuid();
            this.usuarioId = entidad.getUsuarioId();
            this.titulo = entidad.getTitulo();
            this.mensaje = entidad.getMensaje();
            this.tipo = entidad.getTipo();
            this.tipoAudiencia = entidad.getTipoAudiencia();
            this.targetAudiencia = entidad.getTargetAudiencia();
            this.origen = entidad.getOrigen();
            this.target = entidad.getTarget();
            this.datosJson = entidad.getDatosJson();
            this.leida = entidad.getLeida() != null ? entidad.getLeida() : false;
            this.fechaCreacion = entidad.getFechaCreacion();
        }
    }
}

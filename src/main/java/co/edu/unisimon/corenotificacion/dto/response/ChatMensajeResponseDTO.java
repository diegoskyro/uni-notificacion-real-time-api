package co.edu.unisimon.corenotificacion.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import co.edu.unisimon.corenotificacion.entity.ChatHistorial;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatMensajeResponseDTO {

    private Integer id;
    private UUID uuid;
    private UUID conversacionUuid;
    private UUID emisorUuid;
    private UUID receptorUuid;
    private String contenido;
    private String tipoEmisor;
    private Boolean esActivo;
    private LocalDateTime fechaCreacion;

    public ChatMensajeResponseDTO(ChatHistorial entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.uuid = entity.getUuid();
            this.conversacionUuid = entity.getConversacionUuid();
            this.emisorUuid = entity.getEmisorUuid();
            this.receptorUuid = entity.getReceptorUuid();
            this.contenido = entity.getContenido();
            this.tipoEmisor = entity.getTipoEmisor();
            this.esActivo = entity.getEsActivo();
            this.fechaCreacion = entity.getFechaCreacion();
        }
    }
}

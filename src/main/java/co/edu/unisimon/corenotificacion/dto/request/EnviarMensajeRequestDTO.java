package co.edu.unisimon.corenotificacion.dto.request;

import java.util.UUID;
import lombok.Data;

@Data
public class EnviarMensajeRequestDTO {

    private UUID receptorUuid;
    private String contenido;
    private String tipoEmisor = "SOLICITANTE";
}

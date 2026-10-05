package co.edu.unisimon.corenotificacion.dto.request;

import co.edu.unisimon.corenotificacion.dto.external.ExternalMailerVariablesDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalMailerRequestDTO {
    private String to;
    private String subject;
    private String htmlBody;
    private String template;
    private ExternalMailerVariablesDTO variables;
}

package co.edu.unisimon.corenotificacion.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalMailerVariablesDTO {
    private String name;
    private String message;
}

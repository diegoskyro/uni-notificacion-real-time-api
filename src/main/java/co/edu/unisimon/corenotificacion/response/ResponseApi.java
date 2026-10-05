package co.edu.unisimon.corenotificacion.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseApi<T> {
    private String mensaje;
    private int status;
    private T data;
}

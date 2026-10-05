package co.edu.unisimon.corenotificacion.service;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import co.edu.unisimon.corenotificacion.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExternalServiceHandler {
    public <T> T execute(String serviceName, Supplier<T> action) {
        try {
            return action.get();
        } catch (HttpStatusCodeException e) {
            log.error("Error crítico en la comunicación con el servicio externo [{}] ({}): {}",
                    serviceName, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new BusinessException(
                    "Error crítico en la comunicación con el servicio externo: " + serviceName +
                            " (" + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            log.error("Error crítico en la comunicación con el servicio externo [{}]: {}",
                    serviceName, e.getMessage());
            throw new BusinessException(
                    "Error crítico en la comunicación con el servicio externo: " + serviceName);
        }
    }
}

package co.edu.unisimon.corenotificacion.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/status")
public class StatusController {

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping
    public Map<String, Object> getStatus() {
        log.info("Consulta de estado de la API recibida");
        Map<String, Object> status = new HashMap<>();
        status.put("service", applicationName);
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        return status;
    }

    @GetMapping("/error-test")
    public String error() {
        throw new RuntimeException("Error de prueba GlitchTip - " + applicationName);
    }
}

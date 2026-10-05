package co.edu.unisimon.corenotificacion.service;

import co.edu.unisimon.corenotificacion.dto.external.ExternalMailerVariablesDTO;
import co.edu.unisimon.corenotificacion.dto.request.ExternalMailerRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final RestTemplate restTemplate;
    private final ExternalServiceHandler externalServiceHandler;

    @Value("${mailer.url}")
    private String mailerUrl;

    public void enviarCorreo(String destino, String destinoNombre, String asunto, String mensaje) {
        ExternalMailerVariablesDTO variables = ExternalMailerVariablesDTO.builder()
                .name(destinoNombre)
                .message(mensaje)
                .build();

        ExternalMailerRequestDTO request = ExternalMailerRequestDTO.builder()
                .to(destino)
                .subject(asunto)
                .htmlBody("")
                .template("templatecucuta")
                .variables(variables)
                .build();

        try {
            externalServiceHandler.execute("email", () -> {
                restTemplate.postForObject(mailerUrl, request, String.class);
                log.info("Correo enviado exitosamente a: {}", destino);
                return null;
            });
        } catch (Exception e) {
            log.error("El envío de correo falló pero el flujo continuará: {}", e.getMessage());
        }
    }
}

package co.edu.unisimon.corenotificacion.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import co.edu.unisimon.corenotificacion.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchivoExternoService {

    private final RestTemplate restTemplate;
    private final ExternalServiceHandler externalServiceHandler;

    @Value("${app.file-service.url}")
    private String fileServiceUrl;

    @Value("${app.file-service.token}")
    private String fileServiceToken;

    public ResponseEntity<byte[]> obtenerArchivo(UUID uuid) {
        String url = fileServiceUrl + "/get/" + uuid.toString();
        log.info("Consumiendo servicio de archivos externo (GET): {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(fileServiceToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return externalServiceHandler.execute("archivo-get",
                () -> restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class));
    }

    public ResponseEntity<byte[]> verArchivo(UUID uuid) {
        String url = fileServiceUrl + "/view/" + uuid.toString();
        log.info("Consumiendo servicio de archivos externo (VIEW): {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(fileServiceToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return externalServiceHandler.execute("archivo-view",
                () -> restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class));
    }

    public String subirArchivo(String base64, String filename, String path) {
        String url = fileServiceUrl + "/upload";
        log.info("Consumiendo servicio de archivos externo (UPLOAD JSON): {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(fileServiceToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "file", base64,
                "filename", filename,
                "path", path);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        return externalServiceHandler.execute("archivo-upload", () -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, requestEntity, Map.class);

            if (response != null && response.get("data") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String externalUuid = (String) data.get("uuid");
                log.info("Archivo cargado exitosamente. UUID externo: {}", externalUuid);
                return externalUuid;
            }

            throw new BusinessException("Respuesta inválida del servicio de archivos");
        });
    }

    public void eliminarArchivo(UUID uuid) {
        String url = fileServiceUrl + "/delete/" + uuid.toString();
        log.info("Consumiendo servicio de archivos externo (DELETE): {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(fileServiceToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        externalServiceHandler.execute("archivo-delete", () -> {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return null;
        });
    }
}

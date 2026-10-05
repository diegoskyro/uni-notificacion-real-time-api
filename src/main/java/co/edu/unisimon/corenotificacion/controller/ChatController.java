package co.edu.unisimon.corenotificacion.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unisimon.corenotificacion.dto.request.EnviarMensajeRequestDTO;
import co.edu.unisimon.corenotificacion.dto.response.ChatMensajeResponseDTO;
import co.edu.unisimon.corenotificacion.response.ResponseApi;
import co.edu.unisimon.corenotificacion.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private UUID obtenerUsuarioUuid(Authentication authentication, String headerUserUuid) {
        if (headerUserUuid != null && !headerUserUuid.isBlank()) {
            try {
                return UUID.fromString(headerUserUuid);
            } catch (Exception ignored) {
            }
        }
        if (authentication != null && authentication.getName() != null) {
            try {
                return UUID.fromString(authentication.getName());
            } catch (Exception ignored) {
            }
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    @PostMapping("/{conversacionUuid}/mensajes")
    public ResponseEntity<ResponseApi<ChatMensajeResponseDTO>> enviarMensaje(
            @PathVariable UUID conversacionUuid,
            @RequestBody EnviarMensajeRequestDTO request,
            @RequestHeader(name = "X-User-Uuid", required = false) String headerUserUuid,
            Authentication authentication) {
        
        UUID emisorUuid = obtenerUsuarioUuid(authentication, headerUserUuid);
        ChatMensajeResponseDTO dto = chatService.guardarYEnviarMensaje(conversacionUuid, emisorUuid, request);
        return ResponseEntity.ok(new ResponseApi<>("Mensaje enviado con éxito", HttpStatus.OK.value(), dto));
    }

    @GetMapping("/{conversacionUuid}/mensajes")
    public ResponseEntity<ResponseApi<Page<ChatMensajeResponseDTO>>> obtenerHistorialMensajes(
            @PathVariable UUID conversacionUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ChatMensajeResponseDTO> historial = chatService.listarMensajesHistorial(conversacionUuid, PageRequest.of(page, size));
        return ResponseEntity.ok(new ResponseApi<>("Historial de mensajes obtenido con éxito", HttpStatus.OK.value(), historial));
    }
}

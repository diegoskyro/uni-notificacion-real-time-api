package co.edu.unisimon.corenotificacion.controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import co.edu.unisimon.corenotificacion.dto.request.EnviarMensajeRequestDTO;
import co.edu.unisimon.corenotificacion.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.enviar/{chatUuid}")
    public void enviarMensajeStomp(
            @DestinationVariable UUID chatUuid,
            @Payload EnviarMensajeRequestDTO request,
            Principal principal) {

        UUID emisorUuid;
        try {
            emisorUuid = (principal != null && principal.getName() != null) 
                ? UUID.fromString(principal.getName()) 
                : UUID.fromString("00000000-0000-0000-0000-000000000001");
        } catch (Exception e) {
            emisorUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }

        log.info("Mensaje recibido por STOMP para conversación {}: {}", chatUuid, request.getContenido());
        chatService.guardarYEnviarMensaje(chatUuid, emisorUuid, request);
    }

    @MessageMapping("/chat.escribiendo/{chatUuid}")
    public void notificarEscribiendo(
            @DestinationVariable UUID chatUuid,
            @Payload Map<String, Object> payload,
            Principal principal) {

        String destino = "/topic/chat.directo." + chatUuid;
        log.debug("Notificación de escritura recibida para chat {}", chatUuid);
        messagingTemplate.convertAndSend(destino, (Object) Map.of(
            "evento", "ESCRIBIENDO",
            "tipoEmisor", payload.getOrDefault("tipoEmisor", "SOLICITANTE"),
            "emisorUuid", payload.getOrDefault("emisorUuid", ""),
            "chatUuid", chatUuid.toString()
        ));
    }

    @MessageMapping("/chat.enlinea/{chatUuid}")
    public void notificarEnLinea(
            @DestinationVariable UUID chatUuid,
            @Payload Map<String, Object> payload,
            Principal principal) {

        String destino = "/topic/chat.directo." + chatUuid;
        log.info("Notificación de presencia EN_LINEA para chat {}", chatUuid);
        messagingTemplate.convertAndSend(destino, (Object) Map.of(
            "evento", "EN_LINEA",
            "tipoEmisor", payload.getOrDefault("tipoEmisor", "SOLICITANTE"),
            "emisorUuid", payload.getOrDefault("emisorUuid", ""),
            "enLinea", payload.getOrDefault("enLinea", true),
            "chatUuid", chatUuid.toString()
        ));
    }

    @MessageMapping("/chat.visto/{chatUuid}")
    public void notificarVisto(
            @DestinationVariable UUID chatUuid,
            @Payload Map<String, Object> payload,
            Principal principal) {

        String destino = "/topic/chat.directo." + chatUuid;
        log.info("Notificación de lectura VISTO para mensaje en chat {}", chatUuid);
        messagingTemplate.convertAndSend(destino, (Object) Map.of(
            "evento", "VISTO",
            "tipoEmisor", payload.getOrDefault("tipoEmisor", "SOLICITANTE"),
            "emisorUuid", payload.getOrDefault("emisorUuid", ""),
            "mensajeUuid", payload.getOrDefault("mensajeUuid", ""),
            "chatUuid", chatUuid.toString()
        ));
    }

    @MessageMapping("/chat.evento/{chatUuid}")
    public void notificarEventoGenerico(
            @DestinationVariable UUID chatUuid,
            @Payload Map<String, Object> payload,
            Principal principal) {

        String destino = "/topic/chat.directo." + chatUuid;
        log.info("Evento genérico de chat transmitido: {}", payload);
        messagingTemplate.convertAndSend(destino, (Object) payload);
    }
}

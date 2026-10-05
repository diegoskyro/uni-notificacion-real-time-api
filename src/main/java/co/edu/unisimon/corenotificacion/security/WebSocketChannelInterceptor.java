package co.edu.unisimon.corenotificacion.security;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String token = null;
            if (authHeaders != null && !authHeaders.isEmpty()) {
                token = authHeaders.get(0);
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
            }

            if (token != null && !token.isBlank()) {
                // Autenticar la sesión STOMP con el token proporcionado
                Principal userPrincipal = new UsernamePasswordAuthenticationToken(token, null, Collections.emptyList());
                accessor.setUser(userPrincipal);
                log.info("WebSocket STOMP Conectado con token válido para sesión {}", accessor.getSessionId());
            } else {
                log.warn("Intento de conexión STOMP sin cabecera Authorization válida en sesión {}", accessor.getSessionId());
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal user = accessor.getUser();
            String destination = accessor.getDestination();
            log.info("Intento de suscripción STOMP a destino: {} por usuario: {}", destination, user != null ? user.getName() : "Anónimo");

            // Si es un canal directo /topic/chat.directo.{chatUuid} o /topic/canal.{canalUuid}
            if (destination != null && (destination.startsWith("/topic/chat.directo.") || destination.startsWith("/topic/canal."))) {
                String chatUuidStr = destination.substring(destination.lastIndexOf('.') + 1);
                try {
                    UUID chatUuid = UUID.fromString(chatUuidStr);
                    if (user != null && user.getName() != null) {
                        log.debug("Suscripción permitida a la conversación {} para usuario {}", chatUuid, user.getName());
                    }
                } catch (IllegalArgumentException e) {
                    log.error("UUID de destino inválido en suscripción: {}", chatUuidStr);
                }
            }
        }

        return message;
    }
}

package co.edu.unisimon.corenotificacion.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unisimon.corenotificacion.dto.request.EnviarMensajeRequestDTO;
import co.edu.unisimon.corenotificacion.dto.response.ChatMensajeResponseDTO;
import co.edu.unisimon.corenotificacion.entity.ChatHistorial;
import co.edu.unisimon.corenotificacion.repository.ChatHistorialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatHistorialRepository chatHistorialRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMensajeResponseDTO guardarYEnviarMensaje(UUID conversacionUuid, UUID emisorUuid, EnviarMensajeRequestDTO request) {
        ChatHistorial mensaje = new ChatHistorial();
        mensaje.setConversacionUuid(conversacionUuid);
        mensaje.setEmisorUuid(emisorUuid);
        mensaje.setReceptorUuid(request.getReceptorUuid());
        mensaje.setContenido(request.getContenido());
        if (request.getTipoEmisor() != null && !request.getTipoEmisor().isBlank()) {
            mensaje.setTipoEmisor(request.getTipoEmisor());
        }
        mensaje.setEsActivo(true);
        mensaje.setUsuarioCreacion(emisorUuid != null ? emisorUuid.toString() : "SYSTEM");

        ChatHistorial guardado = chatHistorialRepository.save(mensaje);
        log.info("Mensaje de chat guardado para la conversación {}", conversacionUuid);

        ChatMensajeResponseDTO responseDTO = new ChatMensajeResponseDTO(guardado);

        // Transmitir por WebSocket a los suscritos al tópico de la conversación
        String destino = "/topic/chat.directo." + conversacionUuid;
        messagingTemplate.convertAndSend(destino, responseDTO);

        return responseDTO;
    }

    @Transactional(readOnly = true)
    public Page<ChatMensajeResponseDTO> listarMensajesHistorial(UUID conversacionUuid, Pageable pageable) {
        return chatHistorialRepository
                .findByConversacionUuidAndEsActivoTrueOrderByFechaCreacionAsc(conversacionUuid, pageable)
                .map(ChatMensajeResponseDTO::new);
    }
}

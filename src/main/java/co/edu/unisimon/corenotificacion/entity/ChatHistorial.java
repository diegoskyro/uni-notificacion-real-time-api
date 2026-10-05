package co.edu.unisimon.corenotificacion.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(schema = "themis_chat", name = "chat_historial")
@Data
@EqualsAndHashCode(callSuper = false)
public class ChatHistorial extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "conversacion_uuid", nullable = false)
    private UUID conversacionUuid;

    @NotNull
    @Column(name = "emisor_uuid", nullable = false)
    private UUID emisorUuid;

    @Column(name = "receptor_uuid")
    private UUID receptorUuid;

    @NotBlank
    @Column(name = "contenido", nullable = false, columnDefinition = "VARCHAR(MAX)")
    private String contenido;

    @Column(name = "tipo_emisor", nullable = false, length = 20)
    private String tipoEmisor = "SOLICITANTE"; // "SOLICITANTE", "AGENTE", "SISTEMA"

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo = true;
}

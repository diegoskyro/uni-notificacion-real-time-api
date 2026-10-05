package co.edu.unisimon.corenotificacion.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(schema = "themis_chat", name = "notificacion_lectura")
@Data
@EqualsAndHashCode(callSuper = false)
public class NotificacionLectura extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notificacion_id", nullable = false)
    private Notificacion notificacion;

    @NotBlank(message = "El usuarioId es obligatorio")
    @Size(max = 100, message = "El usuarioId no debe exceder 100 caracteres")
    @Column(name = "usuario_id", nullable = false, length = 100)
    private String usuarioId;

    @Column(name = "fecha_lectura", nullable = false)
    private LocalDateTime fechaLectura = LocalDateTime.now();

    @Column(name = "es_activo", nullable = false)
    private Boolean esActivo = true;
}

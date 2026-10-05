package co.edu.unisimon.corenotificacion.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Data;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class Auditoria {

	@CreatedBy
	@Column(name = "usuario_creacion", updatable = false)
	private String usuarioCreacion;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@CreatedDate
	@Column(name = "fecha_creacion", updatable = false)
	private LocalDateTime fechaCreacion;

	@LastModifiedBy
	@Column(name = "usuario_actualizacion")
	private String usuarioActualizacion;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@LastModifiedDate
	@Column(name = "fecha_actualizacion")
	private LocalDateTime fechaActualizacion;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "uuid", updatable = false, unique = true)
	private UUID uuid;

	@PrePersist
	public void generarUuid() {
		if (uuid == null) {
			uuid = UUID.randomUUID();
		}
	}

}

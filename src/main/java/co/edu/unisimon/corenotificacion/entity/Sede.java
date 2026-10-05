package co.edu.unisimon.corenotificacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(schema = "core", name = "sede")
@Data
@EqualsAndHashCode(callSuper = false)
public class Sede extends Auditoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
	@Column(nullable = false)
	private String nombre;

	@Size(max = 255, message = "La descripción no debe exceder los 255 caracteres")
	@Column
	private String descripcion;

	@Column(name = "es_activo", nullable = false)
	private Boolean esActivo = true;

	@Column(name = "universidad_id", nullable = false)
	private Integer universidadId;

}

package co.edu.unisimon.corenotificacion.dto.response;

import java.util.UUID;
import co.edu.unisimon.corenotificacion.entity.Sede;
import lombok.Data;

@Data
public class SedeResponseDTO {

	private Integer id;
	private UUID uuid;
	private String nombre;
	private String descripcion;
	private Boolean esActivo;

	public SedeResponseDTO(Sede sede) {
		this.id = sede.getId();
		this.uuid = sede.getUuid();
		this.nombre = sede.getNombre();
		this.descripcion = sede.getDescripcion();
		this.esActivo = sede.getEsActivo();
	}
}

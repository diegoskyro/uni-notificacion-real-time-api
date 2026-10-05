package co.edu.unisimon.corenotificacion.exception;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String recurso, Object id) {
		super(recurso + " no encontrado con identificador: " + id);
	}
}


package co.edu.unisimon.corenotificacion.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import co.edu.unisimon.corenotificacion.dto.response.SedeResponseDTO;
import co.edu.unisimon.corenotificacion.entity.Sede;
import co.edu.unisimon.corenotificacion.exception.ResourceNotFoundException;
import co.edu.unisimon.corenotificacion.exception.BusinessException;
import co.edu.unisimon.corenotificacion.repository.SedeRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SedeService {

	private final SedeRepository sedeRepository;

	public SedeResponseDTO guardar(Sede request) {
		if (sedeRepository.existsByNombreIgnoreCase(request.getNombre())) {
			throw new BusinessException("Ya existe una sede con ese nombre");
		}
		request.setEsActivo(true);
		request.setUniversidadId(1);
		Sede guardado = sedeRepository.save(request);
		return new SedeResponseDTO(guardado);
	}

	@Transactional(readOnly = true)
	public List<SedeResponseDTO> listar() {
		return sedeRepository.findAll().stream().map(SedeResponseDTO::new).toList();
	}

	@Transactional(readOnly = true)
	public SedeResponseDTO get(UUID uuid) {
		Sede sede = sedeRepository.findByUuid(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Sede", uuid));
		return new SedeResponseDTO(sede);
	}

	public SedeResponseDTO actualizar(UUID uuid, Sede request) {
		Sede actual = sedeRepository.findByUuid(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Sede", uuid));

		String nuevoNombre = request.getNombre().trim();
		if (!actual.getNombre().equalsIgnoreCase(nuevoNombre)
				&& sedeRepository.existsByNombreIgnoreCase(nuevoNombre)) {
			throw new BusinessException("Ya existe una sede con ese nombre");
		}

		actual.setNombre(nuevoNombre);
		actual.setDescripcion(request.getDescripcion());
		actual.setEsActivo(request.getEsActivo());

		Sede actualizado = sedeRepository.save(actual);
		return new SedeResponseDTO(actualizado);
	}

	public void eliminar(UUID uuid) {
		Sede sede = sedeRepository.findByUuid(uuid)
				.orElseThrow(() -> new ResourceNotFoundException("Sede", uuid));
		sedeRepository.delete(sede);
	}
}

package co.edu.unisimon.corenotificacion.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import co.edu.unisimon.corenotificacion.dto.response.SedeResponseDTO;
import co.edu.unisimon.corenotificacion.response.ResponseApi;
import co.edu.unisimon.corenotificacion.service.SedeService;
import co.edu.unisimon.corenotificacion.entity.Sede;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("sedes")
@RequiredArgsConstructor
public class SedeController {

	private final SedeService sedeService;

	@PostMapping
	// @PreAuthorize("@authz.tienePermisoSede('CREAR_SEDE')")
	public ResponseEntity<ResponseApi<SedeResponseDTO>> guardar(@Valid @RequestBody Sede request) {
		SedeResponseDTO dto = sedeService.guardar(request);
		return ResponseEntity
				.ok(new ResponseApi<>("Sede creada correctamente", HttpStatus.OK.value(), dto));
	}

	@GetMapping
	// @PreAuthorize("@authz.tienePermisoSede('LISTAR_SEDE')")
	public ResponseEntity<ResponseApi<List<SedeResponseDTO>>> listar() {
		return ResponseEntity.ok(
				new ResponseApi<>("Listado obtenido correctamente", HttpStatus.OK.value(), sedeService.listar()));
	}

	@GetMapping("/{uuid}")
	// @PreAuthorize("@authz.tienePermisoSede('LISTAR_SEDE')")
	public ResponseEntity<ResponseApi<SedeResponseDTO>> get(@PathVariable UUID uuid) {
		return ResponseEntity
				.ok(new ResponseApi<>("Sede encontrada", HttpStatus.OK.value(), sedeService.get(uuid)));
	}

	@PutMapping("/{uuid}")
	// @PreAuthorize("@authz.tienePermisoSede('ACTUALIZAR_SEDE')")
	public ResponseEntity<ResponseApi<SedeResponseDTO>> actualizar(@PathVariable UUID uuid,
			@Valid @RequestBody Sede request) {
		SedeResponseDTO dto = sedeService.actualizar(uuid, request);
		return ResponseEntity
				.ok(new ResponseApi<>("Sede actualizada correctamente", HttpStatus.OK.value(), dto));
	}

	@DeleteMapping("/{uuid}")
	// @PreAuthorize("@authz.tienePermisoSede('ELIMINAR_SEDE')")
	public ResponseEntity<ResponseApi<Void>> eliminar(@PathVariable UUID uuid) {
		sedeService.eliminar(uuid);
		return ResponseEntity.ok(
				new ResponseApi<>("Sede eliminada correctamente", HttpStatus.OK.value(), null));
	}
}

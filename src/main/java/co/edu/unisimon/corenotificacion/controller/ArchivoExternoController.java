package co.edu.unisimon.corenotificacion.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import co.edu.unisimon.corenotificacion.service.ArchivoExternoService;

@RestController
@RequestMapping("archivo")
@RequiredArgsConstructor
public class ArchivoExternoController {

    private final ArchivoExternoService archivoExternoService;

    @GetMapping("/get/{uuid}")
    public ResponseEntity<byte[]> get(@PathVariable UUID uuid) {
        return archivoExternoService.obtenerArchivo(uuid);
    }

    @GetMapping("/view/{uuid}")
    public ResponseEntity<byte[]> view(@PathVariable UUID uuid) {
        return archivoExternoService.verArchivo(uuid);
    }
}

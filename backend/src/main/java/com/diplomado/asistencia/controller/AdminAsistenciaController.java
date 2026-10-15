package com.diplomado.asistencia.controller;

import com.diplomado.asistencia.dto.ApiResponse;
import com.diplomado.asistencia.dto.MatrizAsistenciaDTO;
import com.diplomado.asistencia.dto.ModificarAsistenciaRequest;
import com.diplomado.asistencia.entity.Asistencia;
import com.diplomado.asistencia.service.AsistenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminAsistenciaController {

    private final AsistenciaService asistenciaService;

    public AdminAsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/modulos/{moduloId}/matriz")
    public ResponseEntity<ApiResponse<MatrizAsistenciaDTO>> obtenerMatriz(
            @PathVariable Long moduloId,
            @RequestParam(required = false) Long fechaId,
            @RequestParam(required = false) String q) {
        MatrizAsistenciaDTO matriz = asistenciaService.obtenerMatrizAsistencia(moduloId, fechaId, q);
        return ResponseEntity.ok(ApiResponse.ok(matriz));
    }

    @PutMapping("/asistencias/modificar")
    public ResponseEntity<ApiResponse<Asistencia>> modificarAsistencia(@Valid @RequestBody ModificarAsistenciaRequest request) {
        Asistencia mod = asistenciaService.modificarAsistencia(request);
        return ResponseEntity.ok(ApiResponse.ok("Asistencia modificada y auditada correctamente.", mod));
    }

    @PostMapping("/modulos/{moduloId}/sincronizar")
    public ResponseEntity<ApiResponse<String>> sincronizar(@PathVariable Long moduloId) {
        asistenciaService.sincronizarAsistenciasModulo(moduloId);
        return ResponseEntity.ok(ApiResponse.ok("Asistencias sincronizadas correctamente"));
    }
}

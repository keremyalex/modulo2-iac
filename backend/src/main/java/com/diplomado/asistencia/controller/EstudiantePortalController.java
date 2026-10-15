package com.diplomado.asistencia.controller;

import com.diplomado.asistencia.dto.ApiResponse;
import com.diplomado.asistencia.dto.MarcarAsistenciaRequest;
import com.diplomado.asistencia.dto.ResumenAsistenciaEstudianteDTO;
import com.diplomado.asistencia.entity.Asistencia;
import com.diplomado.asistencia.service.AsistenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/estudiante")
public class EstudiantePortalController {

    private final AsistenciaService asistenciaService;

    public EstudiantePortalController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping("/{estudianteId}/resumen")
    public ResponseEntity<ApiResponse<ResumenAsistenciaEstudianteDTO>> obtenerResumen(@PathVariable Long estudianteId) {
        ResumenAsistenciaEstudianteDTO resumen = asistenciaService.obtenerResumenEstudiante(estudianteId);
        return ResponseEntity.ok(ApiResponse.ok(resumen));
    }

    @PostMapping("/marcar")
    public ResponseEntity<ApiResponse<Asistencia>> marcarAsistencia(@Valid @RequestBody MarcarAsistenciaRequest request) {
        Asistencia asistencia = asistenciaService.marcarAsistencia(request.getEstudianteId());
        String horaStr = asistencia.getHoraMarcaje() != null ? asistencia.getHoraMarcaje().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";
        return ResponseEntity.ok(ApiResponse.ok("¡Asistencia registrada con éxito a las " + horaStr + "!", asistencia));
    }
}

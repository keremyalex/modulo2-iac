package com.diplomado.asistencia.controller;

import com.diplomado.asistencia.dto.ApiResponse;
import com.diplomado.asistencia.entity.Actividad;
import com.diplomado.asistencia.entity.AuditoriaAsistencia;
import com.diplomado.asistencia.service.ActividadService;
import com.diplomado.asistencia.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminAuditoriaController {

    private final AuditoriaService auditoriaService;
    private final ActividadService actividadService;

    public AdminAuditoriaController(AuditoriaService auditoriaService, ActividadService actividadService) {
        this.auditoriaService = auditoriaService;
        this.actividadService = actividadService;
    }

    @GetMapping("/modulos/{moduloId}/auditorias")
    public ResponseEntity<ApiResponse<List<AuditoriaAsistencia>>> listarAuditoriasPorModulo(@PathVariable Long moduloId) {
        List<AuditoriaAsistencia> lista = auditoriaService.listarPorModulo(moduloId);
        return ResponseEntity.ok(ApiResponse.ok(lista));
    }

    @GetMapping("/auditorias")
    public ResponseEntity<ApiResponse<List<AuditoriaAsistencia>>> listarTodasAuditorias() {
        List<AuditoriaAsistencia> lista = auditoriaService.listarTodas();
        return ResponseEntity.ok(ApiResponse.ok(lista));
    }

    @GetMapping("/modulos/{moduloId}/actividades")
    public ResponseEntity<ApiResponse<List<Actividad>>> listarActividadesPorModulo(@PathVariable Long moduloId) {
        List<Actividad> lista = actividadService.listarPorModulo(moduloId);
        return ResponseEntity.ok(ApiResponse.ok(lista));
    }

    @GetMapping("/actividades")
    public ResponseEntity<ApiResponse<List<Actividad>>> listarTodasActividades() {
        List<Actividad> lista = actividadService.listarTodas();
        return ResponseEntity.ok(ApiResponse.ok(lista));
    }
}

package com.diplomado.asistencia.controller;

import com.diplomado.asistencia.dto.ApiResponse;
import com.diplomado.asistencia.entity.Estudiante;
import com.diplomado.asistencia.entity.Modulo;
import com.diplomado.asistencia.service.AsistenciaService;
import com.diplomado.asistencia.service.EstudianteService;
import com.diplomado.asistencia.service.ModuloService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/modulos/{moduloId}/estudiantes")
public class AdminEstudianteController {

    private final EstudianteService estudianteService;
    private final ModuloService moduloService;
    private final AsistenciaService asistenciaService;

    public AdminEstudianteController(EstudianteService estudianteService, ModuloService moduloService, AsistenciaService asistenciaService) {
        this.estudianteService = estudianteService;
        this.moduloService = moduloService;
        this.asistenciaService = asistenciaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Estudiante>>> listarEstudiantes(
            @PathVariable Long moduloId,
            @RequestParam(required = false) String q) {
        List<Estudiante> lista = estudianteService.buscar(moduloId, q);
        return ResponseEntity.ok(ApiResponse.ok(lista));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Estudiante>> registrarEstudiante(
            @PathVariable Long moduloId,
            @Valid @RequestBody Estudiante estudiante) {
        Modulo modulo = moduloService.buscarPorId(moduloId);
        Estudiante guardado = estudianteService.registrarEstudiante(estudiante, modulo);
        asistenciaService.sincronizarAsistenciasModulo(moduloId);
        return ResponseEntity.ok(ApiResponse.ok("Estudiante registrado exitosamente", guardado));
    }
}

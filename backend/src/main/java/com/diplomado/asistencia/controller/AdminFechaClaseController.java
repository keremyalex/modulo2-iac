package com.diplomado.asistencia.controller;

import com.diplomado.asistencia.dto.ApiResponse;
import com.diplomado.asistencia.entity.FechaClase;
import com.diplomado.asistencia.entity.Modulo;
import com.diplomado.asistencia.service.AsistenciaService;
import com.diplomado.asistencia.service.FechaClaseService;
import com.diplomado.asistencia.service.ModuloService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/modulos/{moduloId}/fechas")
public class AdminFechaClaseController {

    private final FechaClaseService fechaClaseService;
    private final ModuloService moduloService;
    private final AsistenciaService asistenciaService;

    public AdminFechaClaseController(FechaClaseService fechaClaseService, ModuloService moduloService, AsistenciaService asistenciaService) {
        this.fechaClaseService = fechaClaseService;
        this.moduloService = moduloService;
        this.asistenciaService = asistenciaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FechaClase>>> listarFechas(@PathVariable Long moduloId) {
        List<FechaClase> fechas = fechaClaseService.listarPorModulo(moduloId);
        return ResponseEntity.ok(ApiResponse.ok(fechas));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FechaClase>> registrarFecha(
            @PathVariable Long moduloId,
            @Valid @RequestBody FechaClase fechaClase) {
        Modulo modulo = moduloService.buscarPorId(moduloId);
        FechaClase guardada = fechaClaseService.registrarFechaClase(fechaClase, modulo);
        asistenciaService.sincronizarAsistenciasModulo(moduloId);
        return ResponseEntity.ok(ApiResponse.ok("Fecha de clase registrada exitosamente", guardada));
    }
}

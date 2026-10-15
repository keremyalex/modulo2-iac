package com.diplomado.asistencia.controller;

import com.diplomado.asistencia.dto.ApiResponse;
import com.diplomado.asistencia.dto.DashboardStatsDTO;
import com.diplomado.asistencia.dto.NuevoModuloRequest;
import com.diplomado.asistencia.entity.Modulo;
import com.diplomado.asistencia.service.AsistenciaService;
import com.diplomado.asistencia.service.ModuloService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/modulos")
public class AdminModuloController {

    private final ModuloService moduloService;
    private final AsistenciaService asistenciaService;

    public AdminModuloController(ModuloService moduloService, AsistenciaService asistenciaService) {
        this.moduloService = moduloService;
        this.asistenciaService = asistenciaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Modulo>>> listarModulos() {
        return ResponseEntity.ok(ApiResponse.ok(moduloService.listarTodos()));
    }

    @GetMapping("/activo")
    public ResponseEntity<ApiResponse<Modulo>> obtenerActivo() {
        return moduloService.getModuloActivoOptional()
                .map(m -> ResponseEntity.ok(ApiResponse.ok(m)))
                .orElse(ResponseEntity.ok(ApiResponse.ok("No hay ningún módulo activo", null)));
    }

    @PostMapping("/activar-nuevo")
    public ResponseEntity<ApiResponse<Modulo>> activarNuevo(
            @Valid @RequestBody NuevoModuloRequest request,
            @RequestParam(defaultValue = "Administrador") String usuario) {
        Modulo nuevo = moduloService.activarNuevoModulo(request, usuario);
        return ResponseEntity.ok(ApiResponse.ok("Módulo '" + nuevo.getNombre() + "' activado exitosamente.", nuevo));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<Modulo>> activarExistente(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Administrador") String usuario) {
        Modulo activado = moduloService.cambiarModuloActivo(id, usuario);
        return ResponseEntity.ok(ApiResponse.ok("Módulo '" + activado.getNombre() + "' establecido como activo.", activado));
    }

    @PostMapping("/reiniciar")
    public ResponseEntity<ApiResponse<Modulo>> reiniciarModulo(
            @RequestBody(required = false) Map<String, String> body,
            @RequestParam(defaultValue = "Administrador") String usuario) {
        String motivo = (body != null) ? body.get("motivo") : "Reinicio operativo de ciclo";
        Modulo reiniciado = moduloService.reiniciarModulo(usuario, motivo);
        return ResponseEntity.ok(ApiResponse.ok("Módulo reiniciado exitosamente. Sus datos históricos se mantienen guardados.", reiniciado));
    }

    @GetMapping("/{moduloId}/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> obtenerDashboard(@PathVariable Long moduloId) {
        DashboardStatsDTO stats = asistenciaService.obtenerEstadisticasDashboard(moduloId);
        stats.setModuloActivo(moduloService.buscarPorId(moduloId));
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}

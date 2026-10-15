package com.diplomado.asistencia.controller;

import com.diplomado.asistencia.dto.ApiResponse;
import com.diplomado.asistencia.dto.PreviewExcelResponse;
import com.diplomado.asistencia.entity.Estudiante;
import com.diplomado.asistencia.entity.FechaClase;
import com.diplomado.asistencia.entity.Modulo;
import com.diplomado.asistencia.service.ExcelService;
import com.diplomado.asistencia.service.ModuloService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminExcelController {

    private final ExcelService excelService;
    private final ModuloService moduloService;

    public AdminExcelController(ExcelService excelService, ModuloService moduloService) {
        this.excelService = excelService;
        this.moduloService = moduloService;
    }

    /**
     * RF-16: Vista previa de datos de Excel de Estudiantes.
     */
    @PostMapping("/excel/preview-estudiantes")
    public ResponseEntity<ApiResponse<PreviewExcelResponse>> previewEstudiantes(@RequestParam("file") MultipartFile file) {
        PreviewExcelResponse preview = excelService.previsualizarEstudiantes(file);
        return ResponseEntity.ok(ApiResponse.ok("Vista previa generada exitosamente", preview));
    }

    /**
     * RF-16: Vista previa de datos de Excel de Fechas.
     */
    @PostMapping("/excel/preview-fechas")
    public ResponseEntity<ApiResponse<PreviewExcelResponse>> previewFechas(@RequestParam("file") MultipartFile file) {
        PreviewExcelResponse preview = excelService.previsualizarFechas(file);
        return ResponseEntity.ok(ApiResponse.ok("Vista previa generada exitosamente", preview));
    }

    /**
     * RF-11: Importación de estudiantes al módulo.
     */
    @PostMapping("/modulos/{moduloId}/importar-estudiantes")
    public ResponseEntity<ApiResponse<List<Estudiante>>> importarEstudiantes(
            @PathVariable Long moduloId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "Administrador") String usuario) {
        Modulo modulo = moduloService.buscarPorId(moduloId);
        List<Estudiante> guardados = excelService.importarEstudiantes(file, modulo, usuario);
        return ResponseEntity.ok(ApiResponse.ok("Se importaron " + guardados.size() + " estudiantes correctamente.", guardados));
    }

    /**
     * RF-12: Importación de fechas de clase al módulo.
     */
    @PostMapping("/modulos/{moduloId}/importar-fechas")
    public ResponseEntity<ApiResponse<List<FechaClase>>> importarFechas(
            @PathVariable Long moduloId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "Administrador") String usuario) {
        Modulo modulo = moduloService.buscarPorId(moduloId);
        List<FechaClase> guardadas = excelService.importarFechas(file, modulo, usuario);
        return ResponseEntity.ok(ApiResponse.ok("Se importaron " + guardadas.size() + " fechas de clase correctamente.", guardadas));
    }

    /**
     * RF-17: Exportación de las asistencias del módulo a Excel.
     */
    @GetMapping("/modulos/{moduloId}/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel(@PathVariable Long moduloId) {
        byte[] excelBytes = excelService.exportarAsistenciasExcel(moduloId);
        Modulo modulo = moduloService.buscarPorId(moduloId);
        String filename = "Asistencias_" + modulo.getNombre().replaceAll("[^a-zA-Z0-9.-]", "_") + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    /**
     * Descargar plantilla Excel de ejemplo para Estudiantes.
     */
    @GetMapping("/excel/plantilla-estudiantes")
    public ResponseEntity<byte[]> descargarPlantillaEstudiantes() {
        byte[] bytes = excelService.generarPlantillaEstudiantes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Plantilla_Estudiantes.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    /**
     * Descargar plantilla Excel de ejemplo para Fechas de Clase.
     */
    @GetMapping("/excel/plantilla-fechas")
    public ResponseEntity<byte[]> descargarPlantillaFechas() {
        byte[] bytes = excelService.generarPlantillaFechas();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Plantilla_Fechas.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}


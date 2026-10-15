package com.diplomado.asistencia.service;

import com.diplomado.asistencia.dto.FilaMatrizEstudianteDTO;
import com.diplomado.asistencia.dto.MatrizAsistenciaDTO;
import com.diplomado.asistencia.dto.PreviewExcelResponse;
import com.diplomado.asistencia.entity.*;
import com.diplomado.asistencia.exception.BusinessException;
import com.diplomado.asistencia.repository.ActividadRepository;
import com.diplomado.asistencia.repository.EstudianteRepository;
import com.diplomado.asistencia.repository.FechaClaseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelService {

    private final EstudianteRepository estudianteRepository;
    private final FechaClaseRepository fechaClaseRepository;
    private final ActividadRepository actividadRepository;
    private final AsistenciaService asistenciaService;

    public ExcelService(
            EstudianteRepository estudianteRepository,
            FechaClaseRepository fechaClaseRepository,
            ActividadRepository actividadRepository,
            AsistenciaService asistenciaService) {
        this.estudianteRepository = estudianteRepository;
        this.fechaClaseRepository = fechaClaseRepository;
        this.actividadRepository = actividadRepository;
        this.asistenciaService = asistenciaService;
    }

    /**
     * RF-16: Vista previa del archivo Excel de estudiantes.
     */
    public PreviewExcelResponse previsualizarEstudiantes(MultipartFile file) {
        validarArchivoExcel(file);
        PreviewExcelResponse preview = new PreviewExcelResponse();
        preview.setNombreArchivo(file.getOriginalFilename());
        preview.setTipo("ESTUDIANTES");

        List<Map<String, Object>> filasValidas = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        Set<String> registrosVistos = new HashSet<>();
        Set<String> correosVistos = new HashSet<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new BusinessException("El archivo Excel está vacío o no contiene filas de datos.");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("No se encontró la fila de encabezados en la primera fila de la hoja.");
            }

            Map<String, Integer> colIndex = mapearColumnasEstudiante(headerRow);
            int totalFilas = 0;

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || esFilaVacia(row)) {
                    continue;
                }
                totalFilas++;
                int filaNum = r + 1;

                String registro = getCellValueAsString(row.getCell(colIndex.get("registro")));
                String nombre = getCellValueAsString(row.getCell(colIndex.get("nombre")));
                String correo = getCellValueAsString(row.getCell(colIndex.get("correo")));

                List<String> erroresFila = new ArrayList<>();
                if (registro == null || registro.isBlank()) {
                    erroresFila.add("Fila " + filaNum + ": El número de registro es obligatorio.");
                } else if (!registrosVistos.add(registro.trim().toLowerCase())) {
                    erroresFila.add("Fila " + filaNum + ": Número de registro duplicado en el archivo ('" + registro + "').");
                }

                if (nombre == null || nombre.isBlank()) {
                    erroresFila.add("Fila " + filaNum + ": El nombre completo es obligatorio.");
                }

                if (correo == null || correo.isBlank()) {
                    erroresFila.add("Fila " + filaNum + ": El correo electrónico es obligatorio.");
                } else if (!correo.contains("@") || !correo.contains(".")) {
                    erroresFila.add("Fila " + filaNum + ": Formato de correo inválido ('" + correo + "').");
                } else if (!correosVistos.add(correo.trim().toLowerCase())) {
                    erroresFila.add("Fila " + filaNum + ": Correo duplicado en el archivo ('" + correo + "').");
                }

                Map<String, Object> filaData = new HashMap<>();
                filaData.put("fila", filaNum);
                filaData.put("numeroRegistro", registro != null ? registro.trim() : "");
                filaData.put("nombreCompleto", nombre != null ? nombre.trim() : "");
                filaData.put("correo", correo != null ? correo.trim().toLowerCase() : "");
                filaData.put("valida", erroresFila.isEmpty());
                filaData.put("errores", erroresFila);

                filasValidas.add(filaData);
                errores.addAll(erroresFila);
            }

            preview.setTotalFilas(totalFilas);
            preview.setFilasValidas((int) filasValidas.stream().filter(f -> (boolean) f.get("valida")).count());
            preview.setFilasConError(totalFilas - preview.getFilasValidas());
            preview.setDatos(filasValidas);
            preview.setErrores(errores);

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException("Error al procesar el archivo Excel: " + e.getMessage());
        }

        return preview;
    }

    /**
     * RF-11: Importación de estudiantes a partir de Excel con validación estricta y reporte de errores.
     */
    @Transactional
    public List<Estudiante> importarEstudiantes(MultipartFile file, Modulo modulo, String usuario) {
        PreviewExcelResponse preview = previsualizarEstudiantes(file);
        if (preview.getFilasConError() > 0) {
            throw new BusinessException("No se puede importar: Se encontraron " + preview.getFilasConError() +
                    " filas con errores en el archivo. Por favor revise el reporte de validación antes de confirmar.");
        }

        List<Estudiante> guardados = new ArrayList<>();
        for (Map<String, Object> item : preview.getDatos()) {
            String registro = (String) item.get("numeroRegistro");
            String nombre = (String) item.get("nombreCompleto");
            String correo = (String) item.get("correo");

            // RN-02: Unicidad en el módulo
            if (estudianteRepository.existsByModuloIdAndNumeroRegistroIgnoreCase(modulo.getId(), registro)) {
                throw new BusinessException("El número de registro '" + registro + "' ya existe en este módulo.");
            }
            if (estudianteRepository.existsByModuloIdAndCorreoIgnoreCase(modulo.getId(), correo)) {
                throw new BusinessException("El correo '" + correo + "' ya existe en este módulo.");
            }

            Estudiante estudiante = new Estudiante(registro, nombre, correo, modulo);
            guardados.add(estudiante);
        }

        List<Estudiante> resultado = estudianteRepository.saveAll(guardados);

        actividadRepository.save(new Actividad(
                usuario,
                "CARGA_EXCEL_ESTUDIANTES",
                "Se importaron " + resultado.size() + " estudiantes desde el archivo '" + file.getOriginalFilename() + "' para el módulo '" + modulo.getNombre() + "'.",
                modulo
        ));

        // Sincronizar asistencias con fechas existentes
        asistenciaService.sincronizarAsistenciasModulo(modulo.getId());

        return resultado;
    }

    /**
     * RF-16: Vista previa del archivo Excel de fechas de clase.
     */
    public PreviewExcelResponse previsualizarFechas(MultipartFile file) {
        validarArchivoExcel(file);
        PreviewExcelResponse preview = new PreviewExcelResponse();
        preview.setNombreArchivo(file.getOriginalFilename());
        preview.setTipo("FECHAS");

        List<Map<String, Object>> filasValidas = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        Set<LocalDate> fechasVistas = new HashSet<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new BusinessException("El archivo Excel está vacío o no contiene filas de fechas.");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("No se encontró la fila de encabezados en la primera fila de la hoja.");
            }

            Map<String, Integer> colIndex = mapearColumnasFechas(headerRow);
            int totalFilas = 0;

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || esFilaVacia(row)) {
                    continue;
                }
                totalFilas++;
                int filaNum = r + 1;

                Cell cellFecha = row.getCell(colIndex.get("fecha"));
                Cell cellInicio = row.getCell(colIndex.get("inicio"));
                Cell cellFin = row.getCell(colIndex.get("fin"));

                List<String> erroresFila = new ArrayList<>();
                LocalDate fecha = extraerLocalDate(cellFecha, filaNum, erroresFila);
                LocalTime inicio = extraerLocalTime(cellInicio, "Hora Inicio", filaNum, erroresFila);
                LocalTime fin = extraerLocalTime(cellFin, "Hora Fin", filaNum, erroresFila);

                if (fecha != null && !fechasVistas.add(fecha)) {
                    erroresFila.add("Fila " + filaNum + ": Fecha duplicada en el archivo ('" + fecha + "').");
                }

                if (inicio != null && fin != null && fin.isBefore(inicio)) {
                    erroresFila.add("Fila " + filaNum + ": La Hora Fin (" + fin + ") no puede ser anterior a la Hora Inicio (" + inicio + ").");
                }

                Map<String, Object> filaData = new HashMap<>();
                filaData.put("fila", filaNum);
                filaData.put("fecha", fecha != null ? fecha.toString() : "");
                filaData.put("horaInicio", inicio != null ? inicio.toString() : "");
                filaData.put("horaFin", fin != null ? fin.toString() : "");
                filaData.put("valida", erroresFila.isEmpty());
                filaData.put("errores", erroresFila);

                filasValidas.add(filaData);
                errores.addAll(erroresFila);
            }

            preview.setTotalFilas(totalFilas);
            preview.setFilasValidas((int) filasValidas.stream().filter(f -> (boolean) f.get("valida")).count());
            preview.setFilasConError(totalFilas - preview.getFilasValidas());
            preview.setDatos(filasValidas);
            preview.setErrores(errores);

        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException("Error al procesar el archivo de fechas: " + e.getMessage());
        }

        return preview;
    }

    /**
     * RF-12: Importación de fechas/horarios con validación de obligatoriedad y reporte de errores.
     */
    @Transactional
    public List<FechaClase> importarFechas(MultipartFile file, Modulo modulo, String usuario) {
        PreviewExcelResponse preview = previsualizarFechas(file);
        if (preview.getFilasConError() > 0) {
            throw new BusinessException("No se puede importar: Se encontraron " + preview.getFilasConError() +
                    " filas con errores en el archivo de fechas. Revise los detalles antes de continuar.");
        }

        List<FechaClase> guardadas = new ArrayList<>();
        for (Map<String, Object> item : preview.getDatos()) {
            LocalDate fecha = LocalDate.parse((String) item.get("fecha"));
            LocalTime inicio = LocalTime.parse((String) item.get("horaInicio"));
            LocalTime fin = LocalTime.parse((String) item.get("horaFin"));

            if (fechaClaseRepository.existsByModuloIdAndFecha(modulo.getId(), fecha)) {
                throw new BusinessException("La fecha '" + fecha + "' ya existe en este módulo.");
            }

            FechaClase fechaClase = new FechaClase(fecha, inicio, fin, modulo);
            guardadas.add(fechaClase);
        }

        List<FechaClase> resultado = fechaClaseRepository.saveAll(guardadas);

        actividadRepository.save(new Actividad(
                usuario,
                "CARGA_EXCEL_FECHAS",
                "Se importaron " + resultado.size() + " fechas de clase desde el archivo '" + file.getOriginalFilename() + "' para el módulo '" + modulo.getNombre() + "'.",
                modulo
        ));

        // Sincronizar asistencias
        asistenciaService.sincronizarAsistenciasModulo(modulo.getId());

        return resultado;
    }

    /**
     * RF-17: Exportación de las asistencias del módulo activo a Excel (.xlsx).
     */
    public byte[] exportarAsistenciasExcel(Long moduloId) {
        MatrizAsistenciaDTO matriz = asistenciaService.obtenerMatrizAsistencia(moduloId, null, null);
        Modulo modulo = matriz.getModulo();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Asistencias");

            // Estilos
            Font fontTitulo = workbook.createFont();
            fontTitulo.setBold(true);
            fontTitulo.setFontHeightInPoints((short) 14);
            fontTitulo.setColor(IndexedColors.DARK_BLUE.getIndex());

            CellStyle styleTitulo = workbook.createCellStyle();
            styleTitulo.setFont(fontTitulo);

            Font fontHeader = workbook.createFont();
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());

            CellStyle styleHeader = workbook.createCellStyle();
            styleHeader.setFont(fontHeader);
            styleHeader.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle stylePresente = workbook.createCellStyle();
            stylePresente.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            stylePresente.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            stylePresente.setAlignment(HorizontalAlignment.CENTER);

            CellStyle styleFalta = workbook.createCellStyle();
            styleFalta.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            styleFalta.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styleFalta.setAlignment(HorizontalAlignment.CENTER);

            CellStyle styleCenter = workbook.createCellStyle();
            styleCenter.setAlignment(HorizontalAlignment.CENTER);

            // Título
            Row rowTitulo = sheet.createRow(0);
            Cell cellT = rowTitulo.createCell(0);
            cellT.setCellValue("REPORTE OFICIAL DE ASISTENCIA - " + (modulo != null ? modulo.getNombre() : "MÓDULO ACTIVO"));
            cellT.setCellStyle(styleTitulo);

            Row rowFechaGen = sheet.createRow(1);
            rowFechaGen.createCell(0).setCellValue("Fecha de Generación: " + LocalDate.now() + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            // Cabeceras de tabla
            Row headerRow = sheet.createRow(3);
            int colIdx = 0;
            headerRow.createCell(colIdx++).setCellValue("N°");
            headerRow.createCell(colIdx++).setCellValue("N° Registro");
            headerRow.createCell(colIdx++).setCellValue("Nombre Completo");
            headerRow.createCell(colIdx++).setCellValue("Correo Electrónico");

            for (FechaClase f : matriz.getFechas()) {
                Cell c = headerRow.createCell(colIdx++);
                c.setCellValue(f.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                c.setCellStyle(styleHeader);
            }

            headerRow.createCell(colIdx++).setCellValue("Presentes");
            headerRow.createCell(colIdx++).setCellValue("Faltas");
            headerRow.createCell(colIdx++).setCellValue("% Asistencia");

            for (int i = 0; i < colIdx; i++) {
                if (headerRow.getCell(i) != null) {
                    headerRow.getCell(i).setCellStyle(styleHeader);
                }
            }

            // Datos
            int rIdx = 4;
            int num = 1;
            for (FilaMatrizEstudianteDTO est : matriz.getEstudiantes()) {
                Row row = sheet.createRow(rIdx++);
                int cIdx = 0;

                Cell cNum = row.createCell(cIdx++);
                cNum.setCellValue(num++);
                cNum.setCellStyle(styleCenter);

                row.createCell(cIdx++).setCellValue(est.getNumeroRegistro());
                row.createCell(cIdx++).setCellValue(est.getNombreCompleto());
                row.createCell(cIdx++).setCellValue(est.getCorreo());

                for (FechaClase f : matriz.getFechas()) {
                    Cell cEstado = row.createCell(cIdx++);
                    var celda = est.getAsistenciasPorFecha().get(f.getId());
                    if (celda != null && celda.getEstado() == EstadoAsistencia.PRESENTE) {
                        cEstado.setCellValue("PRESENTE" + (celda.getHoraMarcaje() != null ? " (" + celda.getHoraMarcaje().format(DateTimeFormatter.ofPattern("HH:mm")) + ")" : ""));
                        cEstado.setCellStyle(stylePresente);
                    } else {
                        cEstado.setCellValue("FALTA");
                        cEstado.setCellStyle(styleFalta);
                    }
                }

                Cell cPres = row.createCell(cIdx++);
                cPres.setCellValue(est.getTotalPresentes());
                cPres.setCellStyle(styleCenter);

                Cell cFalt = row.createCell(cIdx++);
                cFalt.setCellValue(est.getTotalFaltas());
                cFalt.setCellStyle(styleCenter);

                Cell cPct = row.createCell(cIdx++);
                cPct.setCellValue(est.getPorcentajeAsistencia() + "%");
                cPct.setCellStyle(styleCenter);
            }

            // Auto-size columns
            for (int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(bos);
            return bos.toByteArray();

        } catch (Exception e) {
            throw new BusinessException("Error al generar el reporte Excel: " + e.getMessage());
        }
    }

    /**
     * Generación de plantilla Excel de ejemplo para Estudiantes.
     */
    public byte[] generarPlantillaEstudiantes() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Estudiantes");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Registro");
            header.createCell(1).setCellValue("Nombre Completo");
            header.createCell(2).setCellValue("Correo Electrónico");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("EST-2001");
            r1.createCell(1).setCellValue("Alejandro Morales Ríos");
            r1.createCell(2).setCellValue("alejandro.morales@diplomado.edu");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("EST-2002");
            r2.createCell(1).setCellValue("Valeria Paz Gómez");
            r2.createCell(2).setCellValue("valeria.paz@diplomado.edu");

            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue("EST-2003");
            r3.createCell(1).setCellValue("Gonzalo Fernández Vidal");
            r3.createCell(2).setCellValue("gonzalo.fernandez@diplomado.edu");

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar plantilla de estudiantes: " + e.getMessage());
        }
    }

    /**
     * Generación de plantilla Excel de ejemplo para Fechas de Clase.
     */
    public byte[] generarPlantillaFechas() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Fechas");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Fecha");
            header.createCell(1).setCellValue("Hora Inicio");
            header.createCell(2).setCellValue("Hora Fin");

            LocalDate hoy = LocalDate.now();
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(hoy.plusDays(1).toString());
            r1.createCell(1).setCellValue("08:30");
            r1.createCell(2).setCellValue("12:30");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue(hoy.plusDays(8).toString());
            r2.createCell(1).setCellValue("08:30");
            r2.createCell(2).setCellValue("12:30");

            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue(hoy.plusDays(15).toString());
            r3.createCell(1).setCellValue("08:30");
            r3.createCell(2).setCellValue("12:30");

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar plantilla de fechas: " + e.getMessage());
        }
    }


    // Métodos auxiliares
    private void validarArchivoExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Debe seleccionar un archivo Excel válido.");
        }
        String nombre = file.getOriginalFilename();
        if (nombre == null || (!nombre.endsWith(".xlsx") && !nombre.endsWith(".xls"))) {
            throw new BusinessException("El archivo debe tener formato Excel (.xlsx o .xls).");
        }
    }

    private boolean esFilaVacia(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellValueAsString(cell);
                if (val != null && !val.trim().isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private Map<String, Integer> mapearColumnasEstudiante(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            String val = getCellValueAsString(cell);
            if (val == null) continue;
            String header = val.toLowerCase().trim();
            if (header.contains("registro") || header.contains("matricula") || header.contains("ci") || header.contains("carnet") || header.contains("id")) {
                map.put("registro", c);
            } else if (header.contains("nombre") || header.contains("estudiante") || header.contains("alumno")) {
                map.put("nombre", c);
            } else if (header.contains("correo") || header.contains("email") || header.contains("mail")) {
                map.put("correo", c);
            }
        }

        if (!map.containsKey("registro")) map.put("registro", 0);
        if (!map.containsKey("nombre")) map.put("nombre", 1);
        if (!map.containsKey("correo")) map.put("correo", 2);

        return map;
    }

    private Map<String, Integer> mapearColumnasFechas(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            String val = getCellValueAsString(cell);
            if (val == null) continue;
            String header = val.toLowerCase().trim();
            if (header.contains("fecha") || header.contains("dia")) {
                map.put("fecha", c);
            } else if (header.contains("inicio") || header.contains("desde")) {
                map.put("inicio", c);
            } else if (header.contains("fin") || header.contains("hasta")) {
                map.put("fin", c);
            }
        }

        if (!map.containsKey("fecha")) map.put("fecha", 0);
        if (!map.containsKey("inicio")) map.put("inicio", 1);
        if (!map.containsKey("fin")) map.put("fin", 2);

        return map;
    }

    private LocalDate extraerLocalDate(Cell cell, int filaNum, List<String> errores) {
        if (cell == null) {
            errores.add("Fila " + filaNum + ": La celda de fecha está vacía.");
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            String val = getCellValueAsString(cell);
            if (val == null || val.isBlank()) {
                errores.add("Fila " + filaNum + ": La fecha es obligatoria.");
                return null;
            }
            val = val.trim();
            if (val.contains("/")) {
                String[] p = val.split("/");
                if (p.length == 3) {
                    if (p[0].length() == 4) {
                        return LocalDate.parse(val, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    } else {
                        return LocalDate.parse(val, DateTimeFormatter.ofPattern("d/M/yyyy"));
                    }
                }
            } else if (val.contains("-")) {
                String[] p = val.split("-");
                if (p.length == 3) {
                    if (p[0].length() == 4) {
                        return LocalDate.parse(val, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } else {
                        return LocalDate.parse(val, DateTimeFormatter.ofPattern("d-M-yyyy"));
                    }
                }
            }
            return LocalDate.parse(val);
        } catch (Exception e) {
            errores.add("Fila " + filaNum + ": Formato de fecha no reconocido. Use YYYY-MM-DD o DD/MM/YYYY.");
            return null;
        }
    }

    private LocalTime extraerLocalTime(Cell cell, String campo, int filaNum, List<String> errores) {
        if (cell == null) {
            errores.add("Fila " + filaNum + ": El campo '" + campo + "' está vacío.");
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalTime();
            }
            String val = getCellValueAsString(cell);
            if (val == null || val.isBlank()) {
                errores.add("Fila " + filaNum + ": El campo '" + campo + "' es obligatorio.");
                return null;
            }
            val = val.trim();
            if (val.length() == 5) {
                return LocalTime.parse(val, DateTimeFormatter.ofPattern("HH:mm"));
            } else if (val.length() == 8) {
                return LocalTime.parse(val, DateTimeFormatter.ofPattern("HH:mm:ss"));
            } else if (val.length() == 4 && val.contains(":")) {
                return LocalTime.parse("0" + val, DateTimeFormatter.ofPattern("HH:mm"));
            }
            return LocalTime.parse(val);
        } catch (Exception e) {
            errores.add("Fila " + filaNum + ": Formato de '" + campo + "' no reconocido. Use HH:mm (ej: 08:30).");
            return null;
        }
    }
}

package com.diplomado.asistencia.service;

import com.diplomado.asistencia.dto.*;
import com.diplomado.asistencia.entity.*;
import com.diplomado.asistencia.exception.BusinessException;
import com.diplomado.asistencia.exception.ResourceNotFoundException;
import com.diplomado.asistencia.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final EstudianteRepository estudianteRepository;
    private final FechaClaseRepository fechaClaseRepository;
    private final AuditoriaAsistenciaRepository auditoriaRepository;
    private final ActividadRepository actividadRepository;

    public AsistenciaService(
            AsistenciaRepository asistenciaRepository,
            EstudianteRepository estudianteRepository,
            FechaClaseRepository fechaClaseRepository,
            AuditoriaAsistenciaRepository auditoriaRepository,
            ActividadRepository actividadRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.estudianteRepository = estudianteRepository;
        this.fechaClaseRepository = fechaClaseRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.actividadRepository = actividadRepository;
    }

    /**
     * RN-03, RN-04: Restricción de marcaje exclusivamente en la fecha programada y una sola vez por estudiante.
     */
    @Transactional
    public Asistencia marcarAsistencia(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con ID: " + estudianteId));

        if (!estudiante.getModulo().isActivo()) {
            throw new BusinessException("El módulo académico de este estudiante ya no está activo.");
        }

        LocalDate hoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        // Verificar si hoy es una fecha de clase programada para el módulo
        FechaClase fechaClaseHoy = fechaClaseRepository.findByModuloIdAndFecha(estudiante.getModulo().getId(), hoy)
                .orElseThrow(() -> new BusinessException("Hoy (" + hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ") no hay ninguna clase programada para su módulo."));

        // Verificar si ya marcó asistencia en esta fecha
        Optional<Asistencia> optAsistencia = asistenciaRepository.findByEstudianteIdAndFechaClaseId(estudianteId, fechaClaseHoy.getId());

        if (optAsistencia.isPresent()) {
            Asistencia existente = optAsistencia.get();
            if (existente.getEstado() == EstadoAsistencia.PRESENTE) {
                String horaStr = existente.getHoraMarcaje() != null ? existente.getHoraMarcaje().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "";
                throw new BusinessException("Ya registraste tu asistencia para la sesión de hoy a las " + horaStr + ".");
            } else {
                // Estaba en FALTA y procede a marcar PRESENTE
                existente.setEstado(EstadoAsistencia.PRESENTE);
                existente.setHoraMarcaje(horaActual);
                Asistencia guardada = asistenciaRepository.save(existente);

                actividadRepository.save(new Actividad(
                        estudiante.getNombreCompleto(),
                        "MARCAJE_ASISTENCIA",
                        "El estudiante " + estudiante.getNombreCompleto() + " (" + estudiante.getNumeroRegistro() + ") registró asistencia a las " + horaActual.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        estudiante.getModulo()
                ));
                return guardada;
            }
        }

        // Crear nuevo registro de asistencia PRESENTE
        Asistencia nueva = new Asistencia(estudiante, fechaClaseHoy, EstadoAsistencia.PRESENTE, horaActual);
        Asistencia guardada = asistenciaRepository.save(nueva);

        actividadRepository.save(new Actividad(
                estudiante.getNombreCompleto(),
                "MARCAJE_ASISTENCIA",
                "El estudiante " + estudiante.getNombreCompleto() + " (" + estudiante.getNumeroRegistro() + ") registró asistencia a las " + horaActual.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                estudiante.getModulo()
        ));

        return guardada;
    }

    /**
     * RN-06, RN-07: Modificación administrativa de asistencia con auditoría obligatoria.
     */
    @Transactional
    public Asistencia modificarAsistencia(ModificarAsistenciaRequest request) {
        if (request.getMotivo() == null || request.getMotivo().trim().isBlank()) {
            throw new BusinessException("El motivo de la modificación es obligatorio para el registro de auditoría (RN-06).");
        }

        Asistencia asistencia = asistenciaRepository.findById(request.getAsistenciaId())
                .orElseThrow(() -> new ResourceNotFoundException("Registro de asistencia no encontrado con ID: " + request.getAsistenciaId()));

        String valorAnterior = asistencia.getEstado().name();
        String valorNuevo = request.getNuevoEstado().name();

        if (valorAnterior.equals(valorNuevo)) {
            throw new BusinessException("El nuevo estado (" + valorNuevo + ") es idéntico al estado actual.");
        }

        // Actualizar asistencia
        asistencia.setEstado(request.getNuevoEstado());
        if (request.getNuevoEstado() == EstadoAsistencia.PRESENTE) {
            if (asistencia.getHoraMarcaje() == null) {
                asistencia.setHoraMarcaje(LocalTime.now());
            }
        } else {
            asistencia.setHoraMarcaje(null);
        }

        Asistencia guardada = asistenciaRepository.save(asistencia);

        // Guardar Auditoría obligatoria
        String usuarioAdmin = (request.getUsuarioModificacion() != null && !request.getUsuarioModificacion().isBlank())
                ? request.getUsuarioModificacion().trim()
                : "Administrador";

        AuditoriaAsistencia auditoria = new AuditoriaAsistencia(
                guardada,
                usuarioAdmin,
                valorAnterior,
                valorNuevo,
                request.getMotivo().trim()
        );
        auditoriaRepository.save(auditoria);

        // Registrar en Actividad del módulo
        actividadRepository.save(new Actividad(
                usuarioAdmin,
                "MODIFICACION_ASISTENCIA_AUDITADA",
                "Modificación manual de asistencia para " + asistencia.getEstudiante().getNombreCompleto() +
                        " en fecha " + asistencia.getFechaClase().getFecha() +
                        ": De " + valorAnterior + " a " + valorNuevo + ". Motivo: " + request.getMotivo().trim(),
                asistencia.getFechaClase().getModulo()
        ));

        return guardada;
    }

    /**
     * Sincronización automática de asistencias (RN-05):
     * Garantiza que cada estudiante tenga su registro de Asistencia (en FALTA si no marcó) para las fechas transcurridas.
     */
    @Transactional
    public void sincronizarAsistenciasModulo(Long moduloId) {
        List<Estudiante> estudiantes = estudianteRepository.findByModuloIdOrderByNombreCompletoAsc(moduloId);
        List<FechaClase> fechas = fechaClaseRepository.findByModuloIdOrderByFechaAscHoraInicioAsc(moduloId);

        List<Asistencia> asistenciasExistentes = asistenciaRepository.findByFechaClaseModuloIdOrderByFechaClaseFechaAscEstudianteNombreCompletoAsc(moduloId);
        Map<String, Asistencia> mapa = new HashMap<>();
        for (Asistencia a : asistenciasExistentes) {
            mapa.put(a.getEstudiante().getId() + "_" + a.getFechaClase().getId(), a);
        }

        List<Asistencia> nuevas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (FechaClase f : fechas) {
            for (Estudiante e : estudiantes) {
                String key = e.getId() + "_" + f.getId();
                if (!mapa.containsKey(key)) {
                    // Si la fecha ya pasó o es hoy, se inicializa como FALTA si no ha marcado
                    EstadoAsistencia estado = EstadoAsistencia.FALTA;
                    Asistencia asis = new Asistencia(e, f, estado, null);
                    nuevas.add(asis);
                }
            }
        }

        if (!nuevas.isEmpty()) {
            asistenciaRepository.saveAll(nuevas);
        }
    }

    /**
     * Historial personal para el estudiante con cálculo de métricas.
     */
    @Transactional
    public ResumenAsistenciaEstudianteDTO obtenerResumenEstudiante(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con ID: " + estudianteId));

        Long moduloId = estudiante.getModulo().getId();
        sincronizarAsistenciasModulo(moduloId);

        List<FechaClase> fechas = fechaClaseRepository.findByModuloIdOrderByFechaAscHoraInicioAsc(moduloId);
        List<Asistencia> asistencias = asistenciaRepository.findByEstudianteIdOrderByFechaClaseFechaAsc(estudianteId);

        Map<Long, Asistencia> mapaAsistencias = asistencias.stream()
                .collect(Collectors.toMap(a -> a.getFechaClase().getId(), a -> a, (k1, k2) -> k1));

        LocalDate hoy = LocalDate.now();
        ResumenAsistenciaEstudianteDTO resumen = new ResumenAsistenciaEstudianteDTO();
        resumen.setEstudiante(estudiante);
        resumen.setTotalClases(fechas.size());

        int presentes = 0;
        int faltas = 0;
        int clasesTranscurridas = 0;

        List<DetalleAsistenciaDTO> historial = new ArrayList<>();
        DetalleAsistenciaDTO claseHoy = null;

        for (FechaClase f : fechas) {
            boolean esHoy = f.getFecha().isEqual(hoy);
            boolean esPasada = f.getFecha().isBefore(hoy);

            Asistencia asis = mapaAsistencias.get(f.getId());
            EstadoAsistencia estado = (asis != null) ? asis.getEstado() : EstadoAsistencia.FALTA;
            LocalTime horaMarcaje = (asis != null) ? asis.getHoraMarcaje() : null;
            Long asistenciaId = (asis != null) ? asis.getId() : null;

            boolean yaMarco = (estado == EstadoAsistencia.PRESENTE);
            boolean puedeMarcar = esHoy && !yaMarco && estudiante.getModulo().isActivo();

            if (esPasada || esHoy) {
                clasesTranscurridas++;
                if (estado == EstadoAsistencia.PRESENTE) {
                    presentes++;
                } else {
                    faltas++;
                }
            }

            DetalleAsistenciaDTO detalle = new DetalleAsistenciaDTO(
                    asistenciaId,
                    f.getId(),
                    f.getFecha(),
                    f.getHoraInicio(),
                    f.getHoraFin(),
                    estado,
                    horaMarcaje,
                    esHoy,
                    puedeMarcar,
                    yaMarco
            );

            historial.add(detalle);
            if (esHoy) {
                claseHoy = detalle;
            }
        }

        resumen.setClasesTranscurridas(clasesTranscurridas);
        resumen.setPresentes(presentes);
        resumen.setFaltas(faltas);
        double pct = (clasesTranscurridas > 0) ? ((double) presentes / clasesTranscurridas) * 100.0 : 0.0;
        resumen.setPorcentajeAsistencia(Math.round(pct * 100.0) / 100.0);
        resumen.setClaseHoy(claseHoy);
        resumen.setHistorial(historial);

        return resumen;
    }

    /**
     * Matriz de asistencia completa para el Administrador con soporte de filtros.
     */
    @Transactional
    public MatrizAsistenciaDTO obtenerMatrizAsistencia(Long moduloId, Long filtroFechaId, String filtroEstudiante) {
        sincronizarAsistenciasModulo(moduloId);

        Modulo modulo = fechaClaseRepository.findByModuloIdOrderByFechaAscHoraInicioAsc(moduloId).stream()
                .findFirst().map(FechaClase::getModulo)
                .orElse(null);

        List<FechaClase> todasLasFechas = fechaClaseRepository.findByModuloIdOrderByFechaAscHoraInicioAsc(moduloId);
        List<FechaClase> fechasFiltradas = todasLasFechas;

        if (filtroFechaId != null && filtroFechaId > 0) {
            fechasFiltradas = todasLasFechas.stream()
                    .filter(f -> f.getId().equals(filtroFechaId))
                    .collect(Collectors.toList());
        }

        List<Estudiante> estudiantes;
        if (filtroEstudiante != null && !filtroEstudiante.trim().isBlank()) {
            estudiantes = estudianteRepository.searchByModulo(moduloId, filtroEstudiante.trim());
        } else {
            estudiantes = estudianteRepository.findByModuloIdOrderByNombreCompletoAsc(moduloId);
        }

        List<Asistencia> todasAsistencias = asistenciaRepository.findByFechaClaseModuloIdOrderByFechaClaseFechaAscEstudianteNombreCompletoAsc(moduloId);
        Map<String, Asistencia> mapa = new HashMap<>();
        for (Asistencia a : todasAsistencias) {
            mapa.put(a.getEstudiante().getId() + "_" + a.getFechaClase().getId(), a);
        }

        List<FilaMatrizEstudianteDTO> filas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (Estudiante e : estudiantes) {
            FilaMatrizEstudianteDTO fila = new FilaMatrizEstudianteDTO();
            fila.setEstudianteId(e.getId());
            fila.setNumeroRegistro(e.getNumeroRegistro());
            fila.setNombreCompleto(e.getNombreCompleto());
            fila.setCorreo(e.getCorreo());

            int presentes = 0;
            int faltas = 0;
            int evaluadas = 0;

            Map<Long, AsistenciaCeldaDTO> celdas = new HashMap<>();
            for (FechaClase f : fechasFiltradas) {
                String key = e.getId() + "_" + f.getId();
                Asistencia a = mapa.get(key);

                EstadoAsistencia est = (a != null) ? a.getEstado() : EstadoAsistencia.FALTA;
                LocalTime hora = (a != null) ? a.getHoraMarcaje() : null;
                Long asisId = (a != null) ? a.getId() : null;

                celdas.put(f.getId(), new AsistenciaCeldaDTO(asisId, est, hora));

                if (f.getFecha().isBefore(hoy) || f.getFecha().isEqual(hoy)) {
                    evaluadas++;
                    if (est == EstadoAsistencia.PRESENTE) {
                        presentes++;
                    } else {
                        faltas++;
                    }
                }
            }

            fila.setAsistenciasPorFecha(celdas);
            fila.setTotalPresentes(presentes);
            fila.setTotalFaltas(faltas);
            double pct = (evaluadas > 0) ? ((double) presentes / evaluadas) * 100.0 : 0.0;
            fila.setPorcentajeAsistencia(Math.round(pct * 100.0) / 100.0);

            filas.add(fila);
        }

        return new MatrizAsistenciaDTO(modulo, fechasFiltradas, filas);
    }

    /**
     * Estadísticas del dashboard de administrador.
     */
    public DashboardStatsDTO obtenerEstadisticasDashboard(Long moduloId) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        long totalEstudiantes = estudianteRepository.countByModuloId(moduloId);
        long totalFechas = fechaClaseRepository.countByModuloId(moduloId);
        long totalPresentes = asistenciaRepository.countByFechaClaseModuloIdAndEstado(moduloId, EstadoAsistencia.PRESENTE);
        long totalFaltas = asistenciaRepository.countByFechaClaseModuloIdAndEstado(moduloId, EstadoAsistencia.FALTA);
        long totalAuditorias = auditoriaRepository.countByAsistenciaFechaClaseModuloId(moduloId);

        stats.setTotalEstudiantes(totalEstudiantes);
        stats.setTotalFechas(totalFechas);
        stats.setTotalPresentes(totalPresentes);
        stats.setTotalFaltas(totalFaltas);
        stats.setTotalAuditorias(totalAuditorias);

        long totalRegistros = totalPresentes + totalFaltas;
        double pct = (totalRegistros > 0) ? ((double) totalPresentes / totalRegistros) * 100.0 : 0.0;
        stats.setTasaAsistenciaPromedio(Math.round(pct * 100.0) / 100.0);

        fechaClaseRepository.findFirstByModuloIdAndFechaGreaterThanEqualOrderByFechaAsc(moduloId, LocalDate.now())
                .ifPresent(stats::setProximaClase);

        return stats;
    }
}

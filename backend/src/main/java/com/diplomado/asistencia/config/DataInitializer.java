package com.diplomado.asistencia.config;

import com.diplomado.asistencia.entity.*;
import com.diplomado.asistencia.repository.*;
import com.diplomado.asistencia.service.AsistenciaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ModuloRepository moduloRepository;
    private final EstudianteRepository estudianteRepository;
    private final FechaClaseRepository fechaClaseRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final ActividadRepository actividadRepository;
    private final AsistenciaService asistenciaService;

    public DataInitializer(
            ModuloRepository moduloRepository,
            EstudianteRepository estudianteRepository,
            FechaClaseRepository fechaClaseRepository,
            AsistenciaRepository asistenciaRepository,
            ActividadRepository actividadRepository,
            AsistenciaService asistenciaService) {
        this.moduloRepository = moduloRepository;
        this.estudianteRepository = estudianteRepository;
        this.fechaClaseRepository = fechaClaseRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.actividadRepository = actividadRepository;
        this.asistenciaService = asistenciaService;
    }

    @Override
    public void run(String... args) {
        if (moduloRepository.count() == 0) {
            // 1. Crear módulo inicial activo
            Modulo modulo = new Modulo("Diplomado en Arquitectura de Software y Cloud - Módulo I", "MOD-ARQ-01", true);
            Modulo modGuardado = moduloRepository.save(modulo);

            // 2. Crear fechas de clase (incluyendo HOY para pruebas interactivas de marcaje)
            LocalDate hoy = LocalDate.now();
            FechaClase f1 = new FechaClase(hoy.minusDays(7), LocalTime.of(8, 30), LocalTime.of(12, 30), modGuardado);
            FechaClase f2Hoy = new FechaClase(hoy, LocalTime.of(8, 30), LocalTime.of(12, 30), modGuardado);
            FechaClase f3 = new FechaClase(hoy.plusDays(7), LocalTime.of(8, 30), LocalTime.of(12, 30), modGuardado);
            FechaClase f4 = new FechaClase(hoy.plusDays(14), LocalTime.of(8, 30), LocalTime.of(12, 30), modGuardado);

            List<FechaClase> fechas = fechaClaseRepository.saveAll(List.of(f1, f2Hoy, f3, f4));

            // 3. Crear estudiantes del diplomado
            Estudiante e1 = new Estudiante("EST-1001", "Carlos Andrés Pérez Gómez", "carlos.perez@diplomado.edu", modGuardado);
            Estudiante e2 = new Estudiante("EST-1002", "Mariana Lucía Morales Silva", "mariana.morales@diplomado.edu", modGuardado);
            Estudiante e3 = new Estudiante("EST-1003", "Jorge Roberto Gutiérrez Vega", "jorge.gutierrez@diplomado.edu", modGuardado);
            Estudiante e4 = new Estudiante("EST-1004", "Ana Sofía Mendoza Vargas", "ana.mendoza@diplomado.edu", modGuardado);
            Estudiante e5 = new Estudiante("EST-1005", "Diego Fernando Rojas Castro", "diego.rojas@diplomado.edu", modGuardado);

            List<Estudiante> estudiantes = estudianteRepository.saveAll(List.of(e1, e2, e3, e4, e5));

            // 4. Asistencias para la clase anterior f1
            asistenciaRepository.save(new Asistencia(e1, f1, EstadoAsistencia.PRESENTE, LocalTime.of(8, 32)));
            asistenciaRepository.save(new Asistencia(e2, f1, EstadoAsistencia.PRESENTE, LocalTime.of(8, 29)));
            asistenciaRepository.save(new Asistencia(e3, f1, EstadoAsistencia.FALTA, null));
            asistenciaRepository.save(new Asistencia(e4, f1, EstadoAsistencia.PRESENTE, LocalTime.of(8, 35)));
            asistenciaRepository.save(new Asistencia(e5, f1, EstadoAsistencia.FALTA, null));

            // Sincronizar el resto
            asistenciaService.sincronizarAsistenciasModulo(modGuardado.getId());

            // Registrar actividad inicial
            actividadRepository.save(new Actividad(
                    "Sistema",
                    "INICIALIZACION_SISTEMA",
                    "Sistema inicializado con módulo activo '" + modGuardado.getNombre() + "', 5 estudiantes y 4 sesiones de clase.",
                    modGuardado
            ));
        }
    }
}

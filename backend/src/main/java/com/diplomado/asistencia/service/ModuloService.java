package com.diplomado.asistencia.service;

import com.diplomado.asistencia.dto.NuevoModuloRequest;
import com.diplomado.asistencia.entity.Actividad;
import com.diplomado.asistencia.entity.Modulo;
import com.diplomado.asistencia.exception.BusinessException;
import com.diplomado.asistencia.exception.ResourceNotFoundException;
import com.diplomado.asistencia.repository.ActividadRepository;
import com.diplomado.asistencia.repository.ModuloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ModuloService {

    private final ModuloRepository moduloRepository;
    private final ActividadRepository actividadRepository;

    public ModuloService(ModuloRepository moduloRepository, ActividadRepository actividadRepository) {
        this.moduloRepository = moduloRepository;
        this.actividadRepository = actividadRepository;
    }

    public Modulo getModuloActivo() {
        return moduloRepository.findByActivoTrue()
                .orElseThrow(() -> new BusinessException("No existe ningún módulo académico activo en este momento. Por favor configure o active uno."));
    }

    public Optional<Modulo> getModuloActivoOptional() {
        return moduloRepository.findByActivoTrue();
    }

    public List<Modulo> listarTodos() {
        return moduloRepository.findAllByOrderByFechaCreacionDesc();
    }

    public Modulo buscarPorId(Long id) {
        return moduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado con ID: " + id));
    }

    @Transactional
    public Modulo activarNuevoModulo(NuevoModuloRequest request, String usuario) {
        // RN-01: Desactivar modulo activo anterior si existe, conservando sus datos
        moduloRepository.findByActivoTrue().ifPresent(anterior -> {
            anterior.setActivo(false);
            anterior.setFechaReinicio(LocalDateTime.now());
            moduloRepository.save(anterior);
            actividadRepository.save(new Actividad(
                    usuario,
                    "DESACTIVACION_MODULO",
                    "Módulo '" + anterior.getNombre() + "' (ID: " + anterior.getId() + ") desactivado al activar un nuevo módulo.",
                    anterior
            ));
        });

        Modulo nuevo = new Modulo(request.getNombre().trim(), request.getCodigo() != null ? request.getCodigo().trim() : null, true);
        Modulo guardado = moduloRepository.save(nuevo);

        actividadRepository.save(new Actividad(
                usuario,
                "ACTIVACION_MODULO",
                "Se activó el nuevo módulo '" + guardado.getNombre() + "' (ID: " + guardado.getId() + ").",
                guardado
        ));

        return guardado;
    }

    @Transactional
    public Modulo reiniciarModulo(String usuario, String motivo) {
        // RN-08, RF-13: Lógica de reinicio que desactiva el módulo sin borrar datos
        Modulo activo = getModuloActivo();
        activo.setActivo(false);
        activo.setFechaReinicio(LocalDateTime.now());
        Modulo guardado = moduloRepository.save(activo);

        actividadRepository.save(new Actividad(
                usuario,
                "REINICIO_MODULO",
                "Reinicio del módulo '" + guardado.getNombre() + "' (ID: " + guardado.getId() + "). Motivo: " + (motivo != null && !motivo.isBlank() ? motivo : "Reinicio operativo de ciclo"),
                guardado
        ));

        return guardado;
    }

    @Transactional
    public Modulo cambiarModuloActivo(Long id, String usuario) {
        Modulo aActivar = buscarPorId(id);
        if (aActivar.isActivo()) {
            return aActivar;
        }

        moduloRepository.findByActivoTrue().ifPresent(anterior -> {
            anterior.setActivo(false);
            anterior.setFechaReinicio(LocalDateTime.now());
            moduloRepository.save(anterior);
        });

        aActivar.setActivo(true);
        Modulo guardado = moduloRepository.save(aActivar);

        actividadRepository.save(new Actividad(
                usuario,
                "CAMBIO_MODULO_ACTIVO",
                "Se estableció como activo el módulo: " + guardado.getNombre() + " (ID: " + guardado.getId() + ")",
                guardado
        ));

        return guardado;
    }
}

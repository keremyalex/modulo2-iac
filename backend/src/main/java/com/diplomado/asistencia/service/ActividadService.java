package com.diplomado.asistencia.service;

import com.diplomado.asistencia.entity.Actividad;
import com.diplomado.asistencia.entity.Modulo;
import com.diplomado.asistencia.repository.ActividadRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;

    public ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    public List<Actividad> listarPorModulo(Long moduloId) {
        return actividadRepository.findByModuloIdOrderByFechaHoraDesc(moduloId);
    }

    public List<Actividad> listarTodas() {
        return actividadRepository.findAllByOrderByFechaHoraDesc();
    }

    public Actividad registrar(String usuario, String accion, String descripcion, Modulo modulo) {
        return actividadRepository.save(new Actividad(usuario, accion, descripcion, modulo));
    }
}

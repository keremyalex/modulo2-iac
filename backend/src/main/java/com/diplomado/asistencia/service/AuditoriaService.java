package com.diplomado.asistencia.service;

import com.diplomado.asistencia.entity.AuditoriaAsistencia;
import com.diplomado.asistencia.repository.AuditoriaAsistenciaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaAsistenciaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaAsistenciaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public List<AuditoriaAsistencia> listarPorModulo(Long moduloId) {
        return auditoriaRepository.findByAsistenciaFechaClaseModuloIdOrderByFechaHoraDesc(moduloId);
    }

    public List<AuditoriaAsistencia> listarTodas() {
        return auditoriaRepository.findAllByOrderByFechaHoraDesc();
    }
}

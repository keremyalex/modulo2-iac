package com.diplomado.asistencia.service;

import com.diplomado.asistencia.entity.FechaClase;
import com.diplomado.asistencia.entity.Modulo;
import com.diplomado.asistencia.exception.BusinessException;
import com.diplomado.asistencia.exception.ResourceNotFoundException;
import com.diplomado.asistencia.repository.FechaClaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FechaClaseService {

    private final FechaClaseRepository fechaClaseRepository;

    public FechaClaseService(FechaClaseRepository fechaClaseRepository) {
        this.fechaClaseRepository = fechaClaseRepository;
    }

    public List<FechaClase> listarPorModulo(Long moduloId) {
        return fechaClaseRepository.findByModuloIdOrderByFechaAscHoraInicioAsc(moduloId);
    }

    public FechaClase buscarPorId(Long id) {
        return fechaClaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fecha de clase no encontrada con ID: " + id));
    }

    public Optional<FechaClase> buscarFechaHoyEnModulo(Long moduloId) {
        return fechaClaseRepository.findByModuloIdAndFecha(moduloId, LocalDate.now());
    }

    public Optional<FechaClase> proximaClase(Long moduloId) {
        return fechaClaseRepository.findFirstByModuloIdAndFechaGreaterThanEqualOrderByFechaAsc(moduloId, LocalDate.now());
    }

    @Transactional
    public FechaClase registrarFechaClase(FechaClase fechaClase, Modulo modulo) {
        if (fechaClaseRepository.existsByModuloIdAndFecha(modulo.getId(), fechaClase.getFecha())) {
            throw new BusinessException("Ya existe una clase programada para la fecha: " + fechaClase.getFecha() + " en este módulo.");
        }
        if (fechaClase.getHoraFin().isBefore(fechaClase.getHoraInicio())) {
            throw new BusinessException("La hora de fin no puede ser anterior a la hora de inicio.");
        }

        fechaClase.setModulo(modulo);
        return fechaClaseRepository.save(fechaClase);
    }

    public long contarPorModulo(Long moduloId) {
        return fechaClaseRepository.countByModuloId(moduloId);
    }
}

package com.diplomado.asistencia.service;

import com.diplomado.asistencia.entity.Estudiante;
import com.diplomado.asistencia.entity.Modulo;
import com.diplomado.asistencia.exception.BusinessException;
import com.diplomado.asistencia.exception.ResourceNotFoundException;
import com.diplomado.asistencia.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;

    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }

    public List<Estudiante> listarPorModulo(Long moduloId) {
        return estudianteRepository.findByModuloIdOrderByNombreCompletoAsc(moduloId);
    }

    public List<Estudiante> buscar(Long moduloId, String query) {
        if (query == null || query.isBlank()) {
            return estudianteRepository.findByModuloIdOrderByNombreCompletoAsc(moduloId);
        }
        return estudianteRepository.searchByModulo(moduloId, query.trim());
    }

    public Estudiante buscarPorId(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con ID: " + id));
    }

    public Estudiante buscarPorIdentificadorEnModuloActivo(String identificador) {
        return estudianteRepository.findByModuloActivoAndIdentificador(identificador.trim())
                .orElseThrow(() -> new BusinessException("No se encontró ningún estudiante con correo o registro '" + identificador + "' en el módulo activo actual."));
    }

    @Transactional
    public Estudiante registrarEstudiante(Estudiante estudiante, Modulo modulo) {
        // RN-02: Validación de unicidad de número de registro por módulo activo
        if (estudianteRepository.existsByModuloIdAndNumeroRegistroIgnoreCase(modulo.getId(), estudiante.getNumeroRegistro().trim())) {
            throw new BusinessException("El número de registro '" + estudiante.getNumeroRegistro() + "' ya está registrado en este módulo.");
        }
        if (estudianteRepository.existsByModuloIdAndCorreoIgnoreCase(modulo.getId(), estudiante.getCorreo().trim())) {
            throw new BusinessException("El correo electrónico '" + estudiante.getCorreo() + "' ya está registrado en este módulo.");
        }

        estudiante.setModulo(modulo);
        estudiante.setNumeroRegistro(estudiante.getNumeroRegistro().trim());
        estudiante.setNombreCompleto(estudiante.getNombreCompleto().trim());
        estudiante.setCorreo(estudiante.getCorreo().trim().toLowerCase());

        return estudianteRepository.save(estudiante);
    }

    public long contarPorModulo(Long moduloId) {
        return estudianteRepository.countByModuloId(moduloId);
    }
}

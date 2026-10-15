package com.diplomado.asistencia.repository;

import com.diplomado.asistencia.entity.Asistencia;
import com.diplomado.asistencia.entity.EstadoAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    Optional<Asistencia> findByEstudianteIdAndFechaClaseId(Long estudianteId, Long fechaClaseId);

    List<Asistencia> findByEstudianteIdOrderByFechaClaseFechaAsc(Long estudianteId);

    List<Asistencia> findByFechaClaseIdOrderByEstudianteNombreCompletoAsc(Long fechaClaseId);

    List<Asistencia> findByFechaClaseModuloIdOrderByFechaClaseFechaAscEstudianteNombreCompletoAsc(Long moduloId);

    long countByFechaClaseModuloIdAndEstado(Long moduloId, EstadoAsistencia estado);

    long countByEstudianteIdAndEstado(Long estudianteId, EstadoAsistencia estado);

    long countByFechaClaseModuloId(Long moduloId);

    boolean existsByEstudianteIdAndFechaClaseId(Long estudianteId, Long fechaClaseId);

    @Query("SELECT a FROM Asistencia a WHERE a.estudiante.id = :estudianteId AND a.fechaClase.fecha = :fecha")
    Optional<Asistencia> findByEstudianteIdAndFecha(@Param("estudianteId") Long estudianteId, @Param("fecha") LocalDate fecha);

    @Query("SELECT a FROM Asistencia a WHERE a.fechaClase.modulo.id = :moduloId AND a.fechaClase.id = :fechaClaseId")
    List<Asistencia> findByModuloIdAndFechaClaseId(@Param("moduloId") Long moduloId, @Param("fechaClaseId") Long fechaClaseId);
}

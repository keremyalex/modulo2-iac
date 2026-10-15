package com.diplomado.asistencia.repository;

import com.diplomado.asistencia.entity.AuditoriaAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaAsistenciaRepository extends JpaRepository<AuditoriaAsistencia, Long> {

    List<AuditoriaAsistencia> findByAsistenciaIdOrderByFechaHoraDesc(Long asistenciaId);

    List<AuditoriaAsistencia> findByAsistenciaFechaClaseModuloIdOrderByFechaHoraDesc(Long moduloId);

    List<AuditoriaAsistencia> findAllByOrderByFechaHoraDesc();

    long countByAsistenciaFechaClaseModuloId(Long moduloId);
}

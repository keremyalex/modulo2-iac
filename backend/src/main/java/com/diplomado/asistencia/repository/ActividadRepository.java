package com.diplomado.asistencia.repository;

import com.diplomado.asistencia.entity.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    List<Actividad> findByModuloIdOrderByFechaHoraDesc(Long moduloId);

    List<Actividad> findAllByOrderByFechaHoraDesc();

    long countByModuloId(Long moduloId);
}

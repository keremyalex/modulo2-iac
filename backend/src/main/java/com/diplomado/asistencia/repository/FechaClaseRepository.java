package com.diplomado.asistencia.repository;

import com.diplomado.asistencia.entity.FechaClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FechaClaseRepository extends JpaRepository<FechaClase, Long> {

    List<FechaClase> findByModuloIdOrderByFechaAscHoraInicioAsc(Long moduloId);

    Optional<FechaClase> findByModuloIdAndFecha(Long moduloId, LocalDate fecha);

    boolean existsByModuloIdAndFecha(Long moduloId, LocalDate fecha);

    long countByModuloId(Long moduloId);

    Optional<FechaClase> findFirstByModuloIdAndFechaGreaterThanEqualOrderByFechaAsc(Long moduloId, LocalDate fecha);

    List<FechaClase> findByModuloIdAndFechaLessThanEqualOrderByFechaAsc(Long moduloId, LocalDate fecha);
}

package com.diplomado.asistencia.repository;

import com.diplomado.asistencia.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    List<Estudiante> findByModuloIdOrderByNombreCompletoAsc(Long moduloId);

    Optional<Estudiante> findByModuloIdAndNumeroRegistroIgnoreCase(Long moduloId, String numeroRegistro);

    Optional<Estudiante> findByModuloIdAndCorreoIgnoreCase(Long moduloId, String correo);

    @Query("SELECT e FROM Estudiante e WHERE e.modulo.activo = true AND (LOWER(e.numeroRegistro) = LOWER(:identificador) OR LOWER(e.correo) = LOWER(:identificador))")
    Optional<Estudiante> findByModuloActivoAndIdentificador(@Param("identificador") String identificador);

    boolean existsByModuloIdAndNumeroRegistroIgnoreCase(Long moduloId, String numeroRegistro);

    boolean existsByModuloIdAndCorreoIgnoreCase(Long moduloId, String correo);

    @Query("SELECT e FROM Estudiante e WHERE e.modulo.id = :moduloId AND " +
           "(LOWER(e.nombreCompleto) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.numeroRegistro) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.correo) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY e.nombreCompleto ASC")
    List<Estudiante> searchByModulo(@Param("moduloId") Long moduloId, @Param("query") String query);

    long countByModuloId(Long moduloId);
}

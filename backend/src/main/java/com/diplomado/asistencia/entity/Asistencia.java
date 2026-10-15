package com.diplomado.asistencia.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(
    name = "asistencias",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"estudiante_id", "fecha_clase_id"}, name = "uk_asistencia_estudiante_fechaclase")
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fecha_clase_id", nullable = false)
    private FechaClase fechaClase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAsistencia estado = EstadoAsistencia.FALTA;

    private LocalTime horaMarcaje;

    public Asistencia() {
    }

    public Asistencia(Estudiante estudiante, FechaClase fechaClase, EstadoAsistencia estado, LocalTime horaMarcaje) {
        this.estudiante = estudiante;
        this.fechaClase = fechaClase;
        this.estado = estado;
        this.horaMarcaje = horaMarcaje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public FechaClase getFechaClase() {
        return fechaClase;
    }

    public void setFechaClase(FechaClase fechaClase) {
        this.fechaClase = fechaClase;
    }

    public EstadoAsistencia getEstado() {
        return estado;
    }

    public void setEstado(EstadoAsistencia estado) {
        this.estado = estado;
    }

    public LocalTime getHoraMarcaje() {
        return horaMarcaje;
    }

    public void setHoraMarcaje(LocalTime horaMarcaje) {
        this.horaMarcaje = horaMarcaje;
    }
}

package com.diplomado.asistencia.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "fechas_clase",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fecha", "modulo_id"}, name = "uk_fechaclase_fecha_modulo")
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FechaClase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    public FechaClase() {
    }

    public FechaClase(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Modulo modulo) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.modulo = modulo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }
}

package com.diplomado.asistencia.dto;

import com.diplomado.asistencia.entity.EstadoAsistencia;
import java.time.LocalDate;
import java.time.LocalTime;

public class DetalleAsistenciaDTO {

    private Long asistenciaId;
    private Long fechaClaseId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoAsistencia estado;
    private LocalTime horaMarcaje;
    private boolean esHoy;
    private boolean puedeMarcar;
    private boolean yaMarco;

    public DetalleAsistenciaDTO() {
    }

    public DetalleAsistenciaDTO(Long asistenciaId, Long fechaClaseId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, EstadoAsistencia estado, LocalTime horaMarcaje, boolean esHoy, boolean puedeMarcar, boolean yaMarco) {
        this.asistenciaId = asistenciaId;
        this.fechaClaseId = fechaClaseId;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
        this.horaMarcaje = horaMarcaje;
        this.esHoy = esHoy;
        this.puedeMarcar = puedeMarcar;
        this.yaMarco = yaMarco;
    }

    public Long getAsistenciaId() {
        return asistenciaId;
    }

    public void setAsistenciaId(Long asistenciaId) {
        this.asistenciaId = asistenciaId;
    }

    public Long getFechaClaseId() {
        return fechaClaseId;
    }

    public void setFechaClaseId(Long fechaClaseId) {
        this.fechaClaseId = fechaClaseId;
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

    public boolean isEsHoy() {
        return esHoy;
    }

    public void setEsHoy(boolean esHoy) {
        this.esHoy = esHoy;
    }

    public boolean isPuedeMarcar() {
        return puedeMarcar;
    }

    public void setPuedeMarcar(boolean puedeMarcar) {
        this.puedeMarcar = puedeMarcar;
    }

    public boolean isYaMarco() {
        return yaMarco;
    }

    public void setYaMarco(boolean yaMarco) {
        this.yaMarco = yaMarco;
    }
}

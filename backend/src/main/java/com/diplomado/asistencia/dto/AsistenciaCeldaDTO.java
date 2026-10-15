package com.diplomado.asistencia.dto;

import com.diplomado.asistencia.entity.EstadoAsistencia;
import java.time.LocalTime;

public class AsistenciaCeldaDTO {

    private Long asistenciaId;
    private EstadoAsistencia estado;
    private LocalTime horaMarcaje;

    public AsistenciaCeldaDTO() {
    }

    public AsistenciaCeldaDTO(Long asistenciaId, EstadoAsistencia estado, LocalTime horaMarcaje) {
        this.asistenciaId = asistenciaId;
        this.estado = estado;
        this.horaMarcaje = horaMarcaje;
    }

    public Long getAsistenciaId() {
        return asistenciaId;
    }

    public void setAsistenciaId(Long asistenciaId) {
        this.asistenciaId = asistenciaId;
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

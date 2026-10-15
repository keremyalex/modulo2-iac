package com.diplomado.asistencia.dto;

import com.diplomado.asistencia.entity.EstadoAsistencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ModificarAsistenciaRequest {

    @NotNull(message = "El ID de la asistencia es obligatorio")
    private Long asistenciaId;

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoAsistencia nuevoEstado;

    @NotBlank(message = "El motivo de la modificación es obligatorio para auditoría")
    private String motivo;

    private String usuarioModificacion = "Administrador";

    public ModificarAsistenciaRequest() {
    }

    public ModificarAsistenciaRequest(Long asistenciaId, EstadoAsistencia nuevoEstado, String motivo, String usuarioModificacion) {
        this.asistenciaId = asistenciaId;
        this.nuevoEstado = nuevoEstado;
        this.motivo = motivo;
        this.usuarioModificacion = usuarioModificacion;
    }

    public Long getAsistenciaId() {
        return asistenciaId;
    }

    public void setAsistenciaId(Long asistenciaId) {
        this.asistenciaId = asistenciaId;
    }

    public EstadoAsistencia getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(EstadoAsistencia nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }
}

package com.diplomado.asistencia.dto;

import jakarta.validation.constraints.NotNull;

public class MarcarAsistenciaRequest {

    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long estudianteId;

    public MarcarAsistenciaRequest() {
    }

    public MarcarAsistenciaRequest(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }
}

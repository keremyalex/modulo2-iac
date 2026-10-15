package com.diplomado.asistencia.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginEstudianteRequest {

    @NotBlank(message = "El correo o número de registro es obligatorio")
    private String identificador;

    public LoginEstudianteRequest() {
    }

    public LoginEstudianteRequest(String identificador) {
        this.identificador = identificador;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
}

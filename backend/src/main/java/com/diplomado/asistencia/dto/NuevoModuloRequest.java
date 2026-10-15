package com.diplomado.asistencia.dto;

import jakarta.validation.constraints.NotBlank;

public class NuevoModuloRequest {

    @NotBlank(message = "El nombre del módulo es obligatorio")
    private String nombre;

    private String codigo;

    public NuevoModuloRequest() {
    }

    public NuevoModuloRequest(String nombre, String codigo) {
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}

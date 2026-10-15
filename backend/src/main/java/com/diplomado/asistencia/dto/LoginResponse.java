package com.diplomado.asistencia.dto;

public class LoginResponse {
    private String token;
    private String rol; // "ADMIN" o "ESTUDIANTE"
    private String username;
    private String nombre;
    private Long estudianteId;
    private Long moduloId;
    private String moduloNombre;

    public LoginResponse() {
    }

    public LoginResponse(String token, String rol, String username, String nombre, Long estudianteId, Long moduloId, String moduloNombre) {
        this.token = token;
        this.rol = rol;
        this.username = username;
        this.nombre = nombre;
        this.estudianteId = estudianteId;
        this.moduloId = moduloId;
        this.moduloNombre = moduloNombre;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Long getModuloId() {
        return moduloId;
    }

    public void setModuloId(Long moduloId) {
        this.moduloId = moduloId;
    }

    public String getModuloNombre() {
        return moduloNombre;
    }

    public void setModuloNombre(String moduloNombre) {
        this.moduloNombre = moduloNombre;
    }
}

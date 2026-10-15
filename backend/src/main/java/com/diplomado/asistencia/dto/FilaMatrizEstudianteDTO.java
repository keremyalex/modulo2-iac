package com.diplomado.asistencia.dto;

import java.util.HashMap;
import java.util.Map;

public class FilaMatrizEstudianteDTO {

    private Long estudianteId;
    private String numeroRegistro;
    private String nombreCompleto;
    private String correo;
    private Map<Long, AsistenciaCeldaDTO> asistenciasPorFecha = new HashMap<>(); // key: fechaClaseId
    private int totalPresentes;
    private int totalFaltas;
    private double porcentajeAsistencia;

    public FilaMatrizEstudianteDTO() {
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getNumeroRegistro() {
        return numeroRegistro;
    }

    public void setNumeroRegistro(String numeroRegistro) {
        this.numeroRegistro = numeroRegistro;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Map<Long, AsistenciaCeldaDTO> getAsistenciasPorFecha() {
        return asistenciasPorFecha;
    }

    public void setAsistenciasPorFecha(Map<Long, AsistenciaCeldaDTO> asistenciasPorFecha) {
        this.asistenciasPorFecha = asistenciasPorFecha;
    }

    public int getTotalPresentes() {
        return totalPresentes;
    }

    public void setTotalPresentes(int totalPresentes) {
        this.totalPresentes = totalPresentes;
    }

    public int getTotalFaltas() {
        return totalFaltas;
    }

    public void setTotalFaltas(int totalFaltas) {
        this.totalFaltas = totalFaltas;
    }

    public double getPorcentajeAsistencia() {
        return porcentajeAsistencia;
    }

    public void setPorcentajeAsistencia(double porcentajeAsistencia) {
        this.porcentajeAsistencia = porcentajeAsistencia;
    }
}

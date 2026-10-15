package com.diplomado.asistencia.dto;

import com.diplomado.asistencia.entity.Estudiante;
import java.util.ArrayList;
import java.util.List;

public class ResumenAsistenciaEstudianteDTO {

    private Estudiante estudiante;
    private int totalClases;
    private int clasesTranscurridas;
    private int presentes;
    private int faltas;
    private double porcentajeAsistencia;
    private DetalleAsistenciaDTO claseHoy;
    private List<DetalleAsistenciaDTO> historial = new ArrayList<>();

    public ResumenAsistenciaEstudianteDTO() {
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public int getTotalClases() {
        return totalClases;
    }

    public void setTotalClases(int totalClases) {
        this.totalClases = totalClases;
    }

    public int getClasesTranscurridas() {
        return clasesTranscurridas;
    }

    public void setClasesTranscurridas(int clasesTranscurridas) {
        this.clasesTranscurridas = clasesTranscurridas;
    }

    public int getPresentes() {
        return presentes;
    }

    public void setPresentes(int presentes) {
        this.presentes = presentes;
    }

    public int getFaltas() {
        return faltas;
    }

    public void setFaltas(int faltas) {
        this.faltas = faltas;
    }

    public double getPorcentajeAsistencia() {
        return porcentajeAsistencia;
    }

    public void setPorcentajeAsistencia(double porcentajeAsistencia) {
        this.porcentajeAsistencia = porcentajeAsistencia;
    }

    public DetalleAsistenciaDTO getClaseHoy() {
        return claseHoy;
    }

    public void setClaseHoy(DetalleAsistenciaDTO claseHoy) {
        this.claseHoy = claseHoy;
    }

    public List<DetalleAsistenciaDTO> getHistorial() {
        return historial;
    }

    public void setHistorial(List<DetalleAsistenciaDTO> historial) {
        this.historial = historial;
    }
}

package com.diplomado.asistencia.dto;

import com.diplomado.asistencia.entity.FechaClase;
import com.diplomado.asistencia.entity.Modulo;

public class DashboardStatsDTO {

    private Modulo moduloActivo;
    private long totalEstudiantes;
    private long totalFechas;
    private long totalPresentes;
    private long totalFaltas;
    private double tasaAsistenciaPromedio;
    private long totalAuditorias;
    private FechaClase proximaClase;

    public DashboardStatsDTO() {
    }

    public Modulo getModuloActivo() {
        return moduloActivo;
    }

    public void setModuloActivo(Modulo moduloActivo) {
        this.moduloActivo = moduloActivo;
    }

    public long getTotalEstudiantes() {
        return totalEstudiantes;
    }

    public void setTotalEstudiantes(long totalEstudiantes) {
        this.totalEstudiantes = totalEstudiantes;
    }

    public long getTotalFechas() {
        return totalFechas;
    }

    public void setTotalFechas(long totalFechas) {
        this.totalFechas = totalFechas;
    }

    public long getTotalPresentes() {
        return totalPresentes;
    }

    public void setTotalPresentes(long totalPresentes) {
        this.totalPresentes = totalPresentes;
    }

    public long getTotalFaltas() {
        return totalFaltas;
    }

    public void setTotalFaltas(long totalFaltas) {
        this.totalFaltas = totalFaltas;
    }

    public double getTasaAsistenciaPromedio() {
        return tasaAsistenciaPromedio;
    }

    public void setTasaAsistenciaPromedio(double tasaAsistenciaPromedio) {
        this.tasaAsistenciaPromedio = tasaAsistenciaPromedio;
    }

    public long getTotalAuditorias() {
        return totalAuditorias;
    }

    public void setTotalAuditorias(long totalAuditorias) {
        this.totalAuditorias = totalAuditorias;
    }

    public FechaClase getProximaClase() {
        return proximaClase;
    }

    public void setProximaClase(FechaClase proximaClase) {
        this.proximaClase = proximaClase;
    }
}

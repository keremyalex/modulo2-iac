package com.diplomado.asistencia.dto;

import com.diplomado.asistencia.entity.FechaClase;
import com.diplomado.asistencia.entity.Modulo;
import java.util.ArrayList;
import java.util.List;

public class MatrizAsistenciaDTO {

    private Modulo modulo;
    private List<FechaClase> fechas = new ArrayList<>();
    private List<FilaMatrizEstudianteDTO> estudiantes = new ArrayList<>();

    public MatrizAsistenciaDTO() {
    }

    public MatrizAsistenciaDTO(Modulo modulo, List<FechaClase> fechas, List<FilaMatrizEstudianteDTO> estudiantes) {
        this.modulo = modulo;
        this.fechas = fechas;
        this.estudiantes = estudiantes;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public List<FechaClase> getFechas() {
        return fechas;
    }

    public void setFechas(List<FechaClase> fechas) {
        this.fechas = fechas;
    }

    public List<FilaMatrizEstudianteDTO> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(List<FilaMatrizEstudianteDTO> estudiantes) {
        this.estudiantes = estudiantes;
    }
}

package com.diplomado.asistencia.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreviewExcelResponse {

    private String nombreArchivo;
    private String tipo; // "ESTUDIANTES" o "FECHAS"
    private int totalFilas;
    private int filasValidas;
    private int filasConError;
    private List<Map<String, Object>> datos = new ArrayList<>();
    private List<String> errores = new ArrayList<>();

    public PreviewExcelResponse() {
    }

    public PreviewExcelResponse(String nombreArchivo, String tipo, int totalFilas, int filasValidas, int filasConError, List<Map<String, Object>> datos, List<String> errores) {
        this.nombreArchivo = nombreArchivo;
        this.tipo = tipo;
        this.totalFilas = totalFilas;
        this.filasValidas = filasValidas;
        this.filasConError = filasConError;
        this.datos = datos;
        this.errores = errores;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getTotalFilas() {
        return totalFilas;
    }

    public void setTotalFilas(int totalFilas) {
        this.totalFilas = totalFilas;
    }

    public int getFilasValidas() {
        return filasValidas;
    }

    public void setFilasValidas(int filasValidas) {
        this.filasValidas = filasValidas;
    }

    public int getFilasConError() {
        return filasConError;
    }

    public void setFilasConError(int filasConError) {
        this.filasConError = filasConError;
    }

    public List<Map<String, Object>> getDatos() {
        return datos;
    }

    public void setDatos(List<Map<String, Object>> datos) {
        this.datos = datos;
    }

    public List<String> getErrores() {
        return errores;
    }

    public void setErrores(List<String> errores) {
        this.errores = errores;
    }
}

package com.diplomado.asistencia.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "modulos")
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 50)
    private String codigo;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaReinicio;

    public Modulo() {
    }

    public Modulo(String nombre, String codigo, boolean activo) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.activo = activo;
        this.fechaCreacion = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaReinicio() {
        return fechaReinicio;
    }

    public void setFechaReinicio(LocalDateTime fechaReinicio) {
        this.fechaReinicio = fechaReinicio;
    }
}

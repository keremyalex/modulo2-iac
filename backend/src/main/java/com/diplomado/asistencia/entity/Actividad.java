package com.diplomado.asistencia.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "actividades")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(nullable = false, length = 100)
    private String accion;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 500)
    private String descripcionRegistro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modulo_id")
    private Modulo modulo;

    public Actividad() {
    }

    public Actividad(String usuario, String accion, String descripcionRegistro, Modulo modulo) {
        this.usuario = usuario;
        this.accion = accion;
        this.descripcionRegistro = descripcionRegistro;
        this.modulo = modulo;
        this.fechaHora = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDescripcionRegistro() {
        return descripcionRegistro;
    }

    public void setDescripcionRegistro(String descripcionRegistro) {
        this.descripcionRegistro = descripcionRegistro;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }
}

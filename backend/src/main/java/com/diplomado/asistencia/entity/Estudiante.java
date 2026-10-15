package com.diplomado.asistencia.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(
    name = "estudiantes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"numeroRegistro", "modulo_id"}, name = "uk_estudiante_registro_modulo")
    }
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String numeroRegistro;

    @Column(nullable = false, length = 150)
    private String nombreCompleto;

    @Column(nullable = false, length = 120)
    private String correo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    public Estudiante() {
    }

    public Estudiante(String numeroRegistro, String nombreCompleto, String correo, Modulo modulo) {
        this.numeroRegistro = numeroRegistro;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.modulo = modulo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }
}

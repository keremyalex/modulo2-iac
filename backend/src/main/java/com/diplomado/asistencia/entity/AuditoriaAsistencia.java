package com.diplomado.asistencia.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias_asistencia")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AuditoriaAsistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asistencia_id", nullable = false)
    private Asistencia asistencia;

    @Column(nullable = false, length = 100)
    private String usuarioModificacion;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 20)
    private String valorAnterior;

    @Column(nullable = false, length = 20)
    private String valorNuevo;

    @Column(length = 255)
    private String motivo;

    public AuditoriaAsistencia() {
    }

    public AuditoriaAsistencia(Asistencia asistencia, String usuarioModificacion, String valorAnterior, String valorNuevo, String motivo) {
        this.asistencia = asistencia;
        this.usuarioModificacion = usuarioModificacion;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.motivo = motivo;
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

    public Asistencia getAsistencia() {
        return asistencia;
    }

    public void setAsistencia(Asistencia asistencia) {
        this.asistencia = asistencia;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNuevo() {
        return valorNuevo;
    }

    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}

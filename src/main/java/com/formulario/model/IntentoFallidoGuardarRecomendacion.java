package com.formulario.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registra cuando un usuario intentó guardar la recomendación de estudios pero falló
 * (error de red, 5xx en servidor, etc.) para poder mostrar en admin que fue la opción 3.
 */
@Entity
@Table(name = "intentos_fallo_guardar_recomendacion")
public class IntentoFallidoGuardarRecomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "examen_id", nullable = false)
    private Long examenId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "mensaje", length = 500)
    private String mensaje;

    /** "error_servidor" = falló en el backend; "error_cliente" = falló antes de llegar (red/timeout). */
    @Column(name = "tipo", length = 20, nullable = false)
    private String tipo;

    public IntentoFallidoGuardarRecomendacion() {}

    public IntentoFallidoGuardarRecomendacion(Long examenId, String tipo, String mensaje) {
        this.examenId = examenId;
        this.fechaHora = LocalDateTime.now();
        this.tipo = tipo != null ? tipo : "error_cliente";
        this.mensaje = mensaje;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamenId() { return examenId; }
    public void setExamenId(Long examenId) { this.examenId = examenId; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}

package com.formulario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "respuestas_examen")
public class RespuestaExamen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id", nullable = false)
    private Examen examen;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;
    
    @NotNull(message = "La respuesta seleccionada es obligatoria")
    private Integer respuestaSeleccionada; // 1, 2, 3, 4
    
    private boolean esCorrecta;
    
    // Constructores
    public RespuestaExamen() {}
    
    public RespuestaExamen(Examen examen, Pregunta pregunta, Integer respuestaSeleccionada) {
        this.examen = examen;
        this.pregunta = pregunta;
        this.respuestaSeleccionada = respuestaSeleccionada;
        this.esCorrecta = respuestaSeleccionada.equals(pregunta.getOpcionCorrecta());
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Examen getExamen() {
        return examen;
    }
    
    public void setExamen(Examen examen) {
        this.examen = examen;
    }
    
    public Pregunta getPregunta() {
        return pregunta;
    }
    
    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }
    
    public Integer getRespuestaSeleccionada() {
        return respuestaSeleccionada;
    }
    
    public void setRespuestaSeleccionada(Integer respuestaSeleccionada) {
        this.respuestaSeleccionada = respuestaSeleccionada;
    }
    
    public boolean isEsCorrecta() {
        return esCorrecta;
    }
    
    public void setEsCorrecta(boolean esCorrecta) {
        this.esCorrecta = esCorrecta;
    }
} 
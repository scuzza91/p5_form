package com.formulario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "opciones")
public class Opcion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El texto de la opción es obligatorio")
    @Column(columnDefinition = "TEXT")
    private String texto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;
    
    private Integer orden; // 1, 2, 3, 4 para identificar la opción
    
    // Constructores
    public Opcion() {}
    
    public Opcion(String texto, Pregunta pregunta, Integer orden) {
        this.texto = texto;
        this.pregunta = pregunta;
        this.orden = orden;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTexto() {
        return texto;
    }
    
    public void setTexto(String texto) {
        this.texto = texto;
    }
    
    public Pregunta getPregunta() {
        return pregunta;
    }
    
    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }
    
    public Integer getOrden() {
        return orden;
    }
    
    public void setOrden(Integer orden) {
        this.orden = orden;
    }
} 
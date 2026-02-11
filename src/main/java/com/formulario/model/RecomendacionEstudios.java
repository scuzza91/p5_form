package com.formulario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recomendaciones_estudios")
public class RecomendacionEstudios {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotBlank(message = "El nombre de la institución es obligatorio")
    @Column(name = "nombre_institucion", nullable = false)
    private String nombreInstitucion;
    
    @NotBlank(message = "El nombre de la oferta es obligatorio")
    @Column(name = "nombre_oferta", nullable = false)
    private String nombreOferta;
    
    @Column(name = "duracion")
    private String duracion; // Ej: "6 meses", "2 años", etc.
    
    @Column(name = "imagen_institucion", columnDefinition = "TEXT")
    private String imagenInstitucion; // URL o path de la imagen
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo debe ser mayor o igual a 0")
    @Column(name = "costo", nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "recomendaciones_estudios_posiciones",
        joinColumns = @JoinColumn(name = "recomendacion_estudios_id"),
        inverseJoinColumns = @JoinColumn(name = "posicion_laboral_id")
    )
    @JsonIgnore
    private Set<PosicionLaboral> posicionesLaborales = new HashSet<>();
    
    @Column(name = "activa")
    private boolean activa = true;
    
    /** Si es true, esta recomendación se muestra a todos los candidatos sin importar el resultado del test. */
    @Column(name = "recomendacion_universal")
    private Boolean recomendacionUniversal = false;
    
    // Constructores
    public RecomendacionEstudios() {}
    
    public RecomendacionEstudios(String nombreInstitucion, String nombreOferta, String duracion, 
                                String imagenInstitucion, String descripcion, BigDecimal costo) {
        this.nombreInstitucion = nombreInstitucion;
        this.nombreOferta = nombreOferta;
        this.duracion = duracion;
        this.imagenInstitucion = imagenInstitucion;
        this.descripcion = descripcion;
        this.costo = costo;
    }
    
    // Métodos auxiliares para manejar la relación
    public void agregarPosicionLaboral(PosicionLaboral posicion) {
        this.posicionesLaborales.add(posicion);
    }
    
    public void removerPosicionLaboral(PosicionLaboral posicion) {
        this.posicionesLaborales.remove(posicion);
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombreInstitucion() {
        return nombreInstitucion;
    }
    
    public void setNombreInstitucion(String nombreInstitucion) {
        this.nombreInstitucion = nombreInstitucion;
    }
    
    public String getNombreOferta() {
        return nombreOferta;
    }
    
    public void setNombreOferta(String nombreOferta) {
        this.nombreOferta = nombreOferta;
    }
    
    public String getDuracion() {
        return duracion;
    }
    
    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }
    
    public String getImagenInstitucion() {
        return imagenInstitucion;
    }
    
    public void setImagenInstitucion(String imagenInstitucion) {
        this.imagenInstitucion = imagenInstitucion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public BigDecimal getCosto() {
        return costo;
    }
    
    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }
    
    public Set<PosicionLaboral> getPosicionesLaborales() {
        return posicionesLaborales;
    }
    
    public void setPosicionesLaborales(Set<PosicionLaboral> posicionesLaborales) {
        this.posicionesLaborales = posicionesLaborales;
    }
    
    public boolean isActiva() {
        return activa;
    }
    
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
    
    /** Devuelve true solo si el valor está explícitamente en true (null en BD se trata como false). */
    public boolean isRecomendacionUniversal() {
        return Boolean.TRUE.equals(recomendacionUniversal);
    }
    
    public void setRecomendacionUniversal(Boolean recomendacionUniversal) {
        this.recomendacionUniversal = recomendacionUniversal != null ? recomendacionUniversal : false;
    }
}


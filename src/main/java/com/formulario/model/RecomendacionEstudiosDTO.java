package com.formulario.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RecomendacionEstudiosDTO {
    
    private Long id;
    private String nombreInstitucion;
    private String nombreOferta;
    private String duracion;
    private String imagenInstitucion;
    private String descripcion;
    private BigDecimal costo;
    private boolean activa;
    private List<Long> posicionesLaboralesIds;
    private List<String> posicionesLaboralesTitulos; // Para mostrar en el frontend
    
    public RecomendacionEstudiosDTO() {
        this.posicionesLaboralesIds = new ArrayList<>();
        this.posicionesLaboralesTitulos = new ArrayList<>();
    }
    
    public RecomendacionEstudiosDTO(RecomendacionEstudios recomendacion) {
        this.id = recomendacion.getId();
        this.nombreInstitucion = recomendacion.getNombreInstitucion();
        this.nombreOferta = recomendacion.getNombreOferta();
        this.duracion = recomendacion.getDuracion();
        this.imagenInstitucion = recomendacion.getImagenInstitucion();
        this.descripcion = recomendacion.getDescripcion();
        this.costo = recomendacion.getCosto();
        this.activa = recomendacion.isActiva();
        this.posicionesLaboralesIds = new ArrayList<>();
        this.posicionesLaboralesTitulos = new ArrayList<>();
        
        // Mapear posiciones laborales
        if (recomendacion.getPosicionesLaborales() != null) {
            recomendacion.getPosicionesLaborales().forEach(posicion -> {
                this.posicionesLaboralesIds.add(posicion.getId());
                this.posicionesLaboralesTitulos.add(posicion.getTitulo());
            });
        }
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
    
    public boolean isActiva() {
        return activa;
    }
    
    public void setActiva(boolean activa) {
        this.activa = activa;
    }
    
    public List<Long> getPosicionesLaboralesIds() {
        return posicionesLaboralesIds;
    }
    
    public void setPosicionesLaboralesIds(List<Long> posicionesLaboralesIds) {
        this.posicionesLaboralesIds = posicionesLaboralesIds;
    }
    
    public List<String> getPosicionesLaboralesTitulos() {
        return posicionesLaboralesTitulos;
    }
    
    public void setPosicionesLaboralesTitulos(List<String> posicionesLaboralesTitulos) {
        this.posicionesLaboralesTitulos = posicionesLaboralesTitulos;
    }
}


package com.formulario.model;

import java.util.ArrayList;
import java.util.List;

public class RecomendacionRolDTO {
    private Long rolId;
    private String titulo;
    private String descripcion;
    private String nivel;
    private String categoria;
    private String responsabilidades;
    private String habilidadesRequeridas;
    private String tecnologiasRecomendadas;
    private String rutaCarrera;
    private double compatibilidad;
    private String nivelCompatibilidad; // Excelente, Muy Buena, Buena, Regular, Baja
    
    // Puntuaciones del candidato para comparación
    private Integer logica;
    private Integer matematica;
    private Integer creatividad;
    private Integer programacion;
    private Double promedio;
    
    // Requisitos del rol
    private Integer minLogica;
    private Integer minMatematica;
    private Integer minCreatividad;
    private Integer minProgramacion;
    private Integer minPromedio;
    
    // Recomendaciones de estudios vinculadas a este rol
    private List<RecomendacionEstudiosDTO> recomendacionesEstudios;
    
    public RecomendacionRolDTO() {
        this.recomendacionesEstudios = new ArrayList<>();
    }
    
    public RecomendacionRolDTO(RolProfesional rol, Examen examen, double compatibilidad) {
        this.rolId = rol.getId();
        this.titulo = rol.getTitulo();
        this.descripcion = rol.getDescripcion();
        this.nivel = rol.getNivel();
        this.categoria = rol.getCategoria();
        this.responsabilidades = rol.getResponsabilidades();
        this.habilidadesRequeridas = rol.getHabilidadesRequeridas();
        this.tecnologiasRecomendadas = rol.getTecnologiasRecomendadas();
        this.rutaCarrera = rol.getRutaCarrera();
        this.compatibilidad = compatibilidad;
        this.nivelCompatibilidad = determinarNivelCompatibilidad(compatibilidad);
        
        // Puntuaciones del candidato
        this.logica = examen.getLogica();
        this.matematica = examen.getMatematica();
        this.creatividad = examen.getCreatividad();
        this.programacion = examen.getProgramacion();
        this.promedio = examen.getPromedio();
        
        // Requisitos del rol
        this.minLogica = rol.getMinLogica();
        this.minMatematica = rol.getMinMatematica();
        this.minCreatividad = rol.getMinCreatividad();
        this.minProgramacion = rol.getMinProgramacion();
        this.minPromedio = rol.getMinPromedio();
        
        // Inicializar lista de recomendaciones de estudios
        this.recomendacionesEstudios = new ArrayList<>();
    }
    
    private String determinarNivelCompatibilidad(double compatibilidad) {
        if (compatibilidad >= 90) return "Excelente";
        if (compatibilidad >= 80) return "Muy Buena";
        if (compatibilidad >= 70) return "Buena";
        if (compatibilidad >= 60) return "Regular";
        return "Baja";
    }
    
    // Getters y Setters
    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public String getResponsabilidades() { return responsabilidades; }
    public void setResponsabilidades(String responsabilidades) { this.responsabilidades = responsabilidades; }
    
    public String getHabilidadesRequeridas() { return habilidadesRequeridas; }
    public void setHabilidadesRequeridas(String habilidadesRequeridas) { this.habilidadesRequeridas = habilidadesRequeridas; }
    
    public String getTecnologiasRecomendadas() { return tecnologiasRecomendadas; }
    public void setTecnologiasRecomendadas(String tecnologiasRecomendadas) { this.tecnologiasRecomendadas = tecnologiasRecomendadas; }
    
    public String getRutaCarrera() { return rutaCarrera; }
    public void setRutaCarrera(String rutaCarrera) { this.rutaCarrera = rutaCarrera; }
    
    public double getCompatibilidad() { return compatibilidad; }
    public void setCompatibilidad(double compatibilidad) { this.compatibilidad = compatibilidad; }
    
    public String getNivelCompatibilidad() { return nivelCompatibilidad; }
    public void setNivelCompatibilidad(String nivelCompatibilidad) { this.nivelCompatibilidad = nivelCompatibilidad; }
    
    public Integer getLogica() { return logica; }
    public void setLogica(Integer logica) { this.logica = logica; }
    
    public Integer getMatematica() { return matematica; }
    public void setMatematica(Integer matematica) { this.matematica = matematica; }
    
    public Integer getCreatividad() { return creatividad; }
    public void setCreatividad(Integer creatividad) { this.creatividad = creatividad; }
    
    public Integer getProgramacion() { return programacion; }
    public void setProgramacion(Integer programacion) { this.programacion = programacion; }
    
    public Double getPromedio() { return promedio; }
    public void setPromedio(Double promedio) { this.promedio = promedio; }
    
    public Integer getMinLogica() { return minLogica; }
    public void setMinLogica(Integer minLogica) { this.minLogica = minLogica; }
    
    public Integer getMinMatematica() { return minMatematica; }
    public void setMinMatematica(Integer minMatematica) { this.minMatematica = minMatematica; }
    
    public Integer getMinCreatividad() { return minCreatividad; }
    public void setMinCreatividad(Integer minCreatividad) { this.minCreatividad = minCreatividad; }
    
    public Integer getMinProgramacion() { return minProgramacion; }
    public void setMinProgramacion(Integer minProgramacion) { this.minProgramacion = minProgramacion; }
    
    public Integer getMinPromedio() { return minPromedio; }
    public void setMinPromedio(Integer minPromedio) { this.minPromedio = minPromedio; }
    
    public List<RecomendacionEstudiosDTO> getRecomendacionesEstudios() {
        return recomendacionesEstudios;
    }
    
    public void setRecomendacionesEstudios(List<RecomendacionEstudiosDTO> recomendacionesEstudios) {
        this.recomendacionesEstudios = recomendacionesEstudios;
    }
    
    @Override
    public String toString() {
        return "RecomendacionRolDTO{" +
                "rolId=" + rolId +
                ", titulo='" + titulo + '\'' +
                ", nivel='" + nivel + '\'' +
                ", categoria='" + categoria + '\'' +
                ", compatibilidad=" + compatibilidad +
                '}';
    }
} 
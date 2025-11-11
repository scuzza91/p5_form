package com.formulario.model;

public class RecomendacionDTO {
    private Long posicionId;
    private String titulo;
    private String descripcion;
    private String nivel;
    private String categoria;
    private String empresa;
    private String ubicacion;
    private String tipoContrato;
    private String modalidad;
    private double compatibilidad;
    private String nivelCompatibilidad; // Excelente, Muy Buena, Buena, Regular, Baja
    
    // Puntuaciones del candidato para comparación
    private Integer logica;
    private Integer matematica;
    private Integer creatividad;
    private Integer programacion;
    private Double promedio;
    
    // Requisitos de la posición
    private Integer minLogica;
    private Integer minMatematica;
    private Integer minCreatividad;
    private Integer minProgramacion;
    private Integer minPromedio;
    
    public RecomendacionDTO() {}
    
    public RecomendacionDTO(PosicionLaboral posicion, Examen examen, double compatibilidad) {
        this.posicionId = posicion.getId();
        this.titulo = posicion.getTitulo();
        this.descripcion = posicion.getDescripcion();
        this.nivel = posicion.getNivel();
        this.categoria = posicion.getCategoria();
        this.empresa = posicion.getEmpresa();
        this.ubicacion = posicion.getUbicacion();
        this.tipoContrato = posicion.getTipoContrato();
        this.modalidad = posicion.getModalidad();
        this.compatibilidad = compatibilidad;
        this.nivelCompatibilidad = determinarNivelCompatibilidad(compatibilidad);
        
        // Puntuaciones del candidato
        this.logica = examen.getLogica();
        this.matematica = examen.getMatematica();
        this.creatividad = examen.getCreatividad();
        this.programacion = examen.getProgramacion();
        this.promedio = examen.getPromedio();
        
        // Requisitos de la posición
        this.minLogica = posicion.getMinLogica();
        this.minMatematica = posicion.getMinMatematica();
        this.minCreatividad = posicion.getMinCreatividad();
        this.minProgramacion = posicion.getMinProgramacion();
        this.minPromedio = posicion.getMinPromedio();
    }
    
    private String determinarNivelCompatibilidad(double compatibilidad) {
        if (compatibilidad >= 90) return "Excelente";
        if (compatibilidad >= 80) return "Muy Buena";
        if (compatibilidad >= 70) return "Buena";
        if (compatibilidad >= 60) return "Regular";
        return "Baja";
    }
    
    // Getters y Setters
    public Long getPosicionId() { return posicionId; }
    public void setPosicionId(Long posicionId) { this.posicionId = posicionId; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    
    public String getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(String tipoContrato) { this.tipoContrato = tipoContrato; }
    
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    
    public double getCompatibilidad() { return compatibilidad; }
    public void setCompatibilidad(double compatibilidad) { 
        this.compatibilidad = compatibilidad;
        this.nivelCompatibilidad = determinarNivelCompatibilidad(compatibilidad);
    }
    
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
} 
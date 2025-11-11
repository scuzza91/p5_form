package com.formulario.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles_profesionales")
public class RolProfesional {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(nullable = false)
    private String nivel; // Junior, Semi-Senior, Senior, Lead
    
    @Column(nullable = false)
    private String categoria; // Desarrollo, Análisis, Gestión, Infraestructura, Diseño
    
    @Column(columnDefinition = "TEXT")
    private String responsabilidades;
    
    @Column(columnDefinition = "TEXT")
    private String habilidadesRequeridas;
    
    @Column(columnDefinition = "TEXT")
    private String tecnologiasRecomendadas;
    
    @Column(columnDefinition = "TEXT")
    private String rutaCarrera;
    
    // Requisitos mínimos de habilidades
    private Integer minLogica;
    private Integer minMatematica;
    private Integer minCreatividad;
    private Integer minProgramacion;
    private Integer minPromedio;
    
    // Pesos para el cálculo de compatibilidad
    private Integer pesoLogica;
    private Integer pesoMatematica;
    private Integer pesoCreatividad;
    private Integer pesoProgramacion;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    // Constructores
    public RolProfesional() {}
    
    public RolProfesional(String titulo, String descripcion, String nivel, String categoria) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.categoria = categoria;
    }
    
    // Método para calcular compatibilidad con un candidato
    public double calcularCompatibilidad(Examen examen) {
        try {
            if (examen == null) {
                System.out.println("Examen es null");
                return 0.0;
            }
            
            System.out.println("Calculando compatibilidad para rol: " + this.titulo);
            System.out.println("Examen ID: " + examen.getId());
            System.out.println("Puntuaciones del examen - Lógica: " + examen.getLogica() + 
                             ", Matemática: " + examen.getMatematica() + 
                             ", Creatividad: " + examen.getCreatividad() + 
                             ", Programación: " + examen.getProgramacion() + 
                             ", Promedio: " + examen.getPromedio());
            
            double compatibilidad = 0.0;
            double pesoTotal = 0.0;
            
            // Verificar requisitos mínimos
            if (minPromedio != null && examen.getPromedio() < minPromedio) {
                System.out.println("No cumple promedio mínimo: " + examen.getPromedio() + " < " + minPromedio);
                return 0.0; // No cumple el promedio mínimo
            }
            
            if (minLogica != null && 
                (examen.getLogica() == null || examen.getLogica() < minLogica)) {
                System.out.println("No cumple lógica mínima: " + examen.getLogica() + " < " + minLogica);
                return 0.0;
            }
            
            if (minMatematica != null && 
                (examen.getMatematica() == null || examen.getMatematica() < minMatematica)) {
                System.out.println("No cumple matemática mínima: " + examen.getMatematica() + " < " + minMatematica);
                return 0.0;
            }
            
            if (minCreatividad != null && 
                (examen.getCreatividad() == null || examen.getCreatividad() < minCreatividad)) {
                System.out.println("No cumple creatividad mínima: " + examen.getCreatividad() + " < " + minCreatividad);
                return 0.0;
            }
            
            if (minProgramacion != null && 
                (examen.getProgramacion() == null || examen.getProgramacion() < minProgramacion)) {
                System.out.println("No cumple programación mínima: " + examen.getProgramacion() + " < " + minProgramacion);
                return 0.0;
            }
            
            // Calcular compatibilidad ponderada
            if (pesoLogica != null && pesoLogica > 0 && examen.getLogica() != null) {
                // Si cumple el mínimo, calcular score basado en qué tan bien lo supera
                double scoreLogica;
                if (examen.getLogica() >= minLogica) {
                    // Score máximo 100% si cumple exactamente el mínimo, hasta 120% si lo supera mucho
                    scoreLogica = Math.min(120.0, 100.0 + ((examen.getLogica() - minLogica) / 10.0));
                } else {
                    scoreLogica = 0.0; // No cumple el mínimo
                }
                compatibilidad += scoreLogica * pesoLogica;
                pesoTotal += pesoLogica;
                System.out.println("Lógica - Score: " + scoreLogica + ", Peso: " + pesoLogica);
            }
            
            if (pesoMatematica != null && pesoMatematica > 0 && examen.getMatematica() != null) {
                double scoreMatematica;
                if (examen.getMatematica() >= minMatematica) {
                    scoreMatematica = Math.min(120.0, 100.0 + ((examen.getMatematica() - minMatematica) / 10.0));
                } else {
                    scoreMatematica = 0.0;
                }
                compatibilidad += scoreMatematica * pesoMatematica;
                pesoTotal += pesoMatematica;
                System.out.println("Matemática - Score: " + scoreMatematica + ", Peso: " + pesoMatematica);
            }
            
            if (pesoCreatividad != null && pesoCreatividad > 0 && examen.getCreatividad() != null) {
                double scoreCreatividad;
                if (examen.getCreatividad() >= minCreatividad) {
                    scoreCreatividad = Math.min(120.0, 100.0 + ((examen.getCreatividad() - minCreatividad) / 10.0));
                } else {
                    scoreCreatividad = 0.0;
                }
                compatibilidad += scoreCreatividad * pesoCreatividad;
                pesoTotal += pesoCreatividad;
                System.out.println("Creatividad - Score: " + scoreCreatividad + ", Peso: " + pesoCreatividad);
            }
            
            if (pesoProgramacion != null && pesoProgramacion > 0 && examen.getProgramacion() != null) {
                double scoreProgramacion;
                if (examen.getProgramacion() >= minProgramacion) {
                    scoreProgramacion = Math.min(120.0, 100.0 + ((examen.getProgramacion() - minProgramacion) / 10.0));
                } else {
                    scoreProgramacion = 0.0;
                }
                compatibilidad += scoreProgramacion * pesoProgramacion;
                pesoTotal += pesoProgramacion;
                System.out.println("Programación - Score: " + scoreProgramacion + ", Peso: " + pesoProgramacion);
            }
            
            if (pesoTotal > 0) {
                compatibilidad = compatibilidad / pesoTotal;
                System.out.println("Compatibilidad final: " + compatibilidad);
                return Math.min(100.0, compatibilidad);
            } else {
                System.out.println("No hay pesos definidos, retornando 0");
                return 0.0;
            }
            
        } catch (Exception e) {
            System.err.println("Error al calcular compatibilidad: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public Integer getPesoLogica() { return pesoLogica; }
    public void setPesoLogica(Integer pesoLogica) { this.pesoLogica = pesoLogica; }
    
    public Integer getPesoMatematica() { return pesoMatematica; }
    public void setPesoMatematica(Integer pesoMatematica) { this.pesoMatematica = pesoMatematica; }
    
    public Integer getPesoCreatividad() { return pesoCreatividad; }
    public void setPesoCreatividad(Integer pesoCreatividad) { this.pesoCreatividad = pesoCreatividad; }
    
    public Integer getPesoProgramacion() { return pesoProgramacion; }
    public void setPesoProgramacion(Integer pesoProgramacion) { this.pesoProgramacion = pesoProgramacion; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    @Override
    public String toString() {
        return "RolProfesional{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", nivel='" + nivel + '\'' +
                ", categoria='" + categoria + '\'' +
                '}';
    }
} 
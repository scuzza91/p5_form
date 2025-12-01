package com.formulario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posiciones_laborales")
public class PosicionLaboral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotBlank(message = "El título de la posición es obligatorio")
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(columnDefinition = "TEXT")
    private String responsabilidades;
    
    @Column(columnDefinition = "TEXT")
    private String requisitos;
    
    private String nivel; // Junior, Semi-Senior, Senior, Lead
    private String categoria; // Desarrollo, Análisis, Diseño, Gestión, etc.
    private String empresa;
    private String ubicacion;
    private String tipoContrato; // Full-time, Part-time, Freelance
    private String modalidad; // Presencial, Remoto, Híbrido
    
    // Puntuaciones mínimas requeridas por área (0-100)
    @Min(0) @Max(100)
    private Integer minLogica;
    
    @Min(0) @Max(100)
    private Integer minMatematica;
    
    @Min(0) @Max(100)
    private Integer minCreatividad;
    
    @Min(0) @Max(100)
    private Integer minProgramacion;
    
    @Min(0) @Max(100)
    private Integer minPromedio;
    
    // Peso de cada área para esta posición (0-100)
    @Min(0) @Max(100)
    private Integer pesoLogica;
    
    @Min(0) @Max(100)
    private Integer pesoMatematica;
    
    @Min(0) @Max(100)
    private Integer pesoCreatividad;
    
    @Min(0) @Max(100)
    private Integer pesoProgramacion;
    
    private boolean activa = true;
    
    @ManyToMany(mappedBy = "posicionesLaborales", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<RecomendacionEstudios> recomendacionesEstudios = new HashSet<>();
    
    // Constructores
    public PosicionLaboral() {}
    
    public PosicionLaboral(String titulo, String descripcion, String nivel, String categoria) {
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
            
            System.out.println("Calculando compatibilidad para posición: " + this.titulo);
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
            if (pesoLogica != null && examen.getLogica() != null) {
                compatibilidad += (examen.getLogica() * pesoLogica);
                pesoTotal += pesoLogica;
                System.out.println("Lógica: " + examen.getLogica() + " * " + pesoLogica + " = " + (examen.getLogica() * pesoLogica));
            }
            
            if (pesoMatematica != null && examen.getMatematica() != null) {
                compatibilidad += (examen.getMatematica() * pesoMatematica);
                pesoTotal += pesoMatematica;
                System.out.println("Matemática: " + examen.getMatematica() + " * " + pesoMatematica + " = " + (examen.getMatematica() * pesoMatematica));
            }
            
            if (pesoCreatividad != null && examen.getCreatividad() != null) {
                compatibilidad += (examen.getCreatividad() * pesoCreatividad);
                pesoTotal += pesoCreatividad;
                System.out.println("Creatividad: " + examen.getCreatividad() + " * " + pesoCreatividad + " = " + (examen.getCreatividad() * pesoCreatividad));
            }
            
            if (pesoProgramacion != null && examen.getProgramacion() != null) {
                compatibilidad += (examen.getProgramacion() * pesoProgramacion);
                pesoTotal += pesoProgramacion;
                System.out.println("Programación: " + examen.getProgramacion() + " * " + pesoProgramacion + " = " + (examen.getProgramacion() * pesoProgramacion));
            }
            
            double resultado = pesoTotal > 0 ? compatibilidad / pesoTotal : 0.0;
            System.out.println("Compatibilidad final: " + compatibilidad + " / " + pesoTotal + " = " + resultado);
            
            return resultado;
            
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
    
    public String getResponsabilidades() { return responsabilidades; }
    public void setResponsabilidades(String responsabilidades) { this.responsabilidades = responsabilidades; }
    
    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }
    
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
    
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    
    public Set<RecomendacionEstudios> getRecomendacionesEstudios() {
        return recomendacionesEstudios;
    }
    
    public void setRecomendacionesEstudios(Set<RecomendacionEstudios> recomendacionesEstudios) {
        this.recomendacionesEstudios = recomendacionesEstudios;
    }
} 
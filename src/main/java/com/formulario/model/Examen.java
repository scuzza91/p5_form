package com.formulario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.formulario.model.Pregunta.AreaConocimiento;

@Entity
@Table(name = "examenes")
public class Examen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "persona_id")
    private Persona persona;
    
    // Puntuaciones calculadas por área
    private Integer logica;
    private Integer matematica;
    private Integer creatividad;
    private Integer programacion;
    
    // Información del examen
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer tiempoTotalMinutos;
    private Integer totalPreguntas;
    private Integer respuestasCorrectas;
    
    private String comentarios;
    
    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RespuestaExamen> respuestas;
    
    // Constructores
    public Examen() {}
    
    public Examen(Persona persona) {
        this.persona = persona;
        this.fechaInicio = LocalDateTime.now();
    }
    
    // Método para calcular el promedio
    public double getPromedio() {
        List<Integer> puntuaciones = Arrays.asList(
            logica, matematica, creatividad, programacion
        );
        
        List<Integer> puntuacionesValidas = puntuaciones.stream()
            .filter(p -> p != null)
            .collect(Collectors.toList());
            
        if (puntuacionesValidas.isEmpty()) {
            return 0.0;
        }
        
        return puntuacionesValidas.stream()
            .mapToDouble(Integer::doubleValue)
            .average()
            .orElse(0.0);
    }
    
    // Método para determinar si aprobó (promedio >= 70)
    public boolean isAprobado() {
        return getPromedio() >= 70;
    }
    
    // Método para calcular puntuación por área
    public void calcularPuntuaciones() {
        if (respuestas == null || respuestas.isEmpty()) {
            return;
        }
        
        // Contadores para cada área
        Map<AreaConocimiento, Integer> correctasPorArea = new HashMap<>();
        Map<AreaConocimiento, Integer> totalPorArea = new HashMap<>();
        
        // Inicializar contadores
        for (AreaConocimiento area : AreaConocimiento.values()) {
            correctasPorArea.put(area, 0);
            totalPorArea.put(area, 0);
        }
        
        // Contar respuestas correctas e incorrectas por área
        for (RespuestaExamen respuesta : respuestas) {
            AreaConocimiento area = respuesta.getPregunta().getAreaConocimiento();
            totalPorArea.put(area, totalPorArea.get(area) + 1);
            
            if (respuesta.isEsCorrecta()) {
                correctasPorArea.put(area, correctasPorArea.get(area) + 1);
            }
        }
        
        // Calcular porcentajes por área
        this.logica = calcularPorcentaje(correctasPorArea, totalPorArea, AreaConocimiento.LOGICA);
        this.matematica = calcularPorcentaje(correctasPorArea, totalPorArea, AreaConocimiento.MATEMATICA);
        this.creatividad = calcularPorcentaje(correctasPorArea, totalPorArea, AreaConocimiento.CREATIVIDAD);
        this.programacion = calcularPorcentaje(correctasPorArea, totalPorArea, AreaConocimiento.PROGRAMACION);
        
        // Calcular totales
        this.totalPreguntas = respuestas.size();
        this.respuestasCorrectas = (int) respuestas.stream().filter(RespuestaExamen::isEsCorrecta).count();
    }
    
    // Método auxiliar para calcular porcentaje
    private Integer calcularPorcentaje(Map<AreaConocimiento, Integer> correctas, Map<AreaConocimiento, Integer> totales, AreaConocimiento area) {
        int total = totales.get(area);
        if (total == 0) return 0;
        return (correctas.get(area) * 100) / total;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Persona getPersona() {
        return persona;
    }
    
    public void setPersona(Persona persona) {
        this.persona = persona;
    }
    
    public Integer getLogica() {
        return logica;
    }
    
    public void setLogica(Integer logica) {
        this.logica = logica;
    }
    
    public Integer getMatematica() {
        return matematica;
    }
    
    public void setMatematica(Integer matematica) {
        this.matematica = matematica;
    }
    
    public Integer getCreatividad() {
        return creatividad;
    }
    
    public void setCreatividad(Integer creatividad) {
        this.creatividad = creatividad;
    }
    
    public Integer getProgramacion() {
        return programacion;
    }
    
    public void setProgramacion(Integer programacion) {
        this.programacion = programacion;
    }
    
    // Métodos para compatibilidad con el código existente
    public Integer getProgramacionBasica() {
        return programacion;
    }
    
    public Integer getEstructurasDatos() {
        return logica;
    }
    
    public Integer getAlgoritmos() {
        return matematica;
    }
    
    public Integer getBaseDatos() {
        return creatividad;
    }
    
    public String getComentarios() {
        return comentarios;
    }
    
    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
    
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDateTime getFechaFin() {
        return fechaFin;
    }
    
    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }
    
    public Integer getTiempoTotalMinutos() {
        return tiempoTotalMinutos;
    }
    
    public void setTiempoTotalMinutos(Integer tiempoTotalMinutos) {
        this.tiempoTotalMinutos = tiempoTotalMinutos;
    }
    
    public Integer getTotalPreguntas() {
        return totalPreguntas;
    }
    
    public void setTotalPreguntas(Integer totalPreguntas) {
        this.totalPreguntas = totalPreguntas;
    }
    
    public Integer getRespuestasCorrectas() {
        return respuestasCorrectas;
    }
    
    public void setRespuestasCorrectas(Integer respuestasCorrectas) {
        this.respuestasCorrectas = respuestasCorrectas;
    }
    
    public List<RespuestaExamen> getRespuestas() {
        return respuestas;
    }
    
    public void setRespuestas(List<RespuestaExamen> respuestas) {
        this.respuestas = respuestas;
    }
    

} 
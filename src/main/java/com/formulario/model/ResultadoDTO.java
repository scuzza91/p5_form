package com.formulario.model;

import java.time.LocalDateTime;

public class ResultadoDTO {
    private Long examenId;
    private Long personaId;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String fechaNacimiento;
    private String direccion;
    private String cuil;
    private String provinciaNombre;
    private String localidadNombre;
    private String conocimientosProgramacion;
    
    // Puntuaciones del examen
    private Integer logica;
    private Integer matematica;
    private Integer creatividad;
    private Integer programacion;
    private Double promedio;
    private Boolean aprobado;
    
    // Información del examen
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer tiempoTotalMinutos;
    private Integer totalPreguntas;
    private Integer respuestasCorrectas;
    private String comentarios;
    
    public ResultadoDTO() {}
    
    public ResultadoDTO(Examen examen) {
        this.examenId = examen.getId();
        this.personaId = examen.getPersona().getId();
        this.nombre = examen.getPersona().getNombre();
        this.apellido = examen.getPersona().getApellido();
        this.email = examen.getPersona().getEmail();
        this.telefono = examen.getPersona().getTelefono();
        this.fechaNacimiento = examen.getPersona().getFechaNacimiento();
        this.direccion = examen.getPersona().getDireccion();
        this.cuil = examen.getPersona().getCuil();
        
        // Cargar nombres de provincia y localidad de forma segura
        if (examen.getPersona().getProvincia() != null) {
            this.provinciaNombre = examen.getPersona().getProvincia().getNombre();
        }
        if (examen.getPersona().getLocalidad() != null) {
            this.localidadNombre = examen.getPersona().getLocalidad().getNombre();
        }
        
        // Conocimientos de programación
        this.conocimientosProgramacion = examen.getPersona().getConocimientosProgramacion();
        
        // Puntuaciones
        this.logica = examen.getLogica();
        this.matematica = examen.getMatematica();
        this.creatividad = examen.getCreatividad();
        this.programacion = examen.getProgramacion();
        this.promedio = examen.getPromedio();
        this.aprobado = examen.isAprobado();
        
        // Información del examen
        this.fechaInicio = examen.getFechaInicio();
        this.fechaFin = examen.getFechaFin();
        this.tiempoTotalMinutos = examen.getTiempoTotalMinutos();
        this.totalPreguntas = examen.getTotalPreguntas();
        this.respuestasCorrectas = examen.getRespuestasCorrectas();
        this.comentarios = examen.getComentarios();
    }
    
    // Getters y Setters
    public Long getExamenId() { return examenId; }
    public void setExamenId(Long examenId) { this.examenId = examenId; }
    
    public Long getPersonaId() { return personaId; }
    public void setPersonaId(Long personaId) { this.personaId = personaId; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getCuil() { return cuil; }
    public void setCuil(String cuil) { this.cuil = cuil; }
    
    public String getProvinciaNombre() { return provinciaNombre; }
    public void setProvinciaNombre(String provinciaNombre) { this.provinciaNombre = provinciaNombre; }
    
    public String getLocalidadNombre() { return localidadNombre; }
    public void setLocalidadNombre(String localidadNombre) { this.localidadNombre = localidadNombre; }
    
    public String getConocimientosProgramacion() { return conocimientosProgramacion; }
    public void setConocimientosProgramacion(String conocimientosProgramacion) { this.conocimientosProgramacion = conocimientosProgramacion; }
    
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
    
    public Boolean getAprobado() { return aprobado; }
    public void setAprobado(Boolean aprobado) { this.aprobado = aprobado; }
    
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    
    public Integer getTiempoTotalMinutos() { return tiempoTotalMinutos; }
    public void setTiempoTotalMinutos(Integer tiempoTotalMinutos) { this.tiempoTotalMinutos = tiempoTotalMinutos; }
    
    public Integer getTotalPreguntas() { return totalPreguntas; }
    public void setTotalPreguntas(Integer totalPreguntas) { this.totalPreguntas = totalPreguntas; }
    
    public Integer getRespuestasCorrectas() { return respuestasCorrectas; }
    public void setRespuestasCorrectas(Integer respuestasCorrectas) { this.respuestasCorrectas = respuestasCorrectas; }
    
    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
    

} 
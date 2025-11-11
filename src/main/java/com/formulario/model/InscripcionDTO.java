package com.formulario.model;

public class InscripcionDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni; // Extraído del CUIL
    private String cuil;
    private String email;
    private String trabajaActualmente;
    private String trabajaSectorIT;
    private Integer programacionBasica;
    private Integer estructurasDatos;
    private Integer algoritmos;
    private Integer baseDatos;
    private Double promedio;
    private Boolean aprobado;
    private String fechaExamen;

    public InscripcionDTO() {}

    public InscripcionDTO(Long id, String nombre, String apellido, String cuil, String email,
                         String trabajaActualmente, String trabajaSectorIT,
                         Integer logica, Integer matematica, Integer creatividad, 
                         Integer programacion, Double promedio, Boolean aprobado, String fechaExamen) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.cuil = cuil;
        this.dni = cuil != null && cuil.length() >= 8 ? cuil.substring(2, 10) : "";
        this.email = email;
        this.trabajaActualmente = trabajaActualmente;
        this.trabajaSectorIT = trabajaSectorIT;
        this.programacionBasica = logica; // Mapear a los campos existentes para compatibilidad
        this.estructurasDatos = matematica;
        this.algoritmos = creatividad;
        this.baseDatos = programacion;
        this.promedio = promedio;
        this.aprobado = aprobado;
        this.fechaExamen = fechaExamen;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCuil() {
        return cuil;
    }

    public void setCuil(String cuil) {
        this.cuil = cuil;
        this.dni = cuil != null && cuil.length() >= 8 ? cuil.substring(2, 10) : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTrabajaActualmente() {
        return trabajaActualmente;
    }

    public void setTrabajaActualmente(String trabajaActualmente) {
        this.trabajaActualmente = trabajaActualmente;
    }

    public String getTrabajaSectorIT() {
        return trabajaSectorIT;
    }

    public void setTrabajaSectorIT(String trabajaSectorIT) {
        this.trabajaSectorIT = trabajaSectorIT;
    }

    public Integer getProgramacionBasica() {
        return programacionBasica;
    }

    public void setProgramacionBasica(Integer programacionBasica) {
        this.programacionBasica = programacionBasica;
    }

    public Integer getEstructurasDatos() {
        return estructurasDatos;
    }

    public void setEstructurasDatos(Integer estructurasDatos) {
        this.estructurasDatos = estructurasDatos;
    }

    public Integer getAlgoritmos() {
        return algoritmos;
    }

    public void setAlgoritmos(Integer algoritmos) {
        this.algoritmos = algoritmos;
    }

    public Integer getBaseDatos() {
        return baseDatos;
    }

    public void setBaseDatos(Integer baseDatos) {
        this.baseDatos = baseDatos;
    }

    public Double getPromedio() {
        return promedio;
    }

    public void setPromedio(Double promedio) {
        this.promedio = promedio;
    }

    public Boolean getAprobado() {
        return aprobado;
    }

    public void setAprobado(Boolean aprobado) {
        this.aprobado = aprobado;
    }

    public String getFechaExamen() {
        return fechaExamen;
    }

    public void setFechaExamen(String fechaExamen) {
        this.fechaExamen = fechaExamen;
    }
    
    // Método para calcular el promedio automáticamente
    public void calcularPromedio() {
        if (programacionBasica != null && estructurasDatos != null && 
            algoritmos != null && baseDatos != null) {
            this.promedio = (programacionBasica + estructurasDatos + algoritmos + baseDatos) / 4.0;
        }
    }
    
    // Método para obtener el promedio calculado
    public Double getPromedioCalculado() {
        if (programacionBasica != null && estructurasDatos != null && 
            algoritmos != null && baseDatos != null) {
            return (programacionBasica + estructurasDatos + algoritmos + baseDatos) / 4.0;
        }
        return null;
    }
} 
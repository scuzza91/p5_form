package com.formulario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "personas")
public class Persona {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String apellido;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    private String telefono;
    
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private String fechaNacimiento;
    
    @NotBlank(message = "El género es obligatorio")
    private String genero;
    
    @NotBlank(message = "Los conocimientos de programación son obligatorios")
    private String conocimientosProgramacion;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;
    
    @NotBlank(message = "Debe indicar si cuenta con Internet en su hogar")
    private String internetHogar;
    
    @NotBlank(message = "Debe indicar si trabaja actualmente")
    private String trabajaActualmente;
    
    private String trabajaSectorIT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provincia_id", nullable = false)
    private Provincia provincia;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "localidad_id", nullable = false)
    private Localidad localidad;
    
    @NotBlank(message = "El CUIL es obligatorio")
    @Pattern(regexp = "^[0-9]{11}$", message = "El CUIL debe tener exactamente 11 dígitos")
    private String cuil;
    
    // ID del caso en Bondarea (opcional, solo para personas creadas desde Bondarea)
    @Column(name = "id_caso_bondarea")
    private String idCasoBondarea;
    
    // Constructores
    public Persona() {}
    
    public Persona(String nombre, String apellido, String email, String telefono, 
                   String fechaNacimiento, String genero, String conocimientosProgramacion, 
                   String direccion, String internetHogar, String trabajaActualmente, String trabajaSectorIT,
                   Provincia provincia, Localidad localidad, String cuil) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.conocimientosProgramacion = conocimientosProgramacion;
        this.direccion = direccion;
        this.internetHogar = internetHogar;
        this.trabajaActualmente = trabajaActualmente;
        this.trabajaSectorIT = trabajaSectorIT;
        this.provincia = provincia;
        this.localidad = localidad;
        this.cuil = cuil;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public String getConocimientosProgramacion() {
        return conocimientosProgramacion;
    }
    
    public void setConocimientosProgramacion(String conocimientosProgramacion) {
        this.conocimientosProgramacion = conocimientosProgramacion;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public String getInternetHogar() {
        return internetHogar;
    }
    
    public void setInternetHogar(String internetHogar) {
        this.internetHogar = internetHogar;
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
    
    public Provincia getProvincia() {
        return provincia;
    }
    
    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }
    
    public Localidad getLocalidad() {
        return localidad;
    }
    
    public void setLocalidad(Localidad localidad) {
        this.localidad = localidad;
    }
    
    public String getCuil() {
        return cuil;
    }
    
    public void setCuil(String cuil) {
        this.cuil = cuil;
    }
    
    public String getIdCasoBondarea() {
        return idCasoBondarea;
    }
    
    public void setIdCasoBondarea(String idCasoBondarea) {
        this.idCasoBondarea = idCasoBondarea;
    }
} 
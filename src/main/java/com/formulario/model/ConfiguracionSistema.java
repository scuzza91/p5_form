package com.formulario.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistema {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "clave", unique = true, nullable = false)
    private String clave;
    
    @Column(name = "valor", nullable = false)
    private String valor;
    
    @Column(name = "descripcion")
    private String descripcion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "usuario_actualizacion")
    private String usuarioActualizacion;
    
    public ConfiguracionSistema() {}
    
    public ConfiguracionSistema(String clave, String valor, String descripcion) {
        this.clave = clave;
        this.valor = valor;
        this.descripcion = descripcion;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getClave() {
        return clave;
    }
    
    public void setClave(String clave) {
        this.clave = clave;
    }
    
    public String getValor() {
        return valor;
    }
    
    public void setValor(String valor) {
        this.valor = valor;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public String getUsuarioActualizacion() {
        return usuarioActualizacion;
    }
    
    public void setUsuarioActualizacion(String usuarioActualizacion) {
        this.usuarioActualizacion = usuarioActualizacion;
    }
    
    // Métodos de utilidad
    public boolean isInscripcionesAbiertas() {
        return "true".equalsIgnoreCase(this.valor);
    }
    
    public void setInscripcionesAbiertas(boolean abiertas) {
        this.valor = abiertas ? "true" : "false";
        this.fechaActualizacion = LocalDateTime.now();
    }
} 
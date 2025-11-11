package com.formulario.service;

import com.formulario.model.ConfiguracionSistema;
import com.formulario.repository.ConfiguracionSistemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConfiguracionService {
    
    @Autowired
    private ConfiguracionSistemaRepository configuracionRepository;
    
    // Constantes para las claves de configuración
    public static final String CLAVE_INSCRIPCIONES_ABIERTAS = "inscripciones_abiertas";
    
    /**
     * Obtiene el valor de una configuración por su clave
     */
    public Optional<ConfiguracionSistema> obtenerConfiguracion(String clave) {
        return configuracionRepository.findByClave(clave);
    }
    
    /**
     * Obtiene todas las configuraciones del sistema
     */
    public List<ConfiguracionSistema> obtenerTodasLasConfiguraciones() {
        return configuracionRepository.findAll();
    }
    
    /**
     * Guarda o actualiza una configuración
     */
    @Transactional
    public ConfiguracionSistema guardarConfiguracion(String clave, String valor, String descripcion, String usuario) {
        Optional<ConfiguracionSistema> configOpt = configuracionRepository.findByClave(clave);
        
        ConfiguracionSistema configuracion;
        if (configOpt.isPresent()) {
            configuracion = configOpt.get();
            configuracion.setValor(valor);
            configuracion.setDescripcion(descripcion);
            configuracion.setFechaActualizacion(LocalDateTime.now());
            configuracion.setUsuarioActualizacion(usuario);
        } else {
            configuracion = new ConfiguracionSistema(clave, valor, descripcion);
            configuracion.setUsuarioActualizacion(usuario);
        }
        
        return configuracionRepository.save(configuracion);
    }
    
    /**
     * Verifica si las inscripciones están abiertas
     */
    public boolean estanInscripcionesAbiertas() {
        Optional<ConfiguracionSistema> config = configuracionRepository.findByClave(CLAVE_INSCRIPCIONES_ABIERTAS);
        return config.map(c -> "true".equalsIgnoreCase(c.getValor())).orElse(true); // Por defecto abiertas
    }
    
    /**
     * Abre o cierra las inscripciones
     */
    @Transactional
    public ConfiguracionSistema setInscripcionesAbiertas(boolean abiertas, String usuario) {
        String valor = abiertas ? "true" : "false";
        String descripcion = abiertas ? 
            "Las inscripciones están abiertas y los usuarios pueden registrarse" :
            "Las inscripciones están cerradas y los usuarios no pueden registrarse";
        
        return guardarConfiguracion(CLAVE_INSCRIPCIONES_ABIERTAS, valor, descripcion, usuario);
    }
    
    /**
     * Inicializa las configuraciones por defecto si no existen
     */
    @Transactional
    public void inicializarConfiguracionesPorDefecto() {
        if (!configuracionRepository.existsByClave(CLAVE_INSCRIPCIONES_ABIERTAS)) {
            ConfiguracionSistema config = new ConfiguracionSistema(
                CLAVE_INSCRIPCIONES_ABIERTAS,
                "true",
                "Las inscripciones están abiertas y los usuarios pueden registrarse"
            );
            config.setUsuarioActualizacion("SISTEMA");
            configuracionRepository.save(config);
        }
    }
} 
package com.formulario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para interactuar con la API de Bondarea
 */
@Service
public class BondareaService {
    
    private static final Logger logger = LoggerFactory.getLogger(BondareaService.class);
    
    @Autowired
    private ConfiguracionService configuracionService;
    
    private final RestTemplate restTemplate;
    
    // URL base de la API de Bondarea - Se puede configurar desde la base de datos
    // Por defecto, intenta formatos comunes de endpoints REST
    private static final String[] BONDAREA_API_URL_PATTERNS = {
        "https://www.bondarea.com/api/solicitud-financiamiento/{idStage}",
        "https://www.bondarea.com/api/stage/{idStage}",
        "https://www.bondarea.com/api/stages/{idStage}",
        "https://argentinatech.bondarea.com/api/solicitud-financiamiento/{idStage}",
        "https://argentinatech.bondarea.com/api/stage/{idStage}",
        "https://api.bondarea.com/solicitud-financiamiento/{idStage}",
        "https://api.bondarea.com/stage/{idStage}"
    };
    
    public BondareaService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Obtiene datos de una solicitud de financiamiento desde Bondarea
     * Intenta múltiples patrones de URL hasta encontrar uno que funcione
     * @param idStage ID de la solicitud en Bondarea
     * @return Map con los datos obtenidos de Bondarea, o null si hay error
     */
    public Map<String, Object> obtenerSolicitudFinanciamiento(String idStage) {
        String token = configuracionService.obtenerApiTokenBondarea();
        
        if (token == null || token.isEmpty()) {
            logger.warn("Token de Bondarea no configurado");
            return null;
        }
        
        // Preparar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Token", token);
        headers.set("Authorization", "Bearer " + token);
        // También intentar con otros headers comunes
        headers.set("Accept", "application/json");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        logger.info("Consultando API de Bondarea para idStage: {}", idStage);
        
        // Intentar con cada patrón de URL hasta encontrar uno que funcione
        for (String urlPattern : BONDAREA_API_URL_PATTERNS) {
            try {
                String url = urlPattern.replace("{idStage}", idStage);
                logger.debug("Intentando URL: {}", url);
                
                // Realizar la petición GET
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    logger.info("Datos obtenidos exitosamente de Bondarea desde URL: {} para idStage: {}", url, idStage);
                    return response.getBody();
                }
                
            } catch (RestClientException e) {
                // Si es 404, continuar con el siguiente patrón
                if (e.getMessage() != null && e.getMessage().contains("404")) {
                    logger.debug("URL no encontrada (404), intentando siguiente patrón");
                    continue;
                }
                // Para otros errores, loguear pero continuar
                logger.debug("Error al consultar URL: {} - {}", urlPattern, e.getMessage());
            } catch (Exception e) {
                logger.debug("Error inesperado con URL: {} - {}", urlPattern, e.getMessage());
            }
        }
        
        logger.warn("No se pudo obtener datos de Bondarea para idStage: {} después de intentar todos los patrones", idStage);
        return null;
    }
    
    /**
     * Obtiene datos de examen desde Bondarea usando el ID proporcionado
     * Intenta diferentes formatos de ID (numérico como Long, o String como idStage)
     * @param id ID del examen o idStage de Bondarea
     * @return Map con los datos obtenidos, o null si hay error
     */
    public Map<String, Object> obtenerDatosExamenDesdeBondarea(Object id) {
        try {
            // Intentar como idStage (String)
            String idStage = id.toString();
            logger.info("Intentando obtener datos de Bondarea con ID: {}", idStage);
            
            Map<String, Object> datosBondarea = obtenerSolicitudFinanciamiento(idStage);
            
            if (datosBondarea != null && !datosBondarea.isEmpty()) {
                // Transformar los datos de Bondarea al formato esperado
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("source", "bondarea");
                resultado.put("idStage", idStage);
                resultado.put("datos", datosBondarea);
                resultado.put("status", "OK");
                resultado.put("message", "Datos obtenidos desde Bondarea");
                
                // Mapear campos comunes si están disponibles
                if (datosBondarea.containsKey("custom_B26FNN8U")) {
                    resultado.put("personaNombre", datosBondarea.get("custom_B26FNN8U"));
                }
                if (datosBondarea.containsKey("custom_B26FNN83")) {
                    resultado.put("personaApellido", datosBondarea.get("custom_B26FNN83"));
                }
                if (datosBondarea.containsKey("custom_B26FNN8P")) {
                    resultado.put("personaEmail", datosBondarea.get("custom_B26FNN8P"));
                }
                
                return resultado;
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("Error al obtener datos de examen desde Bondarea para ID: {}", id, e);
            return null;
        }
    }
}


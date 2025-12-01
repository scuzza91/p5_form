package com.formulario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;

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
            logger.warn("Token de Bondarea no configurado - No se puede consultar la API");
            return null;
        }
        
        logger.info("Token de Bondarea encontrado, iniciando consulta para idStage: {}", idStage);
        
        // Preparar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Token", token);
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/json");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        logger.info("Consultando API de Bondarea para idStage: {} - Intentando {} patrones de URL", idStage, BONDAREA_API_URL_PATTERNS.length);
        
        String ultimoError = null;
        int intentos = 0;
        
        // Intentar con cada patrón de URL hasta encontrar uno que funcione
        for (String urlPattern : BONDAREA_API_URL_PATTERNS) {
            intentos++;
            try {
                String url = urlPattern.replace("{idStage}", idStage);
                logger.info("[Intento {}/{}] Intentando URL: {}", intentos, BONDAREA_API_URL_PATTERNS.length, url);
                
                // Realizar la petición GET
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
                );
                
                logger.info("Respuesta recibida - Status: {}, Body presente: {}", 
                    response.getStatusCode(), response.getBody() != null);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    logger.info("✅ Datos obtenidos exitosamente de Bondarea desde URL: {} para idStage: {}", url, idStage);
                    return response.getBody();
                } else {
                    logger.warn("Respuesta no exitosa o vacía - Status: {}, Body: {}", 
                        response.getStatusCode(), response.getBody());
                }
                
            } catch (HttpClientErrorException e) {
                // Manejar errores HTTP específicos
                String errorMsg = String.format("HTTP %d: %s", e.getStatusCode().value(), e.getMessage());
                logger.warn("[Intento {}/{}] Error HTTP al consultar URL {}: {}", 
                    intentos, BONDAREA_API_URL_PATTERNS.length, urlPattern, errorMsg);
                ultimoError = errorMsg;
                
                if (e.getStatusCode().value() == 404) {
                    logger.debug("URL no encontrada (404), intentando siguiente patrón");
                    continue;
                } else if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                    logger.error("Error de autenticación ({}): Verificar que el token sea correcto", e.getStatusCode().value());
                    ultimoError = "Error de autenticación: " + errorMsg;
                    // Continuar intentando otras URLs por si el problema es la URL, no el token
                    continue;
                }
            } catch (RestClientException e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.warn("[Intento {}/{}] Error al consultar URL {}: {}", 
                    intentos, BONDAREA_API_URL_PATTERNS.length, urlPattern, errorMsg);
                ultimoError = errorMsg;
                
                // Si es timeout o conexión rechazada, puede ser que la URL no exista
                if (errorMsg.contains("timeout") || errorMsg.contains("Connection refused") || 
                    errorMsg.contains("UnknownHostException")) {
                    logger.debug("Problema de conexión, intentando siguiente patrón");
                    continue;
                }
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.error("[Intento {}/{}] Error inesperado con URL {}: {}", 
                    intentos, BONDAREA_API_URL_PATTERNS.length, urlPattern, errorMsg, e);
                ultimoError = errorMsg;
            }
        }
        
        logger.error("❌ No se pudo obtener datos de Bondarea para idStage: {} después de intentar {} patrones. Último error: {}", 
            idStage, BONDAREA_API_URL_PATTERNS.length, ultimoError);
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
            logger.info("=== Iniciando consulta a Bondarea con ID: {} ===", idStage);
            
            Map<String, Object> datosBondarea = obtenerSolicitudFinanciamiento(idStage);
            
            if (datosBondarea != null && !datosBondarea.isEmpty()) {
                logger.info("Datos recibidos de Bondarea, transformando respuesta...");
                
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
                
                logger.info("✅ Datos transformados exitosamente para ID: {}", idStage);
                return resultado;
            } else {
                logger.warn("⚠️ No se recibieron datos de Bondarea para ID: {} (respuesta null o vacía)", idStage);
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("❌ Error al obtener datos de examen desde Bondarea para ID: {}", id, e);
            return null;
        }
    }
}


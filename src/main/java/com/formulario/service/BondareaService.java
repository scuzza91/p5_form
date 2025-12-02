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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formulario.model.Examen;

/**
 * Servicio para interactuar con la API de Bondarea
 */
@Service
public class BondareaService {
    
    private static final Logger logger = LoggerFactory.getLogger(BondareaService.class);
    
    @Autowired
    private ConfiguracionService configuracionService;
    
    private final RestTemplate restTemplate;
    
    // URL base de la API de Bondarea
    private static final String[] BONDAREA_BASE_URLS = {
        "https://www.bondarea.com",
        "https://argentinatech.bondarea.com"
    };
    
    // idStages conocidos para intentar cuando solo tenemos el ID del caso
    private static final String[] ID_STAGES_CONOCIDOS = {
        "B26F5NF6",  // Del ejemplo de Postman
        "B26F5HRR"   // Del código existente
    };
    
    // Formato correcto de la URL según la documentación: /api/v2/monitoring/{idStage}/{idCaso}
    private static final String BONDAREA_API_PATH = "/api/v2/monitoring/{idStage}/{idCaso}";
    
    public BondareaService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Obtiene datos de una solicitud de financiamiento desde Bondarea
     * Usa el formato correcto: /monitoring/{idStage}/{idCaso}
     * @param idStage ID del stage en Bondarea (ej: B26F5NF6)
     * @param idCaso ID del caso en Bondarea (ej: 128276)
     * @return Map con los datos obtenidos de Bondarea, o null si hay error
     */
    public Map<String, Object> obtenerSolicitudFinanciamiento(String idStage, String idCaso) {
        String token = configuracionService.obtenerApiTokenBondarea();
        
        if (token == null || token.isEmpty()) {
            logger.warn("Token de Bondarea no configurado - No se puede consultar la API");
            return null;
        }
        
        logger.info("Token de Bondarea encontrado, consultando: idStage={}, idCaso={}", idStage, idCaso);
        
        // Preparar headers según la documentación de Postman
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("X-API-Token", token);
        headers.set("Authorization", "Bearer " + token);
        headers.set("scope", "1258"); // Header visto en el ejemplo de Postman
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        String ultimoError = null;
        int intentos = 0;
        int totalIntentos = BONDAREA_BASE_URLS.length;
        
        // Intentar con cada URL base hasta encontrar una que funcione
        for (String baseUrl : BONDAREA_BASE_URLS) {
            intentos++;
            try {
                String url = baseUrl + BONDAREA_API_PATH
                    .replace("{idStage}", idStage)
                    .replace("{idCaso}", idCaso);
                
                logger.info("[Intento {}/{}] Consultando URL: {}", intentos, totalIntentos, url);
                
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
                    logger.info("✅ Datos obtenidos exitosamente de Bondarea desde URL: {} para idStage: {}, idCaso: {}", 
                        url, idStage, idCaso);
                    return response.getBody();
                } else {
                    logger.warn("Respuesta no exitosa o vacía - Status: {}, Body: {}", 
                        response.getStatusCode(), response.getBody());
                }
                
            } catch (HttpClientErrorException e) {
                // Manejar errores HTTP específicos
                String errorMsg = String.format("HTTP %d: %s", e.getStatusCode().value(), 
                    e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getMessage());
                logger.warn("[Intento {}/{}] Error HTTP al consultar {}: {}", 
                    intentos, totalIntentos, baseUrl, errorMsg);
                ultimoError = errorMsg;
                
                if (e.getStatusCode().value() == 404) {
                    logger.debug("URL no encontrada (404), intentando siguiente URL base");
                    continue;
                } else if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                    logger.error("Error de autenticación ({}): Verificar que el token sea correcto", e.getStatusCode().value());
                    ultimoError = "Error de autenticación: " + errorMsg;
                    continue;
                }
            } catch (RestClientException e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.warn("[Intento {}/{}] Error al consultar {}: {}", 
                    intentos, totalIntentos, baseUrl, errorMsg);
                ultimoError = errorMsg;
                
                if (errorMsg.contains("timeout") || errorMsg.contains("Connection refused") || 
                    errorMsg.contains("UnknownHostException")) {
                    logger.debug("Problema de conexión, intentando siguiente URL base");
                    continue;
                }
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.error("[Intento {}/{}] Error inesperado con {}: {}", 
                    intentos, totalIntentos, baseUrl, errorMsg, e);
                ultimoError = errorMsg;
            }
        }
        
        logger.error("❌ No se pudo obtener datos de Bondarea para idStage: {}, idCaso: {} después de intentar {} URLs. Último error: {}", 
            idStage, idCaso, totalIntentos, ultimoError);
        return null;
    }
    
    /**
     * Valida que un ID de caso existe en Bondarea usando el idStage específico B26F5NF6
     * @param idCaso ID del caso en Bondarea (ej: 128379, 128276)
     * @return true si el ID existe, false en caso contrario
     */
    public boolean validarIdCasoEnBondarea(String idCaso) {
        if (idCaso == null || idCaso.trim().isEmpty()) {
            logger.warn("ID de caso vacío o nulo - No se puede validar");
            return false;
        }
        
        String token = configuracionService.obtenerApiTokenBondarea();
        if (token == null || token.isEmpty()) {
            logger.warn("Token de Bondarea no configurado - No se puede validar el ID");
            return false;
        }
        
        String idStage = "B26F5NF6"; // ID Stage específico según requerimiento
        logger.info("Validando ID de caso en Bondarea: idStage={}, idCaso={}", idStage, idCaso);
        
        // Preparar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("X-API-Token", token);
        headers.set("Authorization", "Bearer " + token);
        headers.set("scope", "1258");
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        // Intentar con cada URL base
        for (String baseUrl : BONDAREA_BASE_URLS) {
            try {
                String url = baseUrl + BONDAREA_API_PATH
                    .replace("{idStage}", idStage)
                    .replace("{idCaso}", idCaso);
                
                logger.debug("Validando existencia en URL: {}", url);
                
                // Realizar la petición GET
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    logger.info("✅ ID de caso {} validado exitosamente en Bondarea (idStage: {})", idCaso, idStage);
                    return true;
                }
                
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 404) {
                    logger.warn("ID de caso {} no encontrado en Bondarea (404) - idStage: {}", idCaso, idStage);
                    return false; // No encontrado, no intentar más URLs
                } else if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                    logger.error("Error de autenticación al validar ID: {}", e.getStatusCode().value());
                    // Continuar con siguiente URL base
                    continue;
                } else {
                    logger.warn("Error HTTP {} al validar ID en {}: {}", e.getStatusCode().value(), baseUrl, e.getMessage());
                    // Continuar con siguiente URL base
                    continue;
                }
            } catch (RestClientException e) {
                logger.warn("Error de conexión al validar ID en {}: {}", baseUrl, e.getMessage());
                // Continuar con siguiente URL base
                continue;
            } catch (Exception e) {
                logger.error("Error inesperado al validar ID en {}: {}", baseUrl, e.getMessage(), e);
                // Continuar con siguiente URL base
                continue;
            }
        }
        
        logger.warn("❌ No se pudo validar el ID de caso {} en Bondarea después de intentar todas las URLs", idCaso);
        return false;
    }
    
    /**
     * Obtiene datos de examen desde Bondarea usando el ID proporcionado
     * Si solo se proporciona el ID del caso, intenta con los idStages conocidos
     * @param id ID del caso en Bondarea (ej: 128379, 128276)
     * @return Map con los datos obtenidos, o null si hay error
     */
    public Map<String, Object> obtenerDatosExamenDesdeBondarea(Object id) {
        try {
            String idCaso = id.toString();
            logger.info("=== Iniciando consulta a Bondarea con ID de caso: {} ===", idCaso);
            
            // Intentar con cada idStage conocido hasta encontrar uno que funcione
            for (String idStage : ID_STAGES_CONOCIDOS) {
                logger.info("Intentando con idStage: {} para idCaso: {}", idStage, idCaso);
                
                Map<String, Object> datosBondarea = obtenerSolicitudFinanciamiento(idStage, idCaso);
                
                if (datosBondarea != null && !datosBondarea.isEmpty()) {
                    logger.info("✅ Datos recibidos de Bondarea con idStage: {}, idCaso: {}", idStage, idCaso);
                    
                    // Transformar los datos de Bondarea al formato esperado
                    Map<String, Object> resultado = new HashMap<>();
                    resultado.put("source", "bondarea");
                    resultado.put("idStage", idStage);
                    resultado.put("idCaso", idCaso);
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
                    
                    logger.info("✅ Datos transformados exitosamente para idStage: {}, idCaso: {}", idStage, idCaso);
                    return resultado;
                } else {
                    logger.debug("No se obtuvieron datos con idStage: {}, intentando siguiente...", idStage);
                }
            }
            
            logger.warn("⚠️ No se recibieron datos de Bondarea para idCaso: {} después de intentar todos los idStages conocidos", idCaso);
            return null;
            
        } catch (Exception e) {
            logger.error("❌ Error al obtener datos de examen desde Bondarea para ID: {}", id, e);
            return null;
        }
    }
    
    /**
     * Actualiza un caso en Bondarea con los resultados del examen
     * @param idCaso ID del caso en Bondarea (ej: "128379")
     * @param examen Examen con los resultados a enviar
     * @param nombreInstitucion Nombre de la institución recomendada (opcional)
     * @param comentarios Comentarios adicionales (opcional)
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean actualizarCasoEnBondarea(String idCaso, Examen examen, String nombreInstitucion, String comentarios) {
        if (idCaso == null || idCaso.trim().isEmpty()) {
            logger.warn("ID de caso vacío o nulo - No se puede actualizar en Bondarea");
            return false;
        }
        
        if (examen == null) {
            logger.warn("Examen es null - No se puede actualizar en Bondarea");
            return false;
        }
        
        String token = configuracionService.obtenerApiTokenBondarea();
        if (token == null || token.isEmpty()) {
            logger.warn("Token de Bondarea no configurado - No se puede actualizar el caso");
            return false;
        }
        
        String idStage = "B26F5NF6"; // ID Stage específico según el curl
        logger.info("Actualizando caso en Bondarea: idStage={}, idCaso={}", idStage, idCaso);
        
        // Preparar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer " + token);
        headers.set("X-API-Token", token);
        headers.set("scope", "1258");
        
        // Construir el body con los datos del examen
        Map<String, Object> requestBody = new HashMap<>();
        
        // Mapear campos del examen a los custom fields de Bondarea
        // custom_B26FNN17: Institución (desde RecomendacionEstudios)
        if (nombreInstitucion != null && !nombreInstitucion.trim().isEmpty()) {
            requestBody.put("custom_B26FNN17", nombreInstitucion);
        }
        
        // custom_B26FNHFU: Lógica
        if (examen.getLogica() != null) {
            requestBody.put("custom_B26FNHFU", examen.getLogica());
        }
        
        // custom_B26FNHF3: Matemática
        if (examen.getMatematica() != null) {
            requestBody.put("custom_B26FNHF3", examen.getMatematica());
        }
        
        // custom_B26FNHF7: Creatividad
        if (examen.getCreatividad() != null) {
            requestBody.put("custom_B26FNHF7", examen.getCreatividad());
        }
        
        // custom_B26FNHFP: Programación
        if (examen.getProgramacion() != null) {
            requestBody.put("custom_B26FNHFP", examen.getProgramacion());
        }
        
        // custom_B26FNHFC: Respuestas correctas
        if (examen.getRespuestasCorrectas() != null) {
            requestBody.put("custom_B26FNHFC", examen.getRespuestasCorrectas());
        }
        
        // custom_B26FNN13: Comentarios
        if (comentarios != null && !comentarios.trim().isEmpty()) {
            requestBody.put("custom_B26FNN13", comentarios);
        } else if (examen.getComentarios() != null && !examen.getComentarios().trim().isEmpty()) {
            requestBody.put("custom_B26FNN13", examen.getComentarios());
        }
        
        // Convertir el body a JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
            logger.debug("Body de actualización: {}", jsonBody);
        } catch (Exception e) {
            logger.error("Error al convertir body a JSON: {}", e.getMessage(), e);
            return false;
        }
        
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        
        // Intentar con cada URL base
        String ultimoError = null;
        for (String baseUrl : BONDAREA_BASE_URLS) {
            try {
                // Construir URL: /api/v2/monitoring/{idStage}/{idCaso}
                String url = baseUrl + BONDAREA_API_PATH
                    .replace("{idStage}", idStage)
                    .replace("{idCaso}", idCaso);
                
                logger.info("Actualizando caso en URL: {}", url);
                
                // Realizar la petición PUT
                @SuppressWarnings("unchecked")
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
                );
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("✅ Caso actualizado exitosamente en Bondarea: idStage={}, idCaso={}", idStage, idCaso);
                    return true;
                } else {
                    logger.warn("Respuesta no exitosa - Status: {}", response.getStatusCode());
                    ultimoError = "Status: " + response.getStatusCode();
                }
                
            } catch (HttpClientErrorException e) {
                String errorMsg = String.format("HTTP %d: %s", e.getStatusCode().value(), 
                    e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getMessage());
                logger.error("Error HTTP al actualizar caso en {}: {}", baseUrl, errorMsg);
                ultimoError = errorMsg;
                
                if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                    logger.error("Error de autenticación: Verificar que el token sea correcto");
                    // No continuar con otras URLs si es error de autenticación
                    break;
                } else if (e.getStatusCode().value() == 404) {
                    logger.warn("Caso no encontrado (404) en {}", baseUrl);
                    // Continuar con siguiente URL base
                    continue;
                }
            } catch (RestClientException e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.warn("Error de conexión al actualizar caso en {}: {}", baseUrl, errorMsg);
                ultimoError = errorMsg;
                // Continuar con siguiente URL base
                continue;
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.error("Error inesperado al actualizar caso en {}: {}", baseUrl, errorMsg, e);
                ultimoError = errorMsg;
            }
        }
        
        logger.error("❌ No se pudo actualizar el caso {} en Bondarea después de intentar todas las URLs. Último error: {}", 
            idCaso, ultimoError);
        return false;
    }
}


package com.formulario.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        
        logger.error("=== ERROR 405 - METHOD NOT ALLOWED ===");
        logger.error("Método HTTP recibido: {}", ex.getMethod());
        String[] supportedMethods = ex.getSupportedMethods();
        if (supportedMethods != null && supportedMethods.length > 0) {
            logger.error("Métodos permitidos: {}", String.join(", ", supportedMethods));
        }
        logger.error("URI: {}", request.getRequestURI());
        logger.error("Query String: {}", request.getQueryString());
        logger.error("Content-Type: {}", request.getContentType());
        logger.error("Remote Address: {}", request.getRemoteAddr());
        logger.error("Headers - User-Agent: {}", request.getHeader("User-Agent"));
        logger.error("Headers - Origin: {}", request.getHeader("Origin"));
        logger.error("Headers - Referer: {}", request.getHeader("Referer"));
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Method Not Allowed");
        errorResponse.put("status", 405);
        String supportedMethodsStr = supportedMethods != null && supportedMethods.length > 0 
            ? String.join(", ", supportedMethods) : "POST, OPTIONS";
        errorResponse.put("message", String.format("El método '%s' no está permitido para esta ruta. Métodos permitidos: %s", 
            ex.getMethod(), supportedMethodsStr));
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("method", ex.getMethod());
        errorResponse.put("supportedMethods", supportedMethods != null ? supportedMethods : new String[]{"POST", "OPTIONS"});
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        logger.warn("=== JSON MAL FORMADO - Intentando continuar con query parameters ===");
        logger.warn("Mensaje de error: {}", ex.getMessage());
        logger.warn("URI: {}", request.getRequestURI());
        logger.warn("Query String: {}", request.getQueryString());
        
        // Si hay query parameters con idCaso, sugerir usarlos
        String idCasoParam = request.getParameter("idCaso");
        String idParam = request.getParameter("id");
        
        if ((idCasoParam != null && !idCasoParam.trim().isEmpty()) || 
            (idParam != null && !idParam.trim().isEmpty())) {
            logger.info("Se encontró idCaso en query parameters - el endpoint debería funcionar sin body");
            // Devolver un error más amigable que sugiera que el body no es necesario
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Bad Request");
            errorResponse.put("status", 400);
            errorResponse.put("message", "El JSON enviado está mal formado. Sin embargo, se detectó 'idCaso' en los query parameters.");
            errorResponse.put("path", request.getRequestURI());
            errorResponse.put("sugerencia", "El body JSON no es necesario si usas query parameters. Puedes hacer la petición sin body o con un body vacío {}.");
            errorResponse.put("queryParams", Map.of(
                "idCaso", idCasoParam != null ? idCasoParam : "no presente",
                "id", idParam != null ? idParam : "no presente"
            ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        // Si no hay query params, devolver error normal
        logger.error("=== ERROR 400 - JSON MAL FORMADO Y SIN QUERY PARAMETERS ===");
        logger.error("Content-Type: {}", request.getContentType());
        logger.error("Content-Length: {}", request.getContentLength());
        logger.error("Remote Address: {}", request.getRemoteAddr());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("status", 400);
        errorResponse.put("message", "El JSON enviado está mal formado o está vacío. Por favor, verifica el formato del request body o usa query parameters.");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("sugerencia", "Usa query parameters: ?idCaso=123 o envía un JSON válido en el body: {\"idCaso\": \"123\"}");
        
        String errorMessage = ex.getMessage();
        if (errorMessage != null && errorMessage.contains("Unexpected character")) {
            errorResponse.put("detalle", "El JSON contiene caracteres inesperados. Verifica que todas las comillas estén correctamente cerradas.");
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}


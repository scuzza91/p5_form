package com.formulario.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
}


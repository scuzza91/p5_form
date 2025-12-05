package com.formulario.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utilidad para generar y validar tokens de examen basados en hash
 * El token no se almacena en BD, se calcula dinámicamente
 */
public class ExamenTokenUtil {
    
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SEPARATOR = "-";
    
    // Secret key para firmar los tokens (se lee de application.properties)
    private static String getSecretKey() {
        String secret = System.getProperty("examen.token.secret");
        if (secret == null || secret.isEmpty()) {
            // Intentar leer de variable de entorno
            secret = System.getenv("EXAMEN_TOKEN_SECRET");
        }
        if (secret == null || secret.isEmpty()) {
            // Valor por defecto (cambiar en producción)
            secret = "default-secret-key-change-in-production-2024";
        }
        return secret;
    }
    
    /**
     * Genera un token único para un examen basado en su ID
     * Formato: {examenId}-{hash}
     * 
     * @param examenId ID del examen
     * @return Token en formato base64 URL-safe
     */
    public static String generarToken(Long examenId) {
        if (examenId == null) {
            throw new IllegalArgumentException("El ID del examen no puede ser null");
        }
        
        try {
            // Calcular hash HMAC del ID del examen
            String hash = calcularHash(examenId.toString());
            
            // Combinar ID y hash: {id}-{hash}
            String token = examenId + SEPARATOR + hash;
            
            // Codificar en base64 URL-safe para URL limpia
            return Base64.getUrlEncoder().withoutPadding().encodeToString(
                token.getBytes(StandardCharsets.UTF_8)
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar token para examen " + examenId, e);
        }
    }
    
    /**
     * Valida un token y extrae el ID del examen si es válido
     * 
     * @param token Token a validar
     * @return ID del examen si el token es válido, null si no lo es
     */
    public static Long validarYExtraerId(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Decodificar base64
            byte[] decodedBytes = Base64.getUrlDecoder().decode(token);
            String decodedToken = new String(decodedBytes, StandardCharsets.UTF_8);
            
            // Separar ID y hash
            int separatorIndex = decodedToken.lastIndexOf(SEPARATOR);
            if (separatorIndex == -1) {
                return null; // Formato inválido
            }
            
            String examenIdStr = decodedToken.substring(0, separatorIndex);
            String hashRecibido = decodedToken.substring(separatorIndex + 1);
            
            // Validar que el ID sea numérico
            Long examenId;
            try {
                examenId = Long.parseLong(examenIdStr);
            } catch (NumberFormatException e) {
                return null; // ID inválido
            }
            
            // Calcular hash esperado
            String hashEsperado = calcularHash(examenIdStr);
            
            // Comparar hashes de forma segura (timing-safe)
            if (hashEsperado.equals(hashRecibido)) {
                return examenId;
            }
            
            return null; // Hash no coincide
            
        } catch (Exception e) {
            // Si hay cualquier error (base64 inválido, etc.), el token es inválido
            return null;
        }
    }
    
    /**
     * Calcula el hash HMAC-SHA256 de un valor
     * 
     * @param valor Valor a hashear
     * @return Hash en hexadecimal
     */
    private static String calcularHash(String valor) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                getSecretKey().getBytes(StandardCharsets.UTF_8), 
                HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);
            
            byte[] hashBytes = mac.doFinal(valor.getBytes(StandardCharsets.UTF_8));
            
            // Convertir a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // Tomar solo los primeros 16 caracteres para token más corto
            return hexString.substring(0, Math.min(16, hexString.length()));
            
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error al calcular hash", e);
        }
    }
    
    /**
     * Verifica si un token es válido para un examen específico
     * 
     * @param token Token a validar
     * @param examenId ID del examen esperado
     * @return true si el token es válido para ese examen
     */
    public static boolean esTokenValido(String token, Long examenId) {
        Long idExtraido = validarYExtraerId(token);
        return idExtraido != null && idExtraido.equals(examenId);
    }
}


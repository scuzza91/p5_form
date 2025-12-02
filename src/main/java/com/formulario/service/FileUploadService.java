package com.formulario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    
    // Directorio donde se guardarán las imágenes subidas
    // En desarrollo: src/main/resources/static/uploads
    // En producción/Docker: /app/uploads (directorio externo al JAR)
    @Value("${app.upload.dir:#{null}}")
    private String uploadDir;
    
    /**
     * Obtiene la ruta absoluta del directorio de uploads
     */
    private Path getUploadPath() {
        try {
            Path uploadPath;
            
            // Detectar si estamos en Docker o en desarrollo
            String userDir = System.getProperty("user.dir");
            logger.info("Directorio de trabajo actual: {}", userDir);
            
            // Si estamos en Docker (/app), usar directorio externo
            if (userDir != null && userDir.equals("/app")) {
                uploadPath = Paths.get("/app", "uploads");
                logger.info("Modo Docker detectado, usando: {}", uploadPath);
            } else {
                // En desarrollo, usar dentro de static
                // Si uploadDir está configurado, usarlo; si no, usar el predeterminado
                if (uploadDir != null && !uploadDir.isEmpty()) {
                    uploadPath = Paths.get(uploadDir);
                    if (!uploadPath.isAbsolute()) {
                        uploadPath = Paths.get(userDir, uploadDir);
                    }
                    logger.info("Usando directorio configurado: {}", uploadPath);
                } else {
                    uploadPath = Paths.get(userDir, "src", "main", "resources", "static", "uploads");
                    logger.info("Modo desarrollo detectado, usando: {}", uploadPath);
                }
            }
            
            // Crear el directorio si no existe
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Directorio de uploads creado: {}", uploadPath);
            }
            
            return uploadPath;
        } catch (Exception e) {
            logger.error("Error al obtener ruta de uploads: {}", e.getMessage(), e);
            // Fallback: usar directorio temporal
            Path fallbackPath = Paths.get(System.getProperty("java.io.tmpdir"), "uploads");
            try {
                if (!Files.exists(fallbackPath)) {
                    Files.createDirectories(fallbackPath);
                }
            } catch (IOException ioException) {
                logger.error("Error al crear directorio fallback: {}", ioException.getMessage());
            }
            return fallbackPath;
        }
    }
    
    /**
     * Guarda un archivo de imagen y retorna la ruta relativa para accederlo
     * @param file Archivo a subir
     * @param subfolder Subcarpeta donde guardar (ej: "instituciones")
     * @return Ruta relativa de la imagen guardada (ej: "/uploads/instituciones/imagen.jpg")
     */
    public String guardarImagen(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            logger.warn("Intento de guardar archivo vacío o nulo");
            return null;
        }
        
        try {
            // Validar que sea una imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warn("Archivo no es una imagen: {}", contentType);
                throw new IllegalArgumentException("El archivo debe ser una imagen");
            }
            
            // Crear directorio si no existe
            Path uploadPath = getUploadPath().resolve(subfolder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Directorio creado: {}", uploadPath);
            }
            
            // Generar nombre único para evitar conflictos
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Guardar el archivo
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Retornar ruta relativa para acceder desde el navegador
            String relativePath = "/uploads/" + subfolder + "/" + uniqueFilename;
            logger.info("Imagen guardada exitosamente: {}", relativePath);
            
            return relativePath;
            
        } catch (IOException e) {
            logger.error("Error al guardar la imagen: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina un archivo de imagen
     * @param imagePath Ruta relativa de la imagen (ej: "/uploads/instituciones/imagen.jpg")
     */
    public void eliminarImagen(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        
        try {
            // Convertir ruta relativa a ruta absoluta
            // Si la ruta empieza con /uploads/, remover el / inicial y obtener la ruta completa
            String pathWithoutSlash = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
            
            // Remover "uploads/" del inicio si existe
            if (pathWithoutSlash.startsWith("uploads/")) {
                pathWithoutSlash = pathWithoutSlash.substring("uploads/".length());
            }
            
            Path filePath = getUploadPath().resolve(pathWithoutSlash);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Imagen eliminada: {}", filePath);
            } else {
                logger.warn("No se encontró la imagen para eliminar: {} (buscada en: {})", imagePath, filePath);
            }
        } catch (IOException e) {
            logger.error("Error al eliminar la imagen: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Valida que el archivo sea una imagen válida
     */
    public boolean esImagenValida(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // Formatos de imagen permitidos
        return contentType.equals("image/jpeg") || 
               contentType.equals("image/jpg") || 
               contentType.equals("image/png") || 
               contentType.equals("image/gif") || 
               contentType.equals("image/webp");
    }
}


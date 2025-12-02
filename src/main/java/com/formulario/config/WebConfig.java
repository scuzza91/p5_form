package com.formulario.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Detectar el directorio de uploads
        String userDir = System.getProperty("user.dir");
        Path uploadsPath;
        
        if (userDir != null && userDir.equals("/app")) {
            // En Docker, usar /app/uploads
            uploadsPath = Paths.get("/app", "uploads");
            logger.info("Configurando recursos estáticos para Docker: {}", uploadsPath);
        } else {
            // En desarrollo, usar src/main/resources/static/uploads
            uploadsPath = Paths.get(userDir, "src", "main", "resources", "static", "uploads");
            logger.info("Configurando recursos estáticos para desarrollo: {}", uploadsPath);
        }
        
        // Registrar el handler para servir archivos desde /uploads/**
        String uploadsPathString = uploadsPath.toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPathString + "/");
        
        logger.info("Recursos estáticos configurados: /uploads/** -> file:{}", uploadsPathString);
    }
}


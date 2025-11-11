package com.formulario.controller;

import com.formulario.service.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/pdf")
public class PdfController {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfController.class);
    
    @Autowired
    private PdfService pdfService;
    
    /**
     * Genera un PDF completo con resultado del examen y recomendaciones laborales
     */
    @GetMapping("/resultado-completo/{personaId}")
    public ResponseEntity<byte[]> generarPdfCompleto(@PathVariable Long personaId) {
        try {
            logger.info("Generando PDF completo para persona ID: {}", personaId);
            
            byte[] pdfBytes = pdfService.generarPdfResultado(personaId);
            
            String filename = "resultado_examen_" + personaId + "_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            logger.info("PDF completo generado exitosamente para persona ID: {}", personaId);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error al generar PDF completo para persona ID: {}", personaId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Genera un PDF solo con el resultado del examen
     */
    @GetMapping("/solo-resultado/{personaId}")
    public ResponseEntity<byte[]> generarPdfSoloResultado(@PathVariable Long personaId) {
        try {
            logger.info("Generando PDF solo resultado para persona ID: {}", personaId);
            
            byte[] pdfBytes = pdfService.generarPdfSoloResultado(personaId);
            
            String filename = "resultado_examen_" + personaId + "_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            logger.info("PDF solo resultado generado exitosamente para persona ID: {}", personaId);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error al generar PDF solo resultado para persona ID: {}", personaId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Genera un PDF solo con las recomendaciones laborales
     */
    @GetMapping("/solo-recomendaciones/{personaId}")
    public ResponseEntity<byte[]> generarPdfSoloRecomendaciones(@PathVariable Long personaId) {
        try {
            logger.info("Generando PDF solo recomendaciones para persona ID: {}", personaId);
            
            byte[] pdfBytes = pdfService.generarPdfSoloRecomendaciones(personaId);
            
            String filename = "recomendaciones_laborales_" + personaId + "_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            logger.info("PDF solo recomendaciones generado exitosamente para persona ID: {}", personaId);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error al generar PDF solo recomendaciones para persona ID: {}", personaId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Genera un PDF según el tipo especificado
     */
    @GetMapping("/{personaId}")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long personaId, 
                                           @RequestParam(defaultValue = "completo") String tipo) {
        try {
            logger.info("Generando PDF tipo '{}' para persona ID: {}", tipo, personaId);
            
            byte[] pdfBytes;
            String filename;
            
            switch (tipo.toLowerCase()) {
                case "resultado":
                    pdfBytes = pdfService.generarPdfSoloResultado(personaId);
                    filename = "resultado_examen_" + personaId + "_" + 
                              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
                    break;
                case "recomendaciones":
                    pdfBytes = pdfService.generarPdfSoloRecomendaciones(personaId);
                    filename = "recomendaciones_laborales_" + personaId + "_" + 
                              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
                    break;
                case "completo":
                default:
                    pdfBytes = pdfService.generarPdfResultado(personaId);
                    filename = "resultado_completo_" + personaId + "_" + 
                              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
                    break;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            logger.info("PDF tipo '{}' generado exitosamente para persona ID: {}", tipo, personaId);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error al generar PDF tipo '{}' para persona ID: {}", tipo, personaId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Genera un PDF completo con preguntas y respuestas (solo para administradores)
     */
    @GetMapping("/admin/completo/{personaId}")
    public ResponseEntity<byte[]> generarPdfCompletoAdmin(@PathVariable Long personaId) {
        try {
            logger.info("Generando PDF completo con preguntas para administrador - persona ID: {}", personaId);
            
            byte[] pdfBytes = pdfService.generarPdfCompletoConPreguntas(personaId);
            
            String filename = "resultado_completo_admin_" + personaId + "_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            logger.info("PDF completo con preguntas generado exitosamente para administrador - persona ID: {}", personaId);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error al generar PDF completo con preguntas para administrador - persona ID: {}", personaId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 
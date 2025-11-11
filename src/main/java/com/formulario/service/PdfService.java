package com.formulario.service;

import com.formulario.model.*;
import com.formulario.repository.RespuestaExamenRepository;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class PdfService {
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Autowired
    private FormularioService formularioService;
    
    @Autowired
    private RolProfesionalService rolProfesionalService;
    
    @Autowired
    private RespuestaExamenRepository respuestaExamenRepository;
    
    /**
     * Genera un PDF con el resultado del examen y recomendaciones laborales
     */
    public byte[] generarPdfResultado(Long personaId) throws Exception {
        try {
            // Obtener el resultado del examen
            Optional<ResultadoDTO> resultadoOpt = formularioService.obtenerResultadoDTO(personaId);
            if (resultadoOpt.isEmpty()) {
                throw new Exception("No se encontró resultado para la persona con ID: " + personaId);
            }
            
            ResultadoDTO resultado = resultadoOpt.get();
            
            // Obtener las recomendaciones laborales
            List<RecomendacionRolDTO> recomendaciones = rolProfesionalService.generarRecomendacionesRoles(personaId);
            
            // Obtener estadísticas
            Map<String, Object> estadisticas = rolProfesionalService.obtenerEstadisticasRecomendacionesRoles(personaId);
            
            // Preparar el contexto para la plantilla (sin incluir respuestas detalladas)
            Context context = new Context();
            context.setVariable("resultado", resultado);
            context.setVariable("recomendaciones", recomendaciones);
            context.setVariable("estadisticas", estadisticas);
            context.setVariable("fechaGeneracion", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            context.setVariable("esPdf", true); // Para aplicar estilos específicos para PDF
            
            // Generar el HTML usando la plantilla
            String htmlContent = templateEngine.process("pdf-resultado", context);
            
            // Convertir HTML a PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, outputStream);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new Exception("Error al generar PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera un PDF solo con el resultado del examen (sin recomendaciones)
     */
    public byte[] generarPdfSoloResultado(Long personaId) throws Exception {
        try {
            // Obtener el resultado del examen
            Optional<ResultadoDTO> resultadoOpt = formularioService.obtenerResultadoDTO(personaId);
            if (resultadoOpt.isEmpty()) {
                throw new Exception("No se encontró resultado para la persona con ID: " + personaId);
            }
            
            ResultadoDTO resultado = resultadoOpt.get();
            
            // Preparar el contexto para la plantilla
            Context context = new Context();
            context.setVariable("resultado", resultado);
            context.setVariable("fechaGeneracion", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            context.setVariable("esPdf", true);
            
            // Generar el HTML usando la plantilla
            String htmlContent = templateEngine.process("pdf-solo-resultado", context);
            
            // Convertir HTML a PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, outputStream);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new Exception("Error al generar PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera un PDF solo con las recomendaciones laborales
     */
    public byte[] generarPdfSoloRecomendaciones(Long personaId) throws Exception {
        try {
            // Obtener las recomendaciones laborales
            List<RecomendacionRolDTO> recomendaciones = rolProfesionalService.generarRecomendacionesRoles(personaId);
            
            // Obtener estadísticas
            Map<String, Object> estadisticas = rolProfesionalService.obtenerEstadisticasRecomendacionesRoles(personaId);
            
            // Obtener información básica de la persona
            Optional<ResultadoDTO> resultadoOpt = formularioService.obtenerResultadoDTO(personaId);
            ResultadoDTO resultado = resultadoOpt.orElse(null);
            
            // Preparar el contexto para la plantilla
            Context context = new Context();
            context.setVariable("recomendaciones", recomendaciones);
            context.setVariable("estadisticas", estadisticas);
            context.setVariable("resultado", resultado);
            context.setVariable("fechaGeneracion", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            context.setVariable("esPdf", true);
            
            // Generar el HTML usando la plantilla
            String htmlContent = templateEngine.process("pdf-solo-recomendaciones", context);
            
            // Convertir HTML a PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, outputStream);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new Exception("Error al generar PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera un PDF completo con resultado, recomendaciones y preguntas/respuestas (solo para administradores)
     */
    public byte[] generarPdfCompletoConPreguntas(Long personaId) throws Exception {
        try {
            // Obtener el resultado del examen
            Optional<ResultadoDTO> resultadoOpt = formularioService.obtenerResultadoDTO(personaId);
            if (resultadoOpt.isEmpty()) {
                throw new Exception("No se encontró resultado para la persona con ID: " + personaId);
            }
            
            ResultadoDTO resultado = resultadoOpt.get();
            
            // Obtener las recomendaciones laborales
            List<RecomendacionRolDTO> recomendaciones = rolProfesionalService.generarRecomendacionesRoles(personaId);
            
            // Obtener estadísticas
            Map<String, Object> estadisticas = rolProfesionalService.obtenerEstadisticasRecomendacionesRoles(personaId);
            
            // Obtener el examen completo con respuestas
            Optional<Examen> examenOpt = formularioService.obtenerResultadoCompleto(personaId);
            List<RespuestaExamen> respuestas = new ArrayList<>();
            if (examenOpt.isPresent()) {
                Examen examen = examenOpt.get();
                respuestas = respuestaExamenRepository.findByExamen(examen);
            }
            
            // Preparar el contexto para la plantilla
            Context context = new Context();
            context.setVariable("resultado", resultado);
            context.setVariable("recomendaciones", recomendaciones);
            context.setVariable("estadisticas", estadisticas);
            context.setVariable("respuestas", respuestas);
            context.setVariable("fechaGeneracion", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            context.setVariable("esPdf", true);
            context.setVariable("esAdmin", true); // Para aplicar estilos específicos para administrador
            
            // Generar el HTML usando la plantilla
            String htmlContent = templateEngine.process("pdf-resultado-admin", context);
            
            // Convertir HTML a PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, outputStream);
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            throw new Exception("Error al generar PDF: " + e.getMessage(), e);
        }
    }
} 